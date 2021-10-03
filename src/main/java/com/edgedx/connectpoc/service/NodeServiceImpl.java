package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.Device;
import com.edgedx.connectpoc.entity.NodeConfiguration;
import com.edgedx.connectpoc.entity.NodeHealthStatistics;
import com.edgedx.connectpoc.repository.DeviceRepository;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import com.edgedx.connectpoc.repository.NodeHealthStatisticsRepository;
import com.edgedx.connectpoc.utils.Constants;
import com.edgedx.connectpoc.utils.OutputHandler;
import lombok.Cleanup;
import lombok.extern.java.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Log
public class NodeServiceImpl implements NodeService {

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    NodeHealthStatisticsRepository nodeHealthStatisticsRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @Override
    public void exportNodeConfigToFile(File targetFolder) throws IOException {
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if(nodeConfiguration.isEmpty()){
            log.severe("Node is not configured yet");
            return;
        }
        if(nodeConfiguration.get().getIsExported())
            return;

        String macId = "";
        Optional<NodeHealthStatistics> nodeHealthStatistics = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
        if(nodeHealthStatistics.isPresent())
            macId = nodeHealthStatistics.get().getMacId();
        String NEW_LINE_SEPARATOR = "\n";
        // Create the CSVFormat object with "," as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();

        String fName = "NodeConfig";
        String fUniqueKey = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());
        File expFile = new File(targetFolder + File.separator + fName + "-(" + fUniqueKey + ").csv");

        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        // write header
        csvFilePrinter.printRecord(Constants.nodeConfigTemplateFormat_H);
            List<Object> tempData = new ArrayList<>();
            tempData = this.writeNodeConfigResults(nodeConfiguration.get(), macId);
            csvFilePrinter.printRecord(tempData);

        log.info("Node Config. file was created successfully");
        NodeConfiguration nodeConfig = nodeConfiguration.get();
        nodeConfig.setIsExported(true);
        nodeConfigurationRepository.save(nodeConfig);
    }

    @Override
    public void exportNodeDevicesToFile(File targetFolder) throws IOException {
        List<Device> devices =deviceRepository.findAllByIsExportedFalse();
        if(devices.isEmpty())
            return;

        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if(nodeConfiguration.isEmpty()){
            log.severe("Node is not configured yet");
            return;
        }
        if(!nodeConfiguration.get().getIsExported())
            return;

        String macId = "";
        Optional<NodeHealthStatistics> nodeHealthStatistics = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
        if(nodeHealthStatistics.isPresent())
            macId = nodeHealthStatistics.get().getMacId();
        String NEW_LINE_SEPARATOR = "\n";
        // Create the CSVFormat object with "," as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();

        String fName = "NodeDevices";
        String fUniqueKey = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());
        File expFile = new File(targetFolder + File.separator + fName + "-(" + fUniqueKey + ").csv");

        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        // write header
        csvFilePrinter.printRecord(Constants.nodeDevicesTemplateFormat_H);
        List<Object> tempData = new ArrayList<>();
        for (Device device: devices) {
            tempData = writeNodeDevices(device, macId);
            csvFilePrinter.printRecord(tempData);
        }
        log.info("Node device file was created successfully");
        for (Device device: devices) {
            device.setIsExported(true);
            deviceRepository.save(device);
        }
    }

    @Override
    public void updateProject() {
        String scriptLocation = propertiesService.getFullPropertyValue("connectpoc.update.script");
        log.info("executing " + scriptLocation);
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "sudo " + scriptLocation);
            process = pb.start();
            log.info("executing update project script ");
            OutputHandler out = new OutputHandler(process.getInputStream(), "UTF-8");
            OutputHandler err = new OutputHandler(process.getErrorStream(), "UTF-8");
            int status = process.waitFor();
            log.info("Status: " + status);
            out.join();
            log.info("Output:");
            log.info(out.getText());
            err.join();
            log.info("Error:");
            log.info(err.getText());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log.severe("can't execute update project script " + e.getMessage());
        }finally {
            if(process != null)
                process.destroy();
        }
    }

    private List<Object> writeNodeConfigResults(NodeConfiguration data, String macId) {
        List<Object> tempData = new ArrayList<>();
        tempData.add(data.getCountry());
        tempData.add(data.getProvince());
        tempData.add(data.getDistrict());
        tempData.add(data.getFacility());
        tempData.add(data.getFacilityCode());
        tempData.add(macId);
        return tempData;
    }

    private List<Object> writeNodeDevices(Device data, String macId) {
        List<Object> tempData = new ArrayList<>();
        tempData.add(data.getSerialNumber());
        tempData.add(data.getTestingPoint());
        tempData.add(data.getDeviceType().getDescription());
        tempData.add(macId);
        return tempData;
    }
}
