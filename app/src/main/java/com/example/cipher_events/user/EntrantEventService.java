package com.example.cipher_events.user;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.organizer.EventQrCodeGenerator;

import java.util.ArrayList;

/**
 * US 01.06.01
 * View event details within the app by scanning the promotional QR code.
 *
 * US 01.06.02
 * Sign up for an event from the event details.
 */
public class EntrantEventService {

    private final DBProxy db;

    public EntrantEventService() {
        this.db = DBProxy.getInstance();
    }

    public EntrantEventService(DBProxy db) {
        this.db = db;
    }

    /**
     * Resolve a scanned QR payload to a PUBLIC event.
     */
    public EntrantQrScanResult getEventDetailsFromScannedQr(String scannedQrText) {
        if (scannedQrText == null || scannedQrText.trim().isEmpty()) {
            throw new IllegalArgumentException("Scanned QR text is empty.");
        }

        String eventId = EventQrCodeGenerator.extractEventId(scannedQrText);
        Event event = db.getEvent(eventId);

        if (event == null) {
            throw new IllegalArgumentException("No event found for this QR code.");
        }

        // Updated US 02.01.01 logic:
        // promotional QR codes are for PUBLIC events only.
        if (!event.isPublicEvent()) {
            throw new IllegalArgumentException("This QR code does not link to a public event.");
        }

        return new EntrantQrScanResult(eventId, event);
    }

    /**
     * Join the waiting list from the event details page.
     */
    public void signUpForEventFromDetails(String eventId, User entrant) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID is required.");
        }

        if (entrant == null) {
            throw new IllegalArgumentException("Entrant is required.");
        }

        Event event = db.getEvent(eventId.trim());
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        ensureLists(event);

        if (containsUser(event.getEntrants(), entrant.getDeviceID())) {
            throw new IllegalArgumentException("Entrant is already on the waiting list.");
        }

        if (containsUser(event.getAttendees(), entrant.getDeviceID())) {
            throw new IllegalArgumentException("Entrant is already registered as an attendee.");
        }

        if (containsUser(event.getInvitedEntrants(), entrant.getDeviceID())) {
            throw new IllegalArgumentException("Entrant has already been invited/selected.");
        }

        event.getEntrants().add(entrant);
        db.updateEvent(event);
    }

    /**
     * Scan and sign up in one step.
     */
    public void signUpForEventFromQr(String scannedQrText, User entrant) {
        EntrantQrScanResult result = getEventDetailsFromScannedQr(scannedQrText);
        signUpForEventFromDetails(result.getEventId(), entrant);
    }

    public Event getEventDetails(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID is required.");
        }

        Event event = db.getEvent(eventId.trim());
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        return event;
    }

    private void ensureLists(Event event) {
        if (event.getEntrants() == null) {
            event.setEntrants(new ArrayList<>());
        }
        if (event.getAttendees() == null) {
            event.setAttendees(new ArrayList<>());
        }
        if (event.getInvitedEntrants() == null) {
            event.setInvitedEntrants(new ArrayList<>());
        }
    }

    private boolean containsUser(ArrayList<User> users, String deviceId) {
        if (users == null || deviceId == null) {
            return false;
        }

        for (User user : users) {
            if (user != null
                    && user.getDeviceID() != null
                    && user.getDeviceID().equals(deviceId)) {
                return true;
            }
        }
        return false;
    }
}