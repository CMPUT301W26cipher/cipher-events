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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.R;
import com.example.cipher_events.database.Admin;

public class AdminProfileFragment extends Fragment {

    private TextView nameText, emailText, phoneText;
    private EditText nameEdit, emailEdit, phoneEdit;
    private Admin currentAdmin;

    public AdminProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        // Temp admin data
        currentAdmin = new Admin(
                "Admin User",
                "admin@example.com",
                "admin123",
                "000-000-0000",
                null
        );

        // Bind views
        nameText = view.findViewById(R.id.admin_profile_name);
        emailText = view.findViewById(R.id.admin_profile_email);
        phoneText = view.findViewById(R.id.admin_profile_phone);

        nameEdit = view.findViewById(R.id.admin_profile_name_edit);
        emailEdit = view.findViewById(R.id.admin_profile_email_edit);
        phoneEdit = view.findViewById(R.id.admin_profile_phone_edit);

        Button historyBtn = view.findViewById(R.id.admin_history_btn);
        Button editProfileBtn = view.findViewById(R.id.admin_edit_profile_btn);
        Button signoutBtn = view.findViewById(R.id.admin_signout_btn);

        // Load data into UI
        nameText.setText(currentAdmin.getName());
        emailText.setText(currentAdmin.getEmail());
        phoneText.setText(currentAdmin.getPhoneNumber());

        // Setup editable fields
        setupEditableField(nameText, nameEdit, "name");
        setupEditableField(emailText, emailEdit, "email");
        setupEditableField(phoneText, phoneEdit, "phone");

        historyBtn.setOnClickListener(v -> Toast.makeText(getContext(), "Admin History", Toast.LENGTH_SHORT).show());
        editProfileBtn.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show());
        signoutBtn.setOnClickListener(v -> Toast.makeText(getContext(), "Sign Out", Toast.LENGTH_SHORT).show());

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

                switch (fieldType) {
                    case "name": currentAdmin.setName(newValue); break;
                    case "email": currentAdmin.setEmail(newValue); break;
                    case "phone": currentAdmin.setPhoneNumber(newValue); break;
                }

                editText.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }
}