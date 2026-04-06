package com.example.cipher_events.message;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagingService {

    private final DBProxy db;

    public MessagingService() {
        this.db = DBProxy.getInstance();
    }

    public MessagingService(DBProxy db) {
        this.db = db;
    }

    private String nowString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA)
                .format(new Date());
    }

    private Event requireEvent(String eventID) {
        Event event = db.getEvent(eventID);
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }
        return event;
    }

    private User requireUser(String userDeviceID) {
        User user = db.getUser(userDeviceID);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        return user;
    }

    private Organizer requireOrganizer(String organizerDeviceID) {
        Organizer organizer = db.getOrganizer(organizerDeviceID);
        if (organizer == null) {
            throw new IllegalArgumentException("Organizer not found.");
        }
        return organizer;
    }

    private boolean eventOwnedByOrganizer(Event event, Organizer organizer) {
        return event.getOrganizer() != null
                && organizer.getDeviceID() != null
                && organizer.getDeviceID().equals(event.getOrganizer().getDeviceID());
    }

    private boolean userBelongsToEvent(Event event, String userDeviceID) {
        return containsUser(event.getEntrants(), userDeviceID)
                || containsUser(event.getInvitedEntrants(), userDeviceID)
                || containsUser(event.getCancelledEntrants(), userDeviceID)
                || containsUser(event.getEnrolledEntrants(), userDeviceID)
                || containsUser(event.getAttendees(), userDeviceID);
    }

    private boolean containsUser(ArrayList<User> users, String deviceID) {
        if (users == null) return false;
        for (User user : users) {
            if (user != null && deviceID.equals(user.getDeviceID())) {
                return true;
            }
        }
        return false;
    }

    private String normalizeMessage(String rawMessage) {
        if (rawMessage == null) {
            return "";
        }
        return rawMessage.trim().replaceAll("\\s+", " ");
    }

    private MessageThread findThread(Event event, String organizerDeviceID, String entrantDeviceID) {
        for (MessageThread thread : event.getMessageThreads()) {
            if (thread != null
                    && organizerDeviceID.equals(thread.getOrganizerDeviceID())
                    && entrantDeviceID.equals(thread.getEntrantDeviceID())) {
                return thread;
            }
        }
        return null;
    }

    private MessageThread requireThreadByID(Event event, String threadID) {
        for (MessageThread thread : event.getMessageThreads()) {
            if (thread != null && threadID.equals(thread.getThreadID())) {
                return thread;
            }
        }
        throw new IllegalArgumentException("Thread not found.");
    }

    /**
     * Create a direct-message thread between one entrant and the event organizer.
     * Reuses existing thread if already created.
     */
    public MessageThread openThread(String eventID, String entrantDeviceID) {
        if (eventID == null || eventID.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be empty.");
        }
        if (entrantDeviceID == null || entrantDeviceID.trim().isEmpty()) {
            throw new IllegalArgumentException("Entrant ID cannot be empty.");
        }

        Event event = requireEvent(eventID);
        User entrant = requireUser(entrantDeviceID);
        Organizer organizer = event.getOrganizer();

        if (organizer == null) {
            throw new IllegalArgumentException("Event organizer not found.");
        }

        if (!userBelongsToEvent(event, entrant.getDeviceID())) {
            throw new IllegalArgumentException("Entrant is not associated with this event.");
        }

        MessageThread existing = findThread(event, organizer.getDeviceID(), entrant.getDeviceID());
        if (existing != null) {
            return existing;
        }

        MessageThread thread = new MessageThread(eventID, organizer.getDeviceID(), entrant.getDeviceID());
        event.getMessageThreads().add(thread);
        event.setMessageThreads(event.getMessageThreads());
        db.updateEvent(event);

        return thread;
    }

    /**
     * Entrant sends a direct message to the event organizer.
     */
    public void sendMessageAsEntrant(String eventID, String entrantDeviceID, String rawMessage) {
        String message = normalizeMessage(rawMessage);
        if (message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }
        if (message.length() > 500) {
            throw new IllegalArgumentException("Message cannot exceed 500 characters.");
        }

        Event event = requireEvent(eventID);
        User entrant = requireUser(entrantDeviceID);
        Organizer organizer = event.getOrganizer();

        if (organizer == null) {
            throw new IllegalArgumentException("Event organizer not found.");
        }

        if (!userBelongsToEvent(event, entrantDeviceID)) {
            throw new IllegalArgumentException("Entrant is not associated with this event.");
        }

        MessageThread thread = findThread(event, organizer.getDeviceID(), entrantDeviceID);
        if (thread == null) {
            thread = openThread(eventID, entrantDeviceID);
            event = requireEvent(eventID);
            thread = findThread(event, organizer.getDeviceID(), entrantDeviceID);
        }

        DirectMessage directMessage = new DirectMessage(
                entrant.getDeviceID(),
                entrant.getName(),
                "entrant",
                message,
                nowString(),
                entrant.getProfilePictureURL()
        );

        thread.getMessages().add(directMessage);
        db.updateEvent(event);
    }

    /**
     * Organizer replies to a direct-message thread in their own event.
     */
    public void sendMessageAsOrganizer(String eventID, String organizerDeviceID, String threadID, String rawMessage) {
        String message = normalizeMessage(rawMessage);
        if (message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }
        if (message.length() > 500) {
            throw new IllegalArgumentException("Message cannot exceed 500 characters.");
        }

        Event event = requireEvent(eventID);
        Organizer organizer = requireOrganizer(organizerDeviceID);

        if (!eventOwnedByOrganizer(event, organizer)) {
            throw new IllegalArgumentException("Organizer does not own this event.");
        }

        MessageThread thread = requireThreadByID(event, threadID);

        if (!organizerDeviceID.equals(thread.getOrganizerDeviceID())) {
            throw new IllegalArgumentException("Organizer does not have access to this thread.");
        }

        DirectMessage directMessage = new DirectMessage(
                organizer.getDeviceID(),
                organizer.getName(),
                "organizer",
                message,
                nowString(),
                organizer.getProfilePictureURL()
        );

        thread.getMessages().add(directMessage);
        db.updateEvent(event);
    }

    /**
     * Entrant or organizer reads a thread they are part of.
     */
    public ArrayList<DirectMessage> getThreadMessages(String eventID, String threadID, String requesterDeviceID) {
        Event event = requireEvent(eventID);
        MessageThread thread = requireThreadByID(event, threadID);

        boolean allowed = requesterDeviceID.equals(thread.getEntrantDeviceID())
                || requesterDeviceID.equals(thread.getOrganizerDeviceID());

        if (!allowed) {
            throw new IllegalArgumentException("Access denied.");
        }

        return new ArrayList<>(thread.getMessages());
    }

    /**
     * Organizer sees all direct-message threads for their own event.
     */
    public List<MessageThread> getThreadsForOrganizer(String eventID, String organizerDeviceID) {
        Event event = requireEvent(eventID);
        Organizer organizer = requireOrganizer(organizerDeviceID);

        if (!eventOwnedByOrganizer(event, organizer)) {
            throw new IllegalArgumentException("Organizer does not own this event.");
        }

        return new ArrayList<>(event.getMessageThreads());
    }
}
