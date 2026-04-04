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
import java.util.List;

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
                if (value != null) {
                    users.clear();
                    for (DocumentSnapshot doc : value) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            users.add(user);
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

    protected void add(User user) {
        usersRef
                .document(user.getDeviceID())
                .set(user);
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

    protected List<User> search(String keyword) {
        String lower = keyword == null ? "" : keyword.trim().toLowerCase();
        List<User> result = new ArrayList<>();
        for (User u : users) {
            if (u == null) continue;
            if ((u.getName() != null && u.getName().toLowerCase().contains(lower)) ||
                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(lower)) ||
                    (u.getPhoneNumber() != null && u.getPhoneNumber().toLowerCase().contains(lower))) {
                result.add(u);
            }
        }
        return result;
    }

    protected void update(User user) {
        String deviceID = user.getDeviceID();
        usersRef.document(deviceID).set(user);
    }

    protected void delete(User user) {
        usersRef.document(user.getDeviceID()).delete();
    }

    protected void delete(String deviceID) {
        usersRef.document(deviceID).delete();
    }
}