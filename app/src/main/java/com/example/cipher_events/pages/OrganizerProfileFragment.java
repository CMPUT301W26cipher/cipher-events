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

public class OrganizerProfileFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private TextView nameText, emailText, phoneText;
    private EditText nameEdit, emailEdit, phoneEdit;
    private ImageView profileImage;
    private DBProxy dbProxy;
    private String deviceId;
    private Organizer currentOrganizer;

    public OrganizerProfileFragment() {}

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
        return inflater.inflate(R.layout.fragment_organizer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        nameText = view.findViewById(R.id.organizer_profile_name);
        emailText = view.findViewById(R.id.organizer_profile_email);
        phoneText = view.findViewById(R.id.organizer_profile_phone);
        nameEdit = view.findViewById(R.id.organizer_profile_name_edit);
        emailEdit = view.findViewById(R.id.organizer_profile_email_edit);
        phoneEdit = view.findViewById(R.id.organizer_profile_phone_edit);
        profileImage = view.findViewById(R.id.organizer_profile_image);

        loadOrganizerData();
        setupButtons(view);

        // Setup editable fields
        setupEditableField(nameText, nameEdit, "name");
        setupEditableField(emailText, emailEdit, "email");
        setupEditableField(phoneText, phoneEdit, "phone");
    }

    @Override
    public void onStart() {
        super.onStart();
        dbProxy.addListener(this);
        loadOrganizerData();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbProxy.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        loadOrganizerData();
    }

    private void loadOrganizerData() {
        User user = dbProxy.getCurrentUser();
        if (user instanceof Organizer) {
            currentOrganizer = (Organizer) user;
        } else {
            currentOrganizer = dbProxy.getOrganizer(deviceId);
        }

        if (currentOrganizer != null) {
            nameText.setText(currentOrganizer.getName() != null && !currentOrganizer.getName().isEmpty() ? currentOrganizer.getName() : "Set Name");
            emailText.setText(currentOrganizer.getEmail() != null && !currentOrganizer.getEmail().isEmpty() ? currentOrganizer.getEmail() : "Set Email");
            phoneText.setText(currentOrganizer.getPhoneNumber() != null && !currentOrganizer.getPhoneNumber().isEmpty() ? currentOrganizer.getPhoneNumber() : "Set Phone");

            String picUrl = currentOrganizer.getProfilePictureURL();
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
    }

    private void setupButtons(View view) {
        MaterialButton myEventsBtn = view.findViewById(R.id.my_events_btn);
        MaterialButton createEventBtn = view.findViewById(R.id.create_event_btn);
        MaterialButton signoutBtn = view.findViewById(R.id.organizer_signout_btn);

        if (myEventsBtn != null) {
            myEventsBtn.setOnClickListener(v -> navigateTo(new OrganizerHomeFragment()));
        }

        if (createEventBtn != null) {
            createEventBtn.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showCreateEventDialog();
                }
            });
        }

        if (signoutBtn != null) {
            signoutBtn.setOnClickListener(v -> {
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
                    updateOrganizerField(fieldType, newValue);
                }

                editText.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }

    private void updateOrganizerField(String fieldType, String value) {
        if (currentOrganizer == null) return;

        switch (fieldType) {
            case "name": currentOrganizer.setName(value); break;
            case "email": currentOrganizer.setEmail(value); break;
            case "phone": currentOrganizer.setPhoneNumber(value); break;
        }
        dbProxy.updateOrganizer(currentOrganizer);
    }
}
