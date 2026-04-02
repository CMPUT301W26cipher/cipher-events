package com.example.cipher_events;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.cipher_events.admin.AdminModerationService;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AdminModerationServiceTest {

    private DBProxy dbProxy;
    private AdminModerationService adminModerationService;

    @Before
    public void setUp() {
        dbProxy = mock(DBProxy.class);
        adminModerationService = new AdminModerationService(dbProxy);
    }

    private Event createTestEvent() {
        return new Event(
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
    }

    private User createTestUser() {
        User user = new User();
        user.setDeviceID("user-1");
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPassword("pass123");
        user.setPhoneNumber("1234567890");
        user.setProfilePictureURL(null);
        return user;
    }

    private Organizer createTestOrganizer() {
        Organizer organizer = new Organizer();
        organizer.setDeviceID("org-1");
        organizer.setName("Org");
        organizer.setEmail("org@example.com");
        organizer.setPassword("pass123");
        organizer.setPhoneNumber("1234567890");
        organizer.setProfilePictureURL(null);
        return organizer;
    }

    // =========================================================
    // US 03.04.01
    // Browse events
    // =========================================================

    @Test
    public void testBrowseAllEvents_returnsEventList() {
        ArrayList<Event> events = new ArrayList<>();
        events.add(createTestEvent());
        events.add(createTestEvent());

        when(dbProxy.getAllEvents()).thenReturn(events);

        List<Event> result = adminModerationService.browseAllEvents();

        assertEquals(2, result.size());
        verify(dbProxy, times(1)).getAllEvents();
    }

    @Test
    public void testBrowseAllEvents_nullFromDb_returnsEmptyList() {
        when(dbProxy.getAllEvents()).thenReturn(null);

        List<Event> result = adminModerationService.browseAllEvents();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================
    // US 03.05.01
    // Browse profiles
    // =========================================================

    @Test
    public void testBrowseAllProfiles_returnsUserList() {
        ArrayList<User> users = new ArrayList<>();
        users.add(createTestUser());
        users.add(createTestUser());

        when(dbProxy.getAllUsers()).thenReturn(users);

        List<User> result = adminModerationService.browseAllProfiles();

        assertEquals(2, result.size());
        verify(dbProxy, times(1)).getAllUsers();
    }

    @Test
    public void testBrowseAllProfiles_nullFromDb_returnsEmptyList() {
        when(dbProxy.getAllUsers()).thenReturn(null);

        List<User> result = adminModerationService.browseAllProfiles();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================
    // US 03.01.01
    // Remove events
    // =========================================================

    @Test
    public void testRemoveEvent_validEvent_deletesEvent() {
        Event event = createTestEvent();
        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        adminModerationService.removeEvent(event.getEventID());

        verify(dbProxy, times(1)).deleteEvent(event.getEventID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEvent_missingEvent_throwsException() {
        when(dbProxy.getEvent("missing")).thenReturn(null);
        adminModerationService.removeEvent("missing");
    }

    // =========================================================
    // US 03.02.01
    // Remove profiles
    // =========================================================

    @Test
    public void testRemoveProfile_validUser_deletesUser() {
        User user = createTestUser();
        when(dbProxy.getUser(user.getDeviceID())).thenReturn(user);

        adminModerationService.removeProfile(user.getDeviceID());

        verify(dbProxy, times(1)).deleteUser(user.getDeviceID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProfile_missingUser_throwsException() {
        when(dbProxy.getUser("missing")).thenReturn(null);
        adminModerationService.removeProfile("missing");
    }

    // =========================================================
    // US 03.07.01
    // Remove organizers
    // =========================================================

    @Test
    public void testRemoveOrganizer_validOrganizer_deletesOrganizer() {
        Organizer organizer = createTestOrganizer();
        when(dbProxy.getOrganizer(organizer.getDeviceID())).thenReturn(organizer);

        adminModerationService.removeOrganizer(organizer.getDeviceID());

        verify(dbProxy, times(1)).deleteOrganizer(organizer.getDeviceID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveOrganizer_missingOrganizer_throwsException() {
        when(dbProxy.getOrganizer("missing")).thenReturn(null);
        adminModerationService.removeOrganizer("missing");
    }
}