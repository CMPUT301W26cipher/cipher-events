package com.example.cipher_events;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.MobileAds;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class App extends Application {
    private static Context context;
    private static Activity currentActivity;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        AndroidThreeTen.init(this);

        // Initialize Google Mobile Ads SDK
        new Thread(() -> {
            MobileAds.initialize(this, initializationStatus -> {});
        }).start();

        // Automatically track current activity
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                currentActivity = null;
            }

            @Override public void onActivityCreated(@NonNull Activity activity, Bundle b) {}
            @Override public void onActivityStarted(@NonNull Activity activity) {}
            @Override public void onActivityStopped(@NonNull Activity activity) {}
            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle b) {}
            @Override public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }

    public static Context getContext() {
        return context;
    }

    public static Activity getActivity() {
        return currentActivity;
    }
}