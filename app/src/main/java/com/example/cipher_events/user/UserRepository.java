package com.example.cipher_events.user;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Database-backed user repository wrapper.
 */
public class UserRepository {
    private final DBProxy db;

    public UserRepository() {
        this.db = DBProxy.getInstance();
    }

    public UserRepository(DBProxy db) {
        this.db = db;
    }

    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        User existing = db.getUser(user.getDeviceID());
        if (existing == null) {
            db.addUser(user);
        } else {
            db.updateUser(user);
        }
    }

    public User findByDeviceId(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return null;
        }
        return db.getUser(deviceId.trim());
    }

    public boolean exists(String deviceId) {
        return findByDeviceId(deviceId) != null;
    }

    public void delete(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return;
        }
        db.deleteUser(deviceId.trim());
    }

    public Collection<User> getAllUsers() {
        ArrayList<User> users = db.getAllUsers();
        if (users == null) {
            return Collections.emptyList();
        }
        return users;
    }
}