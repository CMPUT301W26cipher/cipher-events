package com.example.cipher_events;

import com.example.cipher_events.database.User;
import com.example.cipher_events.notifications.Message;
import com.example.cipher_events.notifications.NotificationService;
import com.example.cipher_events.notifications.Notifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Test implementation of NotificationService.
 * Does not send real notifications — records them for assertions.
 * Only used for testing
 */
public class FakeNotifier extends Notifier {

    private final List<Record> records = new ArrayList<>();

    public static class Record {
        public final String deviceId;
        public final String title;

        public Record(String deviceId, String title) {
            this.deviceId = deviceId;
            this.title = title;
        }
    }

    @Override
    public void sendMessage(String deviceID, Message message) {
        records.add(new Record(deviceID, message.getTitle()));
    }

    public List<Record> getRecords() {
        return records;
    }
}