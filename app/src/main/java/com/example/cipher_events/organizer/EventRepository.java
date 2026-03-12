package com.example.cipher_events.organizer;

import com.example.cipher_events.database.Event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared in-memory repository for events.
 * Replace with Firestore/database code later if needed.
 */
public class EventRepository {
    private static EventRepository instance;

    private final Map<String, EventRecord> eventsById;

    private EventRepository() {
        this.eventsById = new LinkedHashMap<>();
    }
    public static synchronized EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }
        return instance;
    }

    public synchronized void save(EventRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Event record cannot be null");
        }
        eventsById.put(record.getEventId(), record);
    }

    public synchronized EventRecord findRecordById(String eventID) {
        return eventsById.get(eventID);
    }

    public synchronized Event findEventById(String eventId) {
        EventRecord record = eventsById.get(eventId);
        return record == null ? null : record.getEvent();
    }

    public synchronized EventRecord findRecordByQrPayload(String qrPayload) {
        if (qrPayload == null) {
            return null;
        }

        for (EventRecord record : eventsById.values()) {
            if (qrPayload.equals(record.getQrPayload())) {
                return record;
            }
        }
        return null;
    }

    public synchronized List<EventRecord> getAllRecords() {
        return new ArrayList<>(eventsById.values());
    }

    public synchronized List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        for (EventRecord record : eventsById.values()) {
            events.add(record.getEvent());
        }
        return events;
    }

    public synchronized void delete(String eventId) {
        eventsById.remove(eventId);
    }

    public synchronized boolean exists(String eventId) {
        return eventsById.containsKey(eventId);
    }

    public synchronized void clear() {
        eventsById.clear();
    }

}
