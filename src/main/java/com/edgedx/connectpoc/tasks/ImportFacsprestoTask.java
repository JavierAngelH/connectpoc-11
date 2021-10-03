package com.edgedx.connectpoc.tasks;

import com.edgedx.connectpoc.service.FacsprestoService;
import com.edgedx.connectpoc.service.PropertiesService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.logging.Level;

@Component
@Log
public class ImportFacsprestoTask {

    @Autowired
    PropertiesService propertiesService;
    @Autowired
    FacsprestoService facsprestoService;

    @Scheduled(cron = "${schedule.everyminute.cron}")
    public void publish() {
        String sourcePath = propertiesService.getFullPropertyValue("bd.presto.source.folder");
        try{
            File sourceFolder = new File(sourcePath);
            facsprestoService.importFacsprestoFiles(sourceFolder);
        } catch (Exception e) {
            log.log(Level.SEVERE,"Facspresto import file failed", e);
        }
    }
}
