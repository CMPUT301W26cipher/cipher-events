package com.example.cipher_events.organizer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.cipher_events.comment.EventComment;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OrganizerInteractionServiceTest {

    private DBProxy db;
    private OrganizerInteractionService service;

    private Organizer organizer;
    private Organizer otherOrganizer;
    private User entrant;
    private Event event;

    @Before
    public void setUp() {
        db = mock(DBProxy.class);
        service = new OrganizerInteractionService(db);

        organizer = mock(Organizer.class);
        when(organizer.getDeviceID()).thenReturn("org-1");
        when(organizer.getName()).thenReturn("Organizer One");

        otherOrganizer = mock(Organizer.class);
        when(otherOrganizer.getDeviceID()).thenReturn("org-2");
        when(otherOrganizer.getName()).thenReturn("Organizer Two");

        entrant = mock(User.class);
        when(entrant.getDeviceID()).thenReturn("user-1");
        when(entrant.getName()).thenReturn("Entrant One");

        event = mock(Event.class);
        when(event.getOrganizer()).thenReturn(organizer);

        when(db.getOrganizer("org-1")).thenReturn(organizer);
        when(db.getOrganizer("org-2")).thenReturn(otherOrganizer);
        when(db.getUser("user-1")).thenReturn(entrant);
        when(db.getEvent("event-1")).thenReturn(event);
    }

    @Test
    public void getCommentsForOwnedEvent_validOwner_returnsComments() {
        ArrayList<EventComment> comments = new ArrayList<>();
        comments.add(new EventComment("user-1", "Entrant One", "entrant", "Hello", "2026-03-30 10:00:00"));

        when(event.getComments()).thenReturn(comments);

        List<EventComment> result = service.getCommentsForOwnedEvent("org-1", "event-1");

        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0).getMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCommentsForOwnedEvent_notOwner_throwsException() {
        when(event.getOrganizer()).thenReturn(organizer);
        service.getCommentsForOwnedEvent("org-2", "event-1");
    }

    @Test
    public void deleteEntrantComment_validEntrantComment_removesAndUpdates() {
        ArrayList<EventComment> comments = new ArrayList<>();
        EventComment c1 = new EventComment("user-1", "Entrant One", "entrant", "Need help", "2026-03-30 10:00:00");
        comments.add(c1);

        when(event.getComments()).thenReturn(comments);

        service.deleteEntrantComment("org-1", "event-1", c1.getCommentID());

        assertEquals(0, comments.size());
        verify(event).setComments(comments);
        verify(db).updateEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteEntrantComment_commentNotFound_throwsException() {
        when(event.getComments()).thenReturn(new ArrayList<>());

        service.deleteEntrantComment("org-1", "event-1", "missing-comment");
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteEntrantComment_tryDeleteOrganizerComment_throwsException() {
        ArrayList<EventComment> comments = new ArrayList<>();
        EventComment organizerComment =
                new EventComment("org-1", "Organizer One", "organizer", "Internal note", "2026-03-30 10:00:00");
        comments.add(organizerComment);

        when(event.getComments()).thenReturn(comments);

        service.deleteEntrantComment("org-1", "event-1", organizerComment.getCommentID());
    }

    @Test
    public void postOrganizerComment_validInput_addsCommentAndUpdates() {
        ArrayList<EventComment> comments = new ArrayList<>();
        when(event.getComments()).thenReturn(comments);

        EventComment result = service.postOrganizerComment("org-1", "event-1", "Welcome everyone");

        assertNotNull(result);
        assertEquals(1, comments.size());
        assertEquals("organizer", comments.get(0).getAuthorRole());
        assertEquals("Welcome everyone", comments.get(0).getMessage());

        verify(event).setComments(comments);
        verify(db).updateEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void postOrganizerComment_emptyMessage_throwsException() {
        when(event.getComments()).thenReturn(new ArrayList<>());
        service.postOrganizerComment("org-1", "event-1", "   ");
    }

    @Test
    public void assignCoOrganizer_validEntrant_addsIdAndRemovesFromPools() {
        ArrayList<String> coOrganizerIds = new ArrayList<>();

        ArrayList<User> entrants = new ArrayList<>();
        entrants.add(entrant);

        ArrayList<User> invitedEntrants = new ArrayList<>();
        invitedEntrants.add(entrant);

        ArrayList<User> cancelledEntrants = new ArrayList<>();
        ArrayList<User> enrolledEntrants = new ArrayList<>();
        ArrayList<User> attendees = new ArrayList<>();

        when(event.getCoOrganizerIds()).thenReturn(coOrganizerIds);
        when(event.getEntrants()).thenReturn(entrants);
        when(event.getInvitedEntrants()).thenReturn(invitedEntrants);
        when(event.getCancelledEntrants()).thenReturn(cancelledEntrants);
        when(event.getEnrolledEntrants()).thenReturn(enrolledEntrants);
        when(event.getAttendees()).thenReturn(attendees);

        service.assignCoOrganizer("org-1", "event-1", "user-1");

        assertTrue(coOrganizerIds.contains("user-1"));
        assertEquals(0, entrants.size());
        assertEquals(0, invitedEntrants.size());

        verify(event).setCoOrganizerIds(coOrganizerIds);
        verify(db).updateEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assignCoOrganizer_duplicate_throwsException() {
        ArrayList<String> coOrganizerIds = new ArrayList<>();
        coOrganizerIds.add("user-1");

        when(event.getCoOrganizerIds()).thenReturn(coOrganizerIds);

        service.assignCoOrganizer("org-1", "event-1", "user-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void assignCoOrganizer_missingUser_throwsException() {
        when(event.getCoOrganizerIds()).thenReturn(new ArrayList<>());
        when(db.getUser("missing-user")).thenReturn(null);

        service.assignCoOrganizer("org-1", "event-1", "missing-user");
    }

    @Test(expected = IllegalArgumentException.class)
    public void assignCoOrganizer_notOwner_throwsException() {
        when(event.getCoOrganizerIds()).thenReturn(new ArrayList<>());
        service.assignCoOrganizer("org-2", "event-1", "user-1");
    }

    @Test
    public void isCoOrganizer_userPresent_returnsTrue() {
        ArrayList<String> ids = new ArrayList<>();
        ids.add("user-1");

        when(event.getCoOrganizerIds()).thenReturn(ids);

        assertTrue(service.isCoOrganizer("event-1", "user-1"));
    }

    @Test
    public void isCoOrganizer_userMissing_returnsFalse() {
        when(event.getCoOrganizerIds()).thenReturn(new ArrayList<>());

        assertFalse(service.isCoOrganizer("event-1", "user-1"));
    }
}