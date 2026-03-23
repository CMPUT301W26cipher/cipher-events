package com.example.cipher_events.comment;

import java.util.List;

public interface CommentRepository {
    void addComment(EventComment comment);
    List<EventComment> getCommentsByEventId(String eventId);
    void deleteComment(String commentId);
    void clear();
}