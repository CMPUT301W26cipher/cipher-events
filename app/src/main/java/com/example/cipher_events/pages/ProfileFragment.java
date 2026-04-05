package com.example.cipher_events.pages;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private TextView nameText, emailText, locationText;
    private EditText nameEdit, emailEdit, locationEdit;
    private ImageView profileImage;
    private DBProxy dbProxy;
    private User currentUser;

    public ProfileFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbProxy = DBProxy.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameText = view.findViewById(R.id.profile_name);
        emailText = view.findViewById(R.id.profile_email);
        locationText = view.findViewById(R.id.profile_location);
        nameEdit = view.findViewById(R.id.profile_name_edit);
        emailEdit = view.findViewById(R.id.profile_email_edit);
        locationEdit = view.findViewById(R.id.profile_location_edit);
        profileImage = view.findViewById(R.id.profile_image);

        loadUserData();
        setupButtons(view);
        
        setupEditableField(nameText, nameEdit, "name");
        setupEditableField(emailText, emailEdit, "email");
        setupEditableField(locationText, locationEdit, "location");
    }

    @Override
    public void onStart() {
        super.onStart();
        dbProxy.addListener(this);
        loadUserData();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbProxy.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        loadUserData();
    }

    private void loadUserData() {
        currentUser = dbProxy.getCurrentUser();

        if (currentUser != null) {
            nameText.setText(currentUser.getName() != null && !currentUser.getName().isEmpty() ? currentUser.getName() : "Set Name");
            emailText.setText(currentUser.getEmail() != null && !currentUser.getEmail().isEmpty() ? currentUser.getEmail() : "Set Email");
            // Location isn't in User model yet, keeping placeholder
            locationText.setText("Set Location");

            String picUrl = currentUser.getProfilePictureURL();
            if (picUrl != null && !picUrl.isEmpty()) {
                Glide.with(this)
                        .load(picUrl)
                        .placeholder(R.drawable.outline_account_circle_24)
                        .circleCrop()
                        .into(profileImage);
                profileImage.setPadding(0, 0, 0, 0);
                profileImage.setImageTintList(null);
            } else {
                // If no URL, show default icon with proper padding and tint
                profileImage.setImageResource(R.drawable.outline_account_circle_24);
                int paddingPx = (int) (24 * getResources().getDisplayMetrics().density);
                profileImage.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                profileImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
            }
        }
    }

    private void setupButtons(View view) {
        MaterialButton waitlistBtn = view.findViewById(R.id.waitlist_btn);
        if (waitlistBtn != null) {
            waitlistBtn.setOnClickListener(v -> {
                User user = DBProxy.getInstance().getCurrentUser();
                String role = (user instanceof Organizer) ? "organizer" : "attendee";
                WaitingListFragment fragment = WaitingListFragment.newInstance(null, role);
                navigateTo(fragment);
            });
        }

        MaterialButton historyBtn = view.findViewById(R.id.history_btn);
        if (historyBtn != null) {
            historyBtn.setOnClickListener(v -> navigateTo(new OrganizerHistoryFragment()));
        }

        MaterialButton editProfileBtn = view.findViewById(R.id.edit_profile_btn);
        if (editProfileBtn != null) {
            editProfileBtn.setOnClickListener(v -> navigateTo(new UserProfileFragment()));
        }

        MaterialButton signOutBtn = view.findViewById(R.id.signout_btn);
        if (signOutBtn != null) {
            signOutBtn.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).logout();
                }
            });
        }
    }

    private void navigateTo(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupEditableField(TextView textView, EditText editText, String fieldType) {
        if (textView == null || editText == null) return;
        
        textView.setOnClickListener(v -> {
            editText.setText(textView.getText());
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            editText.requestFocus();
        });

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String newValue = editText.getText().toString().trim();
                if (!newValue.isEmpty()) {
                    textView.setText(newValue);
                    updateUserField(fieldType, newValue);
                }

                editText.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }

    private void updateUserField(String fieldType, String value) {
        if (currentUser != null) {
            switch (fieldType) {
                case "name": currentUser.setName(value); break;
                case "email": currentUser.setEmail(value); break;
            }
            dbProxy.updateUser(currentUser);
        }
    }
}
