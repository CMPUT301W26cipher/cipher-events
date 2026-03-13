package com.example.cipher_events;

import static org.junit.Assert.*;

import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.user.Status;
import com.example.cipher_events.user.UserEventHistoryRecord;
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.user.UserProfileService;
import com.example.cipher_events.user.UserRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UserProfileServiceTest {

    private UserRepository userRepository;
    private UserEventHistoryRepository historyRepository;
    private UserProfileService userProfileService;

    @Before
    public void setUp() {
        userRepository = new UserRepository();
        historyRepository = new UserEventHistoryRepository();
        userProfileService = new UserProfileService(userRepository, historyRepository);
    }

    // =========================================================
    // US 01.02.01
    // Create User Profile
    // =========================================================

    @Test
    public void testCreateUserProfile_validInput_createsUserSuccessfully() {
        User user = userProfileService.createUserProfile(
                "Alice",
                "alice@example.com",
                "password123",
                "7801234567",
                null
        );

        assertNotNull(user);
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("7801234567", user.getPhoneNumber());
        assertNotNull(user.getDeviceID());

        User savedUser = userRepository.findByDeviceId(user.getDeviceID());
        assertNotNull(savedUser);
        assertEquals("Alice", savedUser.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUserProfile_emptyName_throwsException() {
        userProfileService.createUserProfile(
                "",
                "alice@example.com",
                "password123",
                "7801234567",
                null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUserProfile_invalidEmail_throwsException() {
        userProfileService.createUserProfile(
                "Alice",
                "alice-example.com",
                "password123",
                "7801234567",
                null
        );
    }

    @Test
    public void testCreateUserProfile_emptyPhone_allowed() {
        User user = userProfileService.createUserProfile(
                "Bob",
                "bob@example.com",
                "password456",
                "",
                null
        );

        assertNotNull(user);
        assertNull(user.getPhoneNumber());
    }

    // =========================================================
    // US 01.02.02
    // Update User Profile
    // =========================================================

    @Test
    public void testUpdateUserProfile_validInput_updatesSuccessfully() {
        User user = userProfileService.createUserProfile(
                "Charlie",
                "charlie@example.com",
                "pass123",
                "1112223333",
                null
        );

        User updatedUser = userProfileService.updateUserProfile(
                user.getDeviceID(),
                "Charles",
                "charles@example.com",
                "9998887777",
                "new_pic_url"
        );

        assertNotNull(updatedUser);
        assertEquals("Charles", updatedUser.getName());
        assertEquals("charles@example.com", updatedUser.getEmail());
        assertEquals("9998887777", updatedUser.getPhoneNumber());
        assertEquals("new_pic_url", updatedUser.getProfilePictureURL());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateUserProfile_invalidDeviceId_throwsException() {
        userProfileService.updateUserProfile(
                "invalid-device-id",
                "David",
                "david@example.com",
                "1234567890",
                null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateUserProfile_invalidEmail_throwsException() {
        User user = userProfileService.createUserProfile(
                "Eva",
                "eva@example.com",
                "pass123",
                "1234567890",
                null
        );

        userProfileService.updateUserProfile(
                user.getDeviceID(),
                "Eva",
                "invalid-email",
                "1234567890",
                null
        );
    }

    // =========================================================
    // US 01.02.03
    // User Event History
    // =========================================================

    @Test
    public void testAddEventHistory_validEvent_addsHistorySuccessfully() {
        User user = userProfileService.createUserProfile(
                "Frank",
                "frank@example.com",
                "pass123",
                "1234567890",
                null
        );

        Event event = new Event(
                "Career Fair",
                "Career networking event",
                "2026-03-20 14:00",
                "Edmonton",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null
        );
        event.setName("Hackathon");

        userProfileService.addEventHistory(
                user.getDeviceID(),
                event,
                Status.WAITLISTED
        );

        List<UserEventHistoryRecord> history = userProfileService.getUserEventHistory(user.getDeviceID());

        assertEquals(1, history.size());
        assertEquals("Hackathon", history.get(0).getEvent().getName());
        assertEquals(Status.WAITLISTED, history.get(0).getStatus());
    }

    @Test
    public void testUpsertEventHistory_existingEvent_updatesStatus() {
        User user = userProfileService.createUserProfile(
                "Grace",
                "grace@example.com",
                "pass123",
                "1234567890",
                null
        );

        Event event = new Event(
                "Career Fair",
                "Career networking event",
                "2026-03-20 14:00",
                "Edmonton",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null
        );
        event.setName("Career Fair");

        userProfileService.addEventHistory(
                user.getDeviceID(),
                event,
                Status.WAITLISTED
        );

        userProfileService.upsertEventHistory(
                user.getDeviceID(),
                event,
                Status.SELECTED
        );

        List<UserEventHistoryRecord> history = userProfileService.getUserEventHistory(user.getDeviceID());

        assertEquals(1, history.size());
        assertEquals(Status.SELECTED, history.get(0).getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUserEventHistory_nonExistentUser_throwsException() {
        userProfileService.getUserEventHistory("unknown-user-id");
    }

    // =========================================================
    // US 01.02.04
    // Delete User Profile
    // =========================================================

    @Test
    public void testDeleteUserProfile_validUser_removesUserAndHistory() {
        User user = userProfileService.createUserProfile(
                "Helen",
                "helen@example.com",
                "pass123",
                "1234567890",
                null
        );

        Event event = new Event(
                "Career Fair",
                "Career networking event",
                "2026-03-20 14:00",
                "Edmonton",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null
        );
        event.setName("Workshop");
        event.setEntrants(new ArrayList<>());
        event.setAttendees(new ArrayList<>());

        event.getEntrants().add(user);

        userProfileService.addEventHistory(
                user.getDeviceID(),
                event,
                Status.WAITLISTED
        );

        List<Event> allEvents = new ArrayList<>();
        allEvents.add(event);

        userProfileService.deleteUserProfile(user.getDeviceID(), allEvents);

        assertNull(userRepository.findByDeviceId(user.getDeviceID()));
        assertEquals(0, historyRepository.getHistory(user.getDeviceID()).size());
        assertEquals(0, event.getEntrants().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteUserProfile_nonExistentUser_throwsException() {
        userProfileService.deleteUserProfile("missing-user", new ArrayList<>());
    }
}
