package com.example.cipher_events;

import static org.junit.Assert.*;

import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.organizer.EventQrCodeGenerator;
import com.example.cipher_events.organizer.EventRecord;
import com.example.cipher_events.organizer.EventRepository;
import com.example.cipher_events.organizer.OrganizerEventCreationResult;
import com.example.cipher_events.organizer.OrganizerEventService;

import org.junit.Before;
import org.junit.Test;

public class OrganizerEventServiceTest {

    private EventRepository eventRepository;
    private OrganizerEventService organizerEventService;

    @Before
    public void setUp() {
        eventRepository = EventRepository.getInstance();
        eventRepository.clear();
        organizerEventService = new OrganizerEventService(eventRepository);
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
    public void testCreateEventAndGenerateQr_validInput_createsEventSuccessfully() {
        Organizer organizer = createTestOrganizer();

        OrganizerEventCreationResult result = organizerEventService.createEventAndGenerateQr(
                "Campus Tech Talk",
                "Learn Android basics",
                "2026-03-20 18:00",
                "University of Alberta",
                organizer,
                "poster_url",
                400,
                400
        );

        assertNotNull(result);
        assertNotNull(result.getEventId());
        assertNotNull(result.getEvent());
        assertNotNull(result.getQrPayload());

        assertEquals("Campus Tech Talk", result.getEvent().getName());
        assertEquals("Learn Android basics", result.getEvent().getDescription());
        assertEquals("2026-03-20 18:00", result.getEvent().getTime());
        assertEquals("University of Alberta", result.getEvent().getLocation());
        assertEquals("poster_url", result.getEvent().getPosterPictureURL());

        assertTrue(result.getQrPayload().startsWith(EventQrCodeGenerator.QR_PREFIX));

        EventRecord savedRecord = eventRepository.findRecordById(result.getEventId());
        assertNotNull(savedRecord);
        assertEquals(result.getEventId(), savedRecord.getEventId());
        assertEquals(result.getQrPayload(), savedRecord.getQrPayload());
    }

    @Test
    public void testCreateEventAndGenerateQr_createsUniqueIdsAndPayloads() {
        Organizer organizer = createTestOrganizer();

        OrganizerEventCreationResult first = organizerEventService.createEventAndGenerateQr(
                "Event A",
                "Description A",
                "2026-03-20 10:00",
                "Location A",
                organizer,
                null,
                200,
                200
        );

        OrganizerEventCreationResult second = organizerEventService.createEventAndGenerateQr(
                "Event B",
                "Description B",
                "2026-03-21 11:00",
                "Location B",
                organizer,
                null,
                0,
                400
        );

        assertNotEquals(first.getEventId(), second.getEventId());
        assertNotEquals(first.getQrPayload(), second.getQrPayload());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventAndGenerateQr_emptyName_throwsException() {
        Organizer organizer = createTestOrganizer();

        organizerEventService.createEventAndGenerateQr(
                "",
                "Description",
                "2026-03-20 18:00",
                "Edmonton",
                organizer,
                null,
                400,
                400
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventAndGenerateQr_nullOrganizer_throwsException() {
        organizerEventService.createEventAndGenerateQr(
                "Campus Tech Talk",
                "Description",
                "2026-03-20 18:00",
                "Edmonton",
                null,
                null,
                400,
                400
        );
    }
}