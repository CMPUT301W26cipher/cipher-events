package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.R;
import com.example.cipher_events.database.Organizer;

public class OrganizerProfileFragment extends Fragment {

    private TextView nameText, emailText, phoneText;
    private EditText nameEdit, emailEdit, phoneEdit;

    // TEMP organizer — replace with real Firebase or DB organizer later
    private Organizer currentOrganizer;

    public OrganizerProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_profile, container, false);

        // Load an organizer (replace with real organizer later)
        currentOrganizer = new Organizer(
                "Organizer Name",
                "organizer@example.com",
                "pass123",
                "987-654-3210",
                null
        );

        // Bind views
        nameText = view.findViewById(R.id.organizer_profile_name);
        emailText = view.findViewById(R.id.organizer_profile_email);
        phoneText = view.findViewById(R.id.organizer_profile_phone);

        nameEdit = view.findViewById(R.id.organizer_profile_name_edit);
        emailEdit = view.findViewById(R.id.organizer_profile_email_edit);
        phoneEdit = view.findViewById(R.id.organizer_profile_phone_edit);

        // Load data into UI
        nameText.setText(currentOrganizer.getName());
        emailText.setText(currentOrganizer.getEmail());
        phoneText.setText(currentOrganizer.getPhoneNumber());

        // Setup editable fields
        setupEditableField(nameText, nameEdit, "name");
        setupEditableField(emailText, emailEdit, "email");
        setupEditableField(phoneText, phoneEdit, "phone");

        return view;
    }

    private void setupEditableField(TextView textView, EditText editText, String fieldType) {
        textView.setOnClickListener(v -> {
            editText.setText(textView.getText());
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            editText.requestFocus();
        });

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String newValue = editText.getText().toString();
                textView.setText(newValue);

                // Update the Organizer object
                switch (fieldType) {
                    case "name":
                        currentOrganizer.setName(newValue);
                        break;
                    case "email":
                        currentOrganizer.setEmail(newValue);
                        break;
                    case "phone":
                        currentOrganizer.setPhoneNumber(newValue);
                        break;
                }

                editText.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }
}