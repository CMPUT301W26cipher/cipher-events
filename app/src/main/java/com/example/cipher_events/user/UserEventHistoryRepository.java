package com.example.cipher_events.user;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds user event history from Firestore-backed Event records.
 */
public class UserEventHistoryRepository {
    private final DBProxy db;

    public UserEventHistoryRepository() {
        this.db = DBProxy.getInstance();
    }

    public List<UserEventHistoryRecord> getHistory(String deviceId, UserEventHistoryRecord userEventHistoryRecord) {
        List<UserEventHistoryRecord> history = new ArrayList<>();

        if (deviceId == null || deviceId.trim().isEmpty()) {
            return history;
        }

        ArrayList<Event> events = db.getAllEvents();
        if (events == null) {
            return history;
        }

        for (Event event : events) {
            if (event == null) {
                continue;
            }

            Status status = resolveStatus(event, deviceId);
            if (status != null) {
                history.add(new UserEventHistoryRecord(event, status));
            }
        }

        return history;
    }

    private Status resolveStatus(Event event, String deviceId) {
        if (containsUser(event.getAttendees(), deviceId)) {
            return Status.REGISTERED;
        }

        if (containsUser(event.getEntrants(), deviceId)) {
            return Status.WAITLISTED;
        }

        return null;
    }

    private boolean containsUser(ArrayList<User> users, String deviceId) {
        if (users == null || deviceId == null) {
            return false;
        }

        for (User user : users) {
            if (user != null
                    && user.getDeviceID() != null
                    && user.getDeviceID().equals(deviceId)) {
                return true;
            }
        }
        return false;
    }
}