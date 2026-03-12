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

// UserDB performs operations on the users collection in Firestore.
public class UserDB {
    private static UserDB instance = null;
    ListenerRegistration listener = null;
    FirebaseFirestore db = null;
    CollectionReference usersRef = null;
    ArrayList<User> users = new ArrayList<>(); // local copy of firebase collection
    String TAG = "UserDB";

    private UserDB() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

    public static UserDB getInstance() {
        if (instance == null) {
            instance = new UserDB();
        }
        return instance;
    }

    protected void startListener() {
        listener = usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                if (value != null && !value.isEmpty()) {
                    users.clear();
                    for (DocumentSnapshot doc : value) {
                        User user = doc.toObject(User.class);
                        users.add(user);
                        Log.d(TAG, doc.getId() + " => " + doc.getData());
                    }
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

    protected void add(User user) {
        usersRef
                .document(user.getDeviceID())
                .set(user);
        users.add(user);
    }

    protected User get(String deviceID) {
        for (User u : users) {
            if (u.getDeviceID().equals(deviceID)) {
                return u;
            }
        }
        return null;
    }

    protected ArrayList<User> getAll() {
        return users;
    }

    protected void update(User user) {
        String deviceID = user.getDeviceID();
        usersRef.document(deviceID).set(user);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getDeviceID().equals(deviceID)) {
                users.set(i, user);
            }
        }
    }

    protected void delete(User user) {
        usersRef.document(user.getDeviceID()).delete();
        users.remove(user);
    }

    protected void delete(String deviceID) {
        usersRef.document(deviceID).delete();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getDeviceID().equals(deviceID)) {
                users.remove(i);
                return;
            }
        }
    }
}