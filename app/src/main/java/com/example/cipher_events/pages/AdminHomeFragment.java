package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        Button btnBrowseProfiles = view.findViewById(R.id.btn_manage_users);
        Button btnBrowseEvents = view.findViewById(R.id.btn_manage_events);
        Button btnBrowseImages = view.findViewById(R.id.btn_manage_images);
        Button btnNotificationLogs = view.findViewById(R.id.btn_notification_logs);

        if (btnBrowseProfiles != null) {
            btnBrowseProfiles.setOnClickListener(v -> replaceFragment(new AdminBrowseProfilesFragment()));
        }

        if (btnBrowseEvents != null) {
            btnBrowseEvents.setOnClickListener(v -> replaceFragment(new AdminBrowseEventsFragment()));
        }

        if (btnBrowseImages != null) {
            btnBrowseImages.setOnClickListener(v -> {
                // Placeholder for browse images if implemented later
            });
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