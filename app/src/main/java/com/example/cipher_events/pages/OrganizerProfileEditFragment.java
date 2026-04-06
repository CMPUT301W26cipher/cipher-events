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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class OrganizerProfileEditFragment extends Fragment {

    private TextInputLayout tilName, tilEmail, tilPhone;
    private TextInputEditText editName, editEmail, editPhone;
    private MaterialButton saveButton;
    private ImageView profileImage;
    private View profileImageCard;
    private ProgressBar progressBar;
    
    private DBProxy dbProxy;
    private Organizer currentOrganizer;
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

    public OrganizerProfileEditFragment() {
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
        return inflater.inflate(R.layout.fragment_organizer_profile_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadOrganizerData();
        setupListeners();
    }

    private void initViews(View view) {
        tilName = view.findViewById(R.id.til_name);
        tilEmail = view.findViewById(R.id.til_email);
        tilPhone = view.findViewById(R.id.til_phone);

        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPhone = view.findViewById(R.id.editPhone);

        saveButton = view.findViewById(R.id.saveButton);
        profileImage = view.findViewById(R.id.profileImage);
        profileImageCard = view.findViewById(R.id.profileImageCard);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void loadOrganizerData() {
        User user = dbProxy.getCurrentUser();
        if (user instanceof Organizer) {
            currentOrganizer = (Organizer) user;
            
            editName.setText(currentOrganizer.getName());
            editEmail.setText(currentOrganizer.getEmail());
            editPhone.setText(currentOrganizer.getPhoneNumber());

            String picUrl = currentOrganizer.getProfilePictureURL();
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
        if (currentOrganizer == null) {
            Toast.makeText(getContext(), "Error: No organizer logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

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
                              currentOrganizer.getProfilePictureURL();

        currentOrganizer.setName(name);
        currentOrganizer.setEmail(email);
        currentOrganizer.setPhoneNumber(phone);
        currentOrganizer.setProfilePictureURL(profilePicUrl);
        
        dbProxy.updateOrganizer(currentOrganizer);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
        }, 800);
    }
}
