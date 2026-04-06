package com.example.cipher_events.pages;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.cipher_events.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * DialogFragment for creating a new event.
 * Provides an aesthetic interface for organizers to input event details.
 */
public class CreateEventDialogFragment extends DialogFragment {

    public interface CreateEventListener {
        void onEventCreated(String title, String date, String time, String location, String description, Integer capacity, String bannerUrl, List<String> tags, boolean isPrivate);
    }

    private CreateEventListener listener;
    private String bannerUrl = null;
    private ImageView ivBannerPreview;
    private TextInputLayout tilTitle, tilDate, tilTime, tilLocation, tilDescription;
    private EditText etTitle, etDate, etTime, etLocation, etDescription, etCapacity;
    private MaterialSwitch swPrivate;
    private ChipGroup cgTags;
    private List<String> tags = new ArrayList<>();

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        bannerUrl = selectedImageUri.toString();
                        Glide.with(this)
                             .load(selectedImageUri)
                             .transition(DrawableTransitionOptions.withCrossFade())
                             .placeholder(R.drawable.gray_placeholder)
                             .centerCrop()
                             .into(ivBannerPreview);
                        ivBannerPreview.animate().alpha(1.0f).setDuration(500).start();
                    }
                }
            }
    );

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
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        initViews(view);
        setupPickers();
        setupTextWatchers();

        // Entry animation
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(300).start();

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
        swPrivate = view.findViewById(R.id.switch_private_event);
        
        cgTags = view.findViewById(R.id.cg_event_tags);
        View btnAddTag = view.findViewById(R.id.btn_add_tag);
        ivBannerPreview = view.findViewById(R.id.iv_banner_preview);
        View btnAddBanner = view.findViewById(R.id.btn_add_banner);
        Button btnAddEvent = view.findViewById(R.id.btn_add_event);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnAddBanner.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });
        btnAddTag.setOnClickListener(v -> showAddTagDialog());
        btnCancel.setOnClickListener(v -> dismissWithAnimation());

        btnAddEvent.setOnClickListener(v -> {
            if (validateForm()) {
                String title = etTitle.getText().toString().trim();
                String date = etDate.getText().toString().trim();
                String time = etTime.getText().toString().trim();
                String location = etLocation.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String capacityStr = etCapacity.getText().toString().trim();
                boolean isPrivate = swPrivate != null && swPrivate.isChecked();

                Integer capacity = null;
                if (!capacityStr.isEmpty()) {
                    try {
                        capacity = Integer.parseInt(capacityStr);
                    } catch (NumberFormatException ignored) {}
                }

                if (listener != null) {
                    listener.onEventCreated(title, date, time, location, description, capacity, bannerUrl, new ArrayList<>(tags), isPrivate);
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
                    .setTheme(R.style.CustomMaterialCalendar)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                etDate.setText(format.format(calendar.getTime()));
                tilDate.setError(null);
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        etTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Event Time")
                    .setTheme(R.style.CustomMaterialTimePicker)
                    .build();

            timePicker.addOnPositiveButtonClickListener(view -> {
                String amPm = timePicker.getHour() >= 12 ? "PM" : "AM";
                int hour = timePicker.getHour() % 12;
                if (hour == 0) hour = 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour, timePicker.getMinute(), amPm);
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

    private void showAddTagDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input_simple, null);
        TextView titleTv = dialogView.findViewById(R.id.dialog_input_title);
        TextView messageTv = dialogView.findViewById(R.id.dialog_input_message);
        EditText inputEt = dialogView.findViewById(R.id.dialog_input_edittext);
        
        titleTv.setText("Add Tag");
        messageTv.setText("Enter a category or keyword for your event");
        inputEt.setHint("e.g. Workshop");

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
               .setView(dialogView)
               .setPositiveButton("Add", (d, which) -> {
                   String tag = inputEt.getText().toString().trim();
                   if (!tag.isEmpty() && !tags.contains(tag)) {
                       addTagChip(tag);
                   }
               })
               .setNegativeButton("Cancel", null)
               .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }

    private void addTagChip(String tag) {
        tags.add(tag);
        Chip chip = new Chip(requireContext());
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.input_background);
        chip.setTextColor(Color.WHITE);
        chip.setCloseIconTintResource(R.color.text_hint);
        chip.setChipStrokeWidth(0);
        
        chip.setOnCloseIconClickListener(v -> {
            AlphaAnimation anim = new AlphaAnimation(1f, 0f);
            anim.setDuration(200);
            chip.startAnimation(anim);
            cgTags.postDelayed(() -> {
                cgTags.removeView(chip);
                tags.remove(tag);
            }, 200);
        });

        cgTags.addView(chip);
        
        // Appear animation
        AlphaAnimation anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(300);
        chip.startAnimation(anim);
    }

    private void dismissWithAnimation() {
        if (getView() != null) {
            getView().animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(this::dismiss)
                    .start();
        } else {
            dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }
    }
}