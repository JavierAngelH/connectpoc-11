package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.*;
import com.edgedx.connectpoc.model.DeviceType;
import com.edgedx.connectpoc.model.MgitData;
import com.edgedx.connectpoc.model.MgitGeneralData;
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
public class MgitServiceImpl implements MgitService {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    MessageNotificationRepository messageNotificationRepository;

    @Autowired
    MgitRepository mgitRepository;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Autowired
    NodeHealthStatisticsRepository nodeHealthStatisticsRepository;


    @Override
    public void importMgitFiles(File sourceFolder) {
        try {
            File[] files = sourceFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt") || name.endsWith(".csv");
                }
            });
            if (files != null) {
                for (File child : files) {
                    if (validMGITFile(child)) {
                        String deviceCode = getMGITTests(child);
                        Optional<Device> device = deviceRepository.getDeviceBySerialNumber(deviceCode);
                        log.info(child.getName() + " mgit file imported");
                        MessageNotification messageNotification = messageNotificationRepository.findByTriggerName(NotificationMessage.DATA_SENT_TO_NODE.getTriggerName());
                        if (messageNotification.getStatus()) {
                            String message = messageNotification.getText();
                            Broadcaster.broadcast(message.replace("#deviceCode", device.get().getSerialNumber()), NotificationVariant.LUMO_SUCCESS);
                        }
                        deviceService.verifyRemainingInventoryByDevice(device.get());
                        String targetDirectory = propertiesService.getFullPropertyValue("mgit.backup.folder");
                        FileUtils.moveFileToDirectory(child, new File(targetDirectory), true);
                    } else {
                        log.severe(child.getName() + " is not a valid MGIT file");
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private boolean validMGITFile(File child) throws IOException {
        int ack = 0;
        int end = 0;
        @Cleanup FileReader fileReader = new FileReader(child);
        @Cleanup BufferedReader in = new BufferedReader(fileReader);
        String line;
        while ((line = in.readLine()) != null) {
            if (line.startsWith(Constants.START_OF_TEXT)) {
                ack++;
            }
            if (line.equals(Constants.MGIT_END)) {
                end++;
            }
        }
        if ((end > 0) && (ack > 0)) {
            return true;
        }
        return false;
    }

    private String getMGITTests(File child) throws IOException {
        MgitGeneralData data = new MgitGeneralData();
        Set<MgitData> resultList = new HashSet<>();
        @Cleanup FileReader fr = new FileReader(child);
        @Cleanup BufferedReader in = new BufferedReader(fr);
        String line;
        while ((line = in.readLine()) != null) {
            if (line.startsWith(Constants.START_OF_TEXT)) { //header
                data = new MgitGeneralData();
                String[] headerFields = line.split(",");
                data.setSenderName(headerFields[4]);
                data.setVersion(headerFields[12]);
                try {
                    data.setDatetime(LocalDateTime.parse(headerFields[13], DateUtils.MGIT_FORMAT));
                } catch (DateTimeParseException e) {
                    data.setDatetime(LocalDateTime.parse(headerFields[13], DateUtils.MGIT_FORMAT_2));
                }
            }
            try {
                if (line.startsWith("O,")) { //order
                    String[] orderFields = line.split(",");
                    String[] field2 = orderFields[2].split("\\^");
                    data.setAccessionNumber(field2[0]);
                    if (field2.length > 1) {
                        data.setIsolateNumber(Integer.parseInt(field2[1]));
                    }
                    String[] field4 = orderFields[4].split("\\^");
                    data.setTestId(field4[3]);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
            try {
                if (line.startsWith("R,")) { //result
                    MgitData result = new MgitData();
                    String[] resultFields = line.split(",");
                    String[] field2 = resultFields[2].split("\\^");
                    result.setResultIdCode(field2[3]);
                    result.setTestSequenceNumber(field2[4]);
                    if (field2.length > 5) {
                        result.setAntibiotic(field2[5]);
                        result.setConcentration(Double.parseDouble(field2[6]));
                        result.setConcentrationUnits(field2[7]);
                    }
                    String[] field3 = resultFields[3].split("\\^");
                    result.setTestStatus(field3[0]);
                    result.setGrowthUnits(Integer.parseInt(field3[1]));
                    if (field3.length > 3) {
                        result.setAstSusceptibility(field3[3]);
                    }
                    result.setPreliminaryFinalStatus(resultFields[8]);
                    if (!resultFields[11].isEmpty()) {
                        try {
                            result.setStartDate(LocalDateTime.parse(resultFields[11], DateUtils.MGIT_FORMAT));
                        } catch (DateTimeParseException e) {
                            result.setStartDate(LocalDateTime.parse(resultFields[11], DateUtils.MGIT_FORMAT_2));
                        }
                    }
                    if (!resultFields[12].isEmpty()) {
                        try {
                            result.setResultDate(LocalDateTime.parse(resultFields[12], DateUtils.MGIT_FORMAT));
                        } catch (DateTimeParseException e) {
                            result.setResultDate(LocalDateTime.parse(resultFields[12], DateUtils.MGIT_FORMAT_2));
                        }
                    }
                    String[] field13 = resultFields[13].split("\\^");
                    result.setInstrumentType(field13[0]);
                    if (field13.length > 2) {
                        if (!field13[2].isEmpty()) {
                            result.setProtocolLength(Integer.parseInt(field13[2]));
                        }
                    }
                    result.setInstrumentNumber(Integer.parseInt(field13[3]));
                    result.setInstrumentLocation(field13[4]);
                    resultList.add(result);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }

            if (line.endsWith(Constants.MGIT_END)) {
                data.setResultList(resultList);
                this.saveTest(data);
                resultList = new HashSet<>();
            }
        }
        return data.getDeviceId();
    }

    private void saveTest(MgitGeneralData data) {
        Iterator<MgitData> setIterator = data.getResultList().iterator();
        MgitData firstRow = setIterator.next();
        String deviceId = firstRow.getInstrumentType() + "_" + firstRow.getInstrumentNumber();
        Optional<Device> deviceOptional = deviceRepository.getDeviceBySerialNumber(deviceId);
        Device device;
        Boolean refresh = false;
        if (deviceOptional.isPresent()) {
            device = new Device();
            device.setSerialNumber(deviceId);
            device.setDeviceType(DeviceType.BD_MGIT);
            Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
            if (nodeConfiguration.isPresent())
                device.setTestingPoint(nodeConfiguration.get().getFacility());
            device = deviceRepository.save(device);
            refresh = true;
            log.info("New Device " + device.getSerialNumber() + " Registered at " + DateUtils.getCurrentTimeStamp());
        }else{
            device = deviceOptional.get();
        }
        data.setDeviceId(deviceId);
        for (MgitData result : data.getResultList()) {
            Mgit mgit = new Mgit();
            mgit.setAntibiotic(result.getAntibiotic());
            mgit.setConcentration(result.getConcentration());
            mgit.setDatetime(data.getDatetime());
            mgit.setAstSusceptibility(result.getAstSusceptibility());
            mgit.setConcentrationUnits(result.getConcentrationUnits());
            mgit.setGrowthUnits(result.getGrowthUnits());
            mgit.setSenderName(data.getSenderName());
            mgit.setVersion(data.getVersion());
            mgit.setAccesionNumber(data.getAccessionNumber());
            mgit.setIsolateNumber(data.getIsolateNumber());
            mgit.setTestId(data.getTestId());
            mgit.setResultIdCode(result.getResultIdCode());
            mgit.setTestSequenceNumber(result.getTestSequenceNumber());
            mgit.setTestStatus(result.getTestStatus());
            mgit.setStartDate(result.getStartDate());
            mgit.setResultDate(result.getResultDate());
            mgit.setInstrumentType(result.getInstrumentType());
            mgit.setProtocolLength(result.getProtocolLength());
            mgit.setInstrumentNumber(result.getInstrumentNumber());
            mgit.setInstrumentLocation(result.getInstrumentLocation());
            mgit.setPreliminaryFinalStatus(result.getPreliminaryFinalStatus());
            mgit.setDeviceId(data.getDeviceId());
            mgitRepository.save(mgit);
        }
        for (int i = 0; i < data.getResultList().size(); i++) {
            this.deviceService.updateCartridgeByDevice(device);
        }

        if (refresh) {
            Broadcaster.broadcast("reload", null);
            Broadcaster.broadcast("Device " + device.getDeviceType().getScreenName() + " registered in the node",
                    NotificationVariant.LUMO_SUCCESS);
        }
    }


    @Override
    public void exportMgitDataToFile(File targetFolder) throws IOException {
        List<Mgit> list = mgitRepository.findAllByIsExportedFalse();
        if(list.isEmpty())
            return;
        String NEW_LINE_SEPARATOR = "\n";
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();
        File expFile = null;
        Optional<NodeHealthStatistics> nodeHealthStatistics = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty() || !nodeConfiguration.get().getIsExported()) {
            log.warning("Node configuration isn't exported yet. MGIT Data won't get exported.");
            return;
        }

        String macId = "";
        if(nodeHealthStatistics.isPresent())
            macId = nodeHealthStatistics.get().getMacId();

        String fName = DeviceType.BD_MGIT + "-" + nodeConfiguration.get().getFacility();
        String fUniqueKey = new SimpleDateFormat("yyMMddhhmmssSSS").format(new Date());
        expFile = new File(targetFolder + File.separator + fName + "-"
                + list.get(0).getDeviceId() + "(" + fUniqueKey + ").csv");
        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        // initialize CSVPrinter object
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
        // write header
        csvFilePrinter.printRecord(Constants.mgitTemplateFormat_H);

        for (Mgit data : list) {
            List<Object> tempData = writeMgitResults(data, macId, nodeConfiguration.get());
            csvFilePrinter.printRecord(tempData);
        }

        for (Mgit test : list) {
            test.setIsExported(true);
            mgitRepository.save(test);
        }
        log.info(DeviceType.BD_MGIT.getScreenName() + " file was created successfully");
    }

    @Override
    public void pushMgitFilesToServer(File sourceFolder) throws JSchException, SftpException {
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty()) {
            log.warning("Node configuration is not configured. Abbot file can't be exported");
            return;
        } else if (nodeConfiguration.get().getIsExported()) {
            log.warning("Node configuration is not exported. Abbot file can't be exported");
            return;
        }
        String country = nodeConfiguration.get().getCountry();
        String remoteServerIPAddress;
        if (country.equals("Ethiopia"))
            remoteServerIPAddress = propertiesService.getPropertyValue("ethiopia.remote.server.ipaddress");
        else
            remoteServerIPAddress = propertiesService.getPropertyValue("aws.remote.server.ipaddress");

        String remoteServerCertificate = propertiesService.getFullPropertyValue("remote.server.certificate.path");
        String remoteDirectory = propertiesService.getPropertyValue("remote.server.bdmgit.directory");
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


    private List<Object> writeMgitResults(Mgit data, String macId, NodeConfiguration nodeConfiguration) {
        List<Object> tempData = new ArrayList<>();
        try {
            tempData.add(macId);
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(nodeConfiguration.getCountry());
            tempData.add(nodeConfiguration.getProvince());
            tempData.add(nodeConfiguration.getDistrict());
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(data.getDeviceId());
            tempData.add(data.getSenderName());
            tempData.add(data.getVersion());
            tempData.add(data.getDatetime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getAccesionNumber());
            tempData.add(data.getIsolateNumber());
            tempData.add(data.getTestId());
            tempData.add(data.getResultIdCode());
            tempData.add(data.getTestSequenceNumber());
            tempData.add(data.getAntibiotic());
            tempData.add(data.getConcentration());
            tempData.add(data.getConcentrationUnits());
            tempData.add(data.getTestStatus());
            tempData.add(data.getGrowthUnits());
            tempData.add(data.getAstSusceptibility());
            tempData.add(data.getStartDate().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getResultDate().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getInstrumentType());
            tempData.add(data.getProtocolLength());
            tempData.add(data.getInstrumentNumber());
            tempData.add(data.getInstrumentLocation());
            tempData.add(data.getPreliminaryFinalStatus());
            return tempData;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CsvFileWriter for --" + data.getDeviceId()
                    + "-- operation failed", e);
            return tempData;
        }
    }


}
