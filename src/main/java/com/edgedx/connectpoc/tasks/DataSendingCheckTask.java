package com.edgedx.connectpoc.tasks;

import com.edgedx.connectpoc.model.NotificationMessage;
import com.edgedx.connectpoc.repository.MessageNotificationRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Log
public class DataSendingCheckTask {

    @Autowired
    MessageNotificationRepository messageNotificationRepository;

    @Scheduled(cron = "${schedule.weekly.cron}")
    public void publish() {
        if(messageNotificationRepository.findByTriggerName(NotificationMessage.NOT_SENDING_DATA_PAST_WEEK.getTriggerName()).getStatus()){
       //     this.deviceService.verifyLatestSentDataByDevice();
            log.info("Weekly check of sent data started");
        }
        if(messageNotificationRepository.findByTriggerName(NotificationMessage.SUCCESSFUL_DATA_TRANSMISSION_FOR_A_WEEK.getTriggerName()).getStatus()){
         //   this.deviceService.verifyLast7DaysContinuousData();
            log.info("Weekly check of sent data all 7 days started");

        }

    }
}
