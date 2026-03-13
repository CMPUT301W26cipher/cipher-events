package com.example.cipher_events.waitinglist;
import java.util.Random;

import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.user.Status;
import com.example.cipher_events.user.UserEventHistoryRecord;
import com.example.cipher_events.user.UserEventHistoryRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service handling waiting list operations.
 * Waiting list is represented by Event.entrants.
 */
public class WaitingListService {

    private final UserEventHistoryRepository historyRepository;

    public WaitingListService(UserEventHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    // =========================================================
    // US 01.01.01
    // Join Waiting List
    // =========================================================
    public boolean joinWaitingList(User user, Event event) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        ArrayList<User> entrants = event.getEntrants();

        if (entrants == null) {
            entrants = new ArrayList<>();
            event.setEntrants(entrants);
        }

        if (isUserInWaitingList(user, event)) {
            return false;
        }

        Integer capacity = event.getWaitingListCapacity();

        if (capacity != null && entrants.size() >= capacity) {
            return false;
        }

        entrants.add(user);

        historyRepository.getHistory(
                user.getDeviceID()
        );

        return true;
    }

    // =========================================================
    // US 01.01.02
    // Leave Waiting List
    // =========================================================
    public boolean leaveWaitingList(User user, Event event) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        ArrayList<User> entrants = event.getEntrants();

        if (entrants == null) {
            return false;
        }

        for (int i = 0; i < entrants.size(); i++) {

            User current = entrants.get(i);

            if (sameUser(current, user)) {

                entrants.remove(i);

                historyRepository.getHistory(
                        user.getDeviceID()
                );

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // US 02.02.01
    // View Waiting List
    // =========================================================
    public List<User> getWaitingList(Event event) {

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (event.getEntrants() == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(event.getEntrants()); //return copy
    }

    // =========================================================
    // US 01.05.04
    // View Waiting List Count
    // =========================================================
    public int getWaitingListCount(Event event) {

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (event.getEntrants() == null) {
            return 0;
        }

        return event.getEntrants().size();
    }

    // =========================================================
    // US 02.03.01
    // Set Waiting List Capacity
    // =========================================================
    public void setWaitingListCapacity(Event event, Integer capacity) {

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (capacity != null && capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative.");
        }

        event.setWaitingListCapacity(capacity);
    }

    // =========================================================
    // US 02.07.01
    // Notify Waiting List (Placeholder)
    // =========================================================
    public void notifyAllEntrants(Event event, String message) {

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (event.getEntrants() == null) {
            return;
        }

        for (User user : event.getEntrants()) {

            System.out.println(
                    "Notify user: "
                            + user.getName()
                            + " Message: "
                            + message
            );
        }

        // TODO US 02.07.01
        // Replace with Firebase Cloud Messaging later
    }

    private boolean isUserInWaitingList(User user, Event event) {

        ArrayList<User> entrants = event.getEntrants();

        if (entrants == null) {
            return false;
        }

        for (User entrant : entrants) {

            if (sameUser(user, entrant)) {
                return true;
            }
        }

        return false;
    }

    private boolean sameUser(User a, User b) {

        if (a == null || b == null) {
            return false;
        }

        if (a.getDeviceID() == null || b.getDeviceID() == null) {
            return false;
        }

        return a.getDeviceID().equals(b.getDeviceID());
    }
    // =========================================================
// US 01.05.02
// Accept Invitation
// =========================================================
    public boolean acceptInvitation(User user, Event event) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        ArrayList<User> entrants = event.getEntrants();
        ArrayList<User> attendees = event.getAttendees();

        if (entrants == null || attendees == null) {
            return false;
        }

        for (int i = 0; i < entrants.size(); i++) {

            User current = entrants.get(i);

            if (sameUser(current, user)) {

                entrants.remove(i);
                attendees.add(user);

                return true;
            }
        }

        return false;
    }
    // =========================================================
// US 01.05.03
// Decline Invitation
// =========================================================
    public boolean declineInvitation(User user, Event event) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        ArrayList<User> entrants = event.getEntrants();

        if (entrants == null) {
            return false;
        }

        for (int i = 0; i < entrants.size(); i++) {

            User current = entrants.get(i);

            if (sameUser(current, user)) {

                entrants.remove(i);

                return true;
            }
        }

        return false;
    }
    // =========================================================
// US 01.05.01
// Re-draw Lottery
// =========================================================
    public User redrawLottery(Event event) {

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        ArrayList<User> entrants = event.getEntrants();

        if (entrants == null || entrants.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int index = random.nextInt(entrants.size());

        return entrants.get(index);
    }

    // =========================================================
    // US 02.06.01
    // View Invited Entrants List
    // =========================================================
    public List<User> getInvitedEntrants(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }
        if (event.getInvitedEntrants() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(event.getInvitedEntrants());
    }

    // =========================================================
    // US 02.06.02
    // View Cancelled Entrants List
    // =========================================================
    public List<User> getCancelledEntrants(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }
        if (event.getCancelledEntrants() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(event.getCancelledEntrants());
    }

    // =========================================================
    // US 02.06.03
    // View Final Enrolled List
    // =========================================================
    public List<User> getEnrolledEntrants(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }
        if (event.getEnrolledEntrants() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(event.getEnrolledEntrants());
    }

    // =========================================================
    // US 02.06.04
    // Cancel Non-Responsive Entrants
    // =========================================================
    public boolean markAsNoShow(User user, Event event) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        ArrayList<User> invited = event.getInvitedEntrants();
        if (invited == null) {
            return false;
        }

        for (int i = 0; i < invited.size(); i++) {
            User current = invited.get(i);
            if (sameUser(current, user)) {
                invited.remove(i);
                event.getCancelledEntrants().add(user);
                historyRepository.getHistory(
                        user.getDeviceID()
                );
                return true;
            }
        }
        return false;
    }



}