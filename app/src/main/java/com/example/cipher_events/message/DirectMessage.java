package com.example.cipher_events.message;

import java.util.UUID;

public class DirectMessage {
    private String messageID;
    private String senderDeviceID;
    private String senderName;
    private String senderRole; // "entrant" or "organizer"
    private String content;
    private String timestamp;

    public DirectMessage() {
        // Required for Firestore
    }

    public DirectMessage(String senderDeviceID,
                         String senderName,
                         String senderRole,
                         String content,
                         String timestamp) {
        this.messageID = UUID.randomUUID().toString();
        this.senderDeviceID = senderDeviceID;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getSenderDeviceID() {
        return senderDeviceID;
    }

    public void setSenderDeviceID(String senderDeviceID) {
        this.senderDeviceID = senderDeviceID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}