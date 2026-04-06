package com.example.cipher_events.message;

import androidx.annotation.NonNull;
import java.util.UUID;

/**
 * Represents a single message within a direct-message thread.
 */
public class DirectMessage {
    private String messageID;
    private String senderDeviceID;
    private String senderName;
    private String senderRole; // "entrant" or "organizer"
    private String content;
    private String timestamp;
    private String senderProfilePictureURL;

    public DirectMessage() {
        // Required for Firestore
    }

    public DirectMessage(String senderDeviceID,
                         String senderName,
                         String senderRole,
                         String content,
                         String timestamp) {
        this(senderDeviceID, senderName, senderRole, content, timestamp, null);
    }

    public DirectMessage(String senderDeviceID,
                         String senderName,
                         String senderRole,
                         String content,
                         String timestamp,
                         String senderProfilePictureURL) {
        this.messageID = UUID.randomUUID().toString();
        this.senderDeviceID = senderDeviceID;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.content = content;
        this.timestamp = timestamp;
        this.senderProfilePictureURL = senderProfilePictureURL;
    }

    // Getters and Setters

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

    public String getSenderProfilePictureURL() {
        return senderProfilePictureURL;
    }

    public void setSenderProfilePictureURL(String senderProfilePictureURL) {
        this.senderProfilePictureURL = senderProfilePictureURL;
    }

    // Helpers

    public boolean isFromOrganizer() {
        return "organizer".equalsIgnoreCase(senderRole);
    }

    public boolean isFromEntrant() {
        return "entrant".equalsIgnoreCase(senderRole);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", timestamp, senderName, content);
    }
}
