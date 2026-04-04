package com.example.cipher_events.pages;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.User;

public class UserProfileFragment extends Fragment {

    private EditText editName, editEmail, editPassword, editPhone;
    private Button saveButton, deleteButton, signoutButton;
    private DBProxy dbProxy;
    private String deviceId;
    private User currentUser;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbProxy = DBProxy.getInstance();
        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPassword = view.findViewById(R.id.editPassword);
        editPhone = view.findViewById(R.id.editPhone);
        saveButton = view.findViewById(R.id.saveButton);
        deleteButton = view.findViewById(R.id.button2);
        signoutButton = view.findViewById(R.id.signout_btn);

        // Load existing user data
        currentUser = dbProxy.getUser(deviceId);
        if (currentUser != null) {
            editName.setText(currentUser.getName());
            editEmail.setText(currentUser.getEmail());
            editPassword.setText(currentUser.getPassword());
            editPhone.setText(currentUser.getPhoneNumber());
        }

        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Name and Email are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUser == null) {
                // Create new user if not exists
                currentUser = new User(name, email, password, phone, null);
                currentUser.setDeviceID(deviceId);
                dbProxy.addUser(currentUser);
            } else {
                // Update existing user
                currentUser.setName(name);
                currentUser.setEmail(email);
                currentUser.setPassword(password);
                currentUser.setPhoneNumber(phone);
                dbProxy.updateUser(currentUser);
            }

            Toast.makeText(getContext(), "Profile Saved", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        });

        if (signoutButton != null) {
            signoutButton.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).logout();
                }
            });
        }

        deleteButton.setOnClickListener(v -> {
            if (currentUser != null) {
                dbProxy.deleteUser(deviceId);
                Toast.makeText(getContext(), "Profile Deleted", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).logout();
                }
            }
        });
    }
}