package com.example.cipher_events.pages;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignupFragment extends Fragment {

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private AutoCompleteTextView roleDropdown;

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
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roleDropdown = view.findViewById(R.id.signup_role_dropdown);
        TextInputEditText etName = view.findViewById(R.id.signup_name);
        TextInputEditText etEmail = view.findViewById(R.id.signup_email);
        TextInputEditText etPassword = view.findViewById(R.id.signup_password);
        MaterialButton btnSignup = view.findViewById(R.id.btn_signup);
        SignInButton btnGoogleSignup = view.findViewById(R.id.btn_google_signup);
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

            DBProxy db = DBProxy.getInstance();
            String accountID;

            if ("ORGANIZER".equals(role)) {
                Organizer newOrg = new Organizer(name, email, password, "", null);
                // deviceID is now a unique account UUID generated in User constructor.
                // We no longer overwrite it with hardware ANDROID_ID to support multiple accounts.
                accountID = newOrg.getDeviceID();
                db.addOrganizer(newOrg);
                db.setCurrentUser(newOrg);
            } else {
                User newUser = new User(name, email, password, "", null);
                accountID = newUser.getDeviceID();
                db.addUser(newUser);
                db.setCurrentUser(newUser);
            }

            Toast.makeText(getContext(), "Welcome to Cipher Events!", Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loginSuccess(role, accountID);
            }
        });

        btnGoogleSignup.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        tvGoToLogin.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, LoginFragment.newInstance())
                    .commit();
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                String displayName = account.getDisplayName();
                String profilePic = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
                String role = roleDropdown.getText().toString();

                DBProxy db = DBProxy.getInstance();
                User existingUser = db.getAnyUserByEmail(email);

                if (existingUser != null) {
                    db.setCurrentUser(existingUser);
                    String actualRole = "ENTRANT";
                    if (existingUser instanceof Admin) actualRole = "ADMIN";
                    else if (existingUser instanceof Organizer) actualRole = "ORGANIZER";
                    
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loginSuccess(actualRole, existingUser.getDeviceID());
                    }
                    Toast.makeText(getContext(), "Signed in as " + email, Toast.LENGTH_SHORT).show();
                } else {
                    String accountID;
                    if ("ORGANIZER".equals(role)) {
                        Organizer newOrg = new Organizer(displayName, email, "", "", profilePic);
                        accountID = newOrg.getDeviceID();
                        db.addOrganizer(newOrg);
                        db.setCurrentUser(newOrg);
                    } else {
                        User newUser = new User(displayName, email, "", "", profilePic);
                        accountID = newUser.getDeviceID();
                        db.addUser(newUser);
                        db.setCurrentUser(newUser);
                    }

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loginSuccess(role, accountID);
                    }
                    Toast.makeText(getContext(), "Account created with Google", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ApiException e) {
            Log.w("SignupFragment", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(getContext(), "Google Sign-In failed", Toast.LENGTH_SHORT).show();
        }
    }
}
