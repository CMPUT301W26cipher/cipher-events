package com.example.cipher_events.organizer;

import android.graphics.Bitmap;

import com.example.cipher_events.database.Event;

/**
 * Result object returned after creating an event and its QR code.
 */
public class OrganizerEventCreationResult {
    private final String eventId;
    private final Event event;
    private final String qrPayload;
    private final Bitmap qrBitmap;

    public OrganizerEventCreationResult(String eventId, Event event, String qrPayload, Bitmap qrBitmap) {
        this.eventId = eventId;
        this.event = event;
        this.qrPayload = qrPayload;
        this.qrBitmap = qrBitmap;
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

    public Bitmap getQrBitmap() {
        return qrBitmap;
    }
}