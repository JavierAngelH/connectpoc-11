package com.edgedx.connectpoc.tasks;

import com.edgedx.connectpoc.service.MgitService;
import com.edgedx.connectpoc.service.PropertiesService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.logging.Level;

@Component
@Log
public class ImportMgitTask {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    MgitService mgitService;

    @Scheduled(cron = "${schedule.everyminute.cron}")
    public void publish(){
        String sourcePath = propertiesService.getFullPropertyValue("mgit.destination.folder");
        try{
            File sourceFolder = new File(sourcePath);
            mgitService.importMgitFiles(sourceFolder);
        } catch (Exception e) {
            log.log(Level.SEVERE,"MGIT import file failed", e);
        }
    }
}
