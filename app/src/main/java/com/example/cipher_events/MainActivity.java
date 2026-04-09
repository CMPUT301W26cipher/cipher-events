package com.example.cipher_events;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.databinding.ActivityMainBinding;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.splashscreen.SplashScreen;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DBProxy.OnDataChangedListener {

    private ActivityMainBinding binding;
    private BottomNavigationView bottomNavigationView;
    private static final String PREFS_NAME = "CipherEventsPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ROLE = "userRole";
    private static final String KEY_DEVICE_ID = "deviceID";
    private String currentRole = "";
    private DBProxy DB = DBProxy.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView = binding.bottomNav;
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if ("ORGANIZER".equals(currentRole)) {
                if (id == R.id.menu_home) replaceFragment(new OrganizerHomeFragment());
                else if (id == R.id.menu_create) showCreateEventDialog();
                else if (id == R.id.menu_history) replaceFragment(new OrganizerHistoryFragment());
                else if (id == R.id.menu_profile) replaceFragment(new OrganizerProfileFragment());
            } else if ("ADMIN".equals(currentRole)) {
                if (id == R.id.menu_admin_home) replaceFragment(new AdminHomeFragment());
                else if (id == R.id.menu_admin_profile) replaceFragment(new AdminProfileFragment());
            } else {
                if (id == R.id.menu_home) replaceFragment(new HomeFragment());
                else if (id == R.id.menu_search) replaceFragment(new SearchFragment());
                else if (id == R.id.menu_favourites) replaceFragment(new FavouritesFragment());
                else if (id == R.id.menu_profile) replaceFragment(new ProfileFragment());
            }
            return true;
        });

        DB.addListener(this);
        checkPersistentLogin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DB.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_LOGGED_IN, false);
        if (isLoggedIn) {
            String role = prefs.getString(KEY_ROLE, "");
            if (!this.currentRole.equals(role)) {
                this.currentRole = role;
                updateNavigationMenu();
            }
        }
    }

    private void checkPersistentLogin() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_LOGGED_IN, false)) {
            currentRole = prefs.getString(KEY_ROLE, "");
            String accountID = prefs.getString(KEY_DEVICE_ID, "");

            User found = null;
            if ("ADMIN".equals(currentRole)) found = DB.getAdmin(accountID);
            else if ("ORGANIZER".equals(currentRole)) found = DB.getOrganizer(accountID);
            else found = DB.getUser(accountID);

            if (found != null) {
                DB.setCurrentUser(found);
            } else {
                User placeholder = "ADMIN".equals(currentRole) ? new Admin() :
                        "ORGANIZER".equals(currentRole) ? new Organizer() : new User();
                placeholder.setDeviceID(accountID);
                DB.setCurrentUser(placeholder);
            }
            updateNavigationMenu();
        } else {
            replaceFragment(LoginFragment.newInstance());
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        bottomNavigationView.setVisibility((fragment instanceof LoginFragment || fragment instanceof SignupFragment)
                ? View.GONE : View.VISIBLE);
    }

    public void switchToAdminView() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        currentRole = "ADMIN";
        editor.putString(KEY_ROLE, currentRole);
        editor.apply();
        updateNavigationMenu();
    }

    public void switchToOrganizerView() {
        User currentUser = DB.getCurrentUser();

        if (currentUser instanceof Admin) {
            if (!currentUser.hasOrganizerRole()) {
                android.widget.Toast.makeText(this, "Organizer role is not enabled.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
        }

        currentRole = "ORGANIZER";

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ROLE, currentRole);
        editor.apply();

        updateNavigationMenu();
    }

    public void switchToEntrantView() {
        User currentUser = DB.getCurrentUser();

        if (currentUser instanceof Admin) {
            if (!currentUser.hasEntrantRole()) {
                android.widget.Toast.makeText(this, "Entrant role is not enabled.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
        }

        currentRole = "ENTRANT";

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ROLE, currentRole);
        editor.apply();

        updateNavigationMenu();
    }

    public void showCreateEventDialog() {
        CreateEventDialogFragment dialog = new CreateEventDialogFragment();

        dialog.setCreateEventListener((title, date, time, location, description, capacity, bannerUrl, tags, coOrganizers, isPrivate) -> {
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            Organizer organizer = DB.getOrganizer(deviceId);

            if (organizer == null) {
                organizer = new Organizer("Organizer", "", "", "", null);
                organizer.setDeviceID(deviceId);
            }

            com.example.cipher_events.database.Event event = new com.example.cipher_events.database.Event(
                    title, description, date + " " + time, location, organizer,
                    new ArrayList<>(), new ArrayList<>(), bannerUrl, !isPrivate
            );

            if (capacity != null) event.setWaitingListCapacity(capacity);
            
            // Save Tags
            if (tags != null) {
                event.setTags(new ArrayList<>(tags));
            }

            // Handle co-organizers: Store their emails in the event
            if (coOrganizers != null && !coOrganizers.isEmpty()) {
                ArrayList<String> coOrgEmails = new ArrayList<>();
                for (String email : coOrganizers) {
                    coOrgEmails.add(email.toLowerCase().trim());
                }
                event.setCoOrganizerIds(coOrgEmails);
            }

            event.setInvitedEntrants(new ArrayList<>());
            event.setCancelledEntrants(new ArrayList<>());
            event.setEnrolledEntrants(new ArrayList<>());

            DB.addEvent(event);
            Toast.makeText(this, "Event Created Successfully!", Toast.LENGTH_SHORT).show();
        });

        dialog.show(getSupportFragmentManager(), "CREATE_EVENT");
    }

    public void loginSuccess(String role, String accountID) {
        currentRole = role;
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_ROLE, role)
                .putString(KEY_DEVICE_ID, accountID)
                .apply();
        updateNavigationMenu();
    }

    public void logout() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
        currentRole = "";
        DB.setCurrentUser(null);
        bottomNavigationView.setVisibility(View.GONE);
        replaceFragment(LoginFragment.newInstance());
    }

    private void updateNavigationMenu() {
        if (bottomNavigationView == null) return;
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView.getMenu().clear();

        if ("ORGANIZER".equals(currentRole)) {
            bottomNavigationView.inflateMenu(R.menu.menu_organizer_nav);
            replaceFragment(new OrganizerHomeFragment());
        } else if ("ADMIN".equals(currentRole)) {
            bottomNavigationView.inflateMenu(R.menu.menu_admin_nav);
            replaceFragment(new AdminHomeFragment());
        } else {
            bottomNavigationView.inflateMenu(R.menu.menu_bottom_nav);
            replaceFragment(new HomeFragment());
        }
    }
}
