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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.R;
import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

public class AdminProfileFragment extends Fragment {

    private TextView nameText, emailText, phoneText;
    private EditText nameEdit, emailEdit, phoneEdit;
    private Admin currentAdmin;
    private DBProxy dbProxy;
    private String deviceId;

    private Button enableEntrantBtn, disableEntrantBtn;
    private Button enableOrganizerBtn, disableOrganizerBtn;

    public AdminProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        dbProxy = DBProxy.getInstance();
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Load real admin data
        currentAdmin = dbProxy.getAdmin(deviceId);
        if (currentAdmin == null) {
            // fallback
            currentAdmin = new Admin();
            currentAdmin.setName("Admin User");
            currentAdmin.setEmail("admin@example.com");
            currentAdmin.setPhoneNumber("000-000-0000");
        }

        // Bind views
        nameText = view.findViewById(R.id.admin_profile_name);
        emailText = view.findViewById(R.id.admin_profile_email);
        phoneText = view.findViewById(R.id.admin_profile_phone);
        nameEdit = view.findViewById(R.id.admin_profile_name_edit);
        emailEdit = view.findViewById(R.id.admin_profile_email_edit);
        phoneEdit = view.findViewById(R.id.admin_profile_phone_edit);

        // Load data into UI
        nameText.setText(currentAdmin.getName());
        emailText.setText(currentAdmin.getEmail());
        phoneText.setText(currentAdmin.getPhoneNumber());

        // Setup editable fields
        setupEditableField(nameText, nameEdit, "name");
        setupEditableField(emailText, emailEdit, "email");
        setupEditableField(phoneText, phoneEdit, "phone");

        // Navigation buttons
        Button historyBtn = view.findViewById(R.id.admin_history_btn);
        Button editProfileBtn = view.findViewById(R.id.admin_edit_profile_btn);
        Button signoutBtn = view.findViewById(R.id.admin_signout_btn);

        historyBtn.setOnClickListener(v ->
                Toast.makeText(getContext(), "Admin History", Toast.LENGTH_SHORT).show());

        editProfileBtn.setOnClickListener(v ->
                Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show());

        signoutBtn.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RoleSelectionFragment())
                    .commit();
        });

        // Role toggle buttons
        enableEntrantBtn = view.findViewById(R.id.btn_enable_entrant_role);
        disableEntrantBtn = view.findViewById(R.id.btn_disable_entrant_role);
        enableOrganizerBtn = view.findViewById(R.id.btn_enable_organizer_role);
        disableOrganizerBtn = view.findViewById(R.id.btn_disable_organizer_role);

        updateRoleButtons();

        enableEntrantBtn.setOnClickListener(v -> {
            User u = new User();
            u.setDeviceID(deviceId);
            u.setName(currentAdmin.getName());
            u.setEmail(currentAdmin.getEmail());
            dbProxy.addUser(u);
            Toast.makeText(getContext(), "Entrant role enabled", Toast.LENGTH_SHORT).show();
            updateRoleButtons();
        });

        disableEntrantBtn.setOnClickListener(v -> {
            dbProxy.deleteUser(deviceId);
            Toast.makeText(getContext(), "Entrant role disabled", Toast.LENGTH_SHORT).show();
            updateRoleButtons();
        });

        enableOrganizerBtn.setOnClickListener(v -> {
            Organizer o = new Organizer();
            o.setDeviceID(deviceId);
            o.setName(currentAdmin.getName());
            o.setEmail(currentAdmin.getEmail());
            dbProxy.addOrganizer(o);
            Toast.makeText(getContext(), "Organizer role enabled", Toast.LENGTH_SHORT).show();
            updateRoleButtons();
        });

        disableOrganizerBtn.setOnClickListener(v -> {
            dbProxy.deleteOrganizer(deviceId);
            Toast.makeText(getContext(), "Organizer role disabled", Toast.LENGTH_SHORT).show();
            updateRoleButtons();
        });

        return view;
    }

    private void updateRoleButtons() {
        boolean isEntrant = dbProxy.getUser(deviceId) != null;
        boolean isOrganizer = dbProxy.getOrganizer(deviceId) != null;

        enableEntrantBtn.setVisibility(isEntrant ? View.GONE : View.VISIBLE);
        disableEntrantBtn.setVisibility(isEntrant ? View.VISIBLE : View.GONE);
        enableOrganizerBtn.setVisibility(isOrganizer ? View.GONE : View.VISIBLE);
        disableOrganizerBtn.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
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

                dbProxy.updateAdmin(currentAdmin);
                editText.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }
}