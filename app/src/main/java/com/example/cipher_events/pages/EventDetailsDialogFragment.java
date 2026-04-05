package com.example.cipher_events.pages;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
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
import com.example.cipher_events.database.User;
import com.example.cipher_events.message.MessageThread;
import com.example.cipher_events.message.MessagingService;
import com.example.cipher_events.notifications.Message;
import com.example.cipher_events.notifications.Notifier;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private View lotteryContainer;
    private TextView lotteryHeader;
    private TextView lotteryText;
    private ChipGroup tagContainer;

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
        tagContainer = view.findViewById(R.id.detail_tags_container);

        RecyclerView rvComments = view.findViewById(R.id.rv_comments);
        EditText etCommentInput = view.findViewById(R.id.et_comment_input);
        TextView tvCommentError = view.findViewById(R.id.tv_comment_error);
        Button btnPostComment = view.findViewById(R.id.btn_post_comment);

        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentAdapter = new EventCommentAdapter();
        rvComments.setAdapter(commentAdapter);

        String currentDeviceId = android.provider.Settings.Secure.getString(
                requireContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );
        boolean isAdmin = DBProxy.getInstance().getAdmin(currentDeviceId) != null;
        commentAdapter.setup(currentDeviceId, isAdmin, comment -> {
            new com.example.cipher_events.comment.EntrantCommentService()
                    .deleteComment(eventId, comment.getCommentID());
            refreshUI();
        });

        Bundle args = getArguments();
        ArrayList<String> tagsFromArgs = null;
        if (args != null) {
            eventId = args.getString("eventId");
            isOrganizerView = args.getBoolean("isOrganizerView", false);
            currentDeviceID = args.getString("currentDeviceID");
            tagsFromArgs = args.getStringArrayList("tags");
        }

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
            
            favoriteButton.setVisibility(View.GONE);
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

            favoriteButton.setVisibility(View.VISIBLE);
        }
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
