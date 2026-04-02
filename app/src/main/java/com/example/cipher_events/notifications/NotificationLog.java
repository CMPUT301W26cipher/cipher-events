package com.example.cipher_events.notifications;

import java.util.Date;

/**
 * Represents one notification record for logging purposes.
 */
public class NotificationLog {

    private final String deviceId;
    private final String title;
    private final String message;
    private final Date timestamp;

    public NotificationLog(String deviceId, String title, String message) {
        this.deviceId = deviceId;
        this.title = title;
        this.message = message;
        this.timestamp = new Date();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}