package com.example.cipher_events.admin;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

import java.util.ArrayList;
import java.util.List;

public class AdminModerationService {

    private final DBProxy db;

    public AdminModerationService() {
        this.db = DBProxy.getInstance();
    }

    public AdminModerationService(DBProxy db) {
        this.db = db;
    }

    // US 03.04.01
    public List<Event> browseAllEvents() {
        ArrayList<Event> events = db.getAllEvents();
        return events == null ? new ArrayList<>() : new ArrayList<>(events);
    }

    // US 03.05.01
    public List<User> browseAllProfiles() {
        ArrayList<User> users = db.getAllUsers();
        return users == null ? new ArrayList<>() : new ArrayList<>(users);
    }

    // Optional helper if you want admin to also browse organizers separately
    public List<Organizer> browseAllOrganizers() {
        ArrayList<Organizer> organizers = db.getAllOrganizers();
        return organizers == null ? new ArrayList<>() : new ArrayList<>(organizers);
    }

    // US 03.01.01
    public void removeEvent(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID is required.");
        }

        Event event = db.getEvent(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        db.deleteEvent(eventId);
    }

    // US 03.02.01
    public void removeProfile(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile ID is required.");
        }

        User user = db.getUser(deviceId);
        if (user == null) {
            throw new IllegalArgumentException("User profile not found.");
        }

        db.deleteUser(deviceId);
    }

    // US 03.07.01
    public void removeOrganizer(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Organizer ID is required.");
        }

        Organizer organizer = db.getOrganizer(deviceId);
        if (organizer == null) {
            throw new IllegalArgumentException("Organizer not found.");
        }

        db.deleteOrganizer(deviceId);
    }
}