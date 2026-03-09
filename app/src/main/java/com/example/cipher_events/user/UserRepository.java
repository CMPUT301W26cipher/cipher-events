package com.example.cipher_events.user;

import com.example.cipher_events.database.User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-memory repository for users.
 * Replace with Firestore/DB code later if needed.
 */
public class UserRepository {
    private final Map<String, User> usersByDeviceId;

    public UserRepository() {
        this.usersByDeviceId = new HashMap<>();
    }

    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        usersByDeviceId.put(user.getDeviceID(), user);
    }

    public User findByDeviceId(String deviceId) {
        return usersByDeviceId.get(deviceId);
    }

    public boolean exists(String deviceId) {
        return usersByDeviceId.containsKey(deviceId);
    }

    public void delete(String deviceId) {
        usersByDeviceId.remove(deviceId);
    }

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(usersByDeviceId.values());
    }
}