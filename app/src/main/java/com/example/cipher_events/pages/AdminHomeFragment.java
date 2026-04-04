package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.cipher_events.R;

public class AdminHomeFragment extends Fragment {

    public AdminHomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        CardView btnBrowseProfiles = view.findViewById(R.id.btn_manage_users);
        CardView btnBrowseEvents = view.findViewById(R.id.btn_manage_events);
        CardView btnBrowseImages = view.findViewById(R.id.btn_manage_images);
        CardView btnNotificationLogs = view.findViewById(R.id.btn_notification_logs);

        if (btnBrowseProfiles != null) {
            btnBrowseProfiles.setOnClickListener(v -> replaceFragment(new AdminBrowseProfilesFragment()));
        }

        if (btnBrowseEvents != null) {
            btnBrowseEvents.setOnClickListener(v -> replaceFragment(new AdminBrowseEventsFragment()));
        }

        if (btnBrowseImages != null) {
            btnBrowseImages.setOnClickListener(v -> replaceFragment(new AdminBrowseImagesFragment()));
        }

        if (btnNotificationLogs != null) {
            btnNotificationLogs.setOnClickListener(v -> replaceFragment(new AdminNotificationsFragment()));
        }

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}