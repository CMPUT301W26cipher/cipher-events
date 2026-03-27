package com.example.cipher_events;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.organizer.EventQrCodeGenerator;
import com.example.cipher_events.user.EntrantEventService;
import com.example.cipher_events.user.EntrantQrScanResult;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class EntrantEventServiceTest {

    private DBProxy dbProxy;
    private EntrantEventService entrantEventService;

    @Before
    public void setUp() {
        dbProxy = mock(DBProxy.class);
        entrantEventService = new EntrantEventService(dbProxy);
    }

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

    private Event createTestEvent(boolean publicEvent) {
        return new Event(
                "Career Fair",
                "Meet employers and recruiters",
                "2026-03-22 14:00",
                "University of Alberta",
                createTestOrganizer(),
                new ArrayList<>(),
                new ArrayList<>(),
                "poster_url",
                publicEvent
        );
    }

    // =========================================================
    // US 01.06.01
    // Entrant views event details by scanning QR code
    // =========================================================

    @Test
    public void testGetEventDetailsFromScannedQr_validPublicQr_returnsCorrectEvent() {
        Event event = createTestEvent(true);
        String qrPayload = EventQrCodeGenerator.buildPayload(event.getEventID());

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        EntrantQrScanResult result = entrantEventService.getEventDetailsFromScannedQr(qrPayload);

        assertNotNull(result);
        assertEquals(event.getEventID(), result.getEventId());
        assertNotNull(result.getEvent());
        assertEquals(event.getName(), result.getEvent().getName());
        assertEquals(event.getDescription(), result.getEvent().getDescription());
        assertEquals(event.getLocation(), result.getEvent().getLocation());

        verify(dbProxy, times(1)).getEvent(event.getEventID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEventDetailsFromScannedQr_invalidPrefix_throwsException() {
        entrantEventService.getEventDetailsFromScannedQr("invalid://event/123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEventDetailsFromScannedQr_unknownEvent_throwsException() {
        String qrPayload = EventQrCodeGenerator.buildPayload("missing-event-id");
        when(dbProxy.getEvent("missing-event-id")).thenReturn(null);

        entrantEventService.getEventDetailsFromScannedQr(qrPayload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEventDetailsFromScannedQr_privateEventQr_throwsException() {
        Event event = createTestEvent(false);
        String qrPayload = EventQrCodeGenerator.buildPayload(event.getEventID());

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        entrantEventService.getEventDetailsFromScannedQr(qrPayload);
    }

    // =========================================================
    // US 01.06.02
    // Entrant signs up for event from event details
    // =========================================================

    @Test
    public void testSignUpForEventFromDetails_validUser_addsUserToEntrants() {
        Event event = createTestEvent(true);
        User entrant = createTestUser();

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        entrantEventService.signUpForEventFromDetails(event.getEventID(), entrant);

        assertEquals(1, event.getEntrants().size());
        assertEquals(entrant.getDeviceID(), event.getEntrants().get(0).getDeviceID());
        verify(dbProxy, times(1)).updateEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignUpForEventFromDetails_duplicateUser_throwsException() {
        Event event = createTestEvent(true);
        User entrant = createTestUser();
        event.getEntrants().add(entrant);

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        entrantEventService.signUpForEventFromDetails(event.getEventID(), entrant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignUpForEventFromDetails_userAlreadyAttendee_throwsException() {
        Event event = createTestEvent(true);
        User entrant = createTestUser();
        event.getAttendees().add(entrant);

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        entrantEventService.signUpForEventFromDetails(event.getEventID(), entrant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignUpForEventFromDetails_userAlreadyInvited_throwsException() {
        Event event = createTestEvent(true);
        User entrant = createTestUser();
        event.getInvitedEntrants().add(entrant);

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        entrantEventService.signUpForEventFromDetails(event.getEventID(), entrant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignUpForEventFromDetails_unknownEvent_throwsException() {
        User entrant = createTestUser();
        when(dbProxy.getEvent("missing-event")).thenReturn(null);

        entrantEventService.signUpForEventFromDetails("missing-event", entrant);
    }

    @Test
    public void testSignUpForEventFromQr_validQr_addsUserToEntrants() {
        Event event = createTestEvent(true);
        User entrant = createTestUser();
        String qrPayload = EventQrCodeGenerator.buildPayload(event.getEventID());

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        entrantEventService.signUpForEventFromQr(qrPayload, entrant);

        assertEquals(1, event.getEntrants().size());
        assertEquals(entrant.getDeviceID(), event.getEntrants().get(0).getDeviceID());
        verify(dbProxy, times(1)).updateEvent(event);
    }
}