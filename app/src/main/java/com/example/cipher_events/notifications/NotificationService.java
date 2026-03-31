package com.example.cipher_events.notifications;

import com.example.cipher_events.database.User;
import java.util.List;
import java.util.ArrayList;

/**
 * Abstraction for sending notifications.
 * Allows services to send notifications without depending on Firebase.
 * Used with:
 * - NotifierAdapter (production)
 * - FakeNotifier (unit tests)
 */
public class NotificationService {

    private final Notifier notifier;

    private final List<NotificationLog> logs = new ArrayList<>();

    public NotificationService(Notifier notifier) {
        this.notifier = notifier;
    }

    public void notifyUser(User user, String title, String message) {

        if (user == null || user.getDeviceID() == null) return;

        // respect opt-out
        if (!user.isNotificationsEnabled()) return;

        Message msg = new Message(title, message, null);

        notifier.sendMessage(user.getDeviceID(), msg);

        logs.add(new NotificationLog(
                user.getDeviceID(),
                title,
                message
        ));
    }

    public void notifyUsers(List<User> users, String title, String message) {
        if (users == null) return;

        for (User user : users) {
            notifyUser(user, title, message);
        }
    }

    public List<NotificationLog> getLogs() {
        return new ArrayList<>(logs);
    }
}