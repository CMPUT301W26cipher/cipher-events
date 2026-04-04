package com.example.cipher_events.waitinglist;
import java.util.Random;

import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.user.Status;
import com.example.cipher_events.user.UserEventHistoryRecord;
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.notifications.NotificationService;

import java.util.ArrayList;
import java.util.List;

import com.example.cipher_events.database.DBProxy;

/**
 * Service handling waiting list operations.
 * Waiting list is represented by Event.entrants.
 */
public class WaitingListService {

    private final UserEventHistoryRepository historyRepository;
    private final NotificationService notificationService;

    private final DBProxy db = DBProxy.getInstance();

    public WaitingListService(UserEventHistoryRepository historyRepository,
                              NotificationService notificationService) {
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
    }
    public WaitingListService(UserEventHistoryRepository historyRepository) {
        this(historyRepository, null); // old structure, kept for compatibility
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

        if (!isWithinRegistrationPeriod(event)) {
            return false;
        }

        if (!event.isPublicEvent()) { // for private event

            // only allow if user was invited
            if (event.getInvitedEntrants() == null ||
                    !containsUser(event.getInvitedEntrants(), user)) {
                return false;
            }
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
        db.updateEvent(event);
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
                db.updateEvent(event);
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
        db.updateEvent(event);
    }

    // =========================================================
    // US 02.07.01
    // Notify Waiting List
    // =========================================================
    public void notifyAllEntrants(Event event, String message) {

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (notificationService == null) {
            return; // safety guard for tests or misconfigured service
        }

        if (event.getEntrants() == null || event.getEntrants().isEmpty()) {
            return;
        }

        String title = "Event Update: " + event.getName();

        notificationService.notifyUsers(
                event.getEntrants(),
                title,
                message
        );
    }

    // =========================================================
    // US 02.01.03
    // Invite user to event
    // =========================================================
    public boolean inviteUser(User user, Event event) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        if (isUserInWaitingList(user, event)) {
            return false;
        }

        if (event.getInvitedEntrants() == null) {
            event.setInvitedEntrants(new ArrayList<>());
        }

        ArrayList<User> invited = event.getInvitedEntrants();

        // check duplicate
        for (User u : invited) {
            if (sameUser(u, user)) {
                return false;
            }
        }

        invited.add(user);
        db.updateEvent(event);

        //  send notification
        if (notificationService != null) {
            notificationService.notifyUser(
                    user,
                    "Event Invitation",
                    "You have been invited to: " + event.getName()
            );
        }

        return true;
    }
    // =========================================================
    // US 02.01.03
    // Search users to invite to a private event by keyword
    // =========================================================
    public List<User> searchUsersToInvite(String keyword) {
        return db.searchUsers(keyword);
    }

    private boolean isWithinRegistrationPeriod(Event event) {

        Long open = event.getRegistrationOpenTime();
        Long close = event.getRegistrationCloseTime();

        long now = System.currentTimeMillis();

        if (open != null && now < open) {
            return false;
        }

        if (close != null && now > close) {
            return false;
        }

        return true;
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

        if (user == null || event == null) {
            throw new IllegalArgumentException("User/Event cannot be null.");
        }

        ArrayList<User> invited = event.getInvitedEntrants();

        if (invited == null) {
            return false;
        }

        if (event.getAttendees() == null) {
            event.setAttendees(new ArrayList<>());
        }

        for (int i = 0; i < invited.size(); i++) {
            User current = invited.get(i);

            if (sameUser(current, user)) {

                invited.remove(i);

                if (event.getEntrants() == null) {
                    event.setEntrants(new ArrayList<>());
                }

                if (!containsUser(event.getEntrants(), user)) {
                    event.getEntrants().add(user);
                }
                db.updateEvent(event);
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

        if (user == null || event == null) {
            throw new IllegalArgumentException("User/Event cannot be null.");
        }

        ArrayList<User> invited = event.getInvitedEntrants();
        ArrayList<User> cancelled = event.getCancelledEntrants();

        if (invited == null) {
            return false;
        }

        if (cancelled == null) {
            event.setCancelledEntrants(new ArrayList<>());
            cancelled = event.getCancelledEntrants();
        }

        for (int i = 0; i < invited.size(); i++) {

            User current = invited.get(i);

            if (sameUser(current, user)) {

                invited.remove(i);
                cancelled.add(user);
                drawReplacementEntrant(event);
                db.updateEvent(event);
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
        ArrayList<User> invited = event.getInvitedEntrants();

        if (entrants == null || entrants.isEmpty()) {
            return null;
        }

        if (invited == null) {
            event.setInvitedEntrants(new ArrayList<>());
            invited = event.getInvitedEntrants();
        }

        Random random = new Random();
        int index = random.nextInt(entrants.size());

        // trigger redraw
        User replacement = entrants.get(index);

        // move entrant → invited
        invited.add(replacement);
        entrants.remove(index);
        db.updateEvent(event);

        // notify ONLY this user
        if (notificationService != null) {
            notificationService.notifyUser(
                    replacement,
                    "You're Selected!",
                    "You have been selected for: " + event.getName()
            );
        }

        return replacement;
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
                db.updateEvent(event);
                return true;
            }
        }
        return false;
    }

    // =========================================================
    // US 02.05.02
    // Draw Lottery Winners
    // =========================================================
    public List<User> drawLotteryWinners(Event event, int n) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("N must be greater than 0.");
        }

        ArrayList<User> entrants = event.getEntrants();
        if (entrants == null || entrants.isEmpty()) {
            return new ArrayList<>();
        }

        if (event.getInvitedEntrants() == null) {
            event.setInvitedEntrants(new ArrayList<>());
        }

        ArrayList<User> invited = event.getInvitedEntrants();
        ArrayList<User> selected = new ArrayList<>();
        Random random = new Random();

        int selectCount = Math.min(n, entrants.size());

        for (int i = 0; i < selectCount; i++) {
            int index = random.nextInt(entrants.size());
            User winner = entrants.remove(index);

            invited.add(winner);
            selected.add(winner);

            if (notificationService != null) {
                notificationService.notifyUser(
                        winner,
                        "You're Selected!",
                        "You have been selected for: " + event.getName()
                );
            }
        }

        if (notificationService != null) {
            for (User user : entrants) {
                notificationService.notifyUser(
                        user,
                        "Not Selected",
                        "You were not selected for: " + event.getName()
                );
            }
        }
        db.updateEvent(event);
        return selected;
    }

    // =========================================================
    // US 02.05.03
    // Draw Replacement Entrant
    // =========================================================
    public User drawReplacementEntrant(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        ArrayList<User> entrants = event.getEntrants();
        ArrayList<User> invited = event.getInvitedEntrants();

        if (entrants == null || entrants.isEmpty()) {
            return null;
        }

        // Filter out anyone already invited or previously selected
        ArrayList<User> pool = new ArrayList<>();
        for (User u : entrants) {
            boolean alreadyInvited = invited != null && containsUser(invited, u);
            if (!alreadyInvited) {
                pool.add(u);
            }
        }

        if (pool.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int index = random.nextInt(pool.size());
        User replacement = pool.get(index);

        // Add to invited, remove from entrants
        if (invited == null) {
            event.setInvitedEntrants(new ArrayList<>());
        }
        event.getInvitedEntrants().add(replacement);
        entrants.remove(replacement);
        db.updateEvent(event);

        if (notificationService != null) {
            notificationService.notifyUser(
                    replacement,
                    "You're Selected!",
                    "You have been selected (replacement) for: " + event.getName()
            );
        }

        return replacement;
    }

    // =========================================================
    // US 02.06.05
    // Export Final Enrolled List (CSV)
    // =========================================================
    public String exportEnrolledListAsCsv(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        ArrayList<User> enrolled = event.getEnrolledEntrants();
        if (enrolled == null || enrolled.isEmpty()) {
            return "name,email,phone\n";
        }

        StringBuilder csv = new StringBuilder();
        csv.append("name,email,phone\n");

        for (User user : enrolled) {
            csv.append(user.getName() == null ? "" : user.getName()).append(",");
            csv.append(user.getEmail() == null ? "" : user.getEmail()).append(",");
            csv.append(user.getPhoneNumber() == null ? "" : user.getPhoneNumber()).append("\n");
        }

        return csv.toString();
    }

    private boolean containsUser(ArrayList<User> users, User target) {
        for (User u : users) {
            if (sameUser(u, target)) return true;
        }
        return false;
    }

}