package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.*;
import com.edgedx.connectpoc.model.AbbottData;
import com.edgedx.connectpoc.model.AbbottGeneralData;
import com.edgedx.connectpoc.model.DeviceType;
import com.edgedx.connectpoc.model.NotificationMessage;
import com.edgedx.connectpoc.repository.*;
import com.edgedx.connectpoc.utils.Constants;
import com.edgedx.connectpoc.utils.DateUtils;
import com.edgedx.connectpoc.views.Broadcaster;
import com.jcraft.jsch.*;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.Cleanup;
import lombok.extern.java.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;

@Service
@Log
public class AbbottM2000ServiceImpl implements AbbottM2000Service {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    DeviceService deviceService;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    MessageNotificationRepository messageNotificationRepository;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Autowired
    AbbottM2000Repository abbottM2000Repository;

    @Autowired
    AbbottM2000APIServiceImpl abbottM2000APIService;

    @Autowired
    NodeHealthStatisticsRepository nodeHealthStatisticsRepository;

    @Override
    public void importAbbottFiles(File sourceFolder) {
        try {
            File[] files = sourceFolder.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".csv"));

            if (files != null) {
                for (File child : files) {
                    if (validAbbottFile(child)) {
                        String deviceCode = this.getAbbottTests(child);
                        if (!deviceCode.isEmpty()) {
                            Optional<Device> device = deviceRepository.getDeviceBySerialNumber(deviceCode);

                            log.info(child.getName() + " abbott file imported on");
                            NotificationMessage notificationMessage = NotificationMessage.DATA_SENT_TO_NODE;
                            MessageNotification messageNotification = messageNotificationRepository.findByTriggerName(notificationMessage.getTriggerName());
                            if (messageNotification.getStatus()) {
                                String message = messageNotification.getText();
                                Broadcaster.broadcast(message.replace("#deviceCode", device.get().getSerialNumber()), NotificationVariant.LUMO_SUCCESS);
                            }
                            deviceService.verifyRemainingInventoryByDevice(device.get());
                        }
                        String targetDirectory = propertiesService.getFullPropertyValue("abbott.backup.folder");
                        FileUtils.moveFileToDirectory(child, new File(targetDirectory), true);
                    } else {
                        log.severe(child.getName() + " is not a valid abbott file");
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Abbott file copy operation failed", e);
        }
    }

    private Boolean validAbbottFile(File file) throws IOException {
        @Cleanup FileReader fileReader = new FileReader(file);
        @Cleanup BufferedReader in = new BufferedReader(fileReader);
        String line;
        while ((line = in.readLine()) != null) {
            if (line.startsWith(Constants.START_OF_TEXT)) {
                return true;
            }
        }
        return false;
    }

    private String getAbbottTests(File child) throws IOException {
        String deviceCode;
        Set<AbbottGeneralData> dataSet = new HashSet<>();
        AbbottGeneralData data = new AbbottGeneralData();
        Set<AbbottData> resultList = new HashSet<>();
        @Cleanup FileReader fr = new FileReader(child);
        @Cleanup BufferedReader in = new BufferedReader(fr);
        String line;
        while ((line = in.readLine()) != null) {
            if (line.startsWith(Constants.START_OF_TEXT)) {
                line = line.replace(Constants.START_OF_TEXT, "");
                String[] fields = line.split("\\|");
                if (fields[0].endsWith("H")) { // header
                    resultList = new HashSet<>();
                    data = new AbbottGeneralData();
                    String[] fieldDeviceInformation = fields[4].split("\\^");
                    data.setSenderName(fieldDeviceInformation[0]);
                    data.setVersion(fieldDeviceInformation[1]);
                    data.setDeviceSerialNumber(fieldDeviceInformation[2]);
                    try {
                        data.setDatetime(LocalDateTime.parse(fields[fields.length - 1], DateUtils.MGIT_FORMAT));
                    } catch (DateTimeParseException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                    }
                } else if (fields[0].endsWith("O")) { // order
                    data.setSampleId(fields[2]);
                    String[] fieldOrderInformation = fields[3].split("\\^");
                    data.setCarrierId(fieldOrderInformation[1]);
                    data.setPosition(fieldOrderInformation[2]);
                    String[] fieldAssayInformation = fields[4].split("\\^");
                    data.setAssayNumber(fieldAssayInformation[3]);
                    data.setAssayName(fieldAssayInformation[4]);
                    data.setReportType(fields[fields.length - 1]);
                    data.setResultList(resultList);
                    dataSet.add(data);
                } else if (fields[0].endsWith("R")) { // result
                    AbbottData result = new AbbottData();
                    String[] fieldResultInformation = fields[2].split("\\^");
                    result.setResultType(fieldResultInformation[8]);
                    if (result.getResultType().equals("F")) {
                        result.setReagentLotId(fieldResultInformation[5]);
                        result.setReagentSerialNumber(fieldResultInformation[6]);
                        result.setControlLotNumber(fieldResultInformation[7]);
                        result.setValue(fields[3]);
                        result.setUnits(fields[4]);
                        result.setReferenceRanges(fields[5]);
                        String[] fieldOperatorInformation = fields[10].split("\\^");
                        result.setOperatorIdentification(fieldOperatorInformation[0]);
                        try {
                            result.setDatetimeCompleteTest(LocalDateTime.parse(fields[fields.length - 2], DateUtils.MGIT_FORMAT));
                        } catch (DateTimeParseException e) {
                            log.log(Level.SEVERE, e.getMessage(), e);
                        }
                        resultList.add(result);
                    }
                } else if (fields[0].endsWith("C")) {
                    if (fields[4].trim().equals("I")) {
                        data.setError(fields[3]);
                    }
                }
            }
        }
        deviceCode = data.getDeviceSerialNumber();
        if (!dataSet.isEmpty()) {
            saveTest(dataSet, deviceCode);
            abbottM2000APIService.postTests();
        }
        return deviceCode;
    }

    private void saveTest(Set<AbbottGeneralData> resultSet, String deviceCode) {
        boolean refresh = false;
        Optional<Device> deviceOptional = deviceRepository.getDeviceBySerialNumber(deviceCode);
        Device device;
        if (deviceOptional.isEmpty()) {
            device = new Device();
            device.setSerialNumber(deviceCode);
            device.setDeviceType(DeviceType.ABBOTT_M2000);
            Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
            if (nodeConfiguration.isPresent())
                device.setTestingPoint(nodeConfiguration.get().getFacility());
            device = deviceRepository.save(device);
            refresh = true;
            log.info("New Device " + device.getSerialNumber() + " Registered at " + DateUtils.getCurrentTimeStamp());
        } else {
            device = deviceOptional.get();
        }
        for (AbbottGeneralData data : resultSet) {
            if (!data.getResultList().isEmpty()) {
                for (AbbottData result : data.getResultList()) {
                    AbbottM2000 test = new AbbottM2000();
                    test.setAssayName(data.getAssayName());
                    test.setAssayNumber(data.getAssayNumber());
                    test.setCarrierId(data.getCarrierId());
                    test.setControlLotNumber(test.getControlLotNumber());
                    test.setDeviceCode(device.getSerialNumber());
                    test.setTestError(data.getError());
                    test.setSenderName(data.getSenderName());
                    test.setVersion(data.getVersion());
                    test.setExportDatetime(data.getDatetime());
                    test.setSampleId(data.getSampleId());
                    test.setCarrierId(data.getCarrierId());
                    test.setSamplePosition(data.getPosition());
                    test.setReportType(data.getReportType());
                    test.setReagentLotId(result.getReagentLotId());
                    test.setReagentSerialNumber(result.getReagentSerialNumber());
                    test.setResult(result.getValue());
                    test.setUnits(result.getUnits());
                    test.setReferenceRange(result.getReferenceRanges());
                    test.setOperator(result.getOperatorIdentification());
                    test.setTestDatetime(result.getDatetimeCompleteTest());
                    try{
                    abbottM2000Repository.save(test);
                    deviceService.updateCartridgeByDevice(device);
                    }catch(Exception e){
                        log.warning(e.getMessage());
                    }
                }
            } else {
                AbbottM2000 test = new AbbottM2000();
                test.setAssayName(data.getAssayName());
                test.setAssayNumber(data.getAssayNumber());
                test.setCarrierId(data.getCarrierId());
                test.setControlLotNumber(test.getControlLotNumber());
                test.setDeviceCode(device.getSerialNumber());
                test.setTestError(data.getError());
                test.setSenderName(data.getSenderName());
                test.setVersion(data.getVersion());
                test.setExportDatetime(data.getDatetime());
                test.setSampleId(data.getSampleId());
                test.setCarrierId(data.getCarrierId());
                test.setSamplePosition(data.getPosition());
                test.setReportType(data.getReportType());
                LocalDateTime zeroDate = LocalDateTime.of(0, 1, 1, 0, 0);
                test.setTestDatetime(zeroDate);
                try{
                    abbottM2000Repository.save(test);
                    deviceService.updateCartridgeByDevice(device);
                }catch(Exception e){
                    log.warning(e.getMessage());
                }
            }
        }
        if (refresh) {
            Broadcaster.broadcast("reload", null);
            Broadcaster.broadcast("Device " + device.getDeviceType().getScreenName() + " registered in the node",
                    NotificationVariant.LUMO_SUCCESS);
        }
    }


    @Override
    public void pushAbbottFileToServer(File sourceFolder) throws JSchException, SftpException {
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty()) {
            log.warning("Node configuration is not configured. Abbott file can't be exported");
            return;
        } else if (nodeConfiguration.get().getIsExported()) {
            log.warning("Node configuration is not exported. Abbott file can't be exported");
            return;
        }
        String country = nodeConfiguration.get().getCountry();
        String remoteServerIPAddress;
        if (country.equals("Ethiopia"))
            remoteServerIPAddress = propertiesService.getPropertyValue("ethiopia.remote.server.ipaddress");
        else
            remoteServerIPAddress = propertiesService.getPropertyValue("aws.remote.server.ipaddress");

        String remoteServerCertificate = propertiesService.getFullPropertyValue("remote.server.certificate.path");
        String remoteDirectory = propertiesService.getPropertyValue("remote.server.abbott.directory");
        String remoteServerUser = propertiesService.getPropertyValue("remote.server.user");
        String remoteServerPort = propertiesService.getPropertyValue("remote.server.port");

        if (sourceFolder.listFiles().length > 0) {
            JSch jsch = new JSch();
            String user = remoteServerUser;
            String host = remoteServerIPAddress;
            int port = Integer.parseInt(remoteServerPort);
            jsch.addIdentity(remoteServerCertificate);
            @Cleanup("disconnect") Session session = jsch.getSession(user, host, port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            @Cleanup("disconnect") Channel channel = session.openChannel("sftp");
            channel.connect(60000);
            @Cleanup("disconnect") ChannelSftp sftp = (ChannelSftp) channel;
            sftp.cd(remoteDirectory);
            for (final File fileEntry : sourceFolder.listFiles()) {
                sftp.put(fileEntry.getPath(), fileEntry.getName());
                FileUtils.deleteQuietly(fileEntry);
            }
        }
    }

    @Override
    public void exportAbbottDataToFile(File targetFolder) throws IOException {
        List<AbbottM2000> list = abbottM2000Repository.findAllByIsExportedFalse();
        if(list.isEmpty())
            return;
        String NEW_LINE_SEPARATOR = "\n";
        // Create the CSVFormat object with "," as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();
        File expFile = null;
        Optional<NodeHealthStatistics> nodeHealthStatistics = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty() || !nodeConfiguration.get().getIsExported()) {
            log.warning("Node configuration isn't exported yet. Abbott Data won't get exported.");
            return;
        }

        String macId = "";
        if(nodeHealthStatistics.isPresent())
            macId = nodeHealthStatistics.get().getMacId();
        String fName = DeviceType.ABBOTT_M2000 + "-" + nodeConfiguration.get().getFacility();
        String fUniqueKey = new SimpleDateFormat("yyMMddhhmmssSSS").format(new Date());
        expFile = new File(targetFolder + File.separator + fName + "-"
                + list.get(0).getDeviceCode() + "(" + fUniqueKey + ").csv");
        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        // initialize CSVPrinter object
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
        // write header
        csvFilePrinter.printRecord(Constants.abbottTemplateFormat_H);
        for (AbbottM2000 data : list) {
            List<Object> tempData = writeAbbottResults(data, macId, nodeConfiguration.get());
            csvFilePrinter.printRecord(tempData);
        }
        for (AbbottM2000 test : list) {
            test.setIsExported(true);
            abbottM2000Repository.save(test);
        }
        log.info(DeviceType.ABBOTT_M2000.getScreenName() + " file was created successfully");
    }

    private List<Object> writeAbbottResults(AbbottM2000 data, String macId, NodeConfiguration nodeConfiguration) {
        List<Object> tempData = new ArrayList<>();
        try {
            tempData.add(macId);
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(nodeConfiguration.getCountry());
            tempData.add(nodeConfiguration.getProvince());
            tempData.add(nodeConfiguration.getDistrict());
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(data.getDeviceCode());
            tempData.add(data.getSenderName());
            tempData.add(data.getVersion());
            tempData.add(data.getExportDatetime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getSampleId());
            tempData.add(data.getCarrierId());
            tempData.add(data.getSamplePosition());
            tempData.add(data.getAssayNumber());
            tempData.add(data.getAssayName());
            tempData.add(data.getReportType());
            tempData.add(data.getReagentLotId());
            tempData.add(data.getReagentSerialNumber());
            tempData.add(data.getControlLotNumber());
            tempData.add(data.getResult());
            tempData.add(data.getUnits());
            tempData.add(data.getReferenceRange());
            tempData.add(data.getOperator());
            tempData.add(data.getTestError());
            tempData.add(data.getTestDatetime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getSysLogdate().format(DateUtils.TIME_FORMAT));
            if (data.getNascopTimestamp() != null)
                tempData.add(data.getNascopTimestamp().format(DateUtils.TIME_FORMAT));
            return tempData;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CsvFileWriter for --" + data.getDeviceCode()
                    + "-- operation failed", e);
            return tempData;
        }

    }
}
