package com.example.cipher_events.database;

/*
 * Represents an organizer.
 * Inherits all fields from User.
 */

public class Organizer extends User {

    // Constructor; pass null for optional fields if not provided
    public Organizer(String name, String email, String password, String phoneNumber, String profilePictureURL) {
        super(name, email, password, phoneNumber, profilePictureURL);
    }

    public Organizer() {
        super();
    }

    // String representation for debugging purposes
    @Override
    public String toString() {
        return "Organizer{" +
                "deviceID='" + getDeviceID() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", profilePictureURL='" + getProfilePictureURL() + '\'' +
                '}';
    }
}
