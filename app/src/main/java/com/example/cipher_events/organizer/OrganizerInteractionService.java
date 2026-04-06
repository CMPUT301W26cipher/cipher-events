package com.example.cipher_events.organizer;

import com.example.cipher_events.comment.EventComment;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.notifications.NotificationService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * US 02.08.01 - Organizer views and deletes entrant comments on own event
 * US 02.08.02 - Organizer comments on own event
 * US 02.09.01 - Organizer assigns an entrant as co-organizer for an event
 */
public class OrganizerInteractionService {

    private final DBProxy db;
    private final NotificationService notificationService;

    public OrganizerInteractionService() {

        this.db = DBProxy.getInstance();
        this.notificationService = null;
    }

    // For testing
    public OrganizerInteractionService(DBProxy db) {
        this.db = db;
        this.notificationService = null;
    }

    public OrganizerInteractionService(DBProxy db, NotificationService notificationService) {
        this.db = db;
        this.notificationService = notificationService;
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

    private Organizer requireOrganizer(String organizerDeviceID) {
        Organizer organizer = db.getOrganizer(organizerDeviceID);
        if (organizer == null) {
            throw new IllegalArgumentException("Organizer not found.");
        }
        return organizer;
    }

    private User requireUser(String userDeviceID) {
        User user = db.getUser(userDeviceID);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        return user;
    }

    private boolean organizerOwnsEvent(Organizer organizer, Event event) {
        if (organizer == null || event == null) {
            return false;
        }
        if (event.getOrganizer() == null) {
            return false;
        }
        return organizer.getDeviceID() != null
                && organizer.getDeviceID().equals(event.getOrganizer().getDeviceID());
    }

    private void requireOwnership(String organizerDeviceID, String eventID) {
        Organizer organizer = requireOrganizer(organizerDeviceID);
        Event event = requireEvent(eventID);

        if (!organizerOwnsEvent(organizer, event)) {
            throw new IllegalArgumentException("Organizer does not own this event.");
        }
    }

    /**
     * US 02.08.01
     * Organizer views comments on own event.
     */
    public List<EventComment> getCommentsForOwnedEvent(String organizerDeviceID, String eventID) {
        requireOwnership(organizerDeviceID, eventID);
        Event event = requireEvent(eventID);
        return new ArrayList<>(event.getComments());
    }

    /**
     * US 02.08.01
     * Organizer deletes entrant comments on own event.
     */
    public void deleteEntrantComment(String organizerDeviceID, String eventID, String commentID) {
        requireOwnership(organizerDeviceID, eventID);

        if (commentID == null || commentID.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment ID is required.");
        }

        Event event = requireEvent(eventID);
        ArrayList<EventComment> comments = event.getComments();

        boolean removed = false;
        Iterator<EventComment> iterator = comments.iterator();

        while (iterator.hasNext()) {
            EventComment comment = iterator.next();
            if (comment != null
                    && comment.getCommentID() != null
                    && comment.getCommentID().equals(commentID)) {

                if (!"entrant".equalsIgnoreCase(comment.getAuthorRole())) {
                    throw new IllegalArgumentException("Only entrant comments can be removed.");
                }

                iterator.remove();
                removed = true;
                break;
            }
        }

        if (!removed) {
            throw new IllegalArgumentException("Comment not found.");
        }

        event.setComments(comments);
        db.updateEvent(event);
    }

    /**
     * US 02.08.02
     * Organizer comments on own event.
     */
    public EventComment postOrganizerComment(String organizerDeviceID, String eventID, String message) {
        requireOwnership(organizerDeviceID, eventID);

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment message cannot be empty.");
        }

        Organizer organizer = requireOrganizer(organizerDeviceID);
        Event event = requireEvent(eventID);

        EventComment comment = new EventComment(
                organizer.getDeviceID(),
                organizer.getName(),
                "organizer",
                message.trim(),
                nowString()
        );

        ArrayList<EventComment> comments = event.getComments();
        comments.add(comment);
        event.setComments(comments);

        db.updateEvent(event);
        return comment;
    }

    /**
     * US 01.09.01 + US 02.09.01
     * Organizer sends a co-organizer invitation to an entrant for an owned event.
     */
    public void inviteCoOrganizer(String organizerDeviceID, String eventID, String entrantDeviceID) {
        requireOwnership(organizerDeviceID, eventID);

        if (entrantDeviceID == null || entrantDeviceID.trim().isEmpty()) {
            throw new IllegalArgumentException("Entrant ID is required.");
        }

        Event event = requireEvent(eventID);
        User entrant = requireUser(entrantDeviceID);

        if (!isUserAssociatedWithEvent(event, entrantDeviceID)) {
            throw new IllegalArgumentException("User is not an entrant for this event.");
        }

        if (event.getCoOrganizerIds().contains(entrantDeviceID)) {
            throw new IllegalArgumentException("User is already a co-organizer.");
        }

        ArrayList<String> pendingIds = event.getPendingCoOrganizerIds();
        if (pendingIds.contains(entrantDeviceID)) {
            throw new IllegalArgumentException("User already has a pending co-organizer invitation.");
        }

        pendingIds.add(entrantDeviceID);
        event.setPendingCoOrganizerIds(pendingIds);
        db.updateEvent(event);

        if (notificationService != null) {
            notificationService.notifyUser(
                    entrant,
                    "Co-organizer Invitation",
                    "You have been invited to become a co-organizer for event: " + event.getName()
            );
        }
    }

    /**
     * US 02.09.01
     * Organizer assigns an entrant as co-organizer for an event.
     * That entrant cannot remain in the entrant pool for the same event.
     */
    public void assignCoOrganizer(String organizerDeviceID, String eventID, String entrantDeviceID) {
        requireOwnership(organizerDeviceID, eventID);

        if (entrantDeviceID == null || entrantDeviceID.trim().isEmpty()) {
            throw new IllegalArgumentException("Entrant ID is required.");
        }

        Event event = requireEvent(eventID);
        requireUser(entrantDeviceID);

        ArrayList<String> pendingIds = event.getPendingCoOrganizerIds();
        if (!pendingIds.contains(entrantDeviceID)) {
            throw new IllegalArgumentException("User does not have a pending co-organizer invitation.");
        }

        ArrayList<String> coOrganizerIds = event.getCoOrganizerIds();
        if (coOrganizerIds.contains(entrantDeviceID)) {
            throw new IllegalArgumentException("User is already a co-organizer.");
        }

        removeUserFromEntrantPools(event, entrantDeviceID);

        pendingIds.remove(entrantDeviceID);

        coOrganizerIds.add(entrantDeviceID);
        event.setPendingCoOrganizerIds(pendingIds);
        event.setCoOrganizerIds(coOrganizerIds);
        db.updateEvent(event);
    }

    /**
     * US 01.09.01
     * Entrant declines a co-organizer invitation.
     */
    public void declineCoOrganizerInvitation(String eventID, String entrantDeviceID) {
        if (entrantDeviceID == null || entrantDeviceID.trim().isEmpty()) {
            throw new IllegalArgumentException("Entrant ID is required.");
        }

        Event event = requireEvent(eventID);
        ArrayList<String> pendingIds = event.getPendingCoOrganizerIds();

        if (!pendingIds.contains(entrantDeviceID)) {
            throw new IllegalArgumentException("No pending co-organizer invitation found.");
        }

        pendingIds.remove(entrantDeviceID);
        event.setPendingCoOrganizerIds(pendingIds);
        db.updateEvent(event);
    }

    public boolean isCoOrganizer(String eventID, String userDeviceID) {
        Event event = requireEvent(eventID);
        return event.getCoOrganizerIds().contains(userDeviceID);
    }

    public boolean hasPendingCoOrganizerInvitation(String eventID, String userDeviceID) {
        Event event = requireEvent(eventID);
        return event.getPendingCoOrganizerIds().contains(userDeviceID);
    }

    private boolean isUserAssociatedWithEvent(Event event, String deviceID) {
        return containsUser(event.getEntrants(), deviceID)
                || containsUser(event.getInvitedEntrants(), deviceID)
                || containsUser(event.getCancelledEntrants(), deviceID)
                || containsUser(event.getEnrolledEntrants(), deviceID)
                || containsUser(event.getAttendees(), deviceID);
    }

    private boolean containsUser(List<User> users, String deviceID) {
        if (users == null) return false;
        for (User user : users) {
            if (user != null && deviceID.equals(user.getDeviceID())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Based on your real Event model:
     * entrants
     * invitedEntrants
     * cancelledEntrants
     * enrolledEntrants
     * attendees
     */
    private void removeUserFromEntrantPools(Event event, String deviceID) {
        removeUserByDeviceID(event.getEntrants(), deviceID);
        removeUserByDeviceID(event.getInvitedEntrants(), deviceID);
        removeUserByDeviceID(event.getCancelledEntrants(), deviceID);
        removeUserByDeviceID(event.getEnrolledEntrants(), deviceID);
        removeUserByDeviceID(event.getAttendees(), deviceID);
    }

    private void removeUserByDeviceID(ArrayList<User> users, String deviceID) {
        if (users == null) {
            return;
        }
        users.removeIf(user -> user != null && deviceID.equals(user.getDeviceID()));
    }
}
