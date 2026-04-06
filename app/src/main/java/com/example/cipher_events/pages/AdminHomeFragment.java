package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.R;
import com.google.android.material.card.MaterialCardView;

/**
 * AdminHomeFragment provides a modern dashboard for administrative tasks.
 */
public class AdminHomeFragment extends Fragment {

    public AdminHomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        setupClickListeners(view);

        return view;
    }

    private void setupClickListeners(View view) {
        MaterialCardView btnBrowseProfiles = view.findViewById(R.id.btn_manage_users);
        MaterialCardView btnBrowseEvents = view.findViewById(R.id.btn_manage_events);
        MaterialCardView btnBrowseImages = view.findViewById(R.id.btn_manage_images);
        MaterialCardView btnNotificationLogs = view.findViewById(R.id.btn_notification_logs);
        MaterialCardView btnAdminProfile = view.findViewById(R.id.btn_admin_profile);
        MaterialCardView btnManageComments = view.findViewById(R.id.btn_manage_comments);

        if (btnBrowseProfiles != null) {
            btnBrowseProfiles.setOnClickListener(v -> navigateTo(new AdminBrowseProfilesFragment()));
        }

        if (btnBrowseEvents != null) {
            btnBrowseEvents.setOnClickListener(v -> navigateTo(new AdminBrowseEventsFragment()));
        }

        if (btnBrowseImages != null) {
            btnBrowseImages.setOnClickListener(v -> navigateTo(new AdminBrowseImagesFragment()));
        }

        if (btnNotificationLogs != null) {
            btnNotificationLogs.setOnClickListener(v -> navigateTo(new AdminNotificationsFragment()));
        }

        if (btnAdminProfile != null) {
            btnAdminProfile.setOnClickListener(v -> navigateTo(new AdminProfileFragment()));
        }

        if (btnManageComments != null) {
            btnManageComments.setOnClickListener(v -> navigateTo(new AdminBrowseCommentsFragment()));
        }
    }

    private void navigateTo(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}