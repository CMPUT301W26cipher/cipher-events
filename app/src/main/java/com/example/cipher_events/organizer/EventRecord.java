package com.example.cipher_events.organizer;

import com.example.cipher_events.database.Event;

/**
 * Store one event together with its generated ID and Qr payload
 */
public class EventRecord {
    private final String eventId;
    private final Event event;
    private final String qrPayload;
    private final long createdAt;

    public EventRecord(String eventID, Event event, String qrPayload, long createdAt) {
        this.eventId = eventID;
        this.event = event;
        this.qrPayload = qrPayload;
        this.createdAt = createdAt;
    }

    public String getEventId() {
        return eventId;
    }

    public Event getEvent() {
        return event;
    }

    public String getQrPayload() {
        return qrPayload;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
