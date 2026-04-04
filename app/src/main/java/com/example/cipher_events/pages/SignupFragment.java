package com.example.cipher_events.pages;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        Spinner roleSpinner = view.findViewById(R.id.signup_role_spinner);
        EditText etName = view.findViewById(R.id.signup_name);
        EditText etEmail = view.findViewById(R.id.signup_email);
        EditText etPassword = view.findViewById(R.id.signup_password);
        Button btnSignup = view.findViewById(R.id.btn_signup);
        TextView tvGoToLogin = view.findViewById(R.id.tv_go_to_login);

        String[] roles = {"ENTRANT", "ORGANIZER"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Pre-select role if passed in arguments
        if (getArguments() != null) {
            String initialRole = getArguments().getString("role", "ENTRANT");
            if ("ORGANIZER".equals(initialRole)) {
                roleSpinner.setSelection(1);
            }
        }

        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

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

            Toast.makeText(getContext(), role + " account created", Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onRoleSelected(role);
            }
        });

        tvGoToLogin.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
