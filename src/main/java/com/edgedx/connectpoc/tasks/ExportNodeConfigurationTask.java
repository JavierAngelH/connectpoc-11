package com.edgedx.connectpoc.tasks;

import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import com.edgedx.connectpoc.service.NodeService;
import com.edgedx.connectpoc.service.PropertiesService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.logging.Level;

@Component
@Log
public class ExportNodeConfigurationTask {

    @Autowired
    NodeService nodeService;

    @Autowired
    PropertiesService propertiesService;

    @Scheduled(cron = "${schedule.everyminute.cron}")
    public void publish() {
        String targetPath = propertiesService.getFullPropertyValue("ftp.file.destination.folder");
        try{
            File targetFolder = new File(targetPath);
            nodeService.exportNodeConfigToFile(targetFolder);
        } catch (Exception e) {
            log.log(Level.SEVERE,"Node Config export file failed", e);
        }
    }

}
