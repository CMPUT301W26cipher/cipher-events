package com.example.cipher_events;

import static org.junit.Assert.*;
import com.example.cipher_events.database.User;
import org.junit.Test;

public class UserTest {
    @Test
    public void testUserCreation() {
        User user = new User(
                "John Doe",
                "john@example.com",
                "password123",
                "7801234567",
                null
        );
        assertNotNull(user);
    }

    @Test
    public void testUserGetters() {
        User user = new User(
                "John Doe",
                "john@example.com",
                "password123",
                "7801234567",
                null
        );
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("7801234567", user.getPhoneNumber());
    }

    @Test
    public void testUserSetters() {
        User user = new User(
                "John Doe",
                "john@example.com",
                "password123",
                "7801234567",
                null
        );
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPassword("password456");
        user.setPhoneNumber("7809876543");
        assertEquals("Jane Doe", user.getName());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("password456", user.getPassword());
        assertEquals("7809876543", user.getPhoneNumber());
    }
}
