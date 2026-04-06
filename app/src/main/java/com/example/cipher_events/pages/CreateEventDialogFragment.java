package com.example.cipher_events.pages;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CreateEventDialogFragment extends DialogFragment {

    public interface CreateEventListener {
        void onEventCreated(String title, String date, String time, String location, String description, Integer capacity, String bannerUrl, List<String> tags);
    }

    private CreateEventListener listener;
    private String bannerUrl = null;
    private ImageView ivBannerPreview;
    private TextInputLayout tilTitle, tilDate, tilTime, tilLocation, tilDescription;
    private EditText etTitle, etDate, etTime, etLocation, etDescription, etCapacity;
    private ChipGroup cgTags;
    private List<String> tags = new ArrayList<>();

    public void setCreateEventListener(CreateEventListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_event, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        initViews(view);
        setupPickers();
        setupTextWatchers();

        return view;
    }

    private void initViews(View view) {
        tilTitle = view.findViewById(R.id.til_title);
        tilDate = view.findViewById(R.id.til_date);
        tilTime = view.findViewById(R.id.til_time);
        tilLocation = view.findViewById(R.id.til_location);
        tilDescription = view.findViewById(R.id.til_description);

        etTitle = view.findViewById(R.id.et_event_title);
        etDate = view.findViewById(R.id.et_event_date);
        etTime = view.findViewById(R.id.et_event_time);
        etLocation = view.findViewById(R.id.et_event_location);
        etDescription = view.findViewById(R.id.et_event_description);
        etCapacity = view.findViewById(R.id.et_waiting_list_capacity);
        
        cgTags = view.findViewById(R.id.cg_event_tags);
        View btnAddTag = view.findViewById(R.id.btn_add_tag);
        ivBannerPreview = view.findViewById(R.id.iv_banner_preview);
        View btnAddBanner = view.findViewById(R.id.btn_add_banner);
        Button btnAddEvent = view.findViewById(R.id.btn_add_event);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnAddBanner.setOnClickListener(v -> showAddBannerDialog());
        btnAddTag.setOnClickListener(v -> showAddTagDialog());
        btnCancel.setOnClickListener(v -> dismiss());

        btnAddEvent.setOnClickListener(v -> {
            if (validateForm()) {
                String title = etTitle.getText().toString().trim();
                String date = etDate.getText().toString().trim();
                String time = etTime.getText().toString().trim();
                String location = etLocation.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String capacityStr = etCapacity.getText().toString().trim();

                Integer capacity = null;
                if (!capacityStr.isEmpty()) {
                    try {
                        capacity = Integer.parseInt(capacityStr);
                    } catch (NumberFormatException ignored) {}
                }

                if (listener != null) {
                    listener.onEventCreated(title, date, time, location, description, capacity, bannerUrl, new ArrayList<>(tags));
                }
                dismiss();
            }
        });
    }

    private void setupPickers() {
        etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Event Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setTheme(com.google.android.material.R.style.Widget_Material3_MaterialCalendar)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                etDate.setText(format.format(calendar.getTime()));
                tilDate.setError(null);
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        etTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Event Time")
                    .setTheme(com.google.android.material.R.style.Widget_Material3_MaterialTimePicker)
                    .build();

            timePicker.addOnPositiveButtonClickListener(view -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", timePicker.getHour(), timePicker.getMinute());
                etTime.setText(time);
                tilTime.setError(null);
            });

            timePicker.show(getParentFragmentManager(), "TIME_PICKER");
        });
    }

    private void setupTextWatchers() {
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                tilTitle.setError(null);
                tilLocation.setError(null);
            }
        };
        etTitle.addTextChangedListener(clearErrorWatcher);
        etLocation.addTextChangedListener(clearErrorWatcher);
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (TextUtils.isEmpty(etTitle.getText())) {
            tilTitle.setError("Title is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(etDate.getText())) {
            tilDate.setError("Date is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(etTime.getText())) {
            tilTime.setError("Time is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(etLocation.getText())) {
            tilLocation.setError("Location is required");
            isValid = false;
        }

        return isValid;
    }

    private void showAddBannerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.Theme_Material3_Dark_Dialog_Alert);
        
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, 0);

        final EditText input = new EditText(requireContext());
        input.setHint("https://example.com/image.jpg");
        input.setText(bannerUrl != null ? bannerUrl : "");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(getContext().getColor(R.color.text_hint));
        input.setBackground(getContext().getDrawable(R.drawable.input_bg));
        input.setPadding(padding / 2, padding / 2, padding / 2, padding / 2);
        layout.addView(input);

        builder.setTitle("Event Banner URL")
               .setMessage("Provide a direct link to your event poster image.")
               .setView(layout)
               .setPositiveButton("Set Image", (dialog, which) -> {
                   String url = input.getText().toString().trim();
                   if (!url.isEmpty()) {
                       bannerUrl = url;
                       Glide.with(this)
                            .load(bannerUrl)
                            .placeholder(R.drawable.gray_placeholder)
                            .centerCrop()
                            .into(ivBannerPreview);
                       ivBannerPreview.setAlpha(1.0f);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    private void showAddTagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.Theme_Material3_Dark_Dialog_Alert);
        
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, 0);

        final EditText input = new EditText(requireContext());
        input.setHint("Tag Name");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(getContext().getColor(R.color.text_hint));
        input.setBackground(getContext().getDrawable(R.drawable.input_bg));
        input.setPadding(padding / 2, padding / 2, padding / 2, padding / 2);
        layout.addView(input);

        builder.setTitle("Add Tag")
               .setView(layout)
               .setPositiveButton("Add", (dialog, which) -> {
                   String tag = input.getText().toString().trim();
                   if (!tag.isEmpty() && !tags.contains(tag)) {
                       addTagChip(tag);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    private void addTagChip(String tag) {
        tags.add(tag);
        Chip chip = new Chip(requireContext());
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.input_background);
        chip.setTextColor(Color.WHITE);
        chip.setCloseIconTintResource(R.color.text_hint);
        
        chip.setOnCloseIconClickListener(v -> {
            cgTags.removeView(chip);
            tags.remove(tag);
        });

        cgTags.addView(chip);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
