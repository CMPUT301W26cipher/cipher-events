package com.example.cipher_events.user;

import com.example.cipher_events.database.Event;

/**
 * Result of scanning a QR code and resolving it to an event.
 */
public class EntrantQrScanResult {
    private final String eventId;
    private final Event event;

    public EntrantQrScanResult(String eventId, Event event) {
        this.eventId = eventId;
        this.event = event;
    }

    public String getEventId() {
        return eventId;
    }

    public Event getEvent() {
        return event;
    }
}