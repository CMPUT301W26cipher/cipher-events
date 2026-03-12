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

// AdminDB performs operations on the admins collection in Firestore.
public class AdminDB {
    private static AdminDB instance = null;
    ListenerRegistration listener = null;
    FirebaseFirestore db = null;
    CollectionReference adminsRef = null;
    ArrayList<Admin> admins = new ArrayList<>(); // local copy of firebase collection
    String TAG = "AdminDB";

    private AdminDB() {
        db = FirebaseFirestore.getInstance();
        adminsRef = db.collection("admins");
    }

    public static AdminDB getInstance() {
        if (instance == null) {
            instance = new AdminDB();
        }
        return instance;
    }

    protected void startListener() {
        listener = adminsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                if (value != null && !value.isEmpty()) {
                    admins.clear();
                    for (DocumentSnapshot doc : value) {
                        Admin admin = doc.toObject(Admin.class);
                        admins.add(admin);
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

    protected void add(Admin admin) {
        adminsRef
                .document(admin.getDeviceID())
                .set(admin);
        admins.add(admin);
    }

    protected Admin get(String deviceID) {
        for (Admin a : admins) {
            if (a.getDeviceID().equals(deviceID)) {
                return a;
            }
        }
        return null;
    }

    protected ArrayList<Admin> getAll() {
        return admins;
    }

    protected void update(Admin admin) {
        String deviceID = admin.getDeviceID();
        adminsRef.document(deviceID).set(admin);
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getDeviceID().equals(deviceID)) {
                admins.set(i, admin);
            }
        }
    }

    protected void delete(Admin admin) {
        adminsRef.document(admin.getDeviceID()).delete();
        admins.remove(admin);
    }

    protected void delete(String deviceID) {
        adminsRef.document(deviceID).delete();
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getDeviceID().equals(deviceID)) {
                admins.remove(i);
                return;
            }
        }
    }
}
