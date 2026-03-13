package com.example.cipher_events;
import static org.junit.Assert.*;

import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.organizer.EventQrCodeGenerator;
import com.example.cipher_events.organizer.EventRecord;
import com.example.cipher_events.organizer.EventRepository;
import com.example.cipher_events.user.EntrantEventService;
import com.example.cipher_events.user.EntrantQrScanResult;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class EntrantEventServiceTest {

    private EventRepository eventRepository;
    private EntrantEventService entrantEventService;

    @Before
    public void setUp() {
        eventRepository = EventRepository.getInstance();
        eventRepository.clear();
        entrantEventService = new EntrantEventService(eventRepository);
    }

    /**
     * Adjust this helper if your Organizer constructor is different.
     */
    private Organizer createTestOrganizer() {
        return new Organizer(
                "Organizer One",
                "organizer@example.com",
                "password123",
                "7801234567",
                null
        );
    }

    private User createTestUser() {
        return new User(
                "Alice",
                "alice@example.com",
                "password123",
                "7801112222",
                null
        );
    }

    private EventRecord createStoredEventRecord(String eventId, String qrPayload) {
        Organizer organizer = createTestOrganizer();

        Event event = new Event(
                "Career Fair",
                "Meet employers and recruiters",
                "2026-03-22 14:00",
                "University of Alberta",
                organizer,
                new ArrayList<>(),
                new ArrayList<>(),
                "poster_url"
        );

        EventRecord record = new EventRecord(
                eventId,
                event,
                qrPayload,
                System.currentTimeMillis()
        );

        eventRepository.save(record);
        return record;
    }

    // =========================================================
    // US 01.06.01
    // Entrant views event details by scanning QR code
    // =========================================================

    @Test
    public void testGetEventDetailsFromScannedQr_validQr_returnsCorrectEvent() {
        String eventId = "event-123";
        String qrPayload = EventQrCodeGenerator.buildPayload(eventId);
        EventRecord record = createStoredEventRecord(eventId, qrPayload);

        EntrantQrScanResult result = entrantEventService.getEventDetailsFromScannedQr(qrPayload);

        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        assertNotNull(result.getEvent());
        assertEquals(record.getEvent().getName(), result.getEvent().getName());
        assertEquals(record.getEvent().getDescription(), result.getEvent().getDescription());
        assertEquals(record.getEvent().getLocation(), result.getEvent().getLocation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEventDetailsFromScannedQr_invalidPrefix_throwsException() {
        entrantEventService.getEventDetailsFromScannedQr("invalid://event/123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEventDetailsFromScannedQr_unknownEvent_throwsException() {
        String qrPayload = EventQrCodeGenerator.buildPayload("missing-event-id");
        entrantEventService.getEventDetailsFromScannedQr(qrPayload);
    }

    // =========================================================
    // US 01.06.02
    // Entrant signs up for event from event details
    // =========================================================

    @Test
    public void testSignUpForEventFromDetails_validUser_addsUserToEntrants() {
        String eventId = "event-456";
        String qrPayload = EventQrCodeGenerator.buildPayload(eventId);
        EventRecord record = createStoredEventRecord(eventId, qrPayload);

        User entrant = createTestUser();

        entrantEventService.signUpForEventFromDetails(eventId, entrant);

        assertEquals(1, record.getEvent().getEntrants().size());
        assertEquals(entrant.getDeviceID(), record.getEvent().getEntrants().get(0).getDeviceID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignUpForEventFromDetails_duplicateUser_throwsException() {
        String eventId = "event-789";
        String qrPayload = EventQrCodeGenerator.buildPayload(eventId);
        EventRecord record = createStoredEventRecord(eventId, qrPayload);

        User entrant = createTestUser();
        record.getEvent().getEntrants().add(entrant);

        entrantEventService.signUpForEventFromDetails(eventId, entrant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignUpForEventFromDetails_userAlreadyAttendee_throwsException() {
        String eventId = "event-999";
        String qrPayload = EventQrCodeGenerator.buildPayload(eventId);
        EventRecord record = createStoredEventRecord(eventId, qrPayload);

        User entrant = createTestUser();
        record.getEvent().getAttendees().add(entrant);

        entrantEventService.signUpForEventFromDetails(eventId, entrant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignUpForEventFromDetails_unknownEvent_throwsException() {
        User entrant = createTestUser();
        entrantEventService.signUpForEventFromDetails("missing-event", entrant);
    }

    @Test
    public void testSignUpForEventFromQr_validQr_addsUserToEntrants() {
        String eventId = "event-qr-001";
        String qrPayload = EventQrCodeGenerator.buildPayload(eventId);
        EventRecord record = createStoredEventRecord(eventId, qrPayload);

        User entrant = createTestUser();

        entrantEventService.signUpForEventFromQr(qrPayload, entrant);

        assertEquals(1, record.getEvent().getEntrants().size());
        assertEquals(entrant.getDeviceID(), record.getEvent().getEntrants().get(0).getDeviceID());
    }
}
