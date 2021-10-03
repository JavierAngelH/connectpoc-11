package com.edgedx.connectpoc.tasks;

import com.edgedx.connectpoc.entity.MessageNotification;
import com.edgedx.connectpoc.model.NotificationMessage;
import com.edgedx.connectpoc.repository.MessageNotificationRepository;
import com.edgedx.connectpoc.service.NodeStatisticsService;
import com.edgedx.connectpoc.service.PropertiesService;
import com.edgedx.connectpoc.views.Broadcaster;
import com.edgedx.connectpoc.views.cartridges.CartridgeInventoryView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.logging.Level;

@Component
@Log
public class ImportNodeStatisticsTask {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    NodeStatisticsService nodeStatisticsService;

    @Autowired
    MessageNotificationRepository messageNotificationRepository;

    @Scheduled(cron = "${schedule.everyminute.cron}")
    public void publish() {
        String sourcePath = propertiesService.getFullPropertyValue("bd.presto.environmental.folder");
        try {
            File sourceFolder = new File(sourcePath);
            nodeStatisticsService.importNodeStatFile(sourceFolder);
            nodeStatisticsService.importTemperatureFile(sourceFolder);
        } catch (Exception e) {
            log.log(Level.SEVERE,"Node statistics import file failed", e);
        }

    }

}
