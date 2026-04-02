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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;

public class OrganizerProfileFragment extends Fragment {

    private TextView nameText, emailText, phoneText;
    private EditText nameEdit, emailEdit, locationEdit;
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

        View view = inflater.inflate(R.layout.fragment_organizer_profile, container, false);

        // Bind views
        nameText = view.findViewById(R.id.organizer_profile_name);
        emailText = view.findViewById(R.id.organizer_profile_email);
        phoneText = view.findViewById(R.id.organizer_profile_phone);

        nameEdit = view.findViewById(R.id.organizer_profile_name_edit);
        emailEdit = view.findViewById(R.id.organizer_profile_email_edit);
        locationEdit = view.findViewById(R.id.organizer_profile_phone_edit);

        Button myEventsBtn = view.findViewById(R.id.my_events_btn);
        Button createEventBtn = view.findViewById(R.id.create_event_btn);
        Button signoutBtn = view.findViewById(R.id.organizer_signout_btn);

        // Load real organizer data from database
        currentOrganizer = dbProxy.getOrganizer(deviceId);
        if (currentOrganizer != null) {
            nameText.setText(currentOrganizer.getName());
            emailText.setText(currentOrganizer.getEmail());
            phoneText.setText(currentOrganizer.getPhoneNumber());
        }

        // Setup editable fields
        setupEditableField(nameText, nameEdit, "name");
        setupEditableField(emailText, emailEdit, "email");
        setupEditableField(phoneText, locationEdit, "phone");

        if (myEventsBtn != null) {
            myEventsBtn.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new OrganizerHomeFragment())
                        .addToBackStack(null)
                        .commit();
            });
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
                    ((MainActivity) getActivity()).onRoleSelected(""); // Back to role selection
                }
            });
        }

        return view;
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

                String newValue = editText.getText().toString();
                textView.setText(newValue);

                if (currentOrganizer != null) {
                    switch (fieldType) {
                        case "name": currentOrganizer.setName(newValue); break;
                        case "email": currentOrganizer.setEmail(newValue); break;
                        case "phone": currentOrganizer.setPhoneNumber(newValue); break;
                    }
                    dbProxy.updateOrganizer(currentOrganizer);
                }

                editText.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }
}
