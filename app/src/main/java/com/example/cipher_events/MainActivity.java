package com.example.cipher_events;

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
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.notifications.Notifier;
import com.example.cipher_events.notifications.NotificationService;
import com.example.cipher_events.organizer.OrganizerEventService;
import com.example.cipher_events.pages.AdminBrowseEventsFragment;
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
    TextView tvCurrentRole;

    private String currentRole = "";

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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setVisibility(View.GONE); // Hide by default
        
        tvCurrentRole = findViewById(R.id.tv_current_role);

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
                    return false;
                } else if (id == R.id.menu_history) {
                    selectedFragment = new OrganizerHistoryFragment();
                } else if (id == R.id.menu_profile) {
                    selectedFragment = new OrganizerProfileFragment();
                }
            } else if ("ADMIN".equals(currentRole)) {
                if (id == R.id.menu_admin_events) {
                    selectedFragment = new AdminBrowseEventsFragment();
                } else if (id == R.id.menu_admin_profiles) {
                    selectedFragment = new AdminBrowseProfilesFragment();
                } else if (id == R.id.menu_admin_notifications) {
                    selectedFragment = new AdminNotificationsFragment();
                } else if (id == R.id.menu_admin_profile) {
                    selectedFragment = new AdminProfileFragment();
                }
            }

            replaceFragment(selectedFragment);
            return true;
        });
    }

    public void showCreateEventDialog() {
        CreateEventDialogFragment dialog = new CreateEventDialogFragment();
        dialog.setCreateEventListener((title, date, time, location, description, capacity, bannerUrl) -> {
            // Get current organizer from DB
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            Organizer currentOrganizer = DB.getOrganizer(deviceId);
            
            if (currentOrganizer == null) {
                currentOrganizer = new Organizer("Unknown Organizer", "", "", "", null);
                currentOrganizer.setDeviceID(deviceId);
            }

            Event newEvent = new Event(
                    title,
                    description,
                    date + " " + time,
                    location,
                    currentOrganizer,
                    new ArrayList<>(), // entrants
                    new ArrayList<>(), // attendees
                    bannerUrl,         // posterPictureURL from dialog
                    true               // publicEvent
            );
            
            if (capacity != null) {
                newEvent.setWaitingListCapacity(capacity);
            }
            
            // Explicitly add to DB
            DB.addEvent(newEvent);
            
            Toast.makeText(this, "Event Created and Added to DB", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "CreateEventDialog");
    }

    public void onRoleSelected(String role) {
        currentRole = role;
        
        // Update Role UI
        if (tvCurrentRole != null) {
            tvCurrentRole.setText("Role: " + role);
            tvCurrentRole.setVisibility(View.VISIBLE);
        }

        bottomNavigationView.setVisibility(View.GONE);
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

    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            // Hide role banner if we go back to role selection
            if (fragment instanceof RoleSelectionFragment) {
                if (tvCurrentRole != null) tvCurrentRole.setVisibility(View.GONE);
                currentRole = "";
            }
        }
    }

    @Override
    protected void onDestroy () {
        DB.shutdown();
        if (notifier != null) notifier.stopListener();
        super.onDestroy();
    }
}