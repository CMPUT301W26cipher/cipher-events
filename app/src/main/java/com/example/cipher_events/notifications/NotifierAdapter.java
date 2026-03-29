package com.example.cipher_events.notifications;

import com.example.cipher_events.database.User;
import java.util.List;

/**
 * Adapter that connects NotificationService to the existing Firebase Notifier.
 * Used in the real app to send push notifications.
 */
public class NotifierAdapter implements NotificationService {

    private final Notifier notifier;

    public NotifierAdapter() {
        this.notifier = Notifier.getInstance();
    }

    @Override
    public void notifyUser(User user, String title, String message) {

        if (user == null || user.getDeviceID() == null) return;

        // respect opt-out
        if (!user.isNotificationsEnabled()) return;

        Message msg = new Message(title, message, null);
        notifier.sendMessage(user.getDeviceID(), msg);
    }

    @Override
    public void notifyUsers(List<User> users, String title, String message) {
        if (users == null) return;

        for (User user : users) {
            notifyUser(user, title, message);
        }
    }
}