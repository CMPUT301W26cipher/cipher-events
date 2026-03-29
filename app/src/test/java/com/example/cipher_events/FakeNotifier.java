package com.example.cipher_events;

import com.example.cipher_events.database.User;
import com.example.cipher_events.notifications.NotificationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Test implementation of NotificationService.
 * Does not send real notifications — records them for assertions.
 * Only used for testing
 */
public class FakeNotifier implements NotificationService {

    public static class Record {
        public final String deviceId;
        public final String title;

        public Record(String deviceId, String title) {
            this.deviceId = deviceId;
            this.title = title;
        }
    }

    private final List<Record> records = new ArrayList<>();

    @Override
    public void notifyUser(User user, String title, String message) {
        if (user == null) return;

        // respect opt-out
        if (!user.isNotificationsEnabled()) return;

        records.add(new Record(user.getDeviceID(), title));
    }

    @Override
    public void notifyUsers(List<User> users, String title, String message) {
        if (users == null) return;

        for (User user : users) {
            notifyUser(user, title, message);
        }
    }

    public List<Record> getRecords() {
        return records;
    }
}