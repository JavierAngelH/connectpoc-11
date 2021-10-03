package com.edgedx.connectpoc.tasks;

import com.edgedx.connectpoc.service.PimaService;
import com.edgedx.connectpoc.service.PropertiesService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.logging.Level;

@Component
@Log
public class ImportPimaTask {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    PimaService pimaService;

    @Scheduled(cron = "${schedule.everyminute.cron}")
    public void publish() {
        String sourcePath = propertiesService.getFullPropertyValue("bd.pima.destination.folder");
        try {
            File sourceFolder = new File(sourcePath);
            pimaService.importPimaFile(sourceFolder);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Pima import file failed", e);
        }
    }


}
