package com.example.cipher_events.message;

import java.util.ArrayList;
import java.util.UUID;

public class MessageThread {
    private String threadID;
    private String eventID;
    private String organizerDeviceID;
    private String entrantDeviceID;
    private ArrayList<DirectMessage> messages;

    public MessageThread() {
        // Required for Firestore
    }

    public MessageThread(String eventID, String organizerDeviceID, String entrantDeviceID) {
        this.threadID = UUID.randomUUID().toString();
        this.eventID = eventID;
        this.organizerDeviceID = organizerDeviceID;
        this.entrantDeviceID = entrantDeviceID;
        this.messages = new ArrayList<>();
    }

    public String getThreadID() {
        return threadID;
    }

    public void setThreadID(String threadID) {
        this.threadID = threadID;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getOrganizerDeviceID() {
        return organizerDeviceID;
    }

    public void setOrganizerDeviceID(String organizerDeviceID) {
        this.organizerDeviceID = organizerDeviceID;
    }

    public String getEntrantDeviceID() {
        return entrantDeviceID;
    }

    public void setEntrantDeviceID(String entrantDeviceID) {
        this.entrantDeviceID = entrantDeviceID;
    }

    public ArrayList<DirectMessage> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public void setMessages(ArrayList<DirectMessage> messages) {
        this.messages = messages;
    }
}