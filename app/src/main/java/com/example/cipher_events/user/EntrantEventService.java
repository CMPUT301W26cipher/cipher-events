package com.example.cipher_events.user;

import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.organizer.EventQrCodeGenerator;
import com.example.cipher_events.organizer.EventRecord;
import com.example.cipher_events.organizer.EventRepository;

import java.util.ArrayList;

/**
 * US 01.06.01
 * View event details within the app by scanning the promotional QR code.
 *
 * US 01.06.02
 * Sign up for an event from the event details.
 */
public class EntrantEventService {
    private final EventRepository eventRepository;

    public EntrantEventService() {
        this.eventRepository = EventRepository.getInstance();
    }

    public EntrantEventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Takes the decoded text from a QR scanner and returns the linked event.
     * Your scanner UI can call this after scanning.
     */
    public EntrantQrScanResult getEventDetailsFromScannedQr(String scannedQrText) {
        if (scannedQrText == null || scannedQrText.trim().isEmpty()) {
            throw new IllegalArgumentException("Scanned QR text is empty.");
        }

        String eventId = EventQrCodeGenerator.extractEventId(scannedQrText);
        EventRecord record = eventRepository.findRecordById(eventId);

        if (record == null || record.getEvent() == null) {
            throw new IllegalArgumentException("No event found for this QR code.");
        }

        return new EntrantQrScanResult(eventId, record.getEvent());
    }

    /**
     * Allows the entrant to sign up / join the waiting list from event details.
     * With your current Event model, this stores the entrant in event.getEntrants().
     */
    public void signUpForEventFromDetails(String eventId, User entrant) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID is required.");
        }

        if (entrant == null) {
            throw new IllegalArgumentException("Entrant is required.");
        }

        Event event = eventRepository.findEventById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        ArrayList<User> entrants = event.getEntrants();
        if (entrants == null) {
            entrants = new ArrayList<>();
            event.setEntrants(entrants);
        }

        if (containsUser(entrants, entrant)) {
            throw new IllegalArgumentException("Entrant is already on the waiting list.");
        }

        ArrayList<User> attendees = event.getAttendees();
        if (attendees != null && containsUser(attendees, entrant)) {
            throw new IllegalArgumentException("Entrant is already registered as an attendee.");
        }

        entrants.add(entrant);
    }

    /**
     * Convenience method: scan and sign up in one step.
     */
    public void signUpForEventFromQr(String scannedQrText, User entrant) {
        EntrantQrScanResult result = getEventDetailsFromScannedQr(scannedQrText);
        signUpForEventFromDetails(result.getEventId(), entrant);
    }

    public Event getEventDetails(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID is required.");
        }

        Event event = eventRepository.findEventById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        return event;
    }

    private boolean containsUser(ArrayList<User> users, User target) {
        if (users == null || target == null) {
            return false;
        }

        for (User user : users) {
            if (sameUser(user, target)) {
                return true;
            }
        }
        return false;
    }

    private boolean sameUser(User a, User b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.getDeviceID() == null || b.getDeviceID() == null) {
            return false;
        }

        return a.getDeviceID().equals(b.getDeviceID());
    }
}