package com.example.cipher_events.pages;

import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.google.android.material.button.MaterialButton;

public class AdminProfileFragment extends Fragment {

    private TextView nameText, emailText, phoneText;
    private EditText nameEdit, emailEdit, phoneEdit;
    private ImageView profileImage;
    private Admin currentAdmin;
    private DBProxy dbProxy;
    private String deviceId;

    private MaterialButton enableEntrantBtn, disableEntrantBtn;
    private MaterialButton enableOrganizerBtn, disableOrganizerBtn;

    public AdminProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbProxy = DBProxy.getInstance();
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Bind views
        nameText = view.findViewById(R.id.admin_profile_name);
        emailText = view.findViewById(R.id.admin_profile_email);
        phoneText = view.findViewById(R.id.admin_profile_phone);
        nameEdit = view.findViewById(R.id.admin_profile_name_edit);
        emailEdit = view.findViewById(R.id.admin_profile_email_edit);
        phoneEdit = view.findViewById(R.id.admin_profile_phone_edit);
        profileImage = view.findViewById(R.id.admin_profile_image);

        loadAdminData();
        setupActionButtons(view);
        setupRoleButtons(view);

        // Setup editable fields
        setupEditableField(nameText, nameEdit, "name");
        setupEditableField(emailText, emailEdit, "email");
        setupEditableField(phoneText, phoneEdit, "phone");
    }

    private void loadAdminData() {
        currentAdmin = dbProxy.getAdmin(deviceId);
        if (currentAdmin == null) {
            currentAdmin = new Admin();
            currentAdmin.setName("Admin User");
            currentAdmin.setEmail("admin@example.com");
            currentAdmin.setPhoneNumber("000-000-0000");
        }

        nameText.setText(currentAdmin.getName());
        emailText.setText(currentAdmin.getEmail());
        phoneText.setText(currentAdmin.getPhoneNumber());

        if (currentAdmin.getProfilePictureURL() != null && !currentAdmin.getProfilePictureURL().isEmpty()) {
            Glide.with(this)
                    .load(currentAdmin.getProfilePictureURL())
                    .placeholder(R.drawable.outline_account_circle_24)
                    .circleCrop()
                    .into(profileImage);
            profileImage.setPadding(0, 0, 0, 0);
            profileImage.setImageTintList(null);
        }
    }

    private void setupActionButtons(View view) {
        MaterialButton historyBtn = view.findViewById(R.id.admin_history_btn);
        MaterialButton editProfileBtn = view.findViewById(R.id.admin_edit_profile_btn);
        MaterialButton signoutBtn = view.findViewById(R.id.admin_signout_btn);

        if (historyBtn != null) {
            historyBtn.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Admin History Clicked", Toast.LENGTH_SHORT).show());
        }

        if (editProfileBtn != null) {
            editProfileBtn.setOnClickListener(v -> navigateTo(new UserProfileFragment()));
        }

        if (signoutBtn != null) {
            signoutBtn.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).logout();
                }
            });
        }
    }

    private void setupRoleButtons(View view) {
        enableEntrantBtn = view.findViewById(R.id.btn_enable_entrant_role);
        disableEntrantBtn = view.findViewById(R.id.btn_disable_entrant_role);
        enableOrganizerBtn = view.findViewById(R.id.btn_enable_organizer_role);
        disableOrganizerBtn = view.findViewById(R.id.btn_disable_organizer_role);

        updateRoleButtonsVisibility();

        if (enableEntrantBtn != null) {
            enableEntrantBtn.setOnClickListener(v -> {
                User u = new User(currentAdmin.getName(), currentAdmin.getEmail(), "", currentAdmin.getPhoneNumber(), currentAdmin.getProfilePictureURL());
                u.setDeviceID(deviceId);
                dbProxy.addUser(u);
                Toast.makeText(getContext(), "Entrant role enabled", Toast.LENGTH_SHORT).show();
                updateRoleButtonsVisibility();
            });
        }

        if (disableEntrantBtn != null) {
            disableEntrantBtn.setOnClickListener(v -> {
                dbProxy.deleteUser(deviceId);
                Toast.makeText(getContext(), "Entrant role disabled", Toast.LENGTH_SHORT).show();
                updateRoleButtonsVisibility();
            });
        }

        if (enableOrganizerBtn != null) {
            enableOrganizerBtn.setOnClickListener(v -> {
                Organizer o = new Organizer(currentAdmin.getName(), currentAdmin.getEmail(), "", currentAdmin.getPhoneNumber(), currentAdmin.getProfilePictureURL());
                o.setDeviceID(deviceId);
                dbProxy.addOrganizer(o);
                Toast.makeText(getContext(), "Organizer role enabled", Toast.LENGTH_SHORT).show();
                updateRoleButtonsVisibility();
            });
        }

        if (disableOrganizerBtn != null) {
            disableOrganizerBtn.setOnClickListener(v -> {
                dbProxy.deleteOrganizer(deviceId);
                Toast.makeText(getContext(), "Organizer role disabled", Toast.LENGTH_SHORT).show();
                updateRoleButtonsVisibility();
            });
        }
    }

    private void updateRoleButtonsVisibility() {
        boolean isEntrant = dbProxy.getUser(deviceId) != null;
        boolean isOrganizer = dbProxy.getOrganizer(deviceId) != null;

        if (enableEntrantBtn != null) enableEntrantBtn.setVisibility(isEntrant ? View.GONE : View.VISIBLE);
        if (disableEntrantBtn != null) disableEntrantBtn.setVisibility(isEntrant ? View.VISIBLE : View.GONE);
        if (enableOrganizerBtn != null) enableOrganizerBtn.setVisibility(isOrganizer ? View.GONE : View.VISIBLE);
        if (disableOrganizerBtn != null) disableOrganizerBtn.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
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
                    switch (fieldType) {
                        case "name": currentAdmin.setName(newValue); break;
                        case "email": currentAdmin.setEmail(newValue); break;
                        case "phone": currentAdmin.setPhoneNumber(newValue); break;
                    }
                    dbProxy.updateAdmin(currentAdmin);
                }

                editText.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }
}
