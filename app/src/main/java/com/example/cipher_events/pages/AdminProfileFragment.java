package com.example.cipher_events.pages;

import android.content.res.ColorStateList;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.google.android.material.button.MaterialButton;

public class AdminProfileFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private TextView nameText, emailText, phoneText;
    private EditText nameEdit, emailEdit, phoneEdit;
    private ImageView profileImage;
    private Admin currentAdmin;
    private DBProxy dbProxy;
    private String deviceId;

    private MaterialButton enableEntrantBtn, disableEntrantBtn;
    private MaterialButton enableOrganizerBtn, disableOrganizerBtn;
    private MaterialButton useEntrantBtn, useOrganizerBtn;

    public AdminProfileFragment() {}

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
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    @Override
    public void onStart() {
        super.onStart();
        dbProxy.addListener(this);
        loadAdminData();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbProxy.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        loadAdminData();
    }

    private void loadAdminData() {
        User user = dbProxy.getCurrentUser();
        if (user instanceof Admin) {
            currentAdmin = (Admin) user;
        } else {
            currentAdmin = dbProxy.getAdmin(deviceId);
        }

        if (currentAdmin == null) {
            currentAdmin = new Admin();
            currentAdmin.setName("Admin User");
            currentAdmin.setEmail("admin@example.com");
            currentAdmin.setPhoneNumber("000-000-0000");
            currentAdmin.setDeviceID(deviceId);
        }

        nameText.setText(currentAdmin.getName() != null && !currentAdmin.getName().isEmpty() ? currentAdmin.getName() : "Set Name");
        emailText.setText(currentAdmin.getEmail() != null && !currentAdmin.getEmail().isEmpty() ? currentAdmin.getEmail() : "Set Email");
        phoneText.setText(currentAdmin.getPhoneNumber() != null && !currentAdmin.getPhoneNumber().isEmpty() ? currentAdmin.getPhoneNumber() : "Set Phone");

        String picUrl = currentAdmin.getProfilePictureURL();
        if (picUrl != null && !picUrl.isEmpty()) {
            Glide.with(this)
                    .load(picUrl)
                    .placeholder(R.drawable.outline_account_circle_24)
                    .circleCrop()
                    .into(profileImage);
            profileImage.setPadding(0, 0, 0, 0);
            profileImage.setImageTintList(null);
        } else {
            // Show default icon if no URL
            profileImage.setImageResource(R.drawable.outline_account_circle_24);
            int paddingPx = (int) (24 * getResources().getDisplayMetrics().density);
            profileImage.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
            profileImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
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

        useEntrantBtn = view.findViewById(R.id.btn_use_entrant_view);
        useOrganizerBtn = view.findViewById(R.id.btn_use_organizer_view);
        updateRoleButtonsVisibility();

        if (enableEntrantBtn != null) {
            enableEntrantBtn.setOnClickListener(v -> {
                User u = dbProxy.getUser(deviceId);

                if (u == null) {
                    u = new User(
                            currentAdmin.getName(),
                            currentAdmin.getEmail(),
                            "",
                            currentAdmin.getPhoneNumber(),
                            currentAdmin.getProfilePictureURL()
                    );
                    u.setDeviceID(deviceId);
                    dbProxy.addUser(u);
                }
                currentAdmin.setEntrantRole(true);
                dbProxy.updateAdmin(currentAdmin);

                Toast.makeText(getContext(), "Entrant role enabled", Toast.LENGTH_SHORT).show();
                updateRoleButtonsVisibility();
            });
        }

        if (disableEntrantBtn != null) {
            disableEntrantBtn.setOnClickListener(v -> {
                dbProxy.deleteUser(deviceId);
                currentAdmin.setEntrantRole(false);
                dbProxy.updateAdmin(currentAdmin);

                Toast.makeText(getContext(), "Entrant role disabled", Toast.LENGTH_SHORT).show();
                updateRoleButtonsVisibility();
            });
        }

        if (enableOrganizerBtn != null) {
            enableOrganizerBtn.setOnClickListener(v -> {
                Organizer o = dbProxy.getOrganizer(deviceId);

                if (o == null) {
                    o = new Organizer(
                            currentAdmin.getName(),
                            currentAdmin.getEmail(),
                            "",
                            currentAdmin.getPhoneNumber(),
                            currentAdmin.getProfilePictureURL()
                    );
                    o.setDeviceID(deviceId);
                    dbProxy.addOrganizer(o);
                }

                currentAdmin.setOrganizerRole(true);
                dbProxy.updateAdmin(currentAdmin);
                Toast.makeText(getContext(), "Organizer role enabled", Toast.LENGTH_SHORT).show();
                updateRoleButtonsVisibility();
            });
        }

        if (disableOrganizerBtn != null) {
            disableOrganizerBtn.setOnClickListener(v -> {
                dbProxy.deleteOrganizer(deviceId);
                currentAdmin.setOrganizerRole(false);
                dbProxy.updateAdmin(currentAdmin);

                Toast.makeText(getContext(), "Organizer role disabled", Toast.LENGTH_SHORT).show();
                updateRoleButtonsVisibility();
            });
        }

        if (useEntrantBtn != null) {
            useEntrantBtn.setOnClickListener(v -> {
                if (currentAdmin != null && currentAdmin.hasEntrantRole()) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).switchToEntrantView();
                    }
                } else {
                    Toast.makeText(getContext(), "Entrant role is not enabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (useOrganizerBtn != null) {
            useOrganizerBtn.setOnClickListener(v -> {
                if (currentAdmin != null && currentAdmin.hasOrganizerRole()) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).switchToOrganizerView();
                    }
                } else {
                    Toast.makeText(getContext(), "Organizer role is not enabled", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateRoleButtonsVisibility() {
        boolean isEntrant = false;
        boolean isOrganizer = false;

        if (currentAdmin != null) {
            isEntrant = currentAdmin.hasEntrantRole();
            isOrganizer = currentAdmin.hasOrganizerRole();
        } else {
            isEntrant = dbProxy.getUser(deviceId) != null;
            isOrganizer = dbProxy.getOrganizer(deviceId) != null;
        }

        if (enableEntrantBtn != null) {
            enableEntrantBtn.setVisibility(isEntrant ? View.GONE : View.VISIBLE);
        }
        if (disableEntrantBtn != null) {
            disableEntrantBtn.setVisibility(isEntrant ? View.VISIBLE : View.GONE);
        }
        if (enableOrganizerBtn != null) {
            enableOrganizerBtn.setVisibility(isOrganizer ? View.GONE : View.VISIBLE);
        }
        if (disableOrganizerBtn != null) {
            disableOrganizerBtn.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        }

        if (useEntrantBtn != null) {
            useEntrantBtn.setVisibility(isEntrant ? View.VISIBLE : View.GONE);
        }
        if (useOrganizerBtn != null) {
            useOrganizerBtn.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
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
