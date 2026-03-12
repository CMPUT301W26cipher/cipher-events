package com.example.cipher_events.user;

import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.user.Status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Service for:
 * US 01.02.01 Create profile
 * US 01.02.02 Update profile
 * US 01.02.03 View event history
 * US 01.02.04 Delete profile
 */
public class UserProfileService {
    private final UserRepository userRepository;
    private final UserEventHistoryRepository historyRepository;

    public UserProfileService(UserRepository userRepository,
                              UserEventHistoryRepository historyRepository) {
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * US 01.02.01
     * Create a new user profile with required name/email and optional phone.
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

        userRepository.save(user);
        return user;
    }

    /**
     * US 01.02.02
     * Update existing user profile fields.
     */
    public User updateUserProfile(String deviceId,
                                  String newName,
                                  String newEmail,
                                  String newPhoneNumber,
                                  String newProfilePictureURL) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required.");
        }

        User existingUser = userRepository.findByDeviceId(deviceId);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found.");
        }

        UserProfileValidator.validateRequiredProfileFields(newName, newEmail);
        UserProfileValidator.validateOptionalPhone(newPhoneNumber);

        existingUser.setName(newName.trim());
        existingUser.setEmail(newEmail.trim());
        existingUser.setPhoneNumber(normalizeOptional(newPhoneNumber));
        existingUser.setProfilePictureURL(normalizeOptional(newProfilePictureURL));

        userRepository.save(existingUser);
        return existingUser;
    }

    /**
     * US 01.02.03
     * Record one event status for a user.
     * Example: WAITLISTED, SELECTED, NOT_SELECTED, REGISTERED, CANCELLED.
     */
    public void addEventHistory(String deviceId, Event event, Status status) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required.");
        }
        if (!userRepository.exists(deviceId)) {
            throw new IllegalArgumentException("User not found.");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Selection status cannot be null.");
        }

        UserEventHistoryRecord record = new UserEventHistoryRecord(event, status);
        historyRepository.addRecord(deviceId, record);
    }
    /**
    * Optional helper:
    * update a user's event status if the event already exists in history;
    * otherwise add a new history record.
    */
    public void upsertEventHistory(String deviceId, Event event, Status newStatus) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required.");
        }
        if (!userRepository.exists(deviceId)) {
            throw new IllegalArgumentException("User not found.");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Selection status cannot be null.");
        }

        List<UserEventHistoryRecord> records = historyRepository.getHistory(deviceId);
        for (UserEventHistoryRecord record : records) {
            if (record.getEvent() == event ||
                    (record.getEvent() != null
                            && record.getEvent().getName() != null
                            && record.getEvent().getName().equals(event.getName()))) {
                record.setStatus(newStatus);
                return;
            }
        }

        addEventHistory(deviceId, event, newStatus);
    }
    /**
     * US 01.02.03
     * Get the user's full event history.
     */
    public List<UserEventHistoryRecord> getUserEventHistory(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required.");
        }
        if (!userRepository.exists(deviceId)) {
            throw new IllegalArgumentException("User not found.");
        }

        return historyRepository.getHistory(deviceId);
    }

    /**
     * US 01.02.04
     * Delete user profile and remove user from all provided events/history.
     */
    public void deleteUserProfile(String deviceId, List<Event> allEvents) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required.");
        }

        User user = userRepository.findByDeviceId(deviceId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        removeUserFromAllEvents(user, allEvents);
        historyRepository.clearHistory(deviceId);
        userRepository.delete(deviceId);
    }

    private void removeUserFromAllEvents(User user, List<Event> allEvents) {
        if (allEvents == null) {
            return;
        }

        for (Event event : allEvents) {
            if (event == null) {
                continue;
            }

            removeUserFromList(user, event.getEntrants());
            removeUserFromList(user, event.getAttendees());
        }
    }

    private void removeUserFromList(User user, ArrayList<User> users) {
        if (users == null) {
            return;
        }

        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User current = iterator.next();
            if (current != null && sameUser(current, user)) {
                iterator.remove();
            }
        }
    }

    private boolean sameUser(User a, User b) {
        if (a == null || b == null) {
            return false;
        }
        return a.getDeviceID() != null
                && b.getDeviceID() != null
                && a.getDeviceID().equals(b.getDeviceID());
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}