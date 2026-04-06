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
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    private Button organizerMessageButton;
    private Button deleteButton;
    private Button editButton;
    private View lotteryContainer;
    private TextView lotteryHeader;
    private TextView lotteryText;
    private ChipGroup tagContainer;

    private View organizerContainer;
    private ImageView organizerImage;
    private TextView organizerName;

    private View organizerBadge;
    private View organizerStatsCard;
    private TextView tvStatsWaitlist;
    private TextView tvStatsCapacity;
    private View entrantActionContainer;
    private View organizerActionContainer;
    private Button organizerViewWaitlistButton;

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
        organizerMessageButton = view.findViewById(R.id.organizer_message_button);
        deleteButton = view.findViewById(R.id.delete_event_button);
        editButton = view.findViewById(R.id.edit_event_button);
        tagContainer = view.findViewById(R.id.detail_tags_container);

        organizerContainer = view.findViewById(R.id.organizer_container);
        organizerImage = view.findViewById(R.id.organizer_image);
        organizerName = view.findViewById(R.id.organizer_name);

        organizerBadge = view.findViewById(R.id.organizer_badge);
        organizerStatsCard = view.findViewById(R.id.organizer_stats_card);
        tvStatsWaitlist = view.findViewById(R.id.tv_stats_waitlist);
        tvStatsCapacity = view.findViewById(R.id.tv_stats_capacity);
        entrantActionContainer = view.findViewById(R.id.entrant_action_container);
        organizerActionContainer = view.findViewById(R.id.organizer_action_container);
        organizerViewWaitlistButton = view.findViewById(R.id.organizer_view_waitlist_button);

        RecyclerView rvComments = view.findViewById(R.id.rv_comments);
        EditText etCommentInput = view.findViewById(R.id.et_comment_input);
        TextView tvCommentError = view.findViewById(R.id.tv_comment_error);
        Button btnPostComment = view.findViewById(R.id.btn_post_comment);

        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentAdapter = new EventCommentAdapter();
        rvComments.setAdapter(commentAdapter);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
            isOrganizerView = args.getBoolean("isOrganizerView", false);
            currentDeviceID = args.getString("currentDeviceID");
        }

        if (currentDeviceID == null && db.getCurrentUser() != null) {
            currentDeviceID = db.getCurrentUser().getDeviceID();
        }

        if (currentDeviceID == null) {
            currentDeviceID = android.provider.Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID
            );
        }

        boolean isAdmin = DBProxy.getInstance().getAdmin(currentDeviceID) != null;
        commentAdapter.setup(currentDeviceID, isAdmin, isOrganizerView, comment -> {
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
            if (getView() != null && getView().findViewById(R.id.detail_tags_label) != null) {
                getView().findViewById(R.id.detail_tags_label).setVisibility(View.VISIBLE);
            }
        } else {
            tagContainer.setVisibility(View.GONE);
            if (getView() != null && getView().findViewById(R.id.detail_tags_label) != null) {
                getView().findViewById(R.id.detail_tags_label).setVisibility(View.GONE);
            }
        }
    }

    private void setupViewMode() {
        if (isOrganizerView) {
            organizerBadge.setVisibility(View.VISIBLE);
            organizerStatsCard.setVisibility(View.VISIBLE);
            organizerActionContainer.setVisibility(View.VISIBLE);
            entrantActionContainer.setVisibility(View.GONE);
            
            organizerViewWaitlistButton.setOnClickListener(v -> {
                dismiss();
                WaitingListFragment fragment = WaitingListFragment.newInstance(eventId, "organizer");
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });

            lotteryContainer.setVisibility(View.GONE);
            notifyButton.setOnClickListener(v -> showNotificationInputDialog());

            if (organizerMessageButton != null) {
                organizerMessageButton.setOnClickListener(v -> openOrganizerMessages());
            }

            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
            editButton.setOnClickListener(v -> showEditEventDialog());
            
            favoriteButton.setVisibility(View.GONE);
            if (organizerContainer != null) organizerContainer.setVisibility(View.GONE);
        } else {
            organizerBadge.setVisibility(View.GONE);
            organizerStatsCard.setVisibility(View.GONE);
            organizerActionContainer.setVisibility(View.GONE);
            entrantActionContainer.setVisibility(View.VISIBLE);

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

            if (messageButton != null) {
                messageButton.setText("Message");
                messageButton.setVisibility(View.VISIBLE);
                messageButton.setOnClickListener(v -> openEntrantChat());
            }

            favoriteButton.setVisibility(View.VISIBLE);
            if (organizerContainer != null) organizerContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showEditEventDialog() {
        Event event = db.getEvent(eventId);
        if (event == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_event, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView ivBanner = dialogView.findViewById(R.id.iv_edit_event_banner);
        EditText etBannerUrl = dialogView.findViewById(R.id.et_edit_event_banner_url);
        EditText etTitle = dialogView.findViewById(R.id.et_edit_event_title);
        EditText etDescription = dialogView.findViewById(R.id.et_edit_event_description);
        EditText etLocation = dialogView.findViewById(R.id.et_edit_event_location);
        EditText etDate = dialogView.findViewById(R.id.et_edit_event_date);
        EditText etTime = dialogView.findViewById(R.id.et_edit_event_time);
        EditText etCapacity = dialogView.findViewById(R.id.et_edit_event_capacity);
        EditText etTags = dialogView.findViewById(R.id.et_edit_event_tags);
        SwitchMaterial swPublic = dialogView.findViewById(R.id.switch_edit_event_public);
        Button btnCancel = dialogView.findViewById(R.id.btn_edit_event_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_edit_event_save);

        // Set current values
        etTitle.setText(event.getName());
        etDescription.setText(event.getDescription());
        etLocation.setText(event.getLocation());
        
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
        etTime.setText(currentTime);
        etCapacity.setText(event.getWaitingListCapacity() != null ? String.valueOf(event.getWaitingListCapacity()) : "");
        
        if (event.getTags() != null) {
            StringBuilder tagsBuilder = new StringBuilder();
            for (int i = 0; i < event.getTags().size(); i++) {
                tagsBuilder.append(event.getTags().get(i));
                if (i < event.getTags().size() - 1) tagsBuilder.append(", ");
            }
            etTags.setText(tagsBuilder.toString());
        }
        swPublic.setChecked(event.isPublicEvent());

        // Banner loading
        String currentBannerUrl = event.getPosterPictureURL();
        etBannerUrl.setText(currentBannerUrl != null ? currentBannerUrl : "");
        if (currentBannerUrl != null && !currentBannerUrl.isEmpty()) {
            Glide.with(this).load(currentBannerUrl).placeholder(R.drawable.gray_placeholder).into(ivBanner);
        }

        // Update banner preview when URL changes
        etBannerUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    Glide.with(EventDetailsDialogFragment.this)
                            .load(url)
                            .placeholder(R.drawable.gray_placeholder)
                            .error(R.drawable.gray_placeholder)
                            .into(ivBanner);
                } else {
                    ivBanner.setImageResource(R.drawable.gray_placeholder);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        etDate.setOnClickListener(v -> showDatePicker(etDate));
        etTime.setOnClickListener(v -> showTimePicker(etTime));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newTitle = etTitle.getText().toString().trim();
            String newDesc = etDescription.getText().toString().trim();
            String newLoc = etLocation.getText().toString().trim();
            String newDateStr = etDate.getText().toString().trim();
            String newTimeStr = etTime.getText().toString().trim();
            String capStr = etCapacity.getText().toString().trim();
            String tagsStr = etTags.getText().toString().trim();
            String newBannerUrl = etBannerUrl.getText().toString().trim();

            if (newTitle.isEmpty()) {
                Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            event.setName(newTitle);
            event.setDescription(newDesc);
            event.setLocation(newLoc);
            event.setTime(newDateStr + " " + newTimeStr);
            event.setPosterPictureURL(newBannerUrl.isEmpty() ? null : newBannerUrl);
            
            if (!capStr.isEmpty()) {
                try {
                    event.setWaitingListCapacity(Integer.parseInt(capStr));
                } catch (NumberFormatException ignored) {}
            } else {
                event.setWaitingListCapacity(null);
            }

            ArrayList<String> tagsList = new ArrayList<>();
            if (!tagsStr.isEmpty()) {
                String[] parts = tagsStr.split(",");
                for (String part : parts) {
                    String tag = part.trim();
                    if (!tag.isEmpty()) tagsList.add(tag);
                }
            }
            event.setTags(tagsList);
            event.setPublicEvent(swPublic.isChecked());

            db.updateEvent(event);
            Toast.makeText(getContext(), "Event updated!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            refreshUI();
        });

        dialog.show();
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
        String timestamp = new SimpleDateFormat("yyyy-MM-0dd HH:mm", Locale.getDefault())
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_send_notification, null);
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        AutoCompleteTextView groupSpinner = dialogView.findViewById(R.id.spinner_notification_group);
        EditText titleInput = dialogView.findViewById(R.id.et_notification_title);
        EditText bodyInput = dialogView.findViewById(R.id.et_notification_body);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSend = dialogView.findViewById(R.id.btn_send);

        String[] groups = {"Invited", "Cancelled", "Enrolled"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_dropdown_simple,
                groups
        );
        groupSpinner.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String titleStr = titleInput.getText().toString().trim();
            String bodyStr = bodyInput.getText().toString().trim();
            String selectedGroup = groupSpinner.getText().toString();

            if (!titleStr.isEmpty() && !bodyStr.isEmpty()) {
                sendNotificationToGroup(titleStr, bodyStr, selectedGroup);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(),
                        "Title and message cannot be empty",
                        Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
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
        List<String> deviceIDs = e.getInvitedEntrants().stream()
                .map(User::getDeviceID)
                .collect(Collectors.toList());
        notifier.sendBulkMessages(deviceIDs, m);
    }

    private void notifyCancelledEntrants(String title, String body) {
        Event e = db.getEvent(eventId);
        if (e == null) return;
        Message m = new Message(title, body, e.getOrganizer());
        List<String> deviceIDs = e.getCancelledEntrants().stream()
                .map(User::getDeviceID)
                .collect(Collectors.toList());
        notifier.sendBulkMessages(deviceIDs, m);
    }

    private void notifyEnrolledEntrants(String title, String body) {
        Event e = db.getEvent(eventId);
        if (e == null) return;
        Message m = new Message(title, body, e.getOrganizer());
        List<String> deviceIDs = e.getEnrolledEntrants().stream()
                .map(User::getDeviceID)
                .collect(Collectors.toList());
        notifier.sendBulkMessages(deviceIDs, m);
    }

    private void refreshUI() {
        Event event = db.getEvent(eventId);
        if (event != null) {
            title.setText(event.getName());
            int count = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
            attendees.setText(count + " people in waitlist");
            
            if (isOrganizerView) {
                tvStatsWaitlist.setText(String.valueOf(count));
                Integer cap = event.getWaitingListCapacity();
                tvStatsCapacity.setText(cap != null ? String.valueOf(cap) : "∞");
            }
            
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

            displayTags(event.getTags());
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