package com.example.cipher_events.database;

import java.util.ArrayList;

/*
 * Represents an event.
 * Each event has a name, description, time, location, organizer, entrants, attendees, and a poster picture URL.
 */

public class Event {
    private String name;
    private String description;
    private String time;
    private String location;
    private Organizer organizer;
    private ArrayList<User> entrants;
    private ArrayList<User> attendees;
    private String posterPictureURL; // optional poster picture
    private Integer waitingListCapacity; // Optional waiting list capacity (null means unlimited)

    // Constructor
    // pass empty lists for entrants and attendees if not provided
    // pass null for optional fields if not provided
    public Event(String name, String description, String time, String location, Organizer organizer, ArrayList<User> entrants, ArrayList<User> attendees, String posterPictureURL) {
        this.name = name;
        this.description = description;
        this.time = time;
        this.location = location;
        this.organizer = organizer;
        this.entrants = entrants;
        this.attendees = attendees;
        this.posterPictureURL = posterPictureURL;
        this.waitingListCapacity = null; // default unlimited
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

    public void setPosterPictureURL(String posterPictureURL) {
        this.posterPictureURL = posterPictureURL;
    }

    public Integer getWaitingListCapacity() {
        return waitingListCapacity;
    }

    public void setWaitingListCapacity(Integer waitingListCapacity) {
        this.waitingListCapacity = waitingListCapacity;
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
                '}';
    }
}
