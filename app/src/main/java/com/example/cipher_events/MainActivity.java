package com.example.cipher_events;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.notifications.Notifier;
import com.example.cipher_events.notifications.NotificationService;
import com.example.cipher_events.organizer.OrganizerEventService;
import com.example.cipher_events.pages.AdminBrowseEventsFragment;
import com.example.cipher_events.pages.AdminBrowseImagesFragment;
import com.example.cipher_events.pages.AdminBrowseProfilesFragment;
import com.example.cipher_events.pages.AdminHomeFragment;
import com.example.cipher_events.pages.AdminNotificationsFragment;
import com.example.cipher_events.pages.AdminProfileFragment;
import com.example.cipher_events.pages.CreateEventDialogFragment;
import com.example.cipher_events.pages.FavouritesFragment;
import com.example.cipher_events.pages.HomeFragment;
import com.example.cipher_events.pages.LoginFragment;
import com.example.cipher_events.pages.OrganizerHistoryFragment;
import com.example.cipher_events.pages.OrganizerHomeFragment;
import com.example.cipher_events.pages.OrganizerProfileFragment;
import com.example.cipher_events.pages.ProfileFragment;
import com.example.cipher_events.pages.SearchFragment;
import com.example.cipher_events.pages.SignupFragment;
import com.example.cipher_events.user.EntrantEventService;
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.user.UserProfileService;
import com.example.cipher_events.waitinglist.WaitingListService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    DBProxy DB = DBProxy.getInstance();
    Notifier notifier;
    FragmentManager fragmentManager = getSupportFragmentManager();
    BottomNavigationView bottomNavigationView;
    TextView tvCurrentRole;

    private String currentRole = "";
    private static final String PREFS_NAME = "CipherEventsPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ROLE = "userRole";
    private static final String KEY_DEVICE_ID = "deviceID";

    // Firestore-backed services
    private UserProfileService userProfileService;
    private OrganizerEventService organizerEventService;
    private EntrantEventService entrantEventService;
    private WaitingListService waitingListService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        notifier = Notifier.getInstance();
        fragmentManager = getSupportFragmentManager();

        // User-related services
        UserEventHistoryRepository historyRepository = new UserEventHistoryRepository();
        userProfileService = new UserProfileService();

        // QR / Event-related services
        organizerEventService = new OrganizerEventService();
        entrantEventService = new EntrantEventService();

        NotificationService notificationService = new NotificationService(notifier);
        waitingListService = new WaitingListService(historyRepository, notificationService);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setVisibility(View.GONE); // Hide by default
        
        tvCurrentRole = findViewById(R.id.tv_current_role);

        // Check for persistent login
        checkPersistentLogin();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (currentRole.equals("ORGANIZER")) {
                if (id == R.id.menu_home) {
                    replaceFragment(new OrganizerHomeFragment());
                } else if (id == R.id.menu_create) {
                    showCreateEventDialog();
                } else if (id == R.id.menu_history) {
                    replaceFragment(new OrganizerHistoryFragment());
                } else if (id == R.id.menu_profile) {
                    replaceFragment(new OrganizerProfileFragment());
                }
            } else if (currentRole.equals("ADMIN")) {
                if (id == R.id.menu_admin_events) {
                    replaceFragment(new AdminBrowseEventsFragment());
                } else if (id == R.id.menu_admin_profiles) {
                    replaceFragment(new AdminBrowseProfilesFragment());
                } else if (id == R.id.menu_admin_images) {
                    replaceFragment(new AdminBrowseImagesFragment());
                } else if (id == R.id.menu_admin_logs) {
                    replaceFragment(new AdminNotificationsFragment());
                } else if (id == R.id.menu_admin_profile) {
                    replaceFragment(new AdminProfileFragment());
                }
            } else {
                if (id == R.id.menu_home) {
                    replaceFragment(new HomeFragment());
                } else if (id == R.id.menu_search) {
                    replaceFragment(new SearchFragment());
                } else if (id == R.id.menu_favourites) {
                    replaceFragment(new FavouritesFragment());
                } else if (id == R.id.menu_profile) {
                    replaceFragment(new ProfileFragment());
                }
            }
            return true;
        });
    }

    private void checkPersistentLogin() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_LOGGED_IN, false);

        if (isLoggedIn) {
            currentRole = prefs.getString(KEY_ROLE, "");
            String deviceID = prefs.getString(KEY_DEVICE_ID, "");
            
            // Re-sync with DB
            if ("ADMIN".equals(currentRole)) {
                DB.setCurrentUser(DB.getAdmin(deviceID));
            } else if ("ORGANIZER".equals(currentRole)) {
                DB.setCurrentUser(DB.getOrganizer(deviceID));
            } else {
                DB.setCurrentUser(DB.getUser(deviceID));
            }
            
            updateNavigationMenu();
        } else {
            replaceFragment(LoginFragment.newInstance());
        }
    }

    public void saveLoginSession(String role, String deviceID) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_DEVICE_ID, deviceID);
        editor.apply();
    }

    public void loginSuccess(String role) {
        onRoleSelected(role);
    }

    public void onRoleSelected(String role) {
        currentRole = role;
        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        saveLoginSession(role, deviceID);
        updateNavigationMenu();
    }

    public void logout() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        currentRole = "";
        DB.setCurrentUser(null);
        bottomNavigationView.setVisibility(View.GONE);
        if (tvCurrentRole != null) tvCurrentRole.setVisibility(View.GONE);
        replaceFragment(LoginFragment.newInstance());
    }

    public void showCreateEventDialog() {
        CreateEventDialogFragment dialog = new CreateEventDialogFragment();
        dialog.show(getSupportFragmentManager(), "CreateEventDialog");
    }

    private void updateNavigationMenu() {
        if (bottomNavigationView == null) return;

        bottomNavigationView.setVisibility(View.VISIBLE);
        if (tvCurrentRole != null) {
            tvCurrentRole.setVisibility(View.VISIBLE);
            tvCurrentRole.setText("Role: " + currentRole);
            
            // Optional: Color role tag based on role
            int roleColorRes = R.color.role_attendee;
            if ("ORGANIZER".equals(currentRole)) roleColorRes = R.color.role_organizer;
            else if ("ADMIN".equals(currentRole)) roleColorRes = R.color.role_admin;
            tvCurrentRole.setBackgroundColor(getResources().getColor(roleColorRes, getTheme()));
        }

        if ("ORGANIZER".equals(currentRole)) {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu_organizer_nav);
            replaceFragment(new OrganizerHomeFragment());
        } else if ("ADMIN".equals(currentRole)) {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu_admin_nav);
            replaceFragment(new AdminHomeFragment());
        } else {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu_bottom_nav);
            replaceFragment(new HomeFragment());
        }
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            if (fragment instanceof LoginFragment || fragment instanceof SignupFragment) {
                if (tvCurrentRole != null) tvCurrentRole.setVisibility(View.GONE);
                bottomNavigationView.setVisibility(View.GONE);
                currentRole = "";
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof SignupFragment) {
            replaceFragment(LoginFragment.newInstance());
        } else if (currentFragment instanceof HomeFragment || 
                   currentFragment instanceof OrganizerHomeFragment || 
                   currentFragment instanceof AdminHomeFragment ||
                   currentFragment instanceof LoginFragment) {
            super.onBackPressed();
        } else {
            updateNavigationMenu(); // Return to home based on role
        }
    }
}
