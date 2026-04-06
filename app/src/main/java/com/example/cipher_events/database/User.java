package com.example.cipher_events.database;

import java.util.ArrayList;
import java.util.UUID;


/*
 * Represents a user.
 * Each user has a name, email, password, phone number, and profile picture URL.
 * The deviceID field is used as a unique identifier (UID) for the user in the database.
 */

public class User {
    private String deviceID; // Unique User ID
    private String name;
    private String email;
    private String password;
    private String phoneNumber; // optional phone number
    private String profilePictureURL; // optional profile picture

    private boolean notificationsEnabled = true;
    private ArrayList<String> favoriteEventIds = new ArrayList<>();
    private ArrayList<String> hiddenThreadIds = new ArrayList<>();

    private boolean organizerRole;
    private boolean entrantRole;

    // Constructor; pass null for optional fields if not provided
    public User(String name, String email, String password, String phoneNumber, String profilePictureURL) {
        this.deviceID = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.profilePictureURL = profilePictureURL;
    }

    public User() {};

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
    }

    public ArrayList<String> getFavoriteEventIds() {
        if (favoriteEventIds == null) {
            favoriteEventIds = new ArrayList<>();
        }
        return favoriteEventIds;
    }

    public void setFavoriteEventIds(ArrayList<String> favoriteEventIds) {
        this.favoriteEventIds = favoriteEventIds;
    }

    public void addFavoriteEvent(String eventId) {
        if (favoriteEventIds == null) {
            favoriteEventIds = new ArrayList<>();
        }
        if (!favoriteEventIds.contains(eventId)) {
            favoriteEventIds.add(eventId);
        }
    }

    public void removeFavoriteEvent(String eventId) {
        if (favoriteEventIds != null) {
            favoriteEventIds.remove(eventId);
        }
    }

    public boolean isFavorite(String eventId) {
        return favoriteEventIds != null && favoriteEventIds.contains(eventId);
    }

    public ArrayList<String> getHiddenThreadIds() {
        if (hiddenThreadIds == null) {
            hiddenThreadIds = new ArrayList<>();
        }
        return hiddenThreadIds;
    }

    public void setHiddenThreadIds(ArrayList<String> hiddenThreadIds) {
        this.hiddenThreadIds = hiddenThreadIds;
    }

    public void hideThread(String threadId) {
        if (!getHiddenThreadIds().contains(threadId)) {
            getHiddenThreadIds().add(threadId);
        }
    }

    public void showThread(String threadId) {
        getHiddenThreadIds().remove(threadId);
    }

    public boolean isThreadHidden(String threadId) {
        return getHiddenThreadIds().contains(threadId);
    }

    public boolean hasOrganizerRole() {
        return organizerRole;
    }

    public void setOrganizerRole(boolean organizerRole) {
        this.organizerRole = organizerRole;
    }

    public boolean hasEntrantRole() {
        return entrantRole;
    }

    public void setEntrantRole(boolean entrantRole) {
        this.entrantRole = entrantRole;
    }

    // String representation for debugging purposes
    @Override
    public String toString() {
        return "User{" +
                "deviceID='" + deviceID + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profilePictureURL='" + profilePictureURL + '\'' +
                '}';
    }
}
