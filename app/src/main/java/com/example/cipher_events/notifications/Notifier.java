package com.example.cipher_events.notifications;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.cipher_events.App;
import com.example.cipher_events.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Notifier manages and sends notifications to devices using Firebase Cloud Messaging.
// Notifier is a singleton. Call Notifier.getInstance() to get its instance.
// Currently supports sending to one device at a time.

// Relevant methods:
// sendMessage(deviceID, message)
// TODO: add topic subscription and mass notifying

// Example usage:
// Notifier notifier = Notifier.getInstance();          //  setup Notifier
// notifier.sendMessage(deviceID, message);             //  Sends a notification to one device

// TODO : testing
// TODO : javadoc
// TODO : logging requests
public class Notifier extends FirebaseMessagingService {
    private static Notifier instance = null;
    ListenerRegistration listener = null;
    private Context context;
    ArrayList<Map<String, Object>> FCMlookup = new ArrayList<>(); // local copy FCM device mapping

    public static Notifier getInstance() {
        if (instance == null) {
            instance = new Notifier();
            instance.startListener();
            instance.context = App.getContext();
        }
        return instance;
    }

    protected void startListener() {
        listener = FirebaseFirestore.getInstance().collection("notifications").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null && !value.isEmpty()) {
                    FCMlookup.clear();
                    for (DocumentSnapshot doc : value) {
                        Map<String, Object> mapping = doc.getData();
                        FCMlookup.add(mapping);
                    }
                }
            }
        });
    }

    public void stopListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        requestNotificationPermission();
        super.onNewToken(token);
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        String currentDevice = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID
        );

        Map<String, Object> data = new HashMap<>();
        data.put("deviceID", currentDevice);
        data.put("fcmToken", token);

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(currentDevice)
                .set(data, SetOptions.merge());
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Handle data payload
        if (!remoteMessage.getData().isEmpty()) {
            String title = remoteMessage.getData().get("title");
            String body  = remoteMessage.getData().get("body");
            showNotification(title, body, true);
        }

        // Handle notification payload (app in foreground)
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body  = remoteMessage.getNotification().getBody();

            showInAppMessage(title, body);
            showNotification(title, body, false);
        }
    }

    private void showInAppMessage(String title, String body) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Activity activity = App.getActivity();
            if (activity == null || activity.isFinishing()) return;

            View popupView = LayoutInflater.from(activity)
                    .inflate(R.layout.notification_popup, null);

            ((TextView) popupView.findViewById(R.id.title)).setText(title);
            ((TextView) popupView.findViewById(R.id.body)).setText(body);

            WindowManager windowManager =
                    (WindowManager) activity.getSystemService(WINDOW_SERVICE);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP;
            params.y = 50;

            windowManager.addView(popupView, params);

            // Close button
            popupView.findViewById(R.id.close).setOnClickListener(v ->
                    windowManager.removeView(popupView)
            );

            // Auto dismiss after 4 seconds
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (popupView.isAttachedToWindow()) {
                    windowManager.removeView(popupView);
                }
            }, 4000);
        });
    }

    public void showNotification(String title, String body, boolean showMessage) {
        String channelId = "default_channel";
        int priority;
        int importance;

        if (showMessage) {
            priority = NotificationCompat.PRIORITY_HIGH;
            importance = NotificationManager.IMPORTANCE_HIGH;
        } else {
            priority = NotificationCompat.PRIORITY_DEFAULT;
            importance = NotificationManager.IMPORTANCE_DEFAULT;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(priority)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Default", importance
            );
            manager.createNotificationChannel(channel);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    public void requestNotificationPermission() {
        Activity activity = App.getActivity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    protected Map<String, Object> get(String deviceID) {
        for (Map<String, Object> m : FCMlookup) {
            if (m.get("deviceID").equals(deviceID)) {
                return m;
            }
        }
        return null;
    }

    public void sendMessage(String deviceID, Message message) {
        String PROJECT_ID = "cipher-a3ec2";
        String ENDPOINT = "https://fcm.googleapis.com/v1/projects/"
                + PROJECT_ID + "/messages:send";
        String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
        String[] SCOPES = {MESSAGING_SCOPE};

        InputStream serviceAccountStream;
        try {
            serviceAccountStream = context.getAssets().open("service-account.json");
        } catch (IOException e) {
            Log.e("FCM", "Failed to open service-account.json", e);
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Get OAuth 2.0 access token
                GoogleCredentials credentials = GoogleCredentials
                        .fromStream(serviceAccountStream)
                        .createScoped(Arrays.asList(SCOPES));
                credentials.refreshIfExpired();
                String accessToken = credentials.getAccessToken().getTokenValue();

                String token = get(deviceID).get("fcmToken").toString();

                String payload = """
                    {
                      "message": {
                        "notification": {
                          "title": "%s",
                          "body": "%s"
                        },
                        "token": "%s"
                      }
                    }
                    """.formatted(message.getTitle(), message.getBody(), token);

                // Send request
                URL url = new URL(ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes());
                }

                int responseCode = conn.getResponseCode();
                Log.d("FCM", "Response Code: " + responseCode);

            } catch (IOException e) {
                Log.e("FCM", "IO error sending message", e);
            } catch (Exception e) {
                Log.e("FCM", "Unexpected error sending message", e);
            }
        });
    }
}
