package com.example.cipher_events.database;

import java.util.ArrayList;
import java.util.List;

// DBProxy acts as the single point of access for all database operations.
public class DBProxy {
    private static DBProxy instance = null;
    private UserDB userDB;
    private EventDB eventDB;
    private OrganizerDB organizerDB;
    private AdminDB adminDB;
    private User currentUser;

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Immediate notification when setting user manually
        notifyListeners();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String login(String email, String password) {
        for (Admin admin : getAllAdmins()) {
            if (email.equals(admin.getEmail()) && password.equals(admin.getPassword())) {
                this.setCurrentUser(admin);
                return "ADMIN";
            }
        }

        for (Organizer org : getAllOrganizers()) {
            if (email.equals(org.getEmail()) && password.equals(org.getPassword())) {
                this.setCurrentUser(org);
                return "ORGANIZER";
            }
        }

        for (User user : getAllUsers()) {
            if (email.equals(user.getEmail()) && password.equals(user.getPassword())) {
                this.setCurrentUser(user);
                return "ENTRANT";
            }
        }

        return null;
    }

    private final List<OnDataChangedListener> listeners = new ArrayList<>();

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

    public void addListener(OnDataChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnDataChangedListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners() {
        if (currentUser != null) {
            String deviceID = currentUser.getDeviceID();
            User updated = null;
            
            if (currentUser instanceof Admin) updated = getAdmin(deviceID);
            else if (currentUser instanceof Organizer) updated = getOrganizer(deviceID);
            else updated = getUser(deviceID);
            
            if (updated == null) updated = getAdmin(deviceID);
            if (updated == null) updated = getOrganizer(deviceID);
            if (updated == null) updated = getUser(deviceID);
            
            if (updated != null) {
                if (this.currentUser != updated) {
                    this.currentUser = updated;
                }
            }
        }

        for (OnDataChangedListener listener : new ArrayList<>(listeners)) {
            listener.onDataChanged();
        }
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

    // --- Events ---
    public void addEvent(Event event) {
        if (event.getOrganizer() == null && currentUser instanceof Organizer) {
            event.setOrganizer((Organizer) currentUser);
        }
        eventDB.add(event);
    }
    public Event getEvent(String eventID) { return eventDB.get(eventID); }
    public ArrayList<Event> getAllEvents() { return eventDB.getAll(); }
    public void updateEvent(Event event) { eventDB.update(event); }
    public void deleteEvent(Event event) { eventDB.delete(event); }
    public void deleteEvent(String eventID) { eventDB.delete(eventID); }

    // --- Users ---
    public void addUser(User user) { userDB.add(user); }
    public User getUser(String deviceID) { return userDB.get(deviceID); }
    public ArrayList<User> getAllUsers() { return userDB.getAll(); }
    public List<User> searchUsers(String keyword) { return userDB.search(keyword); }
    
    public void updateUser(User user) {
        if (user instanceof Admin) {
            adminDB.update((Admin) user);
        } else if (user instanceof Organizer) {
            organizerDB.update((Organizer) user);
        } else {
            userDB.update(user);
        }
    }
    
    public void deleteUser(User user) { userDB.delete(user); }
    public void deleteUser(String deviceID) { userDB.delete(deviceID); }

    // --- Organizers ---
    public void addOrganizer(Organizer organizer) { organizerDB.add(organizer); }
    public Organizer getOrganizer(String deviceID) { return organizerDB.get(deviceID); }
    public ArrayList<Organizer> getAllOrganizers() { return organizerDB.getAll(); }
    public void updateOrganizer(Organizer organizer) { organizerDB.update(organizer); }
    public void deleteOrganizer(Organizer organizer) { organizerDB.delete(organizer); }
    public void deleteOrganizer(String deviceID) { organizerDB.delete(deviceID); }

    // --- Admins ---
    public void addAdmin(Admin admin) { adminDB.add(admin); }
    public Admin getAdmin(String deviceID) { return adminDB.get(deviceID); }
    public ArrayList<Admin> getAllAdmins() { return adminDB.getAll(); }
    public void updateAdmin(Admin admin) { adminDB.update(admin); }
    public void deleteAdmin(Admin admin) { adminDB.delete(admin); }
    public void deleteAdmin(String deviceID) { adminDB.delete(deviceID); }
}
