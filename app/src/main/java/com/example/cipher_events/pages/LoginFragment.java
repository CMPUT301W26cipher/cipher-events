package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

public class LoginFragment extends Fragment {

    private String role = "ENTRANT";

    public static LoginFragment newInstance(String role) {
        LoginFragment fragment = new LoginFragment();
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        TextView tvTitle = view.findViewById(R.id.login_title);
        if (tvTitle != null) {
            tvTitle.setText(role.equals("ORGANIZER") ? "Organizer Login" : "Attendee Login");
        }

        Button btnLogin = view.findViewById(R.id.btn_login);
        TextView tvGoToSignup = view.findViewById(R.id.tv_go_to_signup);

        btnLogin.setOnClickListener(v -> {

            DBProxy db = DBProxy.getInstance();

            User user;

            //Create user based on role
            if ("ORGANIZER".equals(role)) {
                    user = new Organizer(
                            "Organizer User",          // name
                            "org@email.com",           // email
                            "password123",             // password
                            "1234567890",              // phone
                            null                       // profilePictureURL (optional)
                );
            } else {
                user = new User(
                        "user_id",
                        "Attendee User",
                        "user@email.com",
                        "1234567890",
                        "extra_field_if_needed"
                );
            }

            //STORE CURRENT USER (THIS IS THE KEY LINE)
            db.setCurrentUser(user);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onRoleSelected(role);
            }
        });

        tvGoToSignup.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, SignupFragment.newInstance(role))
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
