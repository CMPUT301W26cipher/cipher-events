package com.example.cipher_events;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.cipher_events.comment.EntrantCommentService;
import com.example.cipher_events.comment.EventComment;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class EntrantCommentServiceTest {

    private DBProxy dbProxy;
    private EntrantCommentService entrantCommentService;

    @Before
    public void setUp() {
        dbProxy = mock(DBProxy.class);
        entrantCommentService = new EntrantCommentService(dbProxy);
    }

    private User createTestUser() {
        User user = new User();
        user.setDeviceID("user-1");
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPassword("pass123");
        user.setPhoneNumber("7801234567");
        user.setProfilePictureURL(null);
        return user;
    }

    private Event createTestEvent() {
        return new Event(
                "Hackathon",
                "Coding competition",
                "2026-04-10 18:00",
                "Edmonton",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                false
        );
    }

    // =========================================================
    // US 01.08.01
    // Entrant posts a comment on an event
    // =========================================================

    @Test
    public void testAddComment_validInput_addsCommentAndUpdatesEvent() {
        User user = createTestUser();
        Event event = createTestEvent();

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        entrantCommentService.addComment(event.getEventID(), user, "Great event!");

        assertEquals(1, event.getComments().size());
        assertEquals("Great event!", event.getComments().get(0).getMessage());
        assertEquals(user.getDeviceID(), event.getComments().get(0).getAuthorDeviceID());
        assertEquals(user.getName(), event.getComments().get(0).getAuthorName());

        verify(dbProxy, times(1)).updateEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddComment_emptyEventId_throwsException() {
        entrantCommentService.addComment("", createTestUser(), "Hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddComment_nullUser_throwsException() {
        entrantCommentService.addComment("event-id", null, "Hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddComment_emptyComment_throwsException() {
        Event event = createTestEvent();
        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        entrantCommentService.addComment(event.getEventID(), createTestUser(), "   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddComment_eventNotFound_throwsException() {
        when(dbProxy.getEvent("missing-event")).thenReturn(null);

        entrantCommentService.addComment("missing-event", createTestUser(), "Hello");
    }

    // =========================================================
    // US 01.08.02
    // Entrant views comments on an event
    // =========================================================

    @Test
    public void testGetComments_existingEvent_returnsComments() {
        Event event = createTestEvent();
        event.getComments().add(new EventComment("u1", "Alice", "entrant","First!", "2026-03-27 10:00"));
        event.getComments().add(new EventComment("u2", "Bob", "entrant", "Nice event", "2026-03-27 10:05"));

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        ArrayList<EventComment> comments = entrantCommentService.getComments(event.getEventID());

        assertEquals(2, comments.size());
        assertEquals("First!", comments.get(0).getMessage());
        assertEquals("Nice event", comments.get(1).getMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetComments_missingEvent_throwsException() {
        when(dbProxy.getEvent("missing-event")).thenReturn(null);
        entrantCommentService.getComments("missing-event");
    }
}