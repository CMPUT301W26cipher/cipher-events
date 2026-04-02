package com.example.cipher_events;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.user.Status;
import com.example.cipher_events.user.UserEventHistoryRecord;
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.user.UserProfileService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

public class UserProfileServiceTest {

    private DBProxy dbProxy;
    private UserEventHistoryRepository historyRepository;
    private UserProfileService userProfileService;

    @Before
    public void setUp() {
        dbProxy = mock(DBProxy.class);
        historyRepository = mock(UserEventHistoryRepository.class);
        userProfileService = new UserProfileService(dbProxy, historyRepository);
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

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(dbProxy, times(1)).addUser(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("Alice", savedUser.getName());
        assertEquals("alice@example.com", savedUser.getEmail());
        assertEquals("7801234567", savedUser.getPhoneNumber());
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

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(dbProxy).addUser(captor.capture());

        User savedUser = captor.getValue();
        assertNull(savedUser.getPhoneNumber());
    }

    // =========================================================
    // US 01.02.02
    // Update User Profile
    // =========================================================

    @Test
    public void testUpdateUserProfile_validInput_updatesSuccessfully() {
        User existingUser = new User(
                "Charlie",
                "charlie@example.com",
                "pass123",
                "1112223333",
                null
        );

        when(dbProxy.getUser(existingUser.getDeviceID())).thenReturn(existingUser);

        User updatedUser = userProfileService.updateUserProfile(
                existingUser.getDeviceID(),
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

        verify(dbProxy, times(1)).updateUser(existingUser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateUserProfile_invalidDeviceId_throwsException() {
        when(dbProxy.getUser("invalid-device-id")).thenReturn(null);

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
        User existingUser = new User(
                "Eva",
                "eva@example.com",
                "pass123",
                "1234567890",
                null
        );

        when(dbProxy.getUser(existingUser.getDeviceID())).thenReturn(existingUser);

        userProfileService.updateUserProfile(
                existingUser.getDeviceID(),
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
    public void testGetUserEventHistory_existingUser_returnsHistory() {
        User user = new User(
                "Frank",
                "frank@example.com",
                "pass123",
                "1234567890",
                null
        );

        Event event = new Event(
                "Hackathon",
                "A coding competition",
                "2026-03-20 14:00",
                "Edmonton",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                true
        );

        List<UserEventHistoryRecord> expectedHistory = new ArrayList<>();
        expectedHistory.add(new UserEventHistoryRecord(event, Status.WAITLISTED));

        when(dbProxy.getUser(user.getDeviceID())).thenReturn(user);
        when(historyRepository.getHistory(user.getDeviceID())).thenReturn(expectedHistory);

        List<UserEventHistoryRecord> history =
                userProfileService.getUserEventHistory(user.getDeviceID());

        assertEquals(1, history.size());
        assertEquals("Hackathon", history.get(0).getEvent().getName());
        assertEquals(Status.WAITLISTED, history.get(0).getStatus());

        verify(historyRepository, times(1)).getHistory(user.getDeviceID());
    }

    @Test
    public void testAddEventHistory_waitlistedEvent_updatesEventSuccessfully() {
        User user = new User(
                "Grace",
                "grace@example.com",
                "pass123",
                "1234567890",
                null
        );

        Event event = new Event(
                "Career Fair",
                "Meet employers and recruiters",
                "2026-03-20 14:00",
                "University of Alberta",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                true
        );

        when(dbProxy.getUser(user.getDeviceID())).thenReturn(user);
        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        userProfileService.addEventHistory(
                user.getDeviceID(),
                event,
                Status.WAITLISTED
        );

        assertEquals(1, event.getEntrants().size());
        assertEquals(user.getDeviceID(), event.getEntrants().get(0).getDeviceID());
        verify(dbProxy, times(1)).updateEvent(event);
    }

    @Test
    public void testUpsertEventHistory_existingEvent_updatesRegisteredStatus() {
        User user = new User(
                "Helen",
                "helen@example.com",
                "pass123",
                "1234567890",
                null
        );

        Event event = new Event(
                "Workshop",
                "Android workshop",
                "2026-04-01 18:00",
                "Edmonton",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                false
        );

        when(dbProxy.getUser(user.getDeviceID())).thenReturn(user);
        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        userProfileService.upsertEventHistory(
                user.getDeviceID(),
                event,
                Status.REGISTERED
        );

        assertEquals(1, event.getAttendees().size());
        assertEquals(user.getDeviceID(), event.getAttendees().get(0).getDeviceID());
        verify(dbProxy, times(1)).updateEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUserEventHistory_nonExistentUser_throwsException() {
        when(dbProxy.getUser("unknown-user-id")).thenReturn(null);
        userProfileService.getUserEventHistory("unknown-user-id");
    }

    // =========================================================
    // US 01.02.04
    // Delete User Profile
    // =========================================================

    @Test
    public void testDeleteUserProfile_validUser_removesUserAndEventMembership() {
        User user = new User(
                "Ivy",
                "ivy@example.com",
                "pass123",
                "1234567890",
                null
        );

        Event event = new Event(
                "Workshop",
                "Android workshop",
                "2026-04-01 18:00",
                "Edmonton",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                true
        );

        event.getEntrants().add(user);

        ArrayList<Event> allEvents = new ArrayList<>();
        allEvents.add(event);

        when(dbProxy.getUser(user.getDeviceID())).thenReturn(user);
        when(dbProxy.getAllEvents()).thenReturn(allEvents);

        userProfileService.deleteUserProfile(user.getDeviceID());

        assertEquals(0, event.getEntrants().size());
        verify(dbProxy, times(1)).updateEvent(event);
        verify(dbProxy, times(1)).deleteUser(user.getDeviceID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteUserProfile_nonExistentUser_throwsException() {
        when(dbProxy.getUser("missing-user")).thenReturn(null);
        userProfileService.deleteUserProfile("missing-user");
    }
}