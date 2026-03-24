package com.example.cipher_events.comment;

import java.util.UUID;

/**
 * Represents one comment left by an entrant on an event.
 */
public class EventComment {
    private String commentID;
    private String authorDeviceID;
    private String authorName;
    private String message;
    private String createdAt; // keep String to match your current model style

    public EventComment() {
        // Required empty constructor for Firestore
    }

    public EventComment(String authorDeviceID, String authorName, String message, String createdAt) {
        this.commentID = UUID.randomUUID().toString();
        this.authorDeviceID = authorDeviceID;
        this.authorName = authorName;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getAuthorDeviceID() {
        return authorDeviceID;
    }

    public void setAuthorDeviceID(String authorDeviceID) {
        this.authorDeviceID = authorDeviceID;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}