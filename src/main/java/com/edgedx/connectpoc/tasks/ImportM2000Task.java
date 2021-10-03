package com.edgedx.connectpoc.tasks;

import com.edgedx.connectpoc.service.AbbottM2000Service;
import com.edgedx.connectpoc.service.PropertiesService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.logging.Level;

@Component
@Log
public class ImportM2000Task {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    AbbottM2000Service abbottM2000Service;

    @Scheduled(cron = "${schedule.everyminute.cron}")
    public void publish(){
        String sourcePath = propertiesService.getFullPropertyValue("abbott.destination.folder");
        try{
            File sourceFolder = new File(sourcePath);
            abbottM2000Service.importAbbottFiles(sourceFolder);
        } catch (Exception e) {
            log.log(Level.SEVERE,"Abbott import file failed", e);
        }
    }
}
