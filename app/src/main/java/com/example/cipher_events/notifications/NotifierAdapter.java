package com.example.cipher_events.notifications;

import com.example.cipher_events.database.User;
import java.util.List;

/**
 * Adapter that connects NotificationService to the existing Firebase Notifier.
 * Used in the real app to send push notifications.
 */

public class NotifierAdapter {

    private final Notifier notifier;

    public NotifierAdapter() {
        this.notifier = Notifier.getInstance();
    }

    public Notifier getNotifier() {
        return notifier;
    }
}