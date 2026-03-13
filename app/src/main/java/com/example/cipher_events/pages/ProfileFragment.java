package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cipher_events.pages.WaitingListFragment;

import com.example.cipher_events.R;
import com.example.cipher_events.database.User;

/**
 * Displays the user's profile information (name, email, location)
 * Displays profile photo as well (upon click, user is able to change)
 * when clicking profile, email, or location, user is able to edit the text
 *
 * Navigation buttons are included below
 * - Waitlist: navigates to a waitlist fragment to view events that user has joined the waitlist for
 * - History: displays past events of user
 * - Edit Profile: allows user to edit their profile (dialog fragment pop up --> TO BE ADDED)
 * - Sign Out: signs user out of their account, navigates back to sign up/login screen
 */

public class ProfileFragment extends Fragment {

    private TextView nameText, emailText, locationText;
    private EditText nameEdit, emailEdit, locationEdit;

    // TEMP user — replace with real Firebase or DB user later
    private User currentUser;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Load a user (replace with real user later)
        currentUser = new User(
                "User",
                "user@example.com",
                "password123",
                "123-456-7890",
                null
        );

        // Bind views

        nameText = view.findViewById(R.id.profile_name);
        emailText = view.findViewById(R.id.profile_email);
        locationText = view.findViewById(R.id.profile_location);


        nameEdit = view.findViewById(R.id.profile_name_edit);
        emailEdit = view.findViewById(R.id.profile_email_edit);
        locationEdit = view.findViewById(R.id.profile_location_edit);

        // Load user data into UI
        nameText.setText(currentUser.getName());
        emailText.setText(currentUser.getEmail());
        locationText.setText("Edmonton, AB"); // SAVE LOCATION IN USER CLASS

        // Make fields editable
        setupEditableField(nameText, nameEdit, "name");
        setupEditableField(emailText, emailEdit, "email");
        setupEditableField(locationText, locationEdit, "location");

        Button waitlistBtn = view.findViewById(R.id.waitlist_btn);

        waitlistBtn.setOnClickListener(v -> {

            WaitingListFragment fragment = new WaitingListFragment();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

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

                // Update the User object
                switch (fieldType) {
                    case "name":
                        currentUser.setName(newValue);
                        break;
                    case "email":
                        currentUser.setEmail(newValue);
                        break;
                    case "location":
                        // You can add a location field to User class later
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