package com.example.cipher_events.pages;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;

public class AdminEventDetailsDialogFragment extends DialogFragment implements DBProxy.OnDataChangedListener {

    private String eventId;
    private DBProxy db = DBProxy.getInstance();

    private TextView titleText;
    private TextView waitlistCountText;
    private TextView dateLocationText;
    private TextView descriptionText;
    private ImageView bannerImage;
    private ImageView deleteBannerIcon;

    public static AdminEventDetailsDialogFragment newInstance(String eventId) {
        AdminEventDetailsDialogFragment fragment = new AdminEventDetailsDialogFragment();
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

        View view = inflater.inflate(R.layout.dialog_admin_event_details, container, false);

        titleText = view.findViewById(R.id.admin_detail_title);
        waitlistCountText = view.findViewById(R.id.admin_detail_waitlist_count);
        dateLocationText = view.findViewById(R.id.admin_detail_date_location);
        descriptionText = view.findViewById(R.id.admin_detail_description);
        bannerImage = view.findViewById(R.id.admin_detail_banner);
        deleteBannerIcon = view.findViewById(R.id.admin_delete_banner_icon);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            refreshUI();
        }

        deleteBannerIcon.setOnClickListener(v -> showDeleteConfirmation());

        return view;
    }

    private void showDeleteConfirmation() {
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
            event.setPosterPictureURL("");
            db.updateEvent(event);
            refreshUI();
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

    private void refreshUI() {
        Event event = db.getEvent(eventId);
        if (event != null) {
            titleText.setText(event.getName());
            int count = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
            waitlistCountText.setText(count + " people in waitlist");
            dateLocationText.setText(event.getLocation() + " • " + event.getTime());
            descriptionText.setText(event.getDescription());

            if (event.getPosterPictureURL() != null && !event.getPosterPictureURL().isEmpty()) {
                Glide.with(this).load(event.getPosterPictureURL()).into(bannerImage);
            } else {
                bannerImage.setImageResource(R.drawable.gray_placeholder);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}