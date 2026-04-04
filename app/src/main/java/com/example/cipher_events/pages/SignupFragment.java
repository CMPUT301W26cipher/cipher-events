package com.example.cipher_events.pages;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignupFragment extends Fragment {

    public SignupFragment() {
        // Required empty public constructor
    }

    public static SignupFragment newInstance(String role) {
        SignupFragment fragment = new SignupFragment();
        Bundle args = new Bundle();
        args.putString("role", role);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AutoCompleteTextView roleDropdown = view.findViewById(R.id.signup_role_dropdown);
        TextInputEditText etName = view.findViewById(R.id.signup_name);
        TextInputEditText etEmail = view.findViewById(R.id.signup_email);
        TextInputEditText etPassword = view.findViewById(R.id.signup_password);
        MaterialButton btnSignup = view.findViewById(R.id.btn_signup);
        TextView tvGoToLogin = view.findViewById(R.id.tv_go_to_login);

        // Setup Role Dropdown
        String[] roles = {"ENTRANT", "ORGANIZER"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, roles);
        roleDropdown.setAdapter(adapter);

        // Set initial role if provided
        if (getArguments() != null) {
            String initialRole = getArguments().getString("role", "ENTRANT");
            roleDropdown.setText(initialRole, false);
        } else {
            roleDropdown.setText("ENTRANT", false);
        }

        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = roleDropdown.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            DBProxy db = DBProxy.getInstance();

            if ("ORGANIZER".equals(role)) {
                Organizer newOrg = new Organizer(name, email, password, "", null);
                newOrg.setDeviceID(deviceId);
                db.addOrganizer(newOrg);
                db.setCurrentUser(newOrg);
            } else {
                User newUser = new User(name, email, password, "", null);
                newUser.setDeviceID(deviceId);
                db.addUser(newUser);
                db.setCurrentUser(newUser);
            }

            Toast.makeText(getContext(), "Welcome to Cipher Events!", Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onRoleSelected(role);
            }
        });

        tvGoToLogin.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, LoginFragment.newInstance())
                    .commit();
        });
    }
}
