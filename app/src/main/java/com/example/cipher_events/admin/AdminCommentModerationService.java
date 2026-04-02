package com.example.cipher_events.admin;

import com.example.cipher_events.comment.EventComment;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * US 03.10.01
 * Admin can remove event comments that violate app policy.
 */
public class AdminCommentModerationService {

    private final DBProxy db;

    public AdminCommentModerationService() {
        this.db = DBProxy.getInstance();
    }
    public AdminCommentModerationService(DBProxy db) {
        this.db = db;
    }

    /**
     * View all comments on one event.
     */
    public List<EventComment> getEventComments(String eventID) {
        Event event = requireEvent(eventID);
        ArrayList<EventComment> comments = event.getComments();
        return comments == null ? new ArrayList<>() : new ArrayList<>(comments);
    }

    /**
     * Remove one comment from one event and persist through DBProxy.updateEvent().
     */
    public boolean removeEventComment(String eventID, String commentID) {
        Event event = requireEvent(eventID);

        if (commentID == null || commentID.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment ID cannot be empty.");
        }

        ArrayList<EventComment> comments = event.getComments();
        if (comments == null || comments.isEmpty()) {
            return false;
        }

        boolean removed = false;
        for (int i = 0; i < comments.size(); i++) {
            EventComment comment = comments.get(i);
            if (comment != null
                    && comment.getCommentID() != null
                    && comment.getCommentID().equals(commentID.trim())) {
                comments.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            event.setComments(comments);
            db.updateEvent(event);   // persists to Firestore through EventDB.update()
        }

        return removed;
    }

    /**
     * Optional helper: remove all comments by a given author from one event.
     */
    public int removeCommentsByAuthor(String eventID, String authorDeviceID) {
        Event event = requireEvent(eventID);

        if (authorDeviceID == null || authorDeviceID.trim().isEmpty()) {
            throw new IllegalArgumentException("Author device ID cannot be empty.");
        }

        ArrayList<EventComment> comments = event.getComments();
        if (comments == null || comments.isEmpty()) {
            return 0;
        }

        int removedCount = 0;
        for (int i = comments.size() - 1; i >= 0; i--) {
            EventComment comment = comments.get(i);
            if (comment != null
                    && comment.getAuthorDeviceID() != null
                    && comment.getAuthorDeviceID().equals(authorDeviceID.trim())) {
                comments.remove(i);
                removedCount++;
            }
        }

        if (removedCount > 0) {
            event.setComments(comments);
            db.updateEvent(event);
        }

        return removedCount;
    }

    private Event requireEvent(String eventID) {
        if (eventID == null || eventID.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be empty.");
        }

        Event event = db.getEvent(eventID.trim());
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        return event;
    }
}