package com.example.cipher_events.notifications;

import com.example.cipher_events.database.Organizer;

import java.util.Date;

/*
 * Represents a message.
 * Each message has a date, title, body, and organizer.
 */
public class Message {
    private Date date;
    private String title;
    private String body;
    private Organizer organizer;
    private String recipientID; // Added for user-specific messaging

    public Message(String title, String body, Organizer organizer) {
        this(title, body, organizer, null);
    }

    public Message(String title, String body, Organizer organizer, String recipientID) {
        this.date = new Date();
        this.title = title;
        this.body = body;
        this.organizer = organizer;
        this.recipientID = recipientID;
    }

    public Message(){}

    public Date getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    public String getRecipientID() {
        return recipientID;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }


    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }
}
