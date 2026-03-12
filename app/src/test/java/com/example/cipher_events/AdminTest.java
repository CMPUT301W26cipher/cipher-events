package com.example.cipher_events;

import static org.junit.Assert.*;
import com.example.cipher_events.database.Admin;
import org.junit.Test;

public class AdminTest {
    @Test
    public void testAdminCreation() {
        Admin admin = new Admin(
                "John Doe",
                "john@example.com",
                "password123",
                "7801234567",
                null
        );
        assertNotNull(admin);
    }

    @Test
    public void testAdminGetters() {
        Admin admin = new Admin(
                "John Doe",
                "john@example.com",
                "password123",
                "7801234567",
                null
        );
        assertEquals("John Doe", admin.getName());
        assertEquals("john@example.com", admin.getEmail());
        assertEquals("password123", admin.getPassword());
        assertEquals("7801234567", admin.getPhoneNumber());
    }

    @Test
    public void testAdminSetters() {
        Admin admin = new Admin(
                "John Doe",
                "john@example.com",
                "password123",
                "7801234567",
                null
        );
        admin.setName("Jane Doe");
        admin.setEmail("jane@example.com");
        admin.setPassword("password456");
        admin.setPhoneNumber("7809876543");
        assertEquals("Jane Doe", admin.getName());
        assertEquals("jane@example.com", admin.getEmail());
        assertEquals("password456", admin.getPassword());
        assertEquals("7809876543", admin.getPhoneNumber());
    }
}
