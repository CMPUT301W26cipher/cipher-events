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

// OrganizerDB performs operations on the organizers collection in Firestore.
public class OrganizerDB {
    private static OrganizerDB instance = null;
    ListenerRegistration listener = null;
    FirebaseFirestore db = null;
    CollectionReference organizersRef = null;
    ArrayList<Organizer> organizers = new ArrayList<>(); // local copy of firebase collection
    String TAG = "OrganizerDB";

    private OrganizerDB() {
        db = FirebaseFirestore.getInstance();
        organizersRef = db.collection("organizers");
    }

    public static OrganizerDB getInstance() {
        if (instance == null) {
            instance = new OrganizerDB();
        }
        return instance;
    }

    protected void startListener() {
        listener = organizersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                if (value != null) {
                    organizers.clear();
                    for (DocumentSnapshot doc : value) {
                        Organizer organizer = doc.toObject(Organizer.class);
                        if (organizer != null) {
                            organizers.add(organizer);
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

    protected void add(Organizer organizer) {
        organizersRef
                .document(organizer.getDeviceID())
                .set(organizer);
    }

    protected Organizer get(String deviceID) {
        for (Organizer o : organizers) {
            if (o.getDeviceID().equals(deviceID)) {
                return o;
            }
        }
        return null;
    }

    protected ArrayList<Organizer> getAll() {
        return organizers;
    }

    protected void update(Organizer user) {
        String deviceID = user.getDeviceID();
        organizersRef.document(deviceID).set(user);
    }

    protected void delete(Organizer organizer) {
        organizersRef.document(organizer.getDeviceID()).delete();
    }

    protected void delete(String deviceID) {
        organizersRef.document(deviceID).delete();
    }
}
