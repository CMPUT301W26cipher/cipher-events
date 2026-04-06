package com.example.cipher_events.comment;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Service for:
 * US 01.08.01 - Entrant comments on an event
 * US 01.08.02 - Entrant views event comments
 */
public class EntrantCommentService {

    private final DBProxy db;

    public EntrantCommentService() {
        this.db = DBProxy.getInstance();
    }
    public EntrantCommentService(DBProxy db) {
        this.db = db;
    }

    /**
     * US 01.08.01
     * Add a comment to an event and save it to Firestore through EventDB.update().
     */
    public void addComment(String eventID, User user, String rawMessage) {
        if (eventID == null || eventID.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be empty.");
        }

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        String message = normalizeMessage(rawMessage);
        if (message.isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }

        if (message.length() > 300) {
            throw new IllegalArgumentException("Comment cannot exceed 300 characters.");
        }

        Event event = db.getEvent(eventID);
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        ArrayList<EventComment> comments = event.getComments();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());

        EventComment comment = new EventComment(
                user.getDeviceID(),
                user.getName(),
                "entrant",
                message,
                timestamp
        );

        comments.add(comment);
        event.setComments(comments);

        // Persist full updated event to Firestore
        db.updateEvent(event);
    }

    /**
     * US 01.08.02
     * Return all comments for an event.
     */
    public ArrayList<EventComment> getComments(String eventID) {
        if (eventID == null || eventID.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be empty.");
        }

        Event event = db.getEvent(eventID);
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        return event.getComments();
    }

    /**
     * Delete a comment from an event by commentID.
     * Available to comment author and admins.
     */
    public void deleteComment(String eventID, String commentID) {
        if (eventID == null || eventID.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be empty.");
        }
        if (commentID == null || commentID.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment ID cannot be empty.");
        }

        Event event = db.getEvent(eventID);
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        ArrayList<EventComment> comments = event.getComments();
        comments.removeIf(c -> c.getCommentID().equals(commentID));
        event.setComments(comments);
        db.updateEvent(event);
    }

    private String normalizeMessage(String rawMessage) {
        if (rawMessage == null) {
            return "";
        }
        return rawMessage.trim().replaceAll("\\s+", " ");
    }
}
