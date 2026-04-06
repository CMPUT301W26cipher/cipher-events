package com.example.cipher_events.database;

/*
 * Represents an admin.
 */

public class Admin extends User {

    // Constructor; pass null for optional fields if not provided
    public Admin(String name, String email, String password, String phoneNumber, String profilePictureURL) {
        super(name, email, password, phoneNumber, profilePictureURL);
    }

    public Admin() {
        super();
    }

    // String representation for debugging purposes
    @Override
    public String toString() {
        return "Admin{" +
                "deviceID='" + getDeviceID() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", profilePictureURL='" + getProfilePictureURL() + '\'' +
                '}';
    }
}
