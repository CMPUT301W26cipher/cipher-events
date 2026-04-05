package com.example.cipher_events.pages;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class UserProfileFragment extends Fragment {

    private TextInputLayout tilName, tilEmail, tilPassword, tilPhone;
    private TextInputEditText editName, editEmail, editPassword, editPhone;
    private MaterialButton saveButton, signoutButton;
    private TextView deleteButton;
    private ImageView profileImage;
    private View profileImageCard;
    private SwitchMaterial switchNotifications;
    private ProgressBar progressBar;
    
    private DBProxy dbProxy;
    private User currentUser;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this)
                            .load(selectedImageUri)
                            .circleCrop()
                            .into(profileImage);
                    profileImage.setPadding(0, 0, 0, 0);
                }
            }
    );

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbProxy = DBProxy.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadUserData();
        setupListeners();
    }

    private void initViews(View view) {
        tilName = view.findViewById(R.id.til_name);
        tilEmail = view.findViewById(R.id.til_email);
        tilPassword = view.findViewById(R.id.til_password);
        tilPhone = view.findViewById(R.id.til_phone);

        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPassword = view.findViewById(R.id.editPassword);
        editPhone = view.findViewById(R.id.editPhone);

        saveButton = view.findViewById(R.id.saveButton);
        signoutButton = view.findViewById(R.id.signout_btn);
        deleteButton = view.findViewById(R.id.button2);
        profileImage = view.findViewById(R.id.profileImage);
        profileImageCard = view.findViewById(R.id.profileImageCard);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void loadUserData() {
        currentUser = dbProxy.getCurrentUser();
        if (currentUser != null) {
            editName.setText(currentUser.getName());
            editEmail.setText(currentUser.getEmail());
            editPassword.setText(currentUser.getPassword());
            editPhone.setText(currentUser.getPhoneNumber());
            switchNotifications.setChecked(currentUser.isNotificationsEnabled());

            String picUrl = currentUser.getProfilePictureURL();
            if (picUrl != null && !picUrl.isEmpty()) {
                Glide.with(this)
                        .load(picUrl)
                        .placeholder(R.drawable.outline_account_circle_24)
                        .circleCrop()
                        .into(profileImage);
                profileImage.setPadding(0, 0, 0, 0);
            }
        }
    }

    private void setupListeners() {
        profileImageCard.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        saveButton.setOnClickListener(v -> saveProfile());

        if (signoutButton != null) {
            signoutButton.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).logout();
                }
            });
        }

        deleteButton.setOnClickListener(v -> {
            if (currentUser != null) {
                dbProxy.deleteUser(currentUser.getDeviceID());
                Toast.makeText(getContext(), "Profile Deleted", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).logout();
                }
            }
        });

        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                tilName.setError(null);
                tilEmail.setError(null);
            }
        };
        editName.addTextChangedListener(clearErrorWatcher);
        editEmail.addTextChangedListener(clearErrorWatcher);
    }

    private void saveProfile() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        boolean notifications = switchNotifications.isChecked();

        boolean isValid = true;

        if (name.isEmpty()) {
            tilName.setError("Name is required");
            isValid = false;
        }

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email address");
            isValid = false;
        }

        if (!isValid) return;

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        String profilePicUrl = (selectedImageUri != null) ? selectedImageUri.toString() : 
                              currentUser.getProfilePictureURL();

        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setPassword(password);
        currentUser.setPhoneNumber(phone);
        currentUser.setProfilePictureURL(profilePicUrl);
        currentUser.setNotificationsEnabled(notifications);
        
        dbProxy.updateUser(currentUser);

        // Simulate a brief delay for a better "aesthetic" feel of saving
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
        }, 800);
    }
}
