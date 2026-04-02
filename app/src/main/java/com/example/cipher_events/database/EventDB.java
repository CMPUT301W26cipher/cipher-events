package com.example.cipher_events.database;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

// EventDB performs operations on the events collection in Firestore.
public class EventDB {
    private static EventDB instance = null;
    ListenerRegistration listener = null;
    FirebaseFirestore db = null;
    CollectionReference eventsRef = null;
    ArrayList<Event> events = new ArrayList<>(); // local copy of firebase collection
    String TAG = "EventDB";

    private EventDB() {
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
    }

    public static EventDB getInstance() {
        if (instance == null) {
            instance = new EventDB();
        }
        return instance;
    }

    protected void startListener() {
        listener = eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                if (value != null) {
                    events.clear();
                    for (DocumentSnapshot doc : value) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    DBProxy.getInstance().notifyListeners();
                }
            }
        });
    }

    protected void stopListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    protected void add(Event event) {
        eventsRef
                .document(event.getEventID())
                .set(event);
    }

    protected Event get(String eventID) {
        for (Event e : events) {
            if (e.getEventID().equals(eventID)) {
                return e;
            }
        }
        return null;
    }

    protected ArrayList<Event> getAll() {
        return events;
    }

    protected void update(Event event) {
        String eventID = event.getEventID();
        eventsRef.document(eventID).set(event);
    }

    protected void delete(Event event) {
        eventsRef.document(event.getEventID()).delete();
    }

    protected void delete(String eventID) {
        eventsRef.document(eventID).delete();
    }
}
