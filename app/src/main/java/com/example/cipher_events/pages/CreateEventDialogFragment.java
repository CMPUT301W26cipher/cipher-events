package com.example.cipher_events.pages;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class CreateEventDialogFragment extends DialogFragment {

    public interface CreateEventListener {
        void onEventCreated(String title, String date, String time, String location,
                            String description, Integer capacity, String bannerUrl,
                            List<String> tags, List<String> coOrganizers, boolean isPrivate);
    }

    private CreateEventListener listener;
    private String bannerUrl = null;
    private ImageView ivBannerPreview;
    private TextInputLayout tilTitle, tilDate, tilTime, tilLocation;
    private EditText etTitle, etDate, etTime, etLocation, etDescription, etCapacity;
    private MaterialSwitch swPrivate;
    private ChipGroup cgTags, cgCoOrganizers;
    private List<String> tags = new ArrayList<>();
    private List<String> coOrganizers = new ArrayList<>();

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

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // This forces the dialog to take up 95% of the screen width
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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
        return view;
    }

    private void initViews(View view) {
        tilTitle = view.findViewById(R.id.til_title);
        tilDate = view.findViewById(R.id.til_date);
        tilTime = view.findViewById(R.id.til_time);
        tilLocation = view.findViewById(R.id.til_location);

        etTitle = view.findViewById(R.id.et_event_title);
        etDate = view.findViewById(R.id.et_event_date);
        etTime = view.findViewById(R.id.et_event_time);
        etLocation = view.findViewById(R.id.et_event_location);
        etDescription = view.findViewById(R.id.et_event_description);
        etCapacity = view.findViewById(R.id.et_waiting_list_capacity);
        swPrivate = view.findViewById(R.id.switch_private_event);

        cgTags = view.findViewById(R.id.cg_event_tags);
        cgCoOrganizers = view.findViewById(R.id.cg_co_organizers);

        view.findViewById(R.id.btn_add_tag).setOnClickListener(v -> showAddTagDialog());
        view.findViewById(R.id.btn_add_co_organizer).setOnClickListener(v -> showAddCoOrganizerDialog());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());

        ivBannerPreview = view.findViewById(R.id.iv_banner_preview);
        view.findViewById(R.id.btn_add_banner).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        view.findViewById(R.id.btn_add_event).setOnClickListener(v -> {
            if (validateForm()) {
                if (listener != null) {
                    listener.onEventCreated(
                            etTitle.getText().toString().trim(),
                            etDate.getText().toString().trim(),
                            etTime.getText().toString().trim(),
                            etLocation.getText().toString().trim(),
                            etDescription.getText().toString().trim(),
                            parseCapacity(),
                            bannerUrl,
                            new ArrayList<>(tags),
                            new ArrayList<>(coOrganizers),
                            swPrivate.isChecked()
                    );
                }
                dismiss();
            }
        });
    }

    private void showAddCoOrganizerDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input_simple, null);
        TextView titleTv = dialogView.findViewById(R.id.dialog_input_title);
        TextView messageTv = dialogView.findViewById(R.id.dialog_input_message);
        EditText inputEt = dialogView.findViewById(R.id.dialog_input_edittext);

        titleTv.setText("Invite Co Organizer");
        messageTv.setText("Enter the email of the co organizer you wish to invite");
        inputEt.setHint("Enter email here");


        new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Invite", (d, which) -> {
                    String email = inputEt.getText().toString().trim().toLowerCase();
                    if (isValidEmail(email) && !coOrganizers.contains(email)) {
                        addChip(cgCoOrganizers, coOrganizers, email);
                    } else {
                        Toast.makeText(getContext(), "Invalid or duplicate email", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddTagDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input_simple, null);
        TextView titleTv = dialogView.findViewById(R.id.dialog_input_title);
        TextView messageTv = dialogView.findViewById(R.id.dialog_input_message);
        EditText inputEt = dialogView.findViewById(R.id.dialog_input_edittext);

        titleTv.setText("Add Tag");
        messageTv.setText("Enter a category or keyword for your event");
        inputEt.setHint("e.g. Workshop");


        new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Add", (d, which) -> {
                    String tag = inputEt.getText().toString().trim();
                    if (!tag.isEmpty() && !tags.contains(tag)) addChip(cgTags, tags, tag);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addChip(ChipGroup group, List<String> list, String text) {
        list.add(text);
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setTextColor(Color.WHITE);
        chip.setChipBackgroundColorResource(R.color.input_background);
        chip.setOnCloseIconClickListener(v -> {
            group.removeView(chip);
            list.remove(text);
        });
        group.addView(chip);
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private Integer parseCapacity() {
        try {
            return Integer.parseInt(etCapacity.getText().toString().trim());
        } catch (Exception e) { return null; }
    }

    private void setupPickers() {
        etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                etDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.getTime()));
            });
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        etTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).build();
            timePicker.addOnPositiveButtonClickListener(view -> {
                int hour = timePicker.getHour();
                String amPm = hour >= 12 ? "PM" : "AM";
                int displayHour = (hour == 0 || hour == 12) ? 12 : hour % 12;
                etTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, timePicker.getMinute(), amPm));
            });
            timePicker.show(getParentFragmentManager(), "TIME_PICKER");
        });
    }

    private boolean validateForm() {
        boolean valid = true;
        if (TextUtils.isEmpty(etTitle.getText())) { tilTitle.setError("Required"); valid = false; }
        if (TextUtils.isEmpty(etDate.getText())) { tilDate.setError("Required"); valid = false; }
        if (TextUtils.isEmpty(etTime.getText())) { tilTime.setError("Required"); valid = false; }
        if (TextUtils.isEmpty(etLocation.getText())) { tilLocation.setError("Required"); valid = false; }
        return valid;
    }
}