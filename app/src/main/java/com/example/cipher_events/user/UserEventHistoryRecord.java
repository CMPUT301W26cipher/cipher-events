package com.example.cipher_events.user;

import com.example.cipher_events.database.Event;
import com.example.cipher_events.user.Status;

/**
 * Stores one event history entry for a user.
 */
public class UserEventHistoryRecord {
    private Event event;
    private Status status;

    public UserEventHistoryRecord(Event event, Status status) {
        this.event = event;
        this.status = status;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UserEventHistoryRecord{" +
                "event=" + (event == null ? "null" : event.getName()) +
                ", status=" + status +
                '}';
    }
}