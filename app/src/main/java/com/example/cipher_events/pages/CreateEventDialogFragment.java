package com.example.cipher_events.pages;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cipher_events.R;

public class CreateEventDialogFragment extends DialogFragment {

    public interface CreateEventListener {
        void onEventCreated(String title, String date, String time, String location, String description, Integer capacity);

    }

    private CreateEventListener listener;

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
        Button btnAddEvent = view.findViewById(R.id.btn_add_event);

        btnAddEvent.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String date = etDate.getText().toString();
            String time = etTime.getText().toString();
            String location = etLocation.getText().toString();
            String description = etDescription.getText().toString();
            String capacityStr = etCapacity != null ? etCapacity.getText().toString() : "";
            Integer capacity = null;
            if(capacityStr.isEmpty()) {
                try {
                    capacity = Integer.parseInt(capacityStr);
                } catch (NumberFormatException ignored) {}
            }

            if (listener != null) {
                listener.onEventCreated(title, date, time, location, description, capacity);
            }
            dismiss();
        });

        return view;
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