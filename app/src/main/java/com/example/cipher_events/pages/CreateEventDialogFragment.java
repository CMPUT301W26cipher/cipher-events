package com.example.cipher_events.pages;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cipher_events.R;

import java.util.Calendar;
import java.util.Locale;

public class CreateEventDialogFragment extends DialogFragment {

    public interface CreateEventListener {
        void onEventCreated(String title, String date, String time, String location, String description, Integer capacity, String bannerUrl);
    }

    private CreateEventListener listener;
    private String bannerUrl = null;

    public void setCreateEventListener(CreateEventListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_event, container, false);

        EditText etTitle = view.findViewById(R.id.et_event_title);
        EditText etDate = view.findViewById(R.id.et_event_date);
        EditText etTime = view.findViewById(R.id.et_event_time);
        EditText etLocation = view.findViewById(R.id.et_event_location);
        EditText etDescription = view.findViewById(R.id.et_event_description);
        EditText etCapacity = view.findViewById(R.id.et_waiting_list_capacity);
        View btnAddBanner = view.findViewById(R.id.btn_add_banner);
        Button btnAddEvent = view.findViewById(R.id.btn_add_event);

        // Set up click listeners for date and time fields to show pickers
        etDate.setOnClickListener(v -> showDatePicker(etDate));
        etTime.setOnClickListener(v -> showTimePicker(etTime));

        btnAddBanner.setOnClickListener(v -> showAddBannerDialog());

        btnAddEvent.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String date = etDate.getText().toString();
            String time = etTime.getText().toString();
            String location = etLocation.getText().toString();
            String description = etDescription.getText().toString();
            String capacityStr = etCapacity != null ? etCapacity.getText().toString() : "";
            Integer capacity = null;
            if (!capacityStr.isEmpty()) {
                try {
                    capacity = Integer.parseInt(capacityStr);
                } catch (NumberFormatException ignored) {}
            }

            if (listener != null) {
                listener.onEventCreated(title, date, time, location, description, capacity, bannerUrl);
            }
            dismiss();
        });

        return view;
    }

    private void showAddBannerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Banner URL");

        final EditText input = new EditText(requireContext());
        input.setHint("Paste image address here");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            bannerUrl = input.getText().toString().trim();
            if (!bannerUrl.isEmpty()) {
                Toast.makeText(getContext(), "Banner URL added", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

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

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }
}