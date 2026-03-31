package com.example.cipher_events.organizer;

import android.graphics.Bitmap;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.message.MessageThread;

import java.util.ArrayList;
import java.util.UUID;

/**
 * US 02.01.01
 * Organizer creates a new event and generates a unique promotional QR code
 * that links to the event inside the app.
 *
 * Updated behavior:
 *  * - Public event  -> QR payload generated
 *  * - Private event -> no promotional QR payload
 */
public class OrganizerEventService {

    private final DBProxy db;

    public OrganizerEventService() {
        this.db = DBProxy.getInstance();
    }

    public OrganizerEventService(DBProxy db) {
        this.db = db;
    }

    public OrganizerEventCreationResult createEvent(String name,
                                                    String description,
                                                    String time,
                                                    String location,
                                                    Organizer organizer,
                                                    String posterPictureURL,
                                                    boolean publicEvent) {
        validateRequiredEventFields(name, description, time, location, organizer);

        Event event = new Event(
                name.trim(),
                description.trim(),
                time.trim(),
                location.trim(),
                organizer,
                new ArrayList<User>(),
                new ArrayList<User>(),
                normalizeOptional(posterPictureURL),
                publicEvent
        );

        db.addEvent(event);

        String qrPayload = null;
        if (publicEvent) {
            qrPayload = EventQrCodeGenerator.buildPayload(event.getEventID());
        }

        return new OrganizerEventCreationResult(
                event.getEventID(),
                event,
                qrPayload
        );
    }

    /**
     * Optional convenience wrapper for explicitly public events.
     */
    public OrganizerEventCreationResult createPublicEventAndGenerateQr(String name,
                                                                       String description,
                                                                       String time,
                                                                       String location,
                                                                       Organizer organizer,
                                                                       String posterPictureURL) {
        return createEvent(
                name,
                description,
                time,
                location,
                organizer,
                posterPictureURL,
                true
        );
    }

    /**
     * Optional convenience wrapper for explicitly private events.
     */
    public OrganizerEventCreationResult createPrivateEvent(String name,
                                                           String description,
                                                           String time,
                                                           String location,
                                                           Organizer organizer,
                                                           String posterPictureURL) {
        return createEvent(
                name,
                description,
                time,
                location,
                organizer,
                posterPictureURL,
                false
        );
    }

    public Event getEvent(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID is required.");
        }

        Event event = db.getEvent(eventId.trim());
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        return event;
    }

    /**
     * Only public events should expose a promotional QR payload.
     */
    public String getPromotionalQrPayload(String eventId) {
        Event event = getEvent(eventId);

        if (!event.isPublicEvent()) {
            throw new IllegalArgumentException("Private events do not have a public promotional QR code.");
        }

        return EventQrCodeGenerator.buildPayload(event.getEventID());
    }

    private void validateRequiredEventFields(String name,
                                             String description,
                                             String time,
                                             String location,
                                             Organizer organizer) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Event name is required.");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Event description is required.");
        }

        if (time == null || time.trim().isEmpty()) {
            throw new IllegalArgumentException("Event time is required.");
        }

        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Event location is required.");
        }

        if (organizer == null) {
            throw new IllegalArgumentException("Organizer is required.");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}