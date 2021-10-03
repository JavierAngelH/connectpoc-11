package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.*;
import com.edgedx.connectpoc.model.DeviceType;
import com.edgedx.connectpoc.model.NotificationMessage;
import com.edgedx.connectpoc.model.PimaData;
import com.edgedx.connectpoc.repository.*;
import com.edgedx.connectpoc.utils.Constants;
import com.edgedx.connectpoc.utils.DateUtils;
import com.edgedx.connectpoc.views.Broadcaster;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.csv.CSVReader;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.Cleanup;
import lombok.extern.java.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Service
@Log
public class PimaServiceImpl implements PimaService {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Autowired
    PimaBeadRepository pimaBeadRepository;

    @Autowired
    PimaCd4Repository pimaCd4Repository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    MessageNotificationRepository messageNotificationRepository;

    @Autowired
    NodeHealthStatisticsRepository nodeHealthStatisticsRepository;

    @Override
    public void importPimaFile(File sourcePath) {
        try {
            File[] files = sourcePath.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".csv") || name.endsWith(".slk");
                }
            });
            // if files are found; loop through files
            if (files != null) {
                for (File child : files) {
                    boolean validPimaBeadsHeader = false;
                    boolean validaPimaCD4Header = false;
                    List<String> headersList = this.getPIMAHeaders(child);
                    if (headersList.size() == Constants.pimaBeadsTemplateFormat.size()) {
                        if (headersList.equals(Constants.pimaBeadsTemplateFormat)) {
                            validPimaBeadsHeader = true;
                        }
                    }
                    if (headersList.size() == Constants.pimaCd4TemplateFormat.size()) {
                        if (headersList.equals(Constants.pimaCd4TemplateFormat)) {
                            validaPimaCD4Header = true;
                        }
                    }
                    List<PimaData> pimaList = new ArrayList<>();
                    if (validPimaBeadsHeader) {
                        pimaList = this.parsePIMAFile(child, Constants.PIMA_CATEGORY.PIMA_BEADS);
                        for (PimaData data : pimaList) {
                            PimaBead test = new PimaBead();
                            test.setTestId(data.getTestId());
                            test.setBarcode(data.getBarcode());
                            test.setDevice(data.getDevice());
                            test.setAssayId(data.getAssayId());
                            test.setAssayName(data.getAssayName());
                            test.setDeviceId(data.getDeviceId());
                            test.setErrorMessage(data.getErrorMessage());
                            test.setExpiryDate(data.getExpiryDate());
                            test.setOperator(data.getOperator());
                            test.setSample(data.getSample());
                            test.setSoftwareVersion(data.getSoftwareVersion());
                            test.setSample(data.getSample());
                            test.setCd3cd4(data.getCd3cd4());
                            test.setResultDate(data.getResultDate());
                            test.setStartTime(data.getStartTime());
                            pimaBeadRepository.save(test);
                        }

                        log.info(child.getName() + "== file imported");
                        boolean refresh = false;
                        PimaData firstRow = pimaList.get(0);
                        Optional<Device> deviceOptional = deviceRepository.getDeviceBySerialNumber(firstRow.getDeviceId());
                        Device device;
                        if (deviceOptional.isEmpty()) {
                            device = new Device();
                            device.setSerialNumber(firstRow.getDeviceId());
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
                        for (int i = 0; i < pimaList.size(); i++) {
                            deviceService.updateCartridgeByDevice(device);
                        }

                        deviceService.verifyRemainingInventoryByDevice(device);

                        NotificationMessage notificationMessage = NotificationMessage.DATA_SENT_TO_NODE;
                        MessageNotification messageNotification = messageNotificationRepository.findByTriggerName(notificationMessage.getTriggerName());
                        if (messageNotification.getStatus()) {
                            String message = messageNotification.getText();
                            Broadcaster.broadcast(message.replace("#deviceCode", device.getSerialNumber()), NotificationVariant.LUMO_SUCCESS);
                        }
                        if (refresh) {
                            Broadcaster.broadcast("reload", null);
                            Broadcaster.broadcast("Device " + device.getDeviceType().getScreenName() + " registered in the node",
                                    NotificationVariant.LUMO_SUCCESS);
                        }
                        FileUtils.deleteQuietly(child);

                    } else if (validaPimaCD4Header) {
                        boolean refresh = false;
                        pimaList = this.parsePIMAFile(child, Constants.PIMA_CATEGORY.PIMA_CD4);
                        for (PimaData data : pimaList) {
                            PimaBead test = new PimaBead();
                            test.setTestId(data.getTestId());
                            test.setBarcode(data.getBarcode());
                            test.setDevice(data.getDevice());
                            test.setAssayId(data.getAssayId());
                            test.setAssayName(data.getAssayName());
                            test.setDeviceId(data.getDeviceId());
                            test.setErrorMessage(data.getErrorMessage());
                            test.setExpiryDate(data.getExpiryDate());
                            test.setOperator(data.getOperator());
                            test.setSample(data.getSample());
                            test.setSoftwareVersion(data.getSoftwareVersion());
                            test.setSample(data.getSample());
                            test.setCd3cd4(data.getCd3cd4());
                            test.setResultDate(data.getResultDate());
                            test.setStartTime(data.getStartTime());
                            pimaBeadRepository.save(test);
                        }
                        log.info(child.getName() + "== file imported");

                        PimaData firstRow = pimaList.get(0);
                        Optional<Device> deviceOptional = deviceRepository.getDeviceBySerialNumber(firstRow.getDeviceId());
                        Device device;
                        if (deviceOptional.isEmpty()) {
                            device = new Device();
                            device.setSerialNumber(firstRow.getDeviceId());
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
                        for (int i = 0; i < pimaList.size(); i++) {
                            deviceService.updateCartridgeByDevice(device);
                        }

                        deviceService.verifyRemainingInventoryByDevice(device);

                        NotificationMessage notificationMessage = NotificationMessage.DATA_SENT_TO_NODE;
                        MessageNotification messageNotification = messageNotificationRepository.findByTriggerName(notificationMessage.getTriggerName());
                        if (messageNotification.getStatus()) {
                            String message = messageNotification.getText();
                            Broadcaster.broadcast(message.replace("#deviceCode", device.getSerialNumber()), NotificationVariant.LUMO_SUCCESS);
                        }
                        if (refresh) {
                            Broadcaster.broadcast("reload", null);
                            Broadcaster.broadcast("Device " + device.getDeviceType().getScreenName() + " registered in the node",
                                    NotificationVariant.LUMO_SUCCESS);
                        }
                        FileUtils.deleteQuietly(child);

                    } else {
                        log.severe(child.getName() + " not valid Pima file. ");
                    }

                }

            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "PIMA file copy operation failed", e);
        }

    }

    @Override
    public void exportPimaToFile(File targetFolder) throws IOException {
        exportPimaBeadsToFile(targetFolder);
        exportPimaCd4ToFile(targetFolder);
    }

    private List<String> getPIMAHeaders(File sourceFile) throws IOException {
        List<String> headerList = new ArrayList<>();
        @Cleanup FileReader fileReader = new FileReader(sourceFile);
        @Cleanup CSVReader reader = new CSVReader(fileReader);
        ICommonsList<String> nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.get(0).startsWith("C")) {
                String line = StringUtils.substringAfter(nextLine.get(0), ";K");
                String headerCell = line.replaceAll("\"", "");
                headerList.add(headerCell);
                if (headerCell.equals("Software Version")) {
                    break;
                }
            }
        }
        return headerList;
    }

    private List<PimaData> parsePIMAFile(File sourceFile, Constants.PIMA_CATEGORY type) throws IOException {
        int beadsSize = Constants.pimaBeadsTemplateFormat.size();
        int cd4Size = Constants.pimaCd4TemplateFormat.size();
        int maxIndex = 0;
        List<PimaData> dataList = new ArrayList<>();
        @Cleanup FileReader fileReader = new FileReader(sourceFile);
        @Cleanup CSVReader reader = new CSVReader(fileReader);
        int mainIndex = 0;
        switch (type) {
            case PIMA_CD4:
                maxIndex = cd4Size;
                break;
            case PIMA_BEADS:
                maxIndex = beadsSize;
                break;
        }
        ICommonsList<String> nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.get(0).startsWith("C")) {
                mainIndex++;
                if (mainIndex > maxIndex) {
                    ICommonsList<String> data;
                    mainIndex = 1;
                    String line = StringUtils.substringAfter(nextLine.get(0), ";K");
                    String value = line.replaceAll("\"", "");
                    PimaData pimaData = new PimaData();
                    pimaData.setTestId(Integer.parseInt(value));
                    while ((data = reader.readNext()) != null) {
                        if (data.get(0).startsWith("C")) {
                            mainIndex++;
                            line = StringUtils.substringAfter(data.get(0), ";K");
                            value = line.replaceAll("\"", "");
                            switch (mainIndex) {
                                case 1:
                                    pimaData = new PimaData();
                                    pimaData.setTestId(Integer.parseInt(value));
                                    break;
                                case 2:
                                    pimaData.setDeviceId(value);
                                    break;
                                case 3:
                                    pimaData.setAssayId(value);
                                    break;
                                case 4:
                                    pimaData.setAssayName(value);
                                    break;
                                case 5:
                                    pimaData.setSample(value);
                                    break;
                                case 6:
                                    if (!value.isEmpty()) {
                                        pimaData.setCd3cd4(Integer.parseInt(value));
                                    }
                                    break;
                                case 7:
                                    pimaData.setErrorMessage(value);
                                    break;
                                case 8:
                                    pimaData.setOperator(value);
                                    break;
                                case 9:
                                    if (!value.isEmpty()) {
                                        Long longValue = Long.parseLong(value);
                                        pimaData.setResultDate(LocalDate.ofEpochDay(longValue));
                                    }
                                    break;
                                case 10:
                                    if (!value.isEmpty()) {
                                        pimaData.setStartTime(LocalDateTime.parse(value + ":00"));
                                    }
                                    break;
                                case 11:
                                    pimaData.setBarcode(value);
                                    break;
                                case 12:
                                    pimaData.setExpiryDate(value);
                                    break;
                                case 13:
                                    if (type.equals(Constants.PIMA_CATEGORY.PIMA_BEADS)) {
                                        pimaData.setDevice(value);
                                    } else {
                                        pimaData.setVolume(value);
                                    }
                                    break;
                                case 14:
                                    if (type.equals(Constants.PIMA_CATEGORY.PIMA_BEADS)) {
                                        pimaData.setSoftwareVersion(value);
                                    } else {
                                        pimaData.setDevice(value);
                                    }
                                    break;
                                case 15:
                                    pimaData.setReagent(value);
                                    break;
                                case 16:
                                    pimaData.setSoftwareVersion(value);
                                    break;
                                default:
                                    break;
                            }
                            if (mainIndex == (maxIndex)) {
                                mainIndex = 0;
                                dataList.add(pimaData);
                            }
                        }
                    }
                    break;
                }
            }
        }
        return dataList;
    }


    public void exportPimaBeadsToFile(File targetFolder) throws IOException {
        List<PimaBead> list = pimaBeadRepository.findAllByIsExportedFalse();
        if (list.isEmpty())
            return;
        String NEW_LINE_SEPARATOR = "\n";
        // Create the CSVFormat object with "," as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();
        File expFile = null;
        Optional<NodeHealthStatistics> nodeHealthStatistics = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty() || !nodeConfiguration.get().getIsExported()) {
            log.warning("Node configuration isn't exported yet. Pima Beads Data won't get exported.");
            return;
        }

        String macId = "";
        if (nodeHealthStatistics.isPresent())
            macId = nodeHealthStatistics.get().getMacId();
        String fName = Constants.PIMA_CATEGORY.PIMA_BEADS + "-" + nodeConfiguration.get().getFacility();
        String fUniqueKey = new SimpleDateFormat("yyMMddhhmmssSSS").format(new Date());
        expFile = new File(targetFolder + File.separator + fName + "-"
                + list.get(0).getDevice() + "(" + fUniqueKey + ").csv");
        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        // initialize CSVPrinter object
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
        // write header
        csvFilePrinter.printRecord(Constants.pimaBeadsTemplateFormat_H);
        for (PimaBead data : list) {
            List<Object> tempData = writePimaBeads(data, macId, nodeConfiguration.get());
            csvFilePrinter.printRecord(tempData);
        }
        for (PimaBead test : list) {
            test.setIsExported(true);
            pimaBeadRepository.save(test);
        }
        log.info(Constants.PIMA_CATEGORY.PIMA_BEADS + " file was created successfully");
    }

    public void exportPimaCd4ToFile(File targetFolder) throws IOException {
        List<PimaCd4> list = pimaCd4Repository.findAllByIsExportedFalse();
        if (list.isEmpty())
            return;
        String NEW_LINE_SEPARATOR = "\n";
        // Create the CSVFormat object with "," as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();
        File expFile = null;
        Optional<NodeHealthStatistics> nodeHealthStatistics = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty() || !nodeConfiguration.get().getIsExported()) {
            log.warning("Node configuration isn't exported yet. Pima cd4 Data won't get exported.");
            return;
        }

        String macId = "";
        if (nodeHealthStatistics.isPresent())
            macId = nodeHealthStatistics.get().getMacId();
        String fName = Constants.PIMA_CATEGORY.PIMA_CD4 + "-" + nodeConfiguration.get().getFacility();
        String fUniqueKey = new SimpleDateFormat("yyMMddhhmmssSSS").format(new Date());
        expFile = new File(targetFolder + File.separator + fName + "-"
                + list.get(0).getDevice() + "(" + fUniqueKey + ").csv");
        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        // initialize CSVPrinter object
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
        // write header
        csvFilePrinter.printRecord(Constants.pimaCd4TemplateFormat_H);
        for (PimaCd4 data : list) {
            List<Object> tempData = writePimaCD4Results(data, macId, nodeConfiguration.get());
            csvFilePrinter.printRecord(tempData);
        }
        for (PimaCd4 test : list) {
            test.setIsExported(true);
            pimaCd4Repository.save(test);
        }
        log.info(Constants.PIMA_CATEGORY.PIMA_BEADS + " file was created successfully");
    }


    private List<Object> writePimaBeads(PimaBead data, String macId, NodeConfiguration nodeConfiguration) {
        List<Object> tempData = new ArrayList<>();
        try {
            tempData.add(macId);
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(nodeConfiguration.getCountry());
            tempData.add(nodeConfiguration.getProvince());
            tempData.add(nodeConfiguration.getDistrict());
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(data.getDeviceId());
            tempData.add(data.getTestId());
            tempData.add(data.getDeviceId());
            tempData.add(data.getAssayId());
            tempData.add(data.getAssayName());
            tempData.add(data.getSample());
            tempData.add(data.getCd3cd4());
            tempData.add(data.getErrorMessage());
            tempData.add(data.getOperator());
            tempData.add(data.getResultDate().format(DateUtils.DATE_FORMAT));
            tempData.add(data.getStartTime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getBarcode());
            tempData.add(data.getExpiryDate());
            tempData.add(data.getDevice());
            tempData.add(data.getSoftwareVersion());
            return tempData;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CsvFileWriter for --" + data.getDeviceId()
                    + "-- operation failed", e);
            return tempData;
        }

    }

    private List<Object> writePimaCD4Results(PimaCd4 data, String macId, NodeConfiguration nodeConfiguration) {
        List<Object> tempData = new ArrayList<>();
        try {
            tempData.add(macId);
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(nodeConfiguration.getCountry());
            tempData.add(nodeConfiguration.getProvince());
            tempData.add(nodeConfiguration.getDistrict());
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(data.getDeviceId());
            tempData.add(data.getTestId());
            tempData.add(data.getDeviceId());
            tempData.add(data.getAssayId());
            tempData.add(data.getAssayName());
            tempData.add(data.getSample());
            tempData.add(data.getCd3cd4());
            tempData.add(data.getErrorMessage());
            tempData.add(data.getOperator());
            tempData.add(data.getResultDate().format(DateUtils.DATE_FORMAT));
            tempData.add(data.getStartTime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getBarcode());
            tempData.add(data.getExpiryDate());
            tempData.add(data.getVolume());
            tempData.add(data.getDevice());
            tempData.add(data.getReagent());
            tempData.add(data.getSoftwareVersion());
            return tempData;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CsvFileWriter for --" + data.getDeviceId()
                    + "-- operation failed", e);
            return tempData;
        }
    }
}
