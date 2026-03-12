package com.example.cipher_events.organizer;

import android.graphics.Bitmap;

import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.UUID;

/**
 * US 02.01.01
 * Organizer creates a new event and generates a unique promotional QR code
 * that links to the event inside the app.
 */
public class OrganizerEventService {
    private final EventRepository eventRepository;

    public OrganizerEventService() {
        this.eventRepository = EventRepository.getInstance();
    }

    public OrganizerEventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public OrganizerEventCreationResult createEventAndGenerateQr(String name,
                                                                 String description,
                                                                 String time,
                                                                 String location,
                                                                 Organizer organizer,
                                                                 String posterPictureURL,
                                                                 int qrWidth,
                                                                 int qrHeight) throws WriterException {
        validateRequiredEventFields(name, description, time, location, organizer);

        Event event = new Event(
                name.trim(),
                description.trim(),
                time.trim(),
                location.trim(),
                organizer,
                new ArrayList<>(),
                new ArrayList<>(),
                normalizeOptional(posterPictureURL)
        );

        String eventId = UUID.randomUUID().toString();
        String qrPayload = EventQrCodeGenerator.buildPayload(eventId);
        Bitmap qrBitmap = EventQrCodeGenerator.generateQrBitmap(qrPayload, qrWidth, qrHeight);

        EventRecord record = new EventRecord(
                eventId,
                event,
                qrPayload,
                System.currentTimeMillis()
        );

        eventRepository.save(record);

        return new OrganizerEventCreationResult(eventId, event, qrPayload, qrBitmap);
    }

    public EventRecord getEventRecord(String eventId) {
        return eventRepository.findRecordById(eventId);
    }

    public Event getEvent(String eventId) {
        return eventRepository.findEventById(eventId);
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