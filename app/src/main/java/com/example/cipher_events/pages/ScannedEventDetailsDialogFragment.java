package com.example.cipher_events.pages;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
    private ImageView favoriteButton;
    private ImageView closeButton;
    private TextView titleText;
    private TextView waitlistCountText;
    private TextView descriptionText;
    private TextView dateLocationText;
    private Button joinButton;

    public static ScannedEventDetailsDialogFragment newInstance(String eventId) {
        ScannedEventDetailsDialogFragment fragment = new ScannedEventDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View view = inflater.inflate(R.layout.dialog_scanned_event_details, container, false);

        bannerImage = view.findViewById(R.id.scanned_event_banner);
        favoriteButton = view.findViewById(R.id.btn_favorite);
        closeButton = view.findViewById(R.id.btn_close);
        titleText = view.findViewById(R.id.scanned_event_title);
        waitlistCountText = view.findViewById(R.id.scanned_event_waitlist_count);
        descriptionText = view.findViewById(R.id.scanned_event_description);
        dateLocationText = view.findViewById(R.id.scanned_event_date_location);
        joinButton = view.findViewById(R.id.btn_join_waitlist);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            refreshUI();
        }

        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dismiss());
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
            
            if (dateLocationText != null) {
                dateLocationText.setText(event.getTime() + " • " + event.getLocation());
            }

            if (event.getPosterPictureURL() != null && !event.getPosterPictureURL().isEmpty()) {
                Glide.with(this)
                        .load(event.getPosterPictureURL())
                        .placeholder(R.drawable.gray_placeholder)
                        .into(bannerImage);
            } else {
                bannerImage.setImageResource(R.drawable.gray_placeholder);
            }

            // Get real user from DB
            User currentUser = db.getCurrentUser();

            if (currentUser != null) {
                favoriteButton.setVisibility(View.VISIBLE);
                if (currentUser.isFavorite(eventId)) {
                    favoriteButton.setImageResource(R.drawable.baseline_star_24);
                } else {
                    favoriteButton.setImageResource(R.drawable.baseline_star_border_24);
                }

                favoriteButton.setOnClickListener(v -> {
                    if (currentUser.isFavorite(eventId)) {
                        currentUser.removeFavoriteEvent(eventId);
                    } else {
                        currentUser.addFavoriteEvent(eventId);
                    }
                    db.updateUser(currentUser);
                    refreshUI();
                });
            } else {
                favoriteButton.setVisibility(View.GONE);
            }

            if (currentUser == null) {
                setButtonToJoinWaitlist(joinButton);
                return;
            }

// Check if invited
            boolean isInvited = false;
            if (event.getInvitedEntrants() != null) {
                for (User u : event.getInvitedEntrants()) {
                    if (u.getDeviceID().equals(currentUser.getDeviceID())) {
                        isInvited = true;
                        break;
                    }
                }
            }

// Check if already in waitlist
            boolean alreadyIn = false;
            if (event.getEntrants() != null) {
                for (User u : event.getEntrants()) {
                    if (u.getDeviceID().equals(currentUser.getDeviceID())) {
                        alreadyIn = true;
                        break;
                    }
                }
            }

            if (isInvited) {
                // Show accept/decline buttons
                joinButton.setText("Accept Invitation");
                joinButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                joinButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.role_organizer)));
                joinButton.setOnClickListener(v -> {
                    com.example.cipher_events.waitinglist.WaitingListService service =
                            new com.example.cipher_events.waitinglist.WaitingListService(
                                    new com.example.cipher_events.user.UserEventHistoryRepository());
                    service.acceptInvitation(currentUser, event);
                    // Move to enrolled
                    if (event.getEnrolledEntrants() == null) event.setEnrolledEntrants(new java.util.ArrayList<>());
                    event.getEnrolledEntrants().add(currentUser);
                    db.updateEvent(event);
                    Toast.makeText(getContext(), "Invitation accepted!", Toast.LENGTH_SHORT).show();
                    refreshUI();
                });

                // Add decline button - reuse existing button, add new one programmatically
                // For now show toast option via long press
                joinButton.setOnLongClickListener(v -> {
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Decline Invitation")
                            .setMessage("Are you sure you want to decline?")
                            .setPositiveButton("Decline", (dialog, which) -> {
                                com.example.cipher_events.waitinglist.WaitingListService service =
                                        new com.example.cipher_events.waitinglist.WaitingListService(
                                                new com.example.cipher_events.user.UserEventHistoryRepository());
                                service.declineInvitation(currentUser, event);
                                if (event.getCancelledEntrants() == null) event.setCancelledEntrants(new java.util.ArrayList<>());
                                event.getCancelledEntrants().add(currentUser);
                                db.updateEvent(event);
                                Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                                refreshUI();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                });
            } else if (alreadyIn) {
                setButtonToJoined(joinButton);
            } else {
                setButtonToJoinWaitlist(joinButton);
            }

            joinButton.setOnClickListener(v -> {
                if (currentUser == null) return;
                
                if (event.getEntrants() == null) {
                    event.setEntrants(new ArrayList<>());
                }

                // Check again to toggle
                User foundUser = null;
                for (User u : event.getEntrants()) {
                    if (u.getDeviceID().equals(currentUser.getDeviceID())) {
                        foundUser = u;
                        break;
                    }
                }

                if (foundUser == null) {
                    event.getEntrants().add(currentUser);
                    db.updateEvent(event);
                    Toast.makeText(getContext(), "Joined waitlist successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    event.getEntrants().remove(foundUser);
                    db.updateEvent(event);
                    Toast.makeText(getContext(), "Left waitlist successfully!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            titleText.setText("Event Not Found");
            joinButton.setEnabled(false);
            favoriteButton.setVisibility(View.GONE);
        }
    }

    private void setButtonToJoined(Button button) {
        button.setText("Joined Waitlist");
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.role_organizer)));
    }

    private void setButtonToJoinWaitlist(Button button) {
        button.setText("Join Waitlist");
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.button_purple)));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
