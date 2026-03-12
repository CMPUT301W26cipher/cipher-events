package com.example.cipher_events.database;

import java.util.ArrayList;

// DBProxy acts as the single point of access for all database operations.
// DBProxy is a singleton. Call DBProxy.getInstance() to get its instance.

// Relevant methods:
// addUser(), addEvent(), addOrganizer(), addAdmin()
// getUser(), getEvent(), getOrganizer(), getAdmin()
// getAllUsers(), getAllEvents(), getAllOrganizers(), getAllAdmins()
// updateUser(), updateEvent(), updateOrganizer(), updateAdmin()
// deleteUser(), deleteEvent(), deleteOrganizer(), deleteAdmin()

// Example usage:
// DBProxy db = DBProxy.getInstance();          // setup DBProxy
//
// User user = new User("John Doe", "john@gmail.com", "password123", null, null)
// db.addUser(user);                            // add a user
//
// String deviceID = user.getDeviceID();
// user = db.getUser(deviceID);                 // get user by deviceID
//
// ArrayList<User> users = db.getAllUsers();    // get all
//
// user.setName("John Cena");
// db.updateUser(user);                         // update using object
//
// db.deleteUser(user);                         // delete using object
// db.deleteUser(deviceID);                     // delete using deviceID

// TODO : testing
// TODO : javadoc
// TODO : logging requests
public class DBProxy {
    private static DBProxy instance = null;
    private UserDB userDB;
    private EventDB eventDB;
    private OrganizerDB organizerDB;
    private AdminDB adminDB;

    private DBProxy() {
        userDB = UserDB.getInstance();
        eventDB = EventDB.getInstance();
        organizerDB = OrganizerDB.getInstance();
        adminDB = AdminDB.getInstance();
        start();
    }

    public static DBProxy getInstance() {
        if (instance == null) {
            instance = new DBProxy();
        }
        return instance;
    }

    private void start() {
        userDB.startListener();
        eventDB.startListener();
        organizerDB.startListener();
        adminDB.startListener();
    }

    public void shutdown() {
        userDB.stopListener();
        eventDB.stopListener();
        organizerDB.stopListener();
        adminDB.stopListener();
    }

    public void addUser(User user) {
        userDB.add(user);
    }

    public void addEvent(Event event) {
        eventDB.add(event);
    }

    public void addOrganizer(Organizer organizer) {
        organizerDB.add(organizer);
    }

    public void addAdmin(Admin admin) {
        adminDB.add(admin);
    }


    public User getUser(String deviceID) {
        return userDB.get(deviceID);
    }

    public Event getEvent(String eventID) {
        return eventDB.get(eventID);
    }

    public Organizer getOrganizer(String deviceID) {
        return organizerDB.get(deviceID);
    }

    public Admin getAdmin(String deviceID) {
        return adminDB.get(deviceID);
    }

    public ArrayList<User> getAllUsers() {
        return userDB.getAll();
    }

    public ArrayList<Event> getAllEvents() {
        return eventDB.getAll();
    }

    public ArrayList<Organizer> getAllOrganizers() {
        return organizerDB.getAll();
    }

    public ArrayList<Admin> getAllAdmins() {
        return adminDB.getAll();
    }

    public void updateUser(User user) {
        userDB.update(user);
    }

    public void updateEvent(Event event) {
        eventDB.update(event);
    }

    public void updateOrganizer(Organizer organizer) {
        organizerDB.update(organizer);
    }

    public void updateAdmin(Admin admin) {
        adminDB.update(admin);
    }

    public void deleteUser(User user) {
        userDB.delete(user);
    }

    public void deleteUser(String deviceID) {
        userDB.delete(deviceID);
    }

    public void deleteEvent(Event event) {
        eventDB.delete(event);
    }

    public void deleteEvent(String eventID) {
        eventDB.delete(eventID);
    }

    public void deleteOrganizer(Organizer organizer) {
        organizerDB.delete(organizer);
    }

    public void deleteOrganizer(String deviceID) {
        organizerDB.delete(deviceID);
    }

    public void deleteAdmin(Admin admin) {
        adminDB.delete(admin);
    }

    public void deleteAdmin(String deviceID) {
        adminDB.delete(deviceID);
    }
}
