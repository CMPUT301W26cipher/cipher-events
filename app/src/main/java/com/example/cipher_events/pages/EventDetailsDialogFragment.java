package com.example.cipher_events.pages;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventCommentAdapter;
import com.example.cipher_events.comment.EventComment;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.message.MessageThread;
import com.example.cipher_events.message.MessagingService;
import com.example.cipher_events.notifications.Message;
import com.example.cipher_events.notifications.Notifier;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventDetailsDialogFragment extends DialogFragment implements DBProxy.OnDataChangedListener {

    private boolean isOrganizerView = false;
    private String eventId;
    private String currentDeviceID;

    private final DBProxy db = DBProxy.getInstance();
    private final Notifier notifier = Notifier.getInstance();
    private final MessagingService messagingService = new MessagingService();

    private EventCommentAdapter commentAdapter;

    private TextView title;
    private TextView attendees;
    private TextView description;
    private TextView descriptionLabel;
    private TextView dateLocation;
    private ImageView banner;
    private ImageView favoriteButton;
    private ImageView closeButton;
    private Button closeButtonBottom;
    private Button actionButton;
    private Button notifyButton;
    private Button messageButton;
    private Button deleteButton;
    private Button editButton;
    private View lotteryContainer;
    private TextView lotteryHeader;
    private TextView lotteryText;
    private ChipGroup tagContainer;

    private View organizerContainer;
    private ImageView organizerImage;
    private TextView organizerName;

    public static EventDetailsDialogFragment newInstance(
            String eventId,
            String name,
            String description,
            String time,
            String location,
            int waitlistCount,
            ArrayList<String> tags
    ) {
        return newInstance(eventId, name, description, time, location, waitlistCount, tags, false, null);
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
        return newInstance(eventId, name, description, time, location, waitlistCount, tags, isOrganizerView, null);
    }

    public static EventDetailsDialogFragment newInstance(
            String eventId,
            String name,
            String description,
            String time,
            String location,
            int waitlistCount,
            ArrayList<String> tags,
            boolean isOrganizerView,
            String currentDeviceID
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
        args.putString("currentDeviceID", currentDeviceID);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View view = inflater.inflate(R.layout.dialog_event_details, container, false);

        title = view.findViewById(R.id.detail_title);
        attendees = view.findViewById(R.id.detail_attendees);
        descriptionLabel = view.findViewById(R.id.description_label);
        description = view.findViewById(R.id.detail_description);
        dateLocation = view.findViewById(R.id.detail_date_location);
        banner = view.findViewById(R.id.detail_banner);
        favoriteButton = view.findViewById(R.id.btn_favorite);
        closeButton = view.findViewById(R.id.btn_close);
        closeButtonBottom = view.findViewById(R.id.btn_close_bottom);
        lotteryContainer = view.findViewById(R.id.lottery_container);
        lotteryHeader = view.findViewById(R.id.detail_lottery_header);
        lotteryText = view.findViewById(R.id.detail_lottery_text);
        actionButton = view.findViewById(R.id.scan_button);
        notifyButton = view.findViewById(R.id.notify_button);
        messageButton = view.findViewById(R.id.message_button);
        deleteButton = view.findViewById(R.id.delete_event_button);
        editButton = view.findViewById(R.id.edit_event_button);
        tagContainer = view.findViewById(R.id.detail_tags_container);

        organizerContainer = view.findViewById(R.id.organizer_container);
        organizerImage = view.findViewById(R.id.organizer_image);
        organizerName = view.findViewById(R.id.organizer_name);

        RecyclerView rvComments = view.findViewById(R.id.rv_comments);
        EditText etCommentInput = view.findViewById(R.id.et_comment_input);
        TextView tvCommentError = view.findViewById(R.id.tv_comment_error);
        Button btnPostComment = view.findViewById(R.id.btn_post_comment);

        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentAdapter = new EventCommentAdapter();
        rvComments.setAdapter(commentAdapter);

        Bundle args = getArguments();
        ArrayList<String> tagsFromArgs = null;
        if (args != null) {
            eventId = args.getString("eventId");
            isOrganizerView = args.getBoolean("isOrganizerView", false);
            currentDeviceID = args.getString("currentDeviceID");
            tagsFromArgs = args.getStringArrayList("tags");
        }

        if (currentDeviceID == null) {
            currentDeviceID = android.provider.Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID
            );
        }

        boolean isAdmin = DBProxy.getInstance().getAdmin(currentDeviceID) != null;
        commentAdapter.setup(currentDeviceID, isAdmin, comment -> {
            new com.example.cipher_events.comment.EntrantCommentService()
                    .deleteComment(eventId, comment.getCommentID());
            refreshUI();
        });

        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dismiss());
        }
        if (closeButtonBottom != null) {
            closeButtonBottom.setOnClickListener(v -> dismiss());
        }

        setupViewMode();
        displayTags(tagsFromArgs);

        btnPostComment.setOnClickListener(v -> {
            String commentText = etCommentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                tvCommentError.setText("Comment cannot be empty");
                tvCommentError.setVisibility(View.VISIBLE);
            } else if (commentText.length() > 200) {
                tvCommentError.setText("Comment is too long (max 200 chars)");
                tvCommentError.setVisibility(View.VISIBLE);
            } else {
                tvCommentError.setVisibility(View.GONE);
                postComment(commentText);
                etCommentInput.setText("");
            }
        });

        etCommentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCommentError.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        refreshUI();

        return view;
    }

    private void displayTags(ArrayList<String> tags) {
        if (tagContainer == null) return;
        tagContainer.removeAllViews();
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                Chip chip = new Chip(requireContext());
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.input_background);
                chip.setTextColor(Color.WHITE);
                chip.setChipStrokeWidth(0);
                tagContainer.addView(chip);
            }
            tagContainer.setVisibility(View.VISIBLE);
            if (getView() != null) getView().findViewById(R.id.detail_tags_label).setVisibility(View.VISIBLE);
        } else {
            tagContainer.setVisibility(View.GONE);
            if (getView() != null) getView().findViewById(R.id.detail_tags_label).setVisibility(View.GONE);
        }
    }

    private void setupViewMode() {
        if (isOrganizerView) {
            actionButton.setText("View Waitlist");
            actionButton.setBackgroundTintList(requireContext().getColorStateList(R.color.button_purple));
            actionButton.setOnClickListener(v -> {
                dismiss();
                WaitingListFragment fragment = WaitingListFragment.newInstance(eventId, "organizer");
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });

            lotteryContainer.setVisibility(View.GONE);
            notifyButton.setVisibility(View.VISIBLE);
            notifyButton.setOnClickListener(v -> showNotificationInputDialog());

            messageButton.setText("Messages");
            messageButton.setVisibility(View.VISIBLE);
            messageButton.setOnClickListener(v -> openOrganizerMessages());

            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v -> showEditEventDialog());
            
            favoriteButton.setVisibility(View.GONE);
            if (organizerContainer != null) organizerContainer.setVisibility(View.GONE);
        } else {
            actionButton.setText("Scan to Join");
            actionButton.setOnClickListener(v -> {
                if (eventId != null) {
                    QrScannerDialogFragment qrDialog = QrScannerDialogFragment.newInstance(eventId);
                    qrDialog.show(getParentFragmentManager(), "QrScannerDialog");
                } else {
                    Toast.makeText(getContext(), "Error: Event ID missing", Toast.LENGTH_SHORT).show();
                }
            });
            
            lotteryContainer.setVisibility(View.VISIBLE);
            lotteryText.setText(
                    "⚠️ Disclaimer\n" +
                            "Some events use a lottery system when more people join than there are available spots.\n\n" +
                            "What this means for you:\n" +
                            "• When you join, you're entered into the lottery.\n" +
                            "• Everyone who joins before the deadline has the same chance.\n" +
                            "• Joining earlier does not increase your odds.\n" +
                            "• If you're selected, you'll receive a confirmation.\n" +
                            "• If you're not selected, you may be placed on a waitlist.\n\n" +
                            "This system helps keep things fair and avoids first-come-first-served pressure."
            );

            messageButton.setText("Message Organizer");
            messageButton.setVisibility(View.VISIBLE);
            messageButton.setOnClickListener(v -> openEntrantChat());

            deleteButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);

            favoriteButton.setVisibility(View.VISIBLE);
            if (organizerContainer != null) organizerContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showEditEventDialog() {
        Event event = db.getEvent(eventId);
        if (event == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Event Details");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etTitle = new EditText(requireContext());
        etTitle.setHint("Event Title");
        etTitle.setText(event.getName());
        layout.addView(etTitle);

        final EditText etDescription = new EditText(requireContext());
        etDescription.setHint("Description");
        etDescription.setText(event.getDescription());
        etDescription.setMinLines(2);
        layout.addView(etDescription);

        final EditText etLocation = new EditText(requireContext());
        etLocation.setHint("Location");
        etLocation.setText(event.getLocation());
        layout.addView(etLocation);

        final EditText etDate = new EditText(requireContext());
        etDate.setHint("Date (YYYY-MM-DD)");
        etDate.setFocusable(false);
        String currentDateTime = event.getTime() != null ? event.getTime() : "";
        String currentDate = "";
        String currentTime = "";
        if (currentDateTime.contains(" ")) {
            String[] parts = currentDateTime.split(" ");
            if (parts.length > 0) currentDate = parts[0];
            if (parts.length > 1) currentTime = parts[1];
        } else {
            currentDate = currentDateTime;
        }
        etDate.setText(currentDate);
        etDate.setOnClickListener(v -> showDatePicker(etDate));
        layout.addView(etDate);

        final EditText etTime = new EditText(requireContext());
        etTime.setHint("Time (HH:MM)");
        etTime.setFocusable(false);
        etTime.setText(currentTime);
        etTime.setOnClickListener(v -> showTimePicker(etTime));
        layout.addView(etTime);

        final EditText etCapacity = new EditText(requireContext());
        etCapacity.setHint("Waitlist Capacity (leave empty for unlimited)");
        etCapacity.setText(event.getWaitingListCapacity() != null ? String.valueOf(event.getWaitingListCapacity()) : "");
        etCapacity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etCapacity);

        builder.setView(layout);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            String newDesc = etDescription.getText().toString().trim();
            String newLoc = etLocation.getText().toString().trim();
            String newDateStr = etDate.getText().toString().trim();
            String newTimeStr = etTime.getText().toString().trim();
            String capStr = etCapacity.getText().toString().trim();

            if (newTitle.isEmpty()) {
                Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            event.setName(newTitle);
            event.setDescription(newDesc);
            event.setLocation(newLoc);
            event.setTime(newDateStr + " " + newTimeStr);
            
            if (!capStr.isEmpty()) {
                try {
                    event.setWaitingListCapacity(Integer.parseInt(capStr));
                } catch (NumberFormatException ignored) {}
            } else {
                event.setWaitingListCapacity(null);
            }

            db.updateEvent(event);
            Toast.makeText(getContext(), "Event updated!", Toast.LENGTH_SHORT).show();
            refreshUI();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDatePicker(EditText etDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year1, month1, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            etDate.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker(EditText etTime) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minute1) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
            etTime.setText(time);
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Event event = db.getEvent(eventId);
                    if (event != null) {
                        db.deleteEvent(event);
                        Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openOrganizerMessages() {
        if (eventId == null || currentDeviceID == null) return;
        dismiss();
        MessageThreadsFragment fragment = MessageThreadsFragment.newInstance(eventId, currentDeviceID);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openEntrantChat() {
        if (eventId == null || currentDeviceID == null) return;

        try {
            MessageThread thread = messagingService.openThread(eventId, currentDeviceID);
            dismiss();
            DirectChatFragment fragment = DirectChatFragment.newInstance(
                    eventId,
                    thread.getThreadID(),
                    currentDeviceID,
                    false
            );
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadComments() {
        Event event = db.getEvent(eventId);
        if (event != null && event.getComments() != null) {
            commentAdapter.setComments(event.getComments());
        }
    }

    private void postComment(String message) {
        User currentUser = db.getCurrentUser();
        String deviceID = currentUser != null ? currentUser.getDeviceID() : "unknown";
        String authorName = (currentUser != null) ? currentUser.getName() : "Anonymous";
        String role = isOrganizerView ? "organizer" : "entrant";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());

        EventComment newComment = new EventComment(deviceID, authorName, role, message, timestamp);
        Event event = db.getEvent(eventId);
        if (event != null) {
            event.getComments().add(newComment);
            db.updateEvent(event);
            Toast.makeText(getContext(), "Comment posted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotificationInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Send Notification");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);

        final EditText titleInput = new EditText(requireContext());
        titleInput.setHint("Notification Title");
        layout.addView(titleInput);

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
            String titleStr = titleInput.getText().toString().trim();
            String bodyStr = bodyInput.getText().toString().trim();
            String selectedGroup = groupSpinner.getSelectedItem().toString();

            if (!titleStr.isEmpty() && !bodyStr.isEmpty()) {
                sendNotificationToGroup(titleStr, bodyStr, selectedGroup);
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
        if (e == null) return;
        Message m = new Message(title, body, e.getOrganizer());
        ArrayList<User> entrants = e.getInvitedEntrants();
        if (entrants != null) {
            for (User user : entrants) {
                notifier.sendMessage(user.getDeviceID(), m);
            }
        }
    }

    private void notifyCancelledEntrants(String title, String body) {
        Event e = db.getEvent(eventId);
        if (e == null) return;
        Message m = new Message(title, body, e.getOrganizer());
        ArrayList<User> entrants = e.getCancelledEntrants();
        if (entrants != null) {
            for (User user : entrants) {
                notifier.sendMessage(user.getDeviceID(), m);
            }
        }
    }

    private void notifyEnrolledEntrants(String title, String body) {
        Event e = db.getEvent(eventId);
        if (e == null) return;
        Message m = new Message(title, body, e.getOrganizer());
        ArrayList<User> entrants = e.getEnrolledEntrants();
        if (entrants != null) {
            for (User user : entrants) {
                notifier.sendMessage(user.getDeviceID(), m);
            }
        }
    }

    private void refreshUI() {
        Event event = db.getEvent(eventId);
        if (event != null) {
            title.setText(event.getName());
            int count = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
            attendees.setText(count + " people in waitlist");
            
            String desc = event.getDescription();
            if (desc != null && !desc.isEmpty()) {
                description.setText(desc);
                descriptionLabel.setVisibility(View.VISIBLE);
                description.setVisibility(View.VISIBLE);
            } else {
                descriptionLabel.setVisibility(View.GONE);
                description.setVisibility(View.GONE);
            }
            
            dateLocation.setText(event.getTime() + " • " + event.getLocation());

            if (event.getPosterPictureURL() != null && !event.getPosterPictureURL().isEmpty()) {
                Glide.with(this).load(event.getPosterPictureURL()).placeholder(R.drawable.gray_placeholder).into(banner);
            } else {
                banner.setImageResource(R.drawable.gray_placeholder);
            }

            // Organizer UI
            String organizerId = event.getOrganizerID();
            Organizer organizer = (organizerId != null) ? db.getOrganizer(organizerId) : null;
            if (organizer != null && organizerName != null) {
                organizerName.setText(organizer.getName());
                if (organizer.getProfilePictureURL() != null && !organizer.getProfilePictureURL().isEmpty()) {
                    Glide.with(this)
                            .load(organizer.getProfilePictureURL())
                            .placeholder(R.drawable.gray_placeholder)
                            .into(organizerImage);
                } else {
                    organizerImage.setImageResource(R.drawable.gray_placeholder);
                }
            }

            // Favourite logic
            User currentUser = db.getCurrentUser();
            if (currentUser != null && favoriteButton != null && !isOrganizerView) {
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

            loadComments();
        }
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

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.95);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
