package com.example.cipher_events.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.User;

public class UserEditProfile extends AppCompatActivity {

    private EditText editName, editEmail, editPassword, editPhone;
    private ImageView profileImage;
    private Button saveButton, deleteButton;
    private final DBProxy db = DBProxy.getInstance();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_profile_edit);

        initializeViews();
        loadUserData();
        setupListeners();
    }

    private void initializeViews() {
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editPhone = findViewById(R.id.editPhone);
        profileImage = findViewById(R.id.profileImage);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.button2);
    }

    private void loadUserData() {
        currentUser = db.getCurrentUser();
        if (currentUser != null) {
            editName.setText(currentUser.getName());
            editEmail.setText(currentUser.getEmail());
            editPassword.setText(currentUser.getPassword());
            editPhone.setText(currentUser.getPhoneNumber());
            
            // Note: If profilePictureURL is used, it should be loaded here (e.g., using Glide or Picasso)
            // if (currentUser.getProfilePictureURL() != null) { ... }
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveProfile());

        deleteButton.setOnClickListener(v -> {
            if (currentUser != null) {
                db.deleteUser(currentUser.getDeviceID());
                db.setCurrentUser(null);
                Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setPassword(password);
        currentUser.setPhoneNumber(phone);

        db.updateUser(currentUser);
        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}