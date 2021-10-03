package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.MessageNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageNotificationRepository extends JpaRepository<MessageNotification,Integer> {

MessageNotification findByTriggerName(String triggerName);
}
