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

    public Message(String title, String body, Organizer organizer) {
        this.date = new Date();
        this.title = title;
        this.body = body;
        this.organizer = organizer;
    }

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
}
