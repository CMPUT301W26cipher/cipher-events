package com.example.cipher_events.logging;

import android.util.Log;


import androidx.annotation.Nullable;

import com.example.cipher_events.database.Admin;
import com.example.cipher_events.notifications.Message;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Logger {
    private static Logger instance = null;
    private ListenerRegistration logListener = null;
    private ListenerRegistration DBProxyListener = null;
    private CollectionReference logRef = null;
    private CollectionReference DBProxyRef = null;
    private ArrayList<Message> notificationLog = new ArrayList<>();
    private ArrayList<String> DBProxyLog = new ArrayList<>();


    private Logger() {}

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
            instance.logRef = FirebaseFirestore.getInstance().collection("notificationLog");
            instance.DBProxyRef = FirebaseFirestore.getInstance().collection("DBProxyLog");
            instance.startListener();
        }
        return instance;
    }

    protected void startListener() {
        logListener = FirebaseFirestore.getInstance().collection("notificationLog").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null && !value.isEmpty()) {
                    notificationLog.clear();
                    for (DocumentSnapshot doc : value) {
                        Message message = doc.toObject(Message.class);
                        notificationLog.add(message);
                    }
                }
            }
        });

        DBProxyListener = FirebaseFirestore.getInstance().collection("DBProxyLog").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null && !value.isEmpty()) {
                    DBProxyLog.clear();
                    for (DocumentSnapshot doc : value) {
                        Map<String, Object> data = doc.getData();
                        DBProxyLog.add(data.get("message").toString());
                    }
                }
            }
        });
    }

    public void stopListener() {
        if (logListener != null) {
            logListener.remove();
            logListener = null;
        }
        if (DBProxyListener != null) {
            DBProxyListener.remove();
            DBProxyListener = null;
        }
    }

    public void logNotification(Message message) {
        // Use a random UUID or Firestore auto-ID to avoid collisions during bulk sending
        DocumentReference newDoc = logRef.document();
        newDoc.set(message);
        notificationLog.add(message);
    }

    public void logDBProxy(String message) {
    }

    public List<Message> getNotificationLog() {
        return notificationLog;
    }

    public List<String> getDBProxyLog() {
        return DBProxyLog;
    }
}
