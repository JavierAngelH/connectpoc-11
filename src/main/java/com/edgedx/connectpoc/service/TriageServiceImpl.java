package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.*;
import com.edgedx.connectpoc.model.DeviceType;
import com.edgedx.connectpoc.model.NotificationMessage;
import com.edgedx.connectpoc.model.TriageData;
import com.edgedx.connectpoc.model.TriageGeneralData;
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
public class TriageServiceImpl implements TriageService {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    MessageNotificationRepository messageNotificationRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Autowired
    TriageRepository triageRepository;

    @Autowired
    NodeHealthStatisticsRepository nodeHealthStatisticsRepository;

    @Override
    public void importTriageFile(File sourceFolder) {
        try {
            File[] files = sourceFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".csv") || name.endsWith(".txt");
                }
            });

            if (files != null) {
                for (File child : files) {
                    if (validTriageFile(child)) {
                        String deviceCode = getTriageTests(child);
                        Optional<Device> device = deviceRepository.getDeviceBySerialNumber(deviceCode);
                        log.info(child.getName() + " triage file imported");
                        MessageNotification messageNotification = messageNotificationRepository.findByTriggerName(NotificationMessage.DATA_SENT_TO_NODE.getTriggerName());
                        if (messageNotification.getStatus()) {
                            String message = messageNotification.getText();
                            Broadcaster.broadcast(message.replace("#deviceCode", device.get().getSerialNumber()), NotificationVariant.LUMO_SUCCESS);
                        }
                        deviceService.verifyRemainingInventoryByDevice(device.get());
                        String targetDirectory = propertiesService.getFullPropertyValue("triage.backup.folder");
                        FileUtils.moveFileToDirectory(child, new File(targetDirectory), true);
                    } else {
                        log.severe(child.getName() + " is not a valid triage file");
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private boolean validTriageFile(File child) throws IOException {
        int ack = 0;
        int end = 0;
        @Cleanup FileReader fr = new FileReader(child);
        @Cleanup BufferedReader in = new BufferedReader(fr);
        String line;
        while ((line = in.readLine()) != null) {
            if (line.startsWith(Constants.START_OF_TEXT)) {
                ack++;
            }
            if (line.contains(Constants.MGIT_END)) {
                end++;
            }
        }
        if ((end > 0) && (ack > 0)) {
            return true;
        }
        return false;
    }

    private String getTriageTests(File child) throws IOException {
        TriageGeneralData data = new TriageGeneralData();
        Set<TriageData> resultList = new HashSet<>();
        BufferedReader in = new BufferedReader(new FileReader(child));
        String line;
        while ((line = in.readLine()) != null) {
            String[] fields = line.split(",");
            if (fields.length < 2)
                continue;
            if (fields[0].endsWith("H")) { //header
                data = new TriageGeneralData();
                String[] headerFields = line.split(",");
                data.setSenderId(headerFields[4]);
                data.setProcessId(headerFields[11]);
                data.setVersion(headerFields[12]);
                try {
                    data.setDatetime(LocalDateTime.parse(headerFields[13], DateUtils.MGIT_FORMAT));
                } catch (DateTimeParseException e) {
                    data.setDatetime(LocalDateTime.parse(headerFields[13], DateUtils.MGIT_FORMAT_2));
                }
            }
            if (fields[0].endsWith("P")) {
                try {
                    data.setPatientId(fields[2]);
                    data.setLabPatientId(fields[3]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.log(Level.SEVERE, e.getMessage());
                }
            }
            if (fields[0].endsWith("O")) {
                try {
                    String[] field3 = fields[3].split("\\^");
                    data.setInstrumentSerial(field3[0]);
                    if (field3.length > 1) {
                        data.setPatientResultSerial(field3[1]);
                    }
                    String[] field4 = fields[4].split("\\^");
                    data.setPanelType(field4[0]);
                    if (field4.length > 1) {
                        data.setReagentLotNumber(field4[1]);
                    }
                    data.setPriority(fields[5]);
                    data.setQcResultCode(fields[20]);
                    data.setPatientResultApproval(fields[21]);
                    if (!fields[22].isEmpty()) {
                        try {
                            data.setResultDateTime(LocalDateTime.parse(fields[22], DateUtils.MGIT_FORMAT));
                        } catch (DateTimeParseException e) {
                            data.setResultDateTime(LocalDateTime.parse(fields[22], DateUtils.MGIT_FORMAT_2));
                        }
                    }
                    data.setReportType(fields[25]);
                } catch (ArrayIndexOutOfBoundsException e) {

                }
            }
            if (fields[0].endsWith("R")) {
                try {
                    TriageData result = new TriageData();
                    result.setTestId(fields[2]);
                    result.setDataValue(fields[3]);
                    result.setUnits(fields[4]);
                    result.setRange(fields[5]);
                    result.setNormalcyFlag(fields[6]);
                    result.setPopulation(fields[7]);
                    result.setResultStatus(fields[8]);
                    resultList.add(result);
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.log(Level.SEVERE, e.getMessage());
                }
            }
            if (line.endsWith(Constants.MGIT_END)) {
                data.setResultList(resultList);
                saveTest(data);
                resultList = new HashSet<>();
            }
        }
        in.close();
        return data.getDeviceId();
    }

    private void saveTest(TriageGeneralData data) {
        String deviceId = data.getInstrumentSerial();
        boolean refresh = false;
        Optional<Device> deviceOptional = deviceRepository.getDeviceBySerialNumber(deviceId);
        Device device;
        if (deviceOptional.isPresent()) {
            device = new Device();
            device.setSerialNumber(deviceId);
            device.setDeviceType(DeviceType.TRIAGE_METER);
            Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
            if (nodeConfiguration.isPresent())
                device.setTestingPoint(nodeConfiguration.get().getFacility());
            device = deviceRepository.save(device);
            log.info("New Device " + device.getSerialNumber() + " Registered at " + DateUtils.getCurrentTimeStamp());
        }else{
            device = deviceOptional.get();
        }
        data.setDeviceId(deviceId);

        for (TriageData triageData : data.getResultList()) {
            Triage triage = new Triage();
            triage.setSenderId(data.getSenderId());
            triage.setDatetime(data.getDatetime());
            triage.setVersion(data.getVersion());
            triage.setProcessId(data.getProcessId());
            triage.setPatientId(data.getPatientId());
            triage.setLabPatientId(data.getLabPatientId());
            triage.setInstrumentSerial(data.getInstrumentSerial());
            triage.setPatientResultSerial(data.getPatientResultSerial());
            triage.setPanelType(data.getPanelType());
            triage.setReagentLotNumber(data.getReagentLotNumber());
            triage.setPriority(data.getPriority());
            triage.setQcResultCode(data.getQcResultCode());
            triage.setPatientResultApproval(data.getPatientResultApproval());
            triage.setResultDatetime(data.getResultDateTime());
            triage.setReportType(data.getReportType());
            triage.setTestId(triageData.getTestId());
            triage.setDataValue(triageData.getDataValue());
            triage.setUnits(triageData.getUnits());
            triage.setRange(triageData.getRange());
            triage.setNormalcyFlag(triageData.getNormalcyFlag());
            triage.setPopulation(triageData.getPopulation());
            triage.setResultStatus(triageData.getResultStatus());
            triage.setOperatorId(triageData.getOperatorId());
            triage.setDeviceId(data.getDeviceId());
            triageRepository.save(triage);
            deviceService.updateCartridgeByDevice(device);
        }
       if (refresh) {
            Broadcaster.broadcast("reload", null);
            Broadcaster.broadcast("Device " + device.getDeviceType().getScreenName() + " registered in the node",
                    NotificationVariant.LUMO_SUCCESS);
        }
    }


    @Override
    public void exportTriageDataToFile(File targetFolder) throws IOException {
        List<Triage> list = triageRepository.findAllByIsExportedFalse();
        if(list.isEmpty())
            return;
        String NEW_LINE_SEPARATOR = "\n";
        // Create the CSVFormat object with "," as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();
        File expFile = null;
        Optional<NodeHealthStatistics> nodeHealthStatistics = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty() || !nodeConfiguration.get().getIsExported()) {
            log.warning("Node configuration isn't exported yet. Triage Data won't get exported.");
            return;
        }

        String macId = "";
        if(nodeHealthStatistics.isPresent())
            macId = nodeHealthStatistics.get().getMacId();

        String fName = DeviceType.TRIAGE_METER + "-" + nodeConfiguration.get().getFacility();
        String fUniqueKey = new SimpleDateFormat("yyMMddhhmmssSSS").format(new Date());
        expFile = new File(targetFolder + File.separator + fName + "-"
                + list.get(0).getDeviceId() + "(" + fUniqueKey + ").csv");
        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        // initialize CSVPrinter object
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
        // write header
        csvFilePrinter.printRecord(Constants.triageTemplateFormat_H);
        for (Triage data : list) {
            List<Object> tempData = writeTriageResults(data, macId, nodeConfiguration.get());
            csvFilePrinter.printRecord(tempData);
        }
        for (Triage test : list) {
            test.setIsExported(true);
            triageRepository.save(test);
        }
        log.info(DeviceType.TRIAGE_METER.getScreenName() + " file was created successfully");
    }

    @Override
    public void pushTriageFileToServer(File sourceFolder) throws JSchException, SftpException {
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty()) {
            log.warning("Node configuration is not configured. Triage file can't be exported");
            return;
        } else if (nodeConfiguration.get().getIsExported()) {
            log.warning("Node configuration is not exported. Triage file can't be exported");
            return;
        }
        String country = nodeConfiguration.get().getCountry();
        String remoteServerIPAddress;
        if (country.equals("Ethiopia"))
            remoteServerIPAddress = propertiesService.getPropertyValue("ethiopia.remote.server.ipaddress");
        else
            remoteServerIPAddress = propertiesService.getPropertyValue("aws.remote.server.ipaddress");

        String remoteServerCertificate = propertiesService.getFullPropertyValue("remote.server.certificate.path");
        String remoteDirectory = propertiesService.getPropertyValue("remote.server.triage.directory");
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

    private List<Object> writeTriageResults(Triage data, String macId, NodeConfiguration nodeConfiguration) {
        List<Object> tempData = new ArrayList<>();
        try {
            tempData.add(macId);
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(nodeConfiguration.getCountry());
            tempData.add(nodeConfiguration.getProvince());
            tempData.add(nodeConfiguration.getDistrict());
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(data.getDeviceId());
            tempData.add(data.getSenderId());
            tempData.add(data.getVersion());
            tempData.add(data.getDatetime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getProcessId());
            tempData.add(data.getPatientId());
            tempData.add(data.getLabPatientId());
            tempData.add(data.getInstrumentSerial());
            tempData.add(data.getPatientResultSerial());
            tempData.add(data.getPanelType());
            tempData.add(data.getReagentLotNumber());
            tempData.add(data.getPriority());
            tempData.add(data.getQcResultCode());
            tempData.add(data.getPatientResultApproval());
            tempData.add(data.getResultDatetime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getReportType());
            tempData.add(data.getTestId());
            tempData.add(data.getDataValue());
            tempData.add(data.getUnits());
            tempData.add(data.getRange());
            tempData.add(data.getNormalcyFlag());
            tempData.add(data.getPopulation());
            tempData.add(data.getResultStatus());
            tempData.add(data.getOperatorId());

            return tempData;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CsvFileWriter for --" + data.getDeviceId()
                    + "-- operation failed", e);
            return tempData;
        }


    }
}
