package com.example.cipher_events.database;

import com.example.cipher_events.comment.EventComment;
import com.example.cipher_events.message.MessageThread;

import java.util.ArrayList;
import java.util.UUID;

/*
 * Represents an event.
 * Each event has a name, description, time, location, organizer, entrants, attendees, and a poster picture URL.
 */

public class Event {
    private String eventID;
    private String name;
    private String description;
    private String time;
    private String location;
    private Organizer organizer;
    private ArrayList<User> entrants;
    private ArrayList<User> attendees;
    private String posterPictureURL; // optional poster picture
    private Integer waitingListCapacity; // Optional waiting list capacity (null means unlimited)

    private ArrayList<User> invitedEntrants;
    private ArrayList<User> cancelledEntrants;
    private ArrayList<User> enrolledEntrants;

    private ArrayList<EventComment> comments;
    private ArrayList<String> coOrganizerIds;
    private ArrayList<String> pendingCoOrganizerIds;
    private ArrayList<String> tags;
    private boolean publicEvent;
    private Long registrationOpenTime;
    private Long registrationCloseTime;

    private ArrayList<MessageThread> messageThreads;

    public Event() {
    }

    // Constructor
    // pass empty lists for entrants and attendees if not provided
    // pass null for optional fields if not provided
    public Event(String name,
                 String description,
                 String time,
                 String location,
                 Organizer organizer,
                 ArrayList<User> entrants,
                 ArrayList<User> attendees,
                 String posterPictureURL,
                 boolean publicEvent,
                 ArrayList<MessageThread> messageThreads) {
        this.eventID = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.time = time;
        this.location = location;
        this.organizer = organizer;
        this.entrants = entrants;
        this.attendees = attendees;
        this.posterPictureURL = posterPictureURL;
        this.publicEvent = publicEvent;
        this.waitingListCapacity = null; // default unlimited

        this.invitedEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        this.enrolledEntrants = new ArrayList<>();

        this.comments = new ArrayList<>();
        this.coOrganizerIds = new ArrayList<>();
        this.pendingCoOrganizerIds = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.messageThreads = messageThreads;
    }


    public Event(String name, String description, String time, String location,
                 Organizer organizer, ArrayList<User> entrants,
                 ArrayList<User> attendees, String posterPictureURL, boolean publicEvent) {

        this(name, description, time, location,
                organizer, entrants, attendees, posterPictureURL, publicEvent, new ArrayList<>());
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    public String getOrganizerID() {
        return (organizer != null) ? organizer.getDeviceID() : null;
    }

    public ArrayList<User> getEntrants() {
        return entrants;
    }

    public void setEntrants(ArrayList<User> entrants) {
        this.entrants = entrants;
    }

    public ArrayList<User> getAttendees() {
        return attendees;
    }

    public void setAttendees(ArrayList<User> attendees) {
        this.attendees = attendees;
    }

    public String getPosterPictureURL() {
        return posterPictureURL;
    }

    public Long getRegistrationOpenTime() {
        return registrationOpenTime;
    }

    public void setRegistrationOpenTime(Long registrationOpenTime) {
        this.registrationOpenTime = registrationOpenTime;
    }

    public Long getRegistrationCloseTime() {
        return registrationCloseTime;
    }

    public void setRegistrationCloseTime(Long registrationCloseTime) {
        this.registrationCloseTime = registrationCloseTime;
    }
    public void setPosterPictureURL(String posterPictureURL) {
        this.posterPictureURL = posterPictureURL;
    }

    public Integer getWaitingListCapacity() {
        return waitingListCapacity;
    }

    public void setWaitingListCapacity(Integer waitingListCapacity) {
        this.waitingListCapacity = waitingListCapacity;
    }

    public ArrayList<User> getInvitedEntrants() {
        return invitedEntrants;
    }

    public void setInvitedEntrants(ArrayList<User> invitedEntrants) {
        this.invitedEntrants = invitedEntrants;
    }

    public ArrayList<User> getCancelledEntrants() {
        return cancelledEntrants;
    }

    public void setCancelledEntrants(ArrayList<User> cancelledEntrants) {
        this.cancelledEntrants = cancelledEntrants;
    }

    public ArrayList<User> getEnrolledEntrants() {
        return enrolledEntrants;
    }

    public void setEnrolledEntrants(ArrayList<User> enrolledEntrants) {
        this.enrolledEntrants = enrolledEntrants;
    }

    public ArrayList<EventComment> getComments() {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        return comments;
    }

    public void setComments(ArrayList<EventComment> comments) {
        this.comments = comments;
    }

    public boolean isPublicEvent() {
        return publicEvent;
    }

    public void setPublicEvent(boolean publicEvent) {
        this.publicEvent = publicEvent;
    }

    public ArrayList<String> getCoOrganizerIds() {
        if (coOrganizerIds == null) coOrganizerIds = new ArrayList<>();
        return coOrganizerIds;
    }

    public void setCoOrganizerIds(ArrayList<String> coOrganizerIds) {
        this.coOrganizerIds = coOrganizerIds;
    }

    public ArrayList<String> getPendingCoOrganizerIds() {
        if (pendingCoOrganizerIds == null) {
            pendingCoOrganizerIds = new ArrayList<>();
        }
        return pendingCoOrganizerIds;
    }

    public void setPendingCoOrganizerIds(ArrayList<String> pendingCoOrganizerIds) {
        this.pendingCoOrganizerIds = pendingCoOrganizerIds;
    }

    public ArrayList<String> getTags() {
        if (tags == null) tags = new ArrayList<>();
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ArrayList<MessageThread> getMessageThreads() {
        if (messageThreads == null) {
            messageThreads = new ArrayList<>();
        }
        return messageThreads;
    }

    public void setMessageThreads(ArrayList<MessageThread> messageThreads) {
        this.messageThreads = messageThreads;
    }

    // String representation for debugging purposes
    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", time='" + time + '\'' +
                ", location='" + location + '\'' +
                ", organizer='" + organizer + '\'' +
                ", entrants=" + entrants +
                ", attendees=" + attendees +
                ", posterPictureURL='" + posterPictureURL + '\'' +
                ", tags=" + tags +
                '}';
    }
}
