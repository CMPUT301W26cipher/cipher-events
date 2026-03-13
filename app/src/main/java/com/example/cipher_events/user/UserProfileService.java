package com.example.cipher_events.user;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Firestore-backed service for:
 * US 01.02.01 Create profile
 * US 01.02.02 Update profile
 * US 01.02.03 View event history
 * US 01.02.04 Delete profile
 */
public class UserProfileService {
    private final DBProxy db;
    private final UserEventHistoryRepository historyRepository;

    public UserProfileService() {
        this.db = DBProxy.getInstance();
        this.historyRepository = new UserEventHistoryRepository();
    }

    public UserProfileService(DBProxy db, UserEventHistoryRepository historyRepository) {
        this.db = db;
        this.historyRepository = historyRepository;
    }

    /**
     * US 01.02.01
     */
    public User createUserProfile(String name,
                                  String email,
                                  String password,
                                  String phoneNumber,
                                  String profilePictureURL) {
        UserProfileValidator.validateRequiredProfileFields(name, email);
        UserProfileValidator.validateOptionalPhone(phoneNumber);

        User user = new User(
                name.trim(),
                email.trim(),
                password,
                normalizeOptional(phoneNumber),
                normalizeOptional(profilePictureURL)
        );

        db.addUser(user);
        return user;
    }

    /**
     * US 01.02.02
     */
    public User updateUserProfile(String deviceId,
                                  String newName,
                                  String newEmail,
                                  String newPhoneNumber,
                                  String newProfilePictureURL) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required.");
        }

        User existingUser = db.getUser(deviceId);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found.");
        }

        UserProfileValidator.validateRequiredProfileFields(newName, newEmail);
        UserProfileValidator.validateOptionalPhone(newPhoneNumber);

        existingUser.setName(newName.trim());
        existingUser.setEmail(newEmail.trim());
        existingUser.setPhoneNumber(normalizeOptional(newPhoneNumber));
        existingUser.setProfilePictureURL(normalizeOptional(newProfilePictureURL));

        db.updateUser(existingUser);
        return existingUser;
    }

    /**
     * US 01.02.03
     */
    public List<UserEventHistoryRecord> getUserEventHistory(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required.");
        }

        User user = db.getUser(deviceId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return historyRepository.getHistory(deviceId, new UserEventHistoryRecord(event, Status.WAITLISTED));
    }

    /**
     * Optional helper if you still want to manually add a user to an event history state.
     * This updates the actual Event lists in Firestore-backed storage.
     */
    public void addEventHistory(String deviceId, Event event, Status status) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required.");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event is required.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required.");
        }

        User user = db.getUser(deviceId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        Event storedEvent = db.getEvent(event.getEventID());
        if (storedEvent == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        ensureLists(storedEvent);
        removeUserFromEventLists(storedEvent, deviceId);

        switch (status) {
            case WAITLISTED:
                if (!containsUser(storedEvent.getEntrants(), deviceId)) {
                    storedEvent.getEntrants().add(user);
                }
                break;

            case REGISTERED:
            case SELECTED:
                if (!containsUser(storedEvent.getAttendees(), deviceId)) {
                    storedEvent.getAttendees().add(user);
                }
                break;

            case NOT_SELECTED:
            case CANCELLED:
                // For your current Event model there is no dedicated stored list.
                // We simply remove the user from entrant/attendee lists.
                break;
        }

        db.updateEvent(storedEvent);
    }

    /**
     * Optional helper to update/overwrite the user's status in a specific event.
     */
    public void upsertEventHistory(String deviceId, Event event, Status status) {
        addEventHistory(deviceId, event, status);
    }

    /**
     * US 01.02.04
     */
    public void deleteUserProfile(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required.");
        }

        User user = db.getUser(deviceId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        ArrayList<Event> allEvents = db.getAllEvents();
        if (allEvents != null) {
            for (Event event : allEvents) {
                if (event == null) {
                    continue;
                }

                ensureLists(event);
                boolean changed = false;

                changed |= removeUserFromList(event.getEntrants(), deviceId);
                changed |= removeUserFromList(event.getAttendees(), deviceId);

                if (changed) {
                    db.updateEvent(event);
                }
            }
        }

        db.deleteUser(deviceId);
    }

    private void ensureLists(Event event) {
        if (event.getEntrants() == null) {
            event.setEntrants(new ArrayList<>());
        }
        if (event.getAttendees() == null) {
            event.setAttendees(new ArrayList<>());
        }
    }

    private void removeUserFromEventLists(Event event, String deviceId) {
        removeUserFromList(event.getEntrants(), deviceId);
        removeUserFromList(event.getAttendees(), deviceId);
    }

    private boolean removeUserFromList(ArrayList<User> users, String deviceId) {
        if (users == null || deviceId == null) {
            return false;
        }

        boolean removed = false;
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User current = iterator.next();
            if (current != null
                    && current.getDeviceID() != null
                    && current.getDeviceID().equals(deviceId)) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
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

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}