package com.example.cipher_events.pages;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

    private String role = "ENTRANT";

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            role = getArguments().getString("role", "ENTRANT");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        TextView tvTitle = view.findViewById(R.id.signup_title);
        if (tvTitle != null) {
            tvTitle.setText(role.equals("ORGANIZER") ? "Organizer Sign Up" : "Attendee Sign Up");
        }

        EditText etName = view.findViewById(R.id.signup_name);
        EditText etEmail = view.findViewById(R.id.signup_email);
        EditText etPassword = view.findViewById(R.id.signup_password);
        Button btnSignup = view.findViewById(R.id.btn_signup);
        TextView tvGoToLogin = view.findViewById(R.id.tv_go_to_login);

        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

            if (role.equals("ORGANIZER")) {
                Organizer newOrg = new Organizer(name, email, password, "", null);
                newOrg.setDeviceID(deviceId);
                DBProxy.getInstance().addOrganizer(newOrg);
                Toast.makeText(getContext(), "Organizer account created", Toast.LENGTH_SHORT).show();
            } else {
                User newUser = new User(name, email, password, "", null);
                newUser.setDeviceID(deviceId);
                DBProxy.getInstance().addUser(newUser);
                Toast.makeText(getContext(), "Attendee account created", Toast.LENGTH_SHORT).show();
            }

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onRoleSelected(role);
            }
        });

        tvGoToLogin.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment.newInstance(role))
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
