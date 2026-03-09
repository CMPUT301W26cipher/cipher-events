package com.example.cipher_events.user;

import com.example.cipher_events.user.UserEventHistoryRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores event history per user device ID.
 */
public class UserEventHistoryRepository {
    private final Map<String, List<UserEventHistoryRecord>> historyByUserId;

    public UserEventHistoryRepository() {
        this.historyByUserId = new HashMap<>();
    }

    public void addRecord(String deviceId, UserEventHistoryRecord record) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID cannot be empty.");
        }
        if (record == null) {
            throw new IllegalArgumentException("History record cannot be null.");
        }

        historyByUserId
                .computeIfAbsent(deviceId, k -> new ArrayList<>())
                .add(record);
    }

    public List<UserEventHistoryRecord> getHistory(String deviceId) {
        List<UserEventHistoryRecord> records = historyByUserId.get(deviceId);
        if (records == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(records);
    }

    public void clearHistory(String deviceId) {
        historyByUserId.remove(deviceId);
    }

    public List<UserEventHistoryRecord> getUnmodifiableHistory(String deviceId) {
        List<UserEventHistoryRecord> records = historyByUserId.get(deviceId);
        if (records == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(records);
    }
}
