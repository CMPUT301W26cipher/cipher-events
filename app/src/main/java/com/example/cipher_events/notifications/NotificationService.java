package com.example.cipher_events.notifications;

import com.example.cipher_events.database.User;
import java.util.List;

/**
 * Abstraction for sending notifications.
 * Allows services to send notifications without depending on Firebase.
 * Used with:
 * - NotifierAdapter (production)
 * - FakeNotifier (unit tests)
 */
public interface NotificationService {

    void notifyUser(User user, String title, String message);

    void notifyUsers(List<User> users, String title, String message);
}