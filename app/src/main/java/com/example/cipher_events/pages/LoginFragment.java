package com.example.cipher_events.pages;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class LoginFragment extends Fragment {

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText etEmail = view.findViewById(R.id.login_email);
        EditText etPassword = view.findViewById(R.id.login_password);
        Button btnLogin = view.findViewById(R.id.btn_login);
        SignInButton btnGoogleSignIn = view.findViewById(R.id.btn_google_sign_in);
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
                    navigateToMain("ADMIN", admin.getDeviceID());
                    return;
                }
            }

            // 2. Check Organizer
            ArrayList<Organizer> organizers = db.getAllOrganizers();
            for (Organizer org : organizers) {
                if (email.equals(org.getEmail()) && password.equals(org.getPassword())) {
                    db.setCurrentUser(org);
                    navigateToMain("ORGANIZER", org.getDeviceID());
                    return;
                }
            }

            // 3. Check Entrant (User)
            ArrayList<User> users = db.getAllUsers();
            for (User user : users) {
                if (email.equals(user.getEmail()) && password.equals(user.getPassword())) {
                    db.setCurrentUser(user);
                    navigateToMain("ENTRANT", user.getDeviceID());
                    return;
                }
            }

            Toast.makeText(getContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        tvGoToSignup.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, SignupFragment.newInstance("ENTRANT"))
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                String displayName = account.getDisplayName();
                String profilePic = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;

                DBProxy db = DBProxy.getInstance();
                User existingUser = db.getAnyUserByEmail(email);

                if (existingUser != null) {
                    db.setCurrentUser(existingUser);
                    String role = "ENTRANT";
                    if (existingUser instanceof Admin) role = "ADMIN";
                    else if (existingUser instanceof Organizer) role = "ORGANIZER";
                    navigateToMain(role, existingUser.getDeviceID());
                } else {
                    // New user via Google - Default to Entrant for now, or redirect to role selection
                    User newUser = new User(displayName, email, "", "", profilePic);
                    db.addUser(newUser);
                    db.setCurrentUser(newUser);
                    navigateToMain("ENTRANT", newUser.getDeviceID());
                    Toast.makeText(getContext(), "Account created with Google", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ApiException e) {
            Log.w("LoginFragment", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(getContext(), "Google Sign-In failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMain(String role, String accountID) {
        if (getActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) getActivity();
            main.loginSuccess(role, accountID);
        }
    }
}
