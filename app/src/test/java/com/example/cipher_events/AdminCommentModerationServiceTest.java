package com.example.cipher_events;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.cipher_events.admin.AdminCommentModerationService;
import com.example.cipher_events.comment.EventComment;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AdminCommentModerationServiceTest {

    private DBProxy dbProxy;
    private AdminCommentModerationService adminCommentModerationService;

    @Before
    public void setUp() {
        dbProxy = mock(DBProxy.class);
        adminCommentModerationService = new AdminCommentModerationService(dbProxy);
    }

    private Event createTestEvent() {
        return new Event(
                "Seminar",
                "CS seminar",
                "2026-05-01 13:00",
                "UofA",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                true
        );
    }

    private EventComment createComment(String authorId, String authorName, String authorRole, String msg) {
        return new EventComment(authorId, authorName, authorRole, msg, "2026-03-27 12:00");
    }

    @Test
    public void testGetEventComments_existingEvent_returnsComments() {
        Event event = createTestEvent();
        event.getComments().add(createComment("u1", "Alice", "entrant", "Bad comment"));
        event.getComments().add(createComment("u2", "Bob", "entrant", "Another comment"));

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        List<EventComment> comments = adminCommentModerationService.getEventComments(event.getEventID());

        assertEquals(2, comments.size());
        verify(dbProxy, times(1)).getEvent(event.getEventID());
    }

    @Test
    public void testRemoveEventComment_existingComment_removesAndUpdatesEvent() {
        Event event = createTestEvent();
        EventComment comment1 = createComment("u1", "Alice", "entrant", "Bad comment");
        EventComment comment2 = createComment("u2", "Bob", "entrant", "Good comment");

        event.getComments().add(comment1);
        event.getComments().add(comment2);

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        boolean removed = adminCommentModerationService.removeEventComment(
                event.getEventID(),
                comment1.getCommentID()
        );

        assertTrue(removed);
        assertEquals(1, event.getComments().size());
        assertEquals(comment2.getCommentID(), event.getComments().get(0).getCommentID());
        verify(dbProxy, times(1)).updateEvent(event);
    }

    @Test
    public void testRemoveEventComment_missingComment_returnsFalse() {
        Event event = createTestEvent();
        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        boolean removed = adminCommentModerationService.removeEventComment(
                event.getEventID(),
                "missing-comment-id"
        );

        assertFalse(removed);
        verify(dbProxy, never()).updateEvent(event);
    }

    @Test
    public void testRemoveCommentsByAuthor_existingAuthor_removesAllMatchingComments() {
        Event event = createTestEvent();
        event.getComments().add(createComment("u1", "Alice", "entrant", "Spam 1"));
        event.getComments().add(createComment("u2", "Bob", "entrant", "Normal"));
        event.getComments().add(createComment("u1", "Alice", "entrant", "Spam 2"));

        when(dbProxy.getEvent(event.getEventID())).thenReturn(event);

        int removedCount = adminCommentModerationService.removeCommentsByAuthor(
                event.getEventID(),
                "u1"
        );

        assertEquals(2, removedCount);
        assertEquals(1, event.getComments().size());
        assertEquals("u2", event.getComments().get(0).getAuthorDeviceID());
        verify(dbProxy, times(1)).updateEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEventComments_missingEvent_throwsException() {
        when(dbProxy.getEvent("missing-event")).thenReturn(null);
        adminCommentModerationService.getEventComments("missing-event");
    }
}