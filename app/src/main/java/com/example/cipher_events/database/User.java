package com.example.cipher_events.database;

import java.util.UUID;

/*
 * Represents a user.
 * Each user has a name, email, password, phone number, and profile picture URL.
 */

public class User {
    private String deviceID;
    private String name;
    private String email;
    private String password;
    private String phoneNumber; // optional phone number
    private String profilePictureURL; // optional profile picture

    // Constructor; pass null for optional fields if not provided
    public User(String name, String email, String password, String phoneNumber, String profilePictureURL) {
        this.deviceID = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.profilePictureURL = profilePictureURL;
    }

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