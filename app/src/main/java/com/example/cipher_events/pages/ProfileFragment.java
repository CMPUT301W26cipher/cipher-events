package com.example.cipher_events.pages;

import android.os.Bundle;
import android.provider.Settings;
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

import com.example.cipher_events.R;
import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

public class ProfileFragment extends Fragment {

    private TextView nameText, emailText, locationText;
    private EditText nameEdit, emailEdit, locationEdit;
    private DBProxy dbProxy;
    private String deviceId;
    private User currentUser;

    public ProfileFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbProxy = DBProxy.getInstance();
        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameText = view.findViewById(R.id.profile_name);
        emailText = view.findViewById(R.id.profile_email);
        locationText = view.findViewById(R.id.profile_location);
        nameEdit = view.findViewById(R.id.profile_name_edit);
        emailEdit = view.findViewById(R.id.profile_email_edit);
        locationEdit = view.findViewById(R.id.profile_location_edit);

        currentUser = dbProxy.getUser(deviceId);
        if (currentUser != null) {
            nameText.setText(currentUser.getName());
            emailText.setText(currentUser.getEmail());
        } else {
            nameText.setText("Name");
            emailText.setText("Email");
            locationText.setText("Location");
        }

        setupEditableField(nameText, nameEdit, "name");
        setupEditableField(emailText, emailEdit, "email");
        setupEditableField(locationText, locationEdit, "location");

        // Waitlist button
        Button waitlistBtn = view.findViewById(R.id.waitlist_btn);
        waitlistBtn.setOnClickListener(v -> {
            WaitingListFragment fragment = WaitingListFragment.newInstance(null);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // History button
        Button historyBtn = view.findViewById(R.id.history_btn);
        historyBtn.setOnClickListener(v -> {
            OrganizerHistoryFragment fragment = new OrganizerHistoryFragment();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Edit Profile button
        Button editProfileBtn = view.findViewById(R.id.edit_profile_btn);
        editProfileBtn.setOnClickListener(v -> {
            UserProfileFragment fragment = new UserProfileFragment();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Sign Out button
        Button signOutBtn = view.findViewById(R.id.signout_btn);
        signOutBtn.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RoleSelectionFragment())
                    .commit();
        });

        // Show Admin button only if user is an admin
        Admin admin = dbProxy.getAdmin(deviceId);
        if (admin != null) {
            Button adminBtn = view.findViewById(R.id.admin_btn);
            if (adminBtn != null) {
                adminBtn.setVisibility(View.VISIBLE);
                adminBtn.setOnClickListener(v -> {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new AdminHomeFragment())
                            .addToBackStack(null)
                            .commit();
                });
            }
        }

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

                if (currentUser == null) {
                    currentUser = new User(newValue, "Email", "", "", null);
                    currentUser.setDeviceID(deviceId);
                    dbProxy.addUser(currentUser);
                }

                switch (fieldType) {
                    case "name": currentUser.setName(newValue); break;
                    case "email": currentUser.setEmail(newValue); break;
                }

                dbProxy.updateUser(currentUser);
                editText.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }
}