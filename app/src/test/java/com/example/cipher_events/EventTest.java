package com.example.cipher_events;

import static org.junit.Assert.*;
import com.example.cipher_events.database.Event;
import org.junit.Test;

public class EventTest {
    @Test
    public void testEventCreation() {
        Event event = new Event(
                "Tech Expo 2026",
                "Discover the future of technology",
                "2026-03-20 14:00",
                "Edmonton",
                null,
                null,
                null,
                null,
                true
        );
        assertNotNull(event);
    }

    @Test
    public void testEventGetters() {
        Event event = new Event(
                "Tech Expo 2026",
                "Discover the future of technology",
                "2026-03-20 14:00",
                "Edmonton",
                null,
                null,
                null,
                null,
                true
        );
        assertEquals("Tech Expo 2026", event.getName());
        assertEquals("Discover the future of technology", event.getDescription());
        assertEquals("2026-03-20 14:00", event.getTime());
        assertEquals("Edmonton", event.getLocation());
    }

    @Test
    public void testEventSetters() {
        Event event = new Event(
                "Tech Expo 2026",
                "Discover the future of technology",
                "2026-03-20 14:00",
                "Edmonton",
                null,
                null,
                null,
                null,
                false
        );
        event.setName("Tech Expo 2025");
        event.setDescription("Discover the future of technology in 2025");
        event.setTime("2025-03-20 14:00");
        event.setLocation("Calgary");
        assertEquals("Tech Expo 2025", event.getName());
        assertEquals("Discover the future of technology in 2025", event.getDescription());
        assertEquals("2025-03-20 14:00", event.getTime());
        assertEquals("Calgary", event.getLocation());
    }
}
