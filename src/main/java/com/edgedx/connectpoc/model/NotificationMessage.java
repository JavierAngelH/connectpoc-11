package com.edgedx.connectpoc.model;

public enum NotificationMessage {

    DATA_SENT_TO_NODE(1, "Successful data transmission to node "),
    DATA_SENT_TO_SERVER(2, "Successful data transmission to server"),
    SUCCESSFUL_DATA_TRANSMISSION_FOR_A_WEEK(3, "Successful data transmission for a week"),
    POWER_OUTAGE(4, "Power outage"),
    POWER_OUTAGE_SMS(5, "Power outage SMS"),
    POWER_OUTAGE_MAIL(6, "Power outage mail"),
    NOT_SENDING_DATA_PAST_WEEK(7, "Not sending data "),
    EMAIL_NOT_SENDING_DATA_PAST_WEEK(8, "Not sending data mail"),
    RUNNING_OUT_OF_INVENTORY(9, "Cartridge inventory"),
    EXPIRING_CARTRIDGE(10, "Cartridge expiration"),
    EMPTY_INVENTORY(11, "Empty Inventory"),
    PROBLEM_NOTIFICATION_SMS(12, "Problem Notification SMS"),
    PROBLEM_NOTIFICATION_MAIL(13, "Problem Notification Email"),
    ;

   private final Integer code;

    private final String triggerName;

    private NotificationMessage(Integer code, String triggerName) {
        this.code = code;
        this.triggerName = triggerName;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getTriggerName() {
        return this.triggerName;
    }
}
