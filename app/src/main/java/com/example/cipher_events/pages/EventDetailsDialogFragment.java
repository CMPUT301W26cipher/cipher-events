package com.example.cipher_events.pages;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * A unified DialogFragment that displays event details.
 * Supports three view modes: Entrant (default), Organizer, and Admin.
 */
public class EventDetailsDialogFragment extends DialogFragment implements DBProxy.OnDataChangedListener {

    private boolean isOrganizerView = false;
    private boolean isAdminView = false;
    private String eventId;
    private final DBProxy db = DBProxy.getInstance();
    private EventCommentAdapter commentAdapter;

    private TextView title;
    private TextView attendees;
    private TextView description;
    private TextView dateLocation;
    private ImageView banner;
    private ImageView btnActionTopLeft; // Favorite (Entrant) or Delete Banner (Admin/Organizer)

    /**
     * Standard instance for Entrant view.
     */
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

    /**
     * Instance with optional Organizer view flag.
     */
    public static EventDetailsDialogFragment newInstance(
            String eventId,
            String name,
            String description,
            String time,
            String location, int waitlistCount,
            ArrayList<String> tags,
            boolean isOrganizerView
    ) {
        return newInstance(eventId, name, description, time, location, waitlistCount, tags, isOrganizerView, null);
    }

    /**
     * Specialized instance for Admin view.
     */
    public static EventDetailsDialogFragment newAdminInstance(String eventId) {
        EventDetailsDialogFragment fragment = new EventDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putBoolean("isAdminView", true);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Full parameter instance.
     */
    public static EventDetailsDialogFragment newInstance(
            String eventId,
            String name,
            String description,
            String time,
            String location, int waitlistCount,
            ArrayList<String> tags,
            boolean isOrganizerView,
            String currentUserId
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
        args.putString("currentUserId", currentUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogWide);
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

        // UI components
        title = view.findViewById(R.id.detail_title);
        attendees = view.findViewById(R.id.detail_attendees);
        TextView descriptionLabel = view.findViewById(R.id.description_label);
        description = view.findViewById(R.id.detail_description);
        dateLocation = view.findViewById(R.id.detail_date_location);
        banner = view.findViewById(R.id.detail_banner);
        View lotteryContainer = view.findViewById(R.id.lottery_container);
        TextView lotteryHeader = view.findViewById(R.id.detail_lottery_header);
        TextView lotteryText = view.findViewById(R.id.detail_lottery_text);
        Button actionButton = view.findViewById(R.id.scan_button);
        Button messageButton = view.findViewById(R.id.message_button);
        ChipGroup tagContainer = view.findViewById(R.id.detail_tags_container);
        ImageView btnClose = view.findViewById(R.id.btn_close);
        Button btnCloseBottom = view.findViewById(R.id.btn_close_bottom);
        TextView roleBadge = view.findViewById(R.id.organizer_badge);
        btnActionTopLeft = view.findViewById(R.id.btn_favorite);

        // Organizer Action Dashboard
        View organizerActionContainer = view.findViewById(R.id.organizer_action_container);
        Button btnViewWaitlist = view.findViewById(R.id.organizer_view_waitlist_button);
        Button btnViewMap = view.findViewById(R.id.organizer_view_map_button);
        Button btnNotify = view.findViewById(R.id.notify_button);
        Button btnOrganizerMessages = view.findViewById(R.id.organizer_message_button);
        Button btnEdit = view.findViewById(R.id.edit_event_button);
        Button btnDelete = view.findViewById(R.id.delete_event_button);

        // Comments
        RecyclerView rvComments = view.findViewById(R.id.rv_comments);
        EditText etCommentInput = view.findViewById(R.id.et_comment_input);
        TextView tvCommentError = view.findViewById(R.id.tv_comment_error);
        Button btnPostComment = view.findViewById(R.id.btn_post_comment);

        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentAdapter = new EventCommentAdapter();
        rvComments.setAdapter(commentAdapter);

        if (btnClose != null) btnClose.setOnClickListener(v -> dismiss());
        if (btnCloseBottom != null) btnCloseBottom.setOnClickListener(v -> dismiss());

        // Parse args
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
            isOrganizerView = args.getBoolean("isOrganizerView", false);
            isAdminView = args.getBoolean("isAdminView", false);

            ArrayList<String> tags = args.getStringArrayList("tags");
            if (tags != null && tagContainer != null) {
                tagContainer.removeAllViews();
                for (String tag : tags) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(tag);
                    tagContainer.addView(chip);
                }
            }
        }

        // Apply View Configuration
        if (isAdminView) {
            configureAdminView(roleBadge, actionButton, messageButton, lotteryContainer, organizerActionContainer, 
                               btnDelete, btnNotify, btnOrganizerMessages, btnEdit, btnViewWaitlist, btnViewMap);
        } else if (isOrganizerView) {
            configureOrganizerView(roleBadge, actionButton, lotteryContainer, organizerActionContainer, 
                                   btnViewWaitlist, btnViewMap, btnDelete, btnNotify, btnOrganizerMessages, btnEdit);
        } else {
            configureEntrantView(roleBadge, actionButton, messageButton, lotteryContainer, organizerActionContainer, 
                                 lotteryHeader, lotteryText, descriptionLabel);
        }

        // Common Setup
        if (btnPostComment != null) {
            btnPostComment.setOnClickListener(v -> handlePostComment(etCommentInput, tvCommentError));
        }

        if (etCommentInput != null) {
            etCommentInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tvCommentError != null) tvCommentError.setVisibility(View.GONE);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        refreshUI();
        return view;
    }

    private void configureAdminView(TextView roleBadge, Button actionButton, Button messageButton, View lotteryContainer, 
                                    View organizerActionContainer, Button btnDelete, Button btnNotify, 
                                    Button btnMessages, Button btnEdit, Button btnWaitlist, Button btnMap) {
        if (roleBadge != null) {
            roleBadge.setVisibility(View.VISIBLE);
            roleBadge.setText("ADMIN MODE");
            roleBadge.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            roleBadge.setTextColor(Color.WHITE);
        }
        if (actionButton != null) actionButton.setVisibility(View.GONE);
        if (messageButton != null) messageButton.setVisibility(View.GONE);
        if (lotteryContainer != null) lotteryContainer.setVisibility(View.GONE);
        
        if (organizerActionContainer != null) {
            organizerActionContainer.setVisibility(View.VISIBLE);
            organizerActionContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1A000000")));
            
            if (btnNotify != null) btnNotify.setVisibility(View.GONE);
            if (btnMessages != null) btnMessages.setVisibility(View.GONE);
            if (btnEdit != null) btnEdit.setVisibility(View.GONE);
            
            if (btnWaitlist != null) btnWaitlist.setOnClickListener(v -> {
                dismiss();
                WaitingListFragment fragment = WaitingListFragment.newInstance(eventId, "organizer");
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            });

            if (btnMap != null) btnMap.setOnClickListener(v -> {
                dismiss();
                EventMapFragment fragment = EventMapFragment.newInstance(eventId);
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            });

            if (btnDelete != null) {
                btnDelete.setText("Remove Event from System");
                btnDelete.setOnClickListener(v -> showRemoveEventConfirmation());
            }
        }

        if (btnActionTopLeft != null) {
            btnActionTopLeft.setImageResource(R.drawable.baseline_close_24);
            btnActionTopLeft.setOnClickListener(v -> showDeleteBannerConfirmation());
        }
    }

    private void configureOrganizerView(TextView roleBadge, Button actionButton, View lotteryContainer, View organizerActionContainer,
                                        Button btnViewWaitlist, Button btnViewMap, Button btnDelete, Button btnNotify, 
                                        Button btnOrganizerMessages, Button btnEdit) {
        if (roleBadge != null) {
            roleBadge.setVisibility(View.VISIBLE);
            roleBadge.setText(R.string.organizer_mode);
        }
        if (actionButton != null) actionButton.setVisibility(View.GONE);
        if (lotteryContainer != null) lotteryContainer.setVisibility(View.GONE);
        if (organizerActionContainer != null) organizerActionContainer.setVisibility(View.VISIBLE);
        
        if (btnViewWaitlist != null) {
            btnViewWaitlist.setOnClickListener(v -> {
                dismiss();
                WaitingListFragment fragment = WaitingListFragment.newInstance(eventId, "organizer");
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            });
        }

        if (btnViewMap != null) {
            btnViewMap.setOnClickListener(v -> {
                dismiss();
                EventMapFragment fragment = EventMapFragment.newInstance(eventId);
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                Event event = db.getEvent(eventId);
                if (event != null) {
                    db.deleteEvent(event);
                    Toast.makeText(getContext(), R.string.event_deleted, Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });
        }

        if (btnNotify != null) btnNotify.setOnClickListener(v -> Toast.makeText(getContext(), R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());
        if (btnOrganizerMessages != null) btnOrganizerMessages.setOnClickListener(v -> Toast.makeText(getContext(), R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());
        if (btnEdit != null) btnEdit.setOnClickListener(v -> Toast.makeText(getContext(), R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());

        if (btnActionTopLeft != null) {
            btnActionTopLeft.setImageResource(R.drawable.baseline_close_24);
            btnActionTopLeft.setOnClickListener(v -> showDeleteBannerConfirmation());
        }
    }

    private void configureEntrantView(TextView roleBadge, Button actionButton, Button messageButton, View lotteryContainer, 
                                      View organizerActionContainer, TextView lotteryHeader, TextView lotteryText, TextView descriptionLabel) {
        if (roleBadge != null) roleBadge.setVisibility(View.GONE);
        if (organizerActionContainer != null) organizerActionContainer.setVisibility(View.GONE);
        
        if (actionButton != null) {
            actionButton.setVisibility(View.VISIBLE);
            actionButton.setText(R.string.scan_info);
            actionButton.setOnClickListener(v -> {
                if (eventId != null) {
                    QrScannerDialogFragment qrDialog = QrScannerDialogFragment.newInstance(eventId);
                    qrDialog.show(getParentFragmentManager(), "QrScannerDialog");
                } else {
                    Toast.makeText(getContext(), R.string.scan_error_id_missing, Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (messageButton != null) {
            messageButton.setVisibility(View.VISIBLE);
            messageButton.setOnClickListener(v -> Toast.makeText(getContext(), R.string.feature_coming_soon, Toast.LENGTH_SHORT).show());
        }

        if (descriptionLabel != null) descriptionLabel.setVisibility(View.GONE);
        if (description != null) description.setVisibility(View.GONE);
        
        if (lotteryContainer != null) lotteryContainer.setVisibility(View.VISIBLE);
        if (lotteryHeader != null) lotteryHeader.setVisibility(View.VISIBLE);
        if (lotteryText != null) {
            lotteryText.setVisibility(View.VISIBLE);
            lotteryText.setText(R.string.lottery_disclaimer);
        }

        if (btnActionTopLeft != null) {
            btnActionTopLeft.setImageResource(R.drawable.baseline_star_border_24);
            btnActionTopLeft.setOnClickListener(v -> Toast.makeText(getContext(), "Added to Favourites", Toast.LENGTH_SHORT).show());
        }
    }

    private void handlePostComment(EditText etInput, TextView tvError) {
        String commentText = etInput.getText().toString().trim();
        if (commentText.isEmpty()) {
            tvError.setText(R.string.comment_empty_error);
            tvError.setVisibility(View.VISIBLE);
        } else if (commentText.length() > 200) {
            tvError.setText(R.string.comment_too_long_error);
            tvError.setVisibility(View.VISIBLE);
        } else {
            tvError.setVisibility(View.GONE);
            postComment(commentText);
            etInput.setText("");
        }
    }

    private void showDeleteBannerConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Banner")
                .setMessage("Are you sure you want to delete this banner?")
                .setPositiveButton("Delete", (dialog, which) -> deleteBanner())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBanner() {
        Event event = db.getEvent(eventId);
        if (event != null) {
            event.setBannerUrl("");
            db.updateEvent(event);
            Toast.makeText(getContext(), "Banner removed", Toast.LENGTH_SHORT).show();
            refreshUI();
        }
    }

    private void showRemoveEventConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Event")
                .setMessage("Are you sure you want to permanently remove this event from the database?")
                .setPositiveButton("Remove", (dialog, which) -> removeEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeEvent() {
        db.deleteEvent(eventId);
        Toast.makeText(getContext(), "Event removed from system", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void loadComments() {
        Event event = db.getEvent(eventId);
        if (event != null && event.getComments() != null) {
            commentAdapter.setComments(event.getComments());
        }
    }

    private void postComment(String message) {
        String deviceID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        User currentUser = db.getAnyUser(deviceID);
        String authorName = (currentUser != null) ? currentUser.getName() : getString(R.string.anonymous);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        
        String authorRole = "entrant";
        if (isAdminView) authorRole = "admin";
        else if (isOrganizerView) authorRole = "organizer";

        EventComment newComment = new EventComment(deviceID, authorName, authorRole, message, timestamp);
        Event event = db.getEvent(eventId);
        if (event != null) {
            event.addComment(newComment);
            db.updateEvent(event);
            loadComments();
        }
    }

    private void refreshUI() {
        Event event = db.getEvent(eventId);
        if (event == null) return;

        if (title != null) title.setText(event.getName());
        if (attendees != null) {
            int count = (event.getWaitlist() != null) ? event.getWaitlist().size() : 0;
            attendees.setText(getString(R.string.joined_count, count));
        }
        
        if (dateLocation != null) {
            dateLocation.setText(getString(R.string.time_location_format, event.getTime(), event.getLocation()));
        }

        if (description != null && (isAdminView || isOrganizerView)) {
            description.setVisibility(View.VISIBLE);
            description.setText(event.getDescription());
        }

        if (banner != null) {
            if (event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
                Glide.with(this).load(event.getBannerUrl()).into(banner);
                if (btnActionTopLeft != null) btnActionTopLeft.setVisibility(View.VISIBLE);
            } else {
                banner.setImageResource(R.drawable.gray_placeholder);
                if (btnActionTopLeft != null && !(!isAdminView && !isOrganizerView)) {
                    btnActionTopLeft.setVisibility(View.GONE);
                }
            }
        }
        
        loadComments();
    }

    @Override
    public void onDataChanged() {
        if (isAdded()) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::refreshUI);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.98);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        db.addListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        db.removeListener(this);
    }
}
