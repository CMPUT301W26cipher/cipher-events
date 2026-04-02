package com.example.cipher_events.admin;

import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

/**
 * US 03.09.01
 * Admin can also be an organizer and/or entrant using the same admin profile.
 */
public class AdminRoleService {

    private final DBProxy db;

    public AdminRoleService() {
        this.db = DBProxy.getInstance();
    }

    public AdminRoleService(DBProxy db) {
        this.db = db;
    }

    public User enableEntrantRole(String adminDeviceID) {
        Admin admin = requireAdmin(adminDeviceID);

        User existingUser = db.getUser(admin.getDeviceID());
        if (existingUser != null) {
            syncUserFromAdmin(existingUser, admin);
            db.updateUser(existingUser);
            return existingUser;
        }

        User user = new User();
        user.setDeviceID(admin.getDeviceID());
        user.setName(admin.getName());
        user.setEmail(admin.getEmail());
        user.setPassword(admin.getPassword());
        user.setPhoneNumber(admin.getPhoneNumber());
        user.setProfilePictureURL(admin.getProfilePictureURL());

        db.addUser(user);
        return user;
    }

    public Organizer enableOrganizerRole(String adminDeviceID) {
        Admin admin = requireAdmin(adminDeviceID);

        Organizer existingOrganizer = db.getOrganizer(admin.getDeviceID());
        if (existingOrganizer != null) {
            syncOrganizerFromAdmin(existingOrganizer, admin);
            db.updateOrganizer(existingOrganizer);
            return existingOrganizer;
        }

        Organizer organizer = new Organizer();
        organizer.setDeviceID(admin.getDeviceID());
        organizer.setName(admin.getName());
        organizer.setEmail(admin.getEmail());
        organizer.setPassword(admin.getPassword());
        organizer.setPhoneNumber(admin.getPhoneNumber());
        organizer.setProfilePictureURL(admin.getProfilePictureURL());

        db.addOrganizer(organizer);
        return organizer;
    }

    /**
     * Remove entrant role only. Admin profile remains.
     */
    public void disableEntrantRole(String adminDeviceID) {
        Admin admin = requireAdmin(adminDeviceID);

        User user = db.getUser(admin.getDeviceID());
        if (user != null) {
            db.deleteUser(user.getDeviceID());
        }
    }

    /**
     * Remove organizer role only. Admin profile remains.
     */
    public void disableOrganizerRole(String adminDeviceID) {
        Admin admin = requireAdmin(adminDeviceID);

        Organizer organizer = db.getOrganizer(admin.getDeviceID());
        if (organizer != null) {
            db.deleteOrganizer(organizer.getDeviceID());
        }
    }

    public boolean hasEntrantRole(String adminDeviceID) {
        Admin admin = requireAdmin(adminDeviceID);
        return db.getUser(admin.getDeviceID()) != null;
    }

    public boolean hasOrganizerRole(String adminDeviceID) {
        Admin admin = requireAdmin(adminDeviceID);
        return db.getOrganizer(admin.getDeviceID()) != null;
    }

    private Admin requireAdmin(String adminDeviceID) {
        if (adminDeviceID == null || adminDeviceID.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin device ID cannot be empty.");
        }

        Admin admin = db.getAdmin(adminDeviceID.trim());
        if (admin == null) {
            throw new IllegalArgumentException("Admin profile not found.");
        }

        return admin;
    }

    private void syncUserFromAdmin(User user, Admin admin) {
        user.setName(admin.getName());
        user.setEmail(admin.getEmail());
        user.setPassword(admin.getPassword());
        user.setPhoneNumber(admin.getPhoneNumber());
        user.setProfilePictureURL(admin.getProfilePictureURL());
    }

    private void syncOrganizerFromAdmin(Organizer organizer, Admin admin) {
        organizer.setName(admin.getName());
        organizer.setEmail(admin.getEmail());
        organizer.setPassword(admin.getPassword());
        organizer.setPhoneNumber(admin.getPhoneNumber());
        organizer.setProfilePictureURL(admin.getProfilePictureURL());
    }
}