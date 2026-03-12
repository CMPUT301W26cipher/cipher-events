package com.example.cipher_events;

import static org.junit.Assert.*;
import com.example.cipher_events.database.Organizer;
import org.junit.Test;

public class OrganizerTest {
    @Test
    public void testOrganizerCreation() {
        Organizer organizer = new Organizer(
                "John Doe",
                "john@example.com",
                "password123",
                "7801234567",
                null
        );
        assertNotNull(organizer);
    }

    @Test
    public void testOrganizerGetters() {
        Organizer organizer = new Organizer(
                "John Doe",
                "john@example.com",
                "password123",
                "7801234567",
                null
        );
        assertEquals("John Doe", organizer.getName());
        assertEquals("john@example.com", organizer.getEmail());
        assertEquals("password123", organizer.getPassword());
        assertEquals("7801234567", organizer.getPhoneNumber());
    }

    @Test
    public void testOrganizerSetters() {
        Organizer organizer = new Organizer(
                "John Doe",
                "john@example.com",
                "password123",
                "7801234567",
                null
        );
        organizer.setName("Jane Doe");
        organizer.setEmail("jane@example.com");
        organizer.setPassword("password456");
        organizer.setPhoneNumber("7809876543");
        assertEquals("Jane Doe", organizer.getName());
        assertEquals("jane@example.com", organizer.getEmail());
        assertEquals("password456", organizer.getPassword());
        assertEquals("7809876543", organizer.getPhoneNumber());
    }
}
