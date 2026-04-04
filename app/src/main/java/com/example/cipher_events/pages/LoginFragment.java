package com.example.cipher_events.pages;

import android.os.Bundle;
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
import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

import java.util.ArrayList;

public class LoginFragment extends Fragment {

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText etEmail = view.findViewById(R.id.login_email);
        EditText etPassword = view.findViewById(R.id.login_password);
        Button btnLogin = view.findViewById(R.id.btn_login);
        TextView tvGoToSignup = view.findViewById(R.id.tv_go_to_signup);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            DBProxy db = DBProxy.getInstance();
            
            // 1. Check Admin
            ArrayList<Admin> admins = db.getAllAdmins();
            for (Admin admin : admins) {
                if (email.equals(admin.getEmail()) && password.equals(admin.getPassword())) {
                    db.setCurrentUser(admin);
                    navigateToMain("ADMIN");
                    return;
                }
            }

            // 2. Check Organizer
            ArrayList<Organizer> organizers = db.getAllOrganizers();
            for (Organizer org : organizers) {
                if (email.equals(org.getEmail()) && password.equals(org.getPassword())) {
                    db.setCurrentUser(org);
                    navigateToMain("ORGANIZER");
                    return;
                }
            }

            // 3. Check Entrant (User)
            ArrayList<User> users = db.getAllUsers();
            for (User user : users) {
                if (email.equals(user.getEmail()) && password.equals(user.getPassword())) {
                    db.setCurrentUser(user);
                    navigateToMain("ENTRANT");
                    return;
                }
            }

            Toast.makeText(getContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
        });

        tvGoToSignup.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, SignupFragment.newInstance("ENTRANT"))
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void navigateToMain(String role) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onRoleSelected(role);
        }
    }
}
