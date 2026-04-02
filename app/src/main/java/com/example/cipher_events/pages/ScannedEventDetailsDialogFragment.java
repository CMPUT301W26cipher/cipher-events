package com.example.cipher_events.pages;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;

import java.util.ArrayList;

public class ScannedEventDetailsDialogFragment extends DialogFragment implements DBProxy.OnDataChangedListener {

    private String eventId;
    private DBProxy db = DBProxy.getInstance();
    private ImageView bannerImage;
    private TextView titleText;
    private TextView waitlistCountText;
    private TextView descriptionText;
    private Button joinButton;
    private String deviceId;

    public static ScannedEventDetailsDialogFragment newInstance(String eventId) {
        ScannedEventDetailsDialogFragment fragment = new ScannedEventDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View view = inflater.inflate(R.layout.dialog_scanned_event_details, container, false);

        bannerImage = view.findViewById(R.id.scanned_event_banner);
        titleText = view.findViewById(R.id.scanned_event_title);
        waitlistCountText = view.findViewById(R.id.scanned_event_waitlist_count);
        descriptionText = view.findViewById(R.id.scanned_event_description);
        joinButton = view.findViewById(R.id.btn_join_waitlist);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            refreshUI();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
        refreshUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::refreshUI);
        }
    }

    private void refreshUI() {
        Event event = db.getEvent(eventId);
        if (event != null) {
            titleText.setText(event.getName());
            int count = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
            waitlistCountText.setText(count + " people in waitlist");
            descriptionText.setText(event.getDescription());

            if (event.getPosterPictureURL() != null && !event.getPosterPictureURL().isEmpty()) {
                bannerImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(event.getPosterPictureURL()).into(bannerImage);
            }

            // Get real user from DB
            User currentUser = db.getUser(deviceId);
            if (currentUser == null) {
                // Fallback if user doesn't exist in DB yet
                currentUser = new User("New Attendee", "", "", "", null);
                currentUser.setDeviceID(deviceId);
            }

            // Membership check
            boolean alreadyIn = false;
            if (event.getEntrants() != null) {
                for (User u : event.getEntrants()) {
                    if (u.getDeviceID().equals(currentUser.getDeviceID())) {
                        alreadyIn = true;
                        break;
                    }
                }
            }

            if (alreadyIn) {
                setButtonToJoined(joinButton);
            } else {
                setButtonToJoinWaitlist(joinButton);
            }

            final User finalUser = currentUser;
            joinButton.setOnClickListener(v -> {
                if (event.getEntrants() == null) {
                    event.setEntrants(new ArrayList<>());
                }

                // Check again to toggle
                User foundUser = null;
                for (User u : event.getEntrants()) {
                    if (u.getDeviceID().equals(finalUser.getDeviceID())) {
                        foundUser = u;
                        break;
                    }
                }

                if (foundUser == null) {
                    event.getEntrants().add(finalUser);
                    db.updateEvent(event);
                    Toast.makeText(getContext(), "Joined waitlist successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    event.getEntrants().remove(foundUser);
                    db.updateEvent(event);
                    Toast.makeText(getContext(), "Left waitlist successfully!", Toast.LENGTH_SHORT).show();
                }
                // UI will refresh automatically via DB listener
            });
        } else {
            titleText.setText("Event Not Found");
            joinButton.setEnabled(false);
        }
    }

    private void setButtonToJoined(Button button) {
        button.setText("Joined");
        button.setTextColor(Color.parseColor("#FF4081")); // Pink text
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0"))); // Light Gray background
    }

    private void setButtonToJoinWaitlist(Button button) {
        button.setText("Join Waitlist");
        button.setTextColor(Color.WHITE); // White text
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5B5891"))); // Purple background
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
