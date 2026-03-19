package com.example.cipher_events;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.notifications.Notifier;
import com.example.cipher_events.organizer.OrganizerEventService;
import com.example.cipher_events.pages.AdminHomeFragment;
import com.example.cipher_events.pages.CreateEventDialogFragment;
import com.example.cipher_events.pages.FavouritesFragment;
import com.example.cipher_events.pages.HomeFragment;
import com.example.cipher_events.pages.LoginFragment;
import com.example.cipher_events.pages.OrganizerHistoryFragment;
import com.example.cipher_events.pages.OrganizerHomeFragment;
import com.example.cipher_events.pages.OrganizerProfileFragment;
import com.example.cipher_events.pages.ProfileFragment;
import com.example.cipher_events.pages.RoleSelectionFragment;
import com.example.cipher_events.pages.SearchFragment;
import com.example.cipher_events.user.EntrantEventService;
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.user.UserProfileService;
import com.example.cipher_events.waitinglist.WaitingListService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //
    DBProxy DB = DBProxy.getInstance();
    Notifier notifier;
    FragmentManager fragmentManager = getSupportFragmentManager();
    BottomNavigationView bottomNavigationView;

    private String currentRole = "";
    private boolean isLoggedIn = false;
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

        Notifier notifier = Notifier.getInstance();
        fragmentManager = getSupportFragmentManager();

        // User-related services
        UserEventHistoryRepository historyRepository = new UserEventHistoryRepository();
        userProfileService = new UserProfileService();

        // QR / Event-related services
        organizerEventService = new OrganizerEventService();
        entrantEventService = new EntrantEventService();

        waitingListService = new WaitingListService(historyRepository);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setVisibility(View.GONE); // Hide by default

        // Show role selection first
        replaceFragment(new RoleSelectionFragment());

        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            Fragment selectedFragment = null;
            int id = menuItem.getItemId();

            if ("ENTRANT".equals(currentRole)) {
                if (id == R.id.menu_home) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.menu_search) {
                    selectedFragment = new SearchFragment();
                } else if (id == R.id.menu_favourites) {
                    selectedFragment = new FavouritesFragment();
                } else if (id == R.id.menu_profile) {
                    selectedFragment = new ProfileFragment();
                }
            } else if ("ORGANIZER".equals(currentRole)) {
                if (id == R.id.menu_home) {
                    selectedFragment = new OrganizerHomeFragment();
                } else if (id == R.id.menu_create) {
                    showCreateEventDialog();
                    return false; // Show as a popup instead of switching fragments
                } else if (id == R.id.menu_history) {
                    selectedFragment = new OrganizerHistoryFragment(); // Replace with History Fragment if available
                    Toast.makeText(MainActivity.this, "Event History", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.menu_profile) {
                    selectedFragment = new OrganizerProfileFragment();
                }
            }

            replaceFragment(selectedFragment);
            return true;
        });
    }

    private void showCreateEventDialog() {
        CreateEventDialogFragment dialog = new CreateEventDialogFragment();
        dialog.setCreateEventListener((title, date, time, location, description, capacity) -> {
            // Create a new event object
            Event newEvent = new Event(
                    title,
                    description,
                    date + " " + time,
                    location,
                    new Organizer("Temp Organizer", "org@example.com", "", "", null),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    null
            );

            // Set the optional waiting list capacity
            newEvent.setWaitingListCapacity(capacity);

            // Add to system and database
            DB.addEvent(newEvent);

            // Notify the current fragment if it's a home fragment
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeFragment) {
                ((HomeFragment) currentFragment).addEvent(newEvent);
            } else if (currentFragment instanceof OrganizerHomeFragment) {
                ((OrganizerHomeFragment) currentFragment).addEvent(newEvent);
            }

            Toast.makeText(this, "Event Created: " + title, Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "CreateEventDialog");
    }

    public void onRoleSelected(String role) {
        currentRole = role;
        isLoggedIn = false;
        bottomNavigationView.setVisibility(View.GONE);
        replaceFragment(new LoginFragment());
        onLoginSuccess();
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onDestroy () {
        DB.shutdown();
        notifier.stopListener();
        super.onDestroy();
    }

    // Call this after successful login to open the correct first home page for the selected role.
    public void onLoginSuccess() {
        isLoggedIn = true;
        bottomNavigationView.setVisibility(View.VISIBLE);

        if ("ORGANIZER".equals(currentRole)) {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu_organizer_nav);
            replaceFragment(new OrganizerHomeFragment());
        } else if ("ADMIN".equals(currentRole)) {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu_bottom_nav);
            replaceFragment(new AdminHomeFragment());
        } else {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu_bottom_nav);
            replaceFragment(new HomeFragment());
        }
    }
}