package com.example.cipher_events;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.message.DirectMessage;
import com.example.cipher_events.message.MessageThread;
import com.example.cipher_events.message.MessagingService;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MessagingServiceTest {

    private DBProxy db;
    private MessagingService service;

    private Organizer organizer;
    private Organizer otherOrganizer;
    private User entrant;
    private User stranger;
    private Event event;

    @Before
    public void setUp() {
        db = mock(DBProxy.class);
        service = new MessagingService(db);

        organizer = mock(Organizer.class);
        when(organizer.getDeviceID()).thenReturn("org-1");
        when(organizer.getName()).thenReturn("Organizer One");

        otherOrganizer = mock(Organizer.class);
        when(otherOrganizer.getDeviceID()).thenReturn("org-2");
        when(otherOrganizer.getName()).thenReturn("Organizer Two");

        entrant = mock(User.class);
        when(entrant.getDeviceID()).thenReturn("user-1");
        when(entrant.getName()).thenReturn("Entrant One");

        stranger = mock(User.class);
        when(stranger.getDeviceID()).thenReturn("user-2");
        when(stranger.getName()).thenReturn("Stranger");

        event = mock(Event.class);
        when(event.getOrganizer()).thenReturn(organizer);

        ArrayList<User> entrants = new ArrayList<>();
        entrants.add(entrant);

        ArrayList<User> invited = new ArrayList<>();
        ArrayList<User> cancelled = new ArrayList<>();
        ArrayList<User> enrolled = new ArrayList<>();
        ArrayList<User> attendees = new ArrayList<>();
        ArrayList<MessageThread> threads = new ArrayList<>();

        when(event.getEntrants()).thenReturn(entrants);
        when(event.getInvitedEntrants()).thenReturn(invited);
        when(event.getCancelledEntrants()).thenReturn(cancelled);
        when(event.getEnrolledEntrants()).thenReturn(enrolled);
        when(event.getAttendees()).thenReturn(attendees);
        when(event.getMessageThreads()).thenReturn(threads);

        when(db.getEvent("event-1")).thenReturn(event);
        when(db.getUser("user-1")).thenReturn(entrant);
        when(db.getUser("user-2")).thenReturn(stranger);
        when(db.getOrganizer("org-1")).thenReturn(organizer);
        when(db.getOrganizer("org-2")).thenReturn(otherOrganizer);
    }

    @Test
    public void openThread_validEntrant_createsThreadAndUpdatesEvent() {
        MessageThread thread = service.openThread("event-1", "user-1");

        assertNotNull(thread);
        assertEquals("event-1", thread.getEventID());
        assertEquals("org-1", thread.getOrganizerDeviceID());
        assertEquals("user-1", thread.getEntrantDeviceID());
        assertEquals(1, event.getMessageThreads().size());


        verify(db).updateEvent(event);
    }

    @Test
    public void openThread_existingThread_returnsExistingOne() {
        MessageThread existing = new MessageThread("event-1", "org-1", "user-1");
        event.getMessageThreads().add(existing);

        MessageThread result = service.openThread("event-1", "user-1");

        assertSame(existing, result);
        verify(db, never()).updateEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void openThread_userNotAssociatedWithEvent_throwsException() {
        service.openThread("event-1", "user-2");
    }

    @Test
    public void sendMessageAsEntrant_validMessage_addsMessageAndUpdatesEvent() {
        service.sendMessageAsEntrant("event-1", "user-1", "Hello organizer");

        assertEquals(1, event.getMessageThreads().size());
        MessageThread thread = event.getMessageThreads().get(0);
        assertEquals(1, thread.getMessages().size());
        assertEquals("entrant", thread.getMessages().get(0).getSenderRole());
        assertEquals("Hello organizer", thread.getMessages().get(0).getContent());

        verify(db, atLeastOnce()).updateEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessageAsEntrant_emptyMessage_throwsException() {
        service.sendMessageAsEntrant("event-1", "user-1", "   ");
    }

    @Test
    public void sendMessageAsOrganizer_validReply_addsMessageAndUpdatesEvent() {
        MessageThread thread = new MessageThread("event-1", "org-1", "user-1");
        event.getMessageThreads().add(thread);

        service.sendMessageAsOrganizer("event-1", "org-1", thread.getThreadID(), "Hi there");

        assertEquals(1, thread.getMessages().size());
        assertEquals("organizer", thread.getMessages().get(0).getSenderRole());
        assertEquals("Hi there", thread.getMessages().get(0).getContent());

        verify(db).updateEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessageAsOrganizer_wrongOwner_throwsException() {
        MessageThread thread = new MessageThread("event-1", "org-1", "user-1");
        event.getMessageThreads().add(thread);

        service.sendMessageAsOrganizer("event-1", "org-2", thread.getThreadID(), "Hi there");
    }

    @Test
    public void getThreadMessages_authorizedEntrant_returnsMessages() {
        MessageThread thread = new MessageThread("event-1", "org-1", "user-1");
        thread.getMessages().add(new DirectMessage("user-1", "Entrant One", "entrant", "Question", "2026-03-31 10:00:00"));
        thread.getMessages().add(new DirectMessage("org-1", "Organizer One", "organizer", "Answer", "2026-03-31 10:01:00"));
        event.getMessageThreads().add(thread);

        ArrayList<DirectMessage> messages = service.getThreadMessages("event-1", thread.getThreadID(), "user-1");

        assertEquals(2, messages.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getThreadMessages_unauthorizedUser_throwsException() {
        MessageThread thread = new MessageThread("event-1", "org-1", "user-1");
        event.getMessageThreads().add(thread);

        service.getThreadMessages("event-1", thread.getThreadID(), "user-2");
    }

    @Test
    public void getThreadsForOrganizer_validOwner_returnsThreads() {
        MessageThread thread1 = new MessageThread("event-1", "org-1", "user-1");
        MessageThread thread2 = new MessageThread("event-1", "org-1", "user-1");
        event.getMessageThreads().add(thread1);
        event.getMessageThreads().add(thread2);

        List<MessageThread> result = service.getThreadsForOrganizer("event-1", "org-1");

        assertEquals(2, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getThreadsForOrganizer_notOwner_throwsException() {
        service.getThreadsForOrganizer("event-1", "org-2");
    }
}