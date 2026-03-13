package com.example.cipher_events.organizer;

import com.example.cipher_events.database.Event;

/**
 * Result object returned after creating an event and its QR code.
 */
public class OrganizerEventCreationResult {
    private final String eventId;
    private final Event event;
    private final String qrPayload;

    public OrganizerEventCreationResult(String eventId, Event event, String qrPayload) {
        this.eventId = eventId;
        this.event = event;
        this.qrPayload = qrPayload;
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
}