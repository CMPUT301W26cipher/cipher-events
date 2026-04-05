package com.example.cipher_events.pages;

import android.app.AlertDialog;
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
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.waitinglist.WaitingListService;

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
    private Button declineButton;

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
        declineButton = view.findViewById(R.id.btn_decline_invitation);

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
        if (event == null) {
            titleText.setText("Event Not Found");
            joinButton.setEnabled(false);
            if (favoriteButton != null) favoriteButton.setVisibility(View.GONE);
            return;
        }

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

        User currentUser = db.getCurrentUser();

        // Handle favorite button
        if (currentUser != null && favoriteButton != null) {
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
        } else if (favoriteButton != null) {
            favoriteButton.setVisibility(View.GONE);
        }

        if (currentUser == null) {
            setButtonToJoinWaitlist(joinButton);
            joinButton.setEnabled(false);
            return;
        }

        // Check if invited
        boolean isInvited = false;
        if (event.getInvitedEntrants() != null) {
            for (User u : event.getInvitedEntrants()) {
                if (u.getDeviceID() != null && u.getDeviceID().equals(currentUser.getDeviceID())) {
                    isInvited = true;
                    break;
                }
            }
        }

        // Check if already in waitlist
        boolean alreadyIn = false;
        if (event.getEntrants() != null) {
            for (User u : event.getEntrants()) {
                if (u.getDeviceID() != null && u.getDeviceID().equals(currentUser.getDeviceID())) {
                    alreadyIn = true;
                    break;
                }
            }
        }

        // Check if enrolled
        boolean isEnrolled = false;
        if (event.getEnrolledEntrants() != null) {
            for (User u : event.getEnrolledEntrants()) {
                if (u.getDeviceID() != null && u.getDeviceID().equals(currentUser.getDeviceID())) {
                    isEnrolled = true;
                    break;
                }
            }
        }

        final User finalUser = currentUser;

        if (isEnrolled) {
            joinButton.setText("Enrolled ✓");
            joinButton.setEnabled(false);
            joinButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.role_organizer)));
            declineButton.setVisibility(View.GONE);

        } else if (isInvited) {
            joinButton.setText("Accept Invitation");
            joinButton.setEnabled(true);
            joinButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            joinButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.role_organizer)));
            declineButton.setVisibility(View.VISIBLE);

            joinButton.setOnClickListener(v -> {
                WaitingListService service = new WaitingListService(new UserEventHistoryRepository());
                service.acceptInvitation(finalUser, event);
                if (event.getEnrolledEntrants() == null)
                    event.setEnrolledEntrants(new ArrayList<>());
                event.getEnrolledEntrants().add(finalUser);
                db.updateEvent(event);
                Toast.makeText(getContext(), "Invitation accepted!", Toast.LENGTH_SHORT).show();
                refreshUI();
            });

            declineButton.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Decline Invitation")
                        .setMessage("Are you sure you want to decline?")
                        .setPositiveButton("Decline", (dialog, which) -> {
                            WaitingListService service = new WaitingListService(new UserEventHistoryRepository());
                            service.declineInvitation(finalUser, event);
                            if (event.getCancelledEntrants() == null)
                                event.setCancelledEntrants(new ArrayList<>());
                            event.getCancelledEntrants().add(finalUser);
                            db.updateEvent(event);
                            Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                            refreshUI();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

        } else if (alreadyIn) {
            setButtonToJoined(joinButton);
            declineButton.setVisibility(View.GONE);
            joinButton.setOnClickListener(v -> {
                event.getEntrants().remove(finalUser);
                db.updateEvent(event);
                Toast.makeText(getContext(), "Left waitlist successfully!", Toast.LENGTH_SHORT).show();
            });

        } else {
            setButtonToJoinWaitlist(joinButton);
            declineButton.setVisibility(View.GONE);
            joinButton.setOnClickListener(v -> {
                if (event.getEntrants() == null) event.setEntrants(new ArrayList<>());
                event.getEntrants().add(finalUser);
                db.updateEvent(event);
                Toast.makeText(getContext(), "Joined waitlist successfully!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setButtonToJoined(Button button) {
        button.setText("Joined Waitlist");
        button.setEnabled(true);
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.role_organizer)));
    }

    private void setButtonToJoinWaitlist(Button button) {
        button.setText("Join Waitlist");
        button.setEnabled(true);
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.button_purple)));
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