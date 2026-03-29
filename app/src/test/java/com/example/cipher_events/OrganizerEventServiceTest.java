package com.example.cipher_events;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.organizer.EventQrCodeGenerator;
import com.example.cipher_events.organizer.OrganizerEventCreationResult;
import com.example.cipher_events.organizer.OrganizerEventService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class OrganizerEventServiceTest {

    private DBProxy dbProxy;
    private OrganizerEventService organizerEventService;

    @Before
    public void setUp() {
        dbProxy = mock(DBProxy.class);
        organizerEventService = new OrganizerEventService(dbProxy);
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

    @Test
    public void testCreatePublicEventAndGenerateQr_validInput_createsEventSuccessfully() {
        Organizer organizer = createTestOrganizer();

        OrganizerEventCreationResult result =
                organizerEventService.createPublicEventAndGenerateQr(
                        "Campus Tech Talk",
                        "Learn Android basics",
                        "2026-03-20 18:00",
                        "University of Alberta",
                        organizer,
                        "poster_url"
                );

        assertNotNull(result);
        assertNotNull(result.getEventId());
        assertNotNull(result.getEvent());
        assertNotNull(result.getQrPayload());
        assertTrue(result.hasQrCode());

        assertEquals("Campus Tech Talk", result.getEvent().getName());
        assertEquals("Learn Android basics", result.getEvent().getDescription());
        assertEquals("2026-03-20 18:00", result.getEvent().getTime());
        assertEquals("University of Alberta", result.getEvent().getLocation());
        assertEquals("poster_url", result.getEvent().getPosterPictureURL());
        assertTrue(result.getEvent().isPublicEvent());
        assertTrue(result.getQrPayload().startsWith(EventQrCodeGenerator.QR_PREFIX));

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(dbProxy, times(1)).addEvent(captor.capture());

        Event savedEvent = captor.getValue();
        assertEquals("Campus Tech Talk", savedEvent.getName());
        assertTrue(savedEvent.isPublicEvent());
    }

    @Test
    public void testCreatePrivateEvent_validInput_createsEventWithoutQr() {
        Organizer organizer = createTestOrganizer();

        OrganizerEventCreationResult result =
                organizerEventService.createPrivateEvent(
                        "Private Mixer",
                        "Invite only",
                        "2026-03-21 19:00",
                        "Edmonton",
                        organizer,
                        null
                );

        assertNotNull(result);
        assertNotNull(result.getEventId());
        assertNotNull(result.getEvent());
        assertNull(result.getQrPayload());
        assertFalse(result.hasQrCode());
        assertFalse(result.getEvent().isPublicEvent());

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(dbProxy, times(1)).addEvent(captor.capture());

        Event savedEvent = captor.getValue();
        assertEquals("Private Mixer", savedEvent.getName());
        assertFalse(savedEvent.isPublicEvent());
    }

    @Test
    public void testCreateEvents_createsUniqueIdsAndPayloads() {
        Organizer organizer = createTestOrganizer();

        OrganizerEventCreationResult first =
                organizerEventService.createPublicEventAndGenerateQr(
                        "Event A",
                        "Description A",
                        "2026-03-20 10:00",
                        "Location A",
                        organizer,
                        null
                );

        OrganizerEventCreationResult second =
                organizerEventService.createPublicEventAndGenerateQr(
                        "Event B",
                        "Description B",
                        "2026-03-21 11:00",
                        "Location B",
                        organizer,
                        null
                );

        assertNotEquals(first.getEventId(), second.getEventId());
        assertNotEquals(first.getQrPayload(), second.getQrPayload());
        verify(dbProxy, times(2)).addEvent(any(Event.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePublicEventAndGenerateQr_emptyName_throwsException() {
        Organizer organizer = createTestOrganizer();

        organizerEventService.createPublicEventAndGenerateQr(
                "",
                "Description",
                "2026-03-20 18:00",
                "Edmonton",
                organizer,
                null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePublicEventAndGenerateQr_nullOrganizer_throwsException() {
        organizerEventService.createPublicEventAndGenerateQr(
                "Campus Tech Talk",
                "Description",
                "2026-03-20 18:00",
                "Edmonton",
                null,
                null
        );
    }

    @Test
    public void testGetPromotionalQrPayload_publicEvent_returnsPayload() {
        Organizer organizer = createTestOrganizer();

        OrganizerEventCreationResult result =
                organizerEventService.createPublicEventAndGenerateQr(
                        "Open Event",
                        "Public event",
                        "2026-03-22 12:00",
                        "Campus",
                        organizer,
                        null
                );

        Event storedEvent = result.getEvent();
        when(dbProxy.getEvent(result.getEventId())).thenReturn(storedEvent);

        String payload = organizerEventService.getPromotionalQrPayload(result.getEventId());

        assertNotNull(payload);
        assertTrue(payload.startsWith(EventQrCodeGenerator.QR_PREFIX));
        verify(dbProxy, times(1)).getEvent(result.getEventId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPromotionalQrPayload_privateEvent_throwsException() {
        Organizer organizer = createTestOrganizer();

        OrganizerEventCreationResult result =
                organizerEventService.createPrivateEvent(
                        "Private Event",
                        "Invite only",
                        "2026-03-22 12:00",
                        "Campus",
                        organizer,
                        null
                );

        when(dbProxy.getEvent(result.getEventId())).thenReturn(result.getEvent());

        organizerEventService.getPromotionalQrPayload(result.getEventId());
    }
}