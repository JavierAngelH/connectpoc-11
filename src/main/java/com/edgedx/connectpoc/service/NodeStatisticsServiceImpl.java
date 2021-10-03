package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.MessageNotification;
import com.edgedx.connectpoc.entity.NodeConfiguration;
import com.edgedx.connectpoc.entity.NodeHealthStatistics;
import com.edgedx.connectpoc.model.NotificationMessage;
import com.edgedx.connectpoc.repository.MessageNotificationRepository;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import com.edgedx.connectpoc.repository.NodeHealthStatisticsRepository;
import com.edgedx.connectpoc.utils.Constants;
import com.edgedx.connectpoc.utils.DateUtils;
import com.edgedx.connectpoc.views.Broadcaster;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.Cleanup;
import lombok.extern.java.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

@Service
@Log
public class NodeStatisticsServiceImpl implements NodeStatisticsService {

    @Autowired
    NodeHealthStatisticsRepository nodeHealthStatisticsRepository;

    @Autowired
    MessageNotificationRepository messageNotificationRepository;

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Override
    public void importNodeStatFile(File sourceFolder) {
        CSVParser parser = null;
        FileReader fileReader = null;
        try {
            // get downloaded files with .csv extension
            File[] files = sourceFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".csv");
                }
            });

            // if files are found; loop through files
            if (files != null) {
                CSVFormat format = CSVFormat.EXCEL.withHeader();
                for (File nsfile : files) {
                    // check if file is node stat file
                    boolean validNodeStatFile = false;
                    boolean validIncompleteNodeStatFile = false;
                    fileReader = new FileReader(nsfile);
                    parser = new CSVParser(new FileReader(nsfile), format);
                    Map<String, Integer> headers = parser.getHeaderMap();
                    List<String> headersList = new ArrayList<>(headers.keySet());

                    if (headersList.size() == Constants.nodeStatTemplateFormat.size()) {
                        if (headersList.equals(Constants.nodeStatTemplateFormat)) {
                            validNodeStatFile = true;
                        }
                    }

                    if (headersList.size() == Constants.incompleteNodeStatTemplateFormat.size()) {
                        if (headersList.equals(Constants.incompleteNodeStatTemplateFormat)) {
                            validIncompleteNodeStatFile = true;
                        }
                    }
                    /// end of file check
                    if (validNodeStatFile) {
                        List<NodeHealthStatistics> nhstatList = new ArrayList<>();
                        for (CSVRecord record : parser) {
                            NodeHealthStatistics nhstat = new NodeHealthStatistics();
                            nhstat.setMacId(record.get("mac_id"));

                            Long timestampLong = Long.parseLong(record.get("timestamp"));

                            nhstat.setLogDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampLong),
                                    TimeZone.getDefault().toZoneId()));
                            nhstat.setNetworkOperator(record.get("network_operator"));
                            nhstat.setLatitude(record.get("device_latitude"));
                            nhstat.setLongitude(record.get("device_longitude"));
                            if (record.get("cell_signal_strength") != null && !record.get("cell_signal_strength").isEmpty())
                                nhstat.setNetworkSignalStrength(Integer.parseInt(record.get("cell_signal_strength")));
                            else
                                nhstat.setNetworkSignalStrength(0);
                            if (record.get("battery_level") != null && !record.get("battery_level").isEmpty())
                                nhstat.setBatteryStatus(Integer.parseInt(record.get("battery_level")));
                            else
                                nhstat.setBatteryStatus(0);
                            nhstat.setTemperature(Double.parseDouble(record.get("temperature")));
                            nhstat.setHumidity(Double.parseDouble(record.get("humidity")));
                            nhstat.setAmbientLight(Double.parseDouble(record.get("ambient_light")));
                            nhstatList.add(nhstat);
                        }
                        nodeHealthStatisticsRepository.saveAll(nhstatList);
                    } else if (validIncompleteNodeStatFile) {
                        List<NodeHealthStatistics> nhstatList = new ArrayList<>();
                        for (CSVRecord record : parser) {
                            NodeHealthStatistics nhstat = new NodeHealthStatistics();
                            // mac_id,timestamp,network_operator,device_latitude,device_longitude,cell_signal_strength,battery_level
                            nhstat.setMacId(record.get("mac_id"));
                            Long timestampLong = Long.parseLong(record.get("timestamp"));
                            nhstat.setLogDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampLong),
                                    TimeZone.getDefault().toZoneId()));
                            nhstat.setNetworkOperator(record.get("network_operator"));
                            nhstat.setLatitude(record.get("device_latitude"));
                            nhstat.setLongitude(record.get("device_longitude"));
                            if (record.get("cell_signal_strength") != null && !record.get("cell_signal_strength").isEmpty())
                                nhstat.setNetworkSignalStrength(Integer.parseInt(record.get("cell_signal_strength")));
                            else
                                nhstat.setNetworkSignalStrength(0);
                            if (record.get("battery_level") != null && !record.get("battery_level").isEmpty())
                                nhstat.setBatteryStatus(Integer.parseInt(record.get("battery_level")));
                            else
                                nhstat.setBatteryStatus(0);
                            nhstatList.add(nhstat);
                        }
                        nodeHealthStatisticsRepository.saveAll(nhstatList);
                    }
                    if (validIncompleteNodeStatFile || validNodeStatFile) {
                        MessageNotification powerOutageMessage = messageNotificationRepository.findByTriggerName(NotificationMessage.POWER_OUTAGE.getTriggerName());

                        if (powerOutageMessage.getStatus()) {
                            Optional<NodeHealthStatistics> currentNodeStat = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
                            Integer battery = 0;
                            if (currentNodeStat.isPresent())
                                battery = currentNodeStat.get().getBatteryStatus();

                            if (battery <= 10) {
                                Broadcaster.broadcast(powerOutageMessage.getText(), NotificationVariant.LUMO_ERROR);
                                MessageNotification powerOutageSMS = messageNotificationRepository.findByTriggerName(NotificationMessage.POWER_OUTAGE_SMS.getTriggerName());

                                if (powerOutageSMS.getStatus()) {
                                    try {
                                        sendPowerOutageSMS(powerOutageSMS.getText());
                                    } catch (IOException e) {
                                        log.log(Level.SEVERE, e.getMessage(), e);
                                    }
                                }
                                MessageNotification powerOutageMail = messageNotificationRepository.findByTriggerName(NotificationMessage.POWER_OUTAGE_MAIL.getTriggerName());
                                if (powerOutageMail.getStatus()) {
                                    try {
                                        sendPowerOutageMail(powerOutageMail.getText());
                                    } catch (IOException e) {
                                        log.log(Level.SEVERE, e.getMessage(), e);
                                    }
                                }
                            }
                        }
                        FileUtils.deleteQuietly(nsfile);
                    } else {
                        log.log(Level.SEVERE, "Incorrect node stat file format: " + nsfile.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Node health stats file import failed", e);
        } finally {
            if (fileReader != null)
                try {
                    fileReader.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
            if (parser != null)
                try {
                    parser.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
        }
    }

    @Override
    public void importTemperatureFile(File sourcePath) {
        CSVParser parser = null;
        try {
            // get downloaded files with .csv extension
            File[] files = sourcePath.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".csv");
                }
            });

            // if files are found; loop through files
            if (files != null) {
                CSVFormat format = CSVFormat.EXCEL.withHeader();
                for (File nsfile : files) {
                    boolean validNodeStatFile = false;
                    boolean validIncompleteNodeStatFile = false;
                    parser = new CSVParser(new FileReader(nsfile), format);
                    Map<String, Integer> headers = parser.getHeaderMap();
                    List<String> headersList = new ArrayList<>(headers.keySet());
                    if (headersList.size() == 2) {
                        // check if file is node stat file
                        format = CSVFormat.EXCEL;
                        parser = new CSVParser(new FileReader(nsfile), format);

                        for (CSVRecord record : parser) {
                            LocalDateTime dateTime = DateUtils.parseTemperatureDate(record.get(1));
                            Double temp = Double.parseDouble(record.get(0));

                            Optional<NodeHealthStatistics> nodeHealthStatistics = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
                            if (nodeHealthStatistics.isPresent()) {
                                NodeHealthStatistics stats = nodeHealthStatistics.get();
                                stats.setIsExported(false);
                                stats.setTemperature(temp);
                                if (dateTime != null)
                                    stats.setLogDate(dateTime);
                                stats.setSysLogDate(LocalDateTime.now());
                                nodeHealthStatisticsRepository.save(stats);
                            }

                        }

                        FileUtils.deleteQuietly(nsfile);

                    } else {
                        parser.close();

                    }

                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Temperature file import operation failed", e);
        } finally {
            if (parser != null)
                try {
                    parser.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
        }
    }

    @Override
    public void exportNodeStatToFile(File targetFolder) throws IOException {
        List<NodeHealthStatistics> healthStatistics = nodeHealthStatisticsRepository.findAllByIsExportedFalse();
        if(healthStatistics.isEmpty())
            return;

        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if(nodeConfiguration.isEmpty()){
            log.severe("Node is not configured yet");
            return;
        }
        if(!nodeConfiguration.get().getIsExported())
            return;

        String NEW_LINE_SEPARATOR = "\n";
        // Create the CSVFormat object with "," as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();

        String fName = "NodeStat";
        String fUniqueKey = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());
        File expFile = new File(targetFolder + File.separator + fName + "-(" + fUniqueKey + ").csv");

        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        String power = "No";
        List<NodeHealthStatistics> latest2 = nodeHealthStatisticsRepository.findTop2ByOrderByLogDateDesc();
        if(latest2.size() == 2){
            if(latest2.get(0).getBatteryStatus() >= latest2.get(1).getBatteryStatus())
                power = "Yes";
        }
        // write header
        csvFilePrinter.printRecord(Constants.nodeStatTemplateFormat_H);
        List<Object> tempData = new ArrayList<>();
        for (NodeHealthStatistics nodeHealth: healthStatistics) {
            tempData = writeNodeStatResults(nodeHealth, nodeConfiguration.get(), power);
            csvFilePrinter.printRecord(tempData);
        }
        log.info("Node health statistics file was created successfully");
        for (NodeHealthStatistics nodeHealth: healthStatistics) {
            nodeHealth.setIsExported(true);
            nodeHealthStatisticsRepository.save(nodeHealth);
        }
    }

    private List<Object> writeNodeStatResults(NodeHealthStatistics data, NodeConfiguration nodeConfiguration, String power) {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.S");
        List<Object> tempData = new ArrayList<>();
        tempData.add(nodeConfiguration.getCountry());
        tempData.add(nodeConfiguration.getProvince());
        tempData.add(nodeConfiguration.getDistrict());
        tempData.add(nodeConfiguration.getFacility());
        tempData.add(data.getMacId());
        tempData.add(data.getLatitude());
        tempData.add(data.getLongitude());
        tempData.add(data.getBatteryStatus());
        tempData.add(data.getNetworkOperator());
        tempData.add(data.getNetworkSignalStrength());
        tempData.add(data.getTemperature());
        tempData.add(data.getHumidity());
        tempData.add(data.getAmbientLight());
        tempData.add(data.getLogDate().format(timeFormat));
        tempData.add(1);
        tempData.add(power);
        return tempData;
    }

    private void sendPowerOutageSMS(String message) throws IOException {
        String phoneNumber = propertiesService.getPropertyValue("phone.number.contact");
        String notificationScript = propertiesService.getFullPropertyValue("sms.notification.script");
        ProcessBuilder pb = new ProcessBuilder(notificationScript, phoneNumber, message);
        Process proc = pb.start();
        int status = 0;
        try {
            status = proc.waitFor();
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        proc.destroy();
        log.info("Sending Power Outage SMS notification to " + phoneNumber + " status " + status);
    }

    private void sendPowerOutageMail(String message) throws IOException {
        Optional<NodeConfiguration> configuration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (!configuration.isPresent())
            return;
        Channel channel = null;
        InputStream input = null;
        Session session = null;
        InputStreamReader inputReader = null;
        BufferedReader bufferedReader = null;
        String mailAddress = propertiesService.getPropertyValue("mail.address.contact");
        String mailCertificate = propertiesService.getFullPropertyValue("mail.notification.certificate.path");

        message = message.replace("#facility", configuration.get().getFacility());
        message = message.replace("#district", configuration.get().getDistrict());
        String command = "mail -s 'Node Notification' " + mailAddress + " <<< '" + message + "'";

        try {
            JSch jsch = new JSch();
            String user = "ubuntu";
            String host = "brainchex.com";
            int port = 22;
            jsch.addIdentity(mailCertificate);
            session = jsch.getSession(user, host, port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            input = channel.getInputStream();
            channel.connect();
            inputReader = new InputStreamReader(input);
            bufferedReader = new BufferedReader(inputReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info(line);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (input != null)
                input.close();
            if (bufferedReader != null)
                bufferedReader.close();
            if (inputReader != null)
                inputReader.close();
            if (channel != null)
                channel.disconnect();
            if (session != null)
                session.disconnect();
        }
        log.info("Sending Power Outage mail notification to " + mailAddress);
    }
}
