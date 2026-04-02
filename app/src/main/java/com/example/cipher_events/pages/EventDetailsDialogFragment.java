package com.example.cipher_events.pages;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.notifications.Message;
import com.example.cipher_events.notifications.Notifier;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Displays a popup upon clicking an event
 * pop up displays full event details:
 * - Event Title
 * - Number of people in waitlist
 * - Event Description
 * - Date and Location
 * - Tags
 * - Lottery disclaimer
 */

public class EventDetailsDialogFragment extends DialogFragment {

    private boolean isOrganizerView = false;
    private String eventId;
    DBProxy db = DBProxy.getInstance();
    Notifier notifier = Notifier.getInstance();

    public static EventDetailsDialogFragment newInstance(
            String eventId,
            String name,
            String description,
            String time,
            String location,
            int waitlistCount,
            ArrayList<String> tags
    ) {
        return newInstance(eventId, name, description, time, location, waitlistCount, tags, false);
    }

    public static EventDetailsDialogFragment newInstance(
            String eventId,
            String name,
            String description,
            String time,
            String location,
            int waitlistCount,
            ArrayList<String> tags,
            boolean isOrganizerView
    ) {
        EventDetailsDialogFragment fragment = new EventDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("name", name);
        args.putString("description", description);
        args.putString("time", time);
        args.putString("location", location);
        args.putInt("waitlistCount", waitlistCount);
        args.putStringArrayList("tags", tags);
        args.putBoolean("isOrganizerView", isOrganizerView);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Make dialog background transparent so rounded corners show
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View view = inflater.inflate(R.layout.dialog_event_details, container, false);

        TextView title = view.findViewById(R.id.detail_title);
        TextView attendees = view.findViewById(R.id.detail_attendees);
        //TextView description = view.findViewById(R.id.detail_description);
        TextView dateLocation = view.findViewById(R.id.detail_date_location);
        LinearLayout tagContainer = view.findViewById(R.id.detail_tags_container);
        TextView lotteryHeader = view.findViewById(R.id.detail_lottery_header);
        TextView lotteryText = view.findViewById(R.id.detail_lottery_text);
        Button notifyButton = view.findViewById(R.id.notify_button);
        Button actionButton = view.findViewById(R.id.scan_button);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
            title.setText(args.getString("name"));
            attendees.setText(args.getInt("waitlistCount") + " people in waitlist");
            //description.setText(args.getString("description"));
            dateLocation.setText(args.getString("location") + " • " + args.getString("time"));
            isOrganizerView = args.getBoolean("isOrganizerView", false);

            ArrayList<String> tags = args.getStringArrayList("tags");
            if (tags != null) {
                for (String tag : tags) {
                    TextView chip = new TextView(requireContext());
                    chip.setText(tag);
                    chip.setPadding(20, 10, 20, 10);
                    chip.setBackgroundResource(R.drawable.tag_chip_bg);
                    chip.setTextSize(14);

                    LinearLayout.LayoutParams params =
                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(10, 10, 10, 10);
                    chip.setLayoutParams(params);

                    tagContainer.addView(chip);
                }
            }
        }

        if (isOrganizerView) {
            actionButton.setText("View Waitlist");

            actionButton.setOnClickListener(v -> {
                dismiss();
                WaitingListFragment fragment = WaitingListFragment.newInstance(eventId);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });

            // SHOW notify button
            notifyButton.setVisibility(View.VISIBLE);

            notifyButton.setOnClickListener(v -> {
                showNotificationInputDialog();
            });

        } else {
            actionButton.setText("Scan Info");
        }

        // Lottery guidelines text
        lotteryText.setText(
                "⚠️ Disclaimer\n" +
                        "Some events use a lottery system when more people join than there are available spots.\n\n" +
                        "What this means for you:\n" +
                        "• When you join, you're entered into the lottery.\n" +
                        "• Everyone who joins before the deadline has the same chance.\n" +
                        "• Joining earlier does not increase your odds.\n" +
                        "• If you're selected, you'll receive a confirmation.\n" +
                        "• If you're not selected, you may be placed on a waitlist.\n\n" +
                        "This system helps keep things fair and avoids first‑come‑first‑served pressure."
        );


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Make dialog 90% of screen width
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }


    private void showNotificationInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Send Notification");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);

        // Title input
        final EditText titleInput = new EditText(requireContext());
        titleInput.setHint("Notification Title");
        layout.addView(titleInput);

        // Body input
        final EditText bodyInput = new EditText(requireContext());
        bodyInput.setHint("Notification Message");
        bodyInput.setMinLines(3);
        bodyInput.setMaxLines(5);
        bodyInput.setGravity(Gravity.TOP);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 0);
        bodyInput.setLayoutParams(params);

        layout.addView(bodyInput);

        // Group selector
        final Spinner groupSpinner = new Spinner(requireContext());
        String[] groups = {"Invited", "Cancelled", "Enrolled"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                groups
        );
        groupSpinner.setAdapter(adapter);
        layout.addView(groupSpinner);

        builder.setView(layout);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String body = bodyInput.getText().toString().trim();
            String selectedGroup = groupSpinner.getSelectedItem().toString();

            if (!title.isEmpty() && !body.isEmpty()) {
                sendNotificationToGroup(title, body, selectedGroup);
            } else {
                Toast.makeText(getContext(),
                        "Title and message cannot be empty",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void sendNotificationToGroup(String title, String body, String group) {

        switch (group) {
            case "Invited":
                notifyInvitedEntrants(title, body);
                break;

            case "Cancelled":
                notifyCancelledEntrants(title, body);
                break;

            case "Enrolled":
                notifyEnrolledEntrants(title, body);
                break;
        }
    }

    private void notifyInvitedEntrants(String title, String body) {
        Event e = db.getEvent(eventId);
        Message m = new Message(title, body, e.getOrganizer());

        ArrayList<User> entrants = e.getInvitedEntrants();
        for (User user : entrants) {
            notifier.sendMessage(user.getDeviceID(), m);
        }
    }

    private void notifyCancelledEntrants(String title, String body) {
        Event e = db.getEvent(eventId);
        Message m = new Message(title, body, e.getOrganizer());

        ArrayList<User> entrants = e.getCancelledEntrants();
        for (User user : entrants) {
            notifier.sendMessage(user.getDeviceID(), m);
        }
    }

    private void notifyEnrolledEntrants(String title, String body) {
        Event e = db.getEvent(eventId);
        Message m = new Message(title, body, e.getOrganizer());

        ArrayList<User> entrants = e.getEnrolledEntrants();
        for (User user : entrants) {
            notifier.sendMessage(user.getDeviceID(), m);
        }
    }
}