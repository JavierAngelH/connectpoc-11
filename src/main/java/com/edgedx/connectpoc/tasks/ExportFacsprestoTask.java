package com.edgedx.connectpoc.tasks;

import com.edgedx.connectpoc.service.FacsprestoService;
import com.edgedx.connectpoc.service.PropertiesService;
import com.edgedx.connectpoc.utils.Constants;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.logging.Level;

@Component
@Log
public class ExportFacsprestoTask {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    FacsprestoService facsprestoService;

    @Scheduled(cron = "${schedule.everyminute.cron}")
    public void publish() {
        String targetPath = propertiesService.getFullPropertyValue("ftp.file.destination.folder");
        try {
            File targetFolder = new File(targetPath);
            facsprestoService.exportBDPrestoFile(targetFolder, Constants.PRESTO_CATEGORY.PATIENT_SAMPLE);
            facsprestoService.exportBDPrestoFile(targetFolder, Constants.PRESTO_CATEGORY.CD4_CONTROL_PROCESS);
            facsprestoService.exportBDPrestoFile(targetFolder, Constants.PRESTO_CATEGORY.HB_CONTROL_PROCESS);
            facsprestoService.exportBDPrestoFile(targetFolder, Constants.PRESTO_CATEGORY.INSTRUMENT_QC);

        } catch (Exception e) {
            log.log(Level.SEVERE, "Facspresto export file failed", e);
        }
    }

}
