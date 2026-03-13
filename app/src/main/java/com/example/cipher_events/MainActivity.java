package com.example.cipher_events;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.organizer.OrganizerEventCreationResult;
import com.example.cipher_events.organizer.OrganizerEventService;
import com.example.cipher_events.pages.AdminHomeFragment;
import com.example.cipher_events.pages.CreateEventDialogFragment;
import com.example.cipher_events.pages.FavouritesFragment;
import com.example.cipher_events.pages.HomeFragment;
import com.example.cipher_events.pages.OrganizerAddEventFragment;
import com.example.cipher_events.pages.OrganizerHistoryFragment;
import com.example.cipher_events.pages.OrganizerHomeFragment;
import com.example.cipher_events.pages.OrganizerProfileFragment;
import com.example.cipher_events.pages.ProfileFragment;
import com.example.cipher_events.pages.RoleSelectionFragment;
import com.example.cipher_events.pages.SearchFragment;
import com.example.cipher_events.user.EntrantEventService;
import com.example.cipher_events.user.EntrantQrScanResult;
import com.example.cipher_events.user.Status;
import com.example.cipher_events.user.UserEventHistoryRecord;
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.user.UserProfileService;
import com.example.cipher_events.waitinglist.WaitingListService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //
    DBProxy DB = DBProxy.getInstance();
    FragmentManager fragmentManager = getSupportFragmentManager();
    BottomNavigationView bottomNavigationView;

    private String currentRole = "";

    // Firestore-backed services
    private UserEventHistoryRepository historyRepository;
    private UserProfileService userProfileService;
    private OrganizerEventService organizerEventService;
    private EntrantEventService entrantEventService;
    private WaitingListService waitingListService;

    // Optional local cache for UI convenience
    private final List<Event> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        //User-related services
        historyRepository = new UserEventHistoryRepository();
        userProfileService = new UserProfileService();

        //QR / Event-related services
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

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
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
            }
        });
    }

    private void showCreateEventDialog() {
        CreateEventDialogFragment dialog = new CreateEventDialogFragment();
        dialog.setCreateEventListener((title, date, time, location, duration) -> {
            // Create a new event object
            Event newEvent = new Event(
                    title,
                    "Duration: " + duration,
                    date + " " + time,
                    location,
                    new Organizer("Temp Organizer", "org@example.com", "", "", null),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    null
            );

            // Add to system and database
            addEventToSystem(newEvent);
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
        this.currentRole = role;
        bottomNavigationView.setVisibility(View.VISIBLE);

        if ("ORGANIZER".equals(role)) {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.menu_organizer_nav);
            replaceFragment(new OrganizerHomeFragment());
        } else if ("ADMIN".equals(role)) {
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
        }
    }

    @Override
    protected void onDestroy () {
        DB.shutdown();
        super.onDestroy();
    }

    // =========================================================
    // US 01.02.01
    // Create user profile
    // =========================================================
    public User createUserProfile(String name,
                                  String email,
                                  String password,
                                  String phoneNumber,
                                  String profilePictureURL) {
        try {
            User user = userProfileService.createUserProfile(
                    name,
                    email,
                    password,
                    phoneNumber,
                    profilePictureURL
            );
            Toast.makeText(this, "Profile created successfully", Toast.LENGTH_SHORT).show();
            return user;
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    // =========================================================
    // US 01.02.02
    // Update existing user profile
    // =========================================================
    public User updateUserProfile(String deviceId,
                                  String newName,
                                  String newEmail,
                                  String newPhoneNumber,
                                  String newProfilePictureURL) {
        try {
            User updatedUser = userProfileService.updateUserProfile(
                    deviceId,
                    newName,
                    newEmail,
                    newPhoneNumber,
                    newProfilePictureURL
            );
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            return updatedUser;
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    // =========================================================
    // US 01.02.03
    // View user event history
    // =========================================================
    public List<UserEventHistoryRecord> getUserEventHistory(String deviceId) {
        try {
            return userProfileService.getUserEventHistory(deviceId);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }
    }

    /**
     * Optional helper if you still want to force a status update on an event.
     */
    public void addUserEventHistory(String deviceId, Event event, Status status) {
        try {
            userProfileService.addEventHistory(deviceId, event, status);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Optional helper to overwrite/update a user's event status.
     */
    public void upsertUserEventHistory(String deviceId, Event event, Status status) {
        try {
            userProfileService.upsertEventHistory(deviceId, event, status);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // =========================================================
    // US 01.02.04
    // Delete profile
    // =========================================================
    public void deleteUserProfile(String deviceId) {
        try {
            userProfileService.deleteUserProfile(deviceId);
            Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // =========================================================
    // US 02.01.01
    // Organizer creates event and generates QR code
    // =========================================================
    public OrganizerEventCreationResult createEventAndGenerateQr(String name,
                                                                 String description,
                                                                 String time,
                                                                 String location,
                                                                 Organizer organizer,
                                                                 String posterPictureURL,
                                                                 int qrWidth,
                                                                 int qrHeight) {
        try {
            OrganizerEventCreationResult result = organizerEventService.createEventAndGenerateQr(
                    name,
                    description,
                    time,
                    location,
                    organizer,
                    posterPictureURL,
                    qrWidth,
                    qrHeight
            );

            // Keep a local reference for profile deletion/history operations
            Event createdEvent = result.getEvent();
            if (createdEvent != null && !allEvents.contains(createdEvent)) {
                allEvents.add(createdEvent);
            }

            Toast.makeText(this, "Event created and QR generated", Toast.LENGTH_SHORT).show();
            return result;
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * Convenience helper if you only want the QR bitmap in UI code.
     */
    public String createEventAndGetQrPayload(String name,
                                             String description,
                                             String time,
                                             String location,
                                             Organizer organizer,
                                             String posterPictureURL,
                                             int qrWidth,
                                             int qrHeight) {
        OrganizerEventCreationResult result = createEventAndGenerateQr(
                name,
                description,
                time,
                location,
                organizer,
                posterPictureURL,
                qrWidth,
                qrHeight
        );

        return result == null ? null : result.getQrPayload();
    }

    public android.graphics.Bitmap generateQrBitmapFromPayload(String qrPayload, int width, int height) {
        try {
            return com.example.cipher_events.organizer.EventQrCodeGenerator
                    .generateQrBitmap(qrPayload, width, height);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to generate QR bitmap", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    // =========================================================
    // US 01.06.01
    // Entrant scans QR and views event details
    // =========================================================
    public EntrantQrScanResult getEventDetailsFromScannedQr(String scannedQrText) {
        try {
            return entrantEventService.getEventDetailsFromScannedQr(scannedQrText);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    // =========================================================
    // US 01.06.02
    // Entrant signs up for event from event details
    // =========================================================
    public void signUpForEventFromDetails(String eventId, User entrant) {
        try {
            entrantEventService.signUpForEventFromDetails(eventId, entrant);
            Toast.makeText(this, "Successfully joined the event waiting list", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // =========================================================
    // Utility methods
    // =========================================================
    public User getUserByDeviceId(String deviceId) {
        return DB.getUser(deviceId);
    }

    public void addEventToSystem(Event event) {
        if (event != null && !allEvents.contains(event)) {
            allEvents.add(event);
        }
    }

    public void setAllEvents(List<Event> events) {
        allEvents.clear();
        if (events != null) {
            allEvents.addAll(events);
        }
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(allEvents);
    }

    public UserProfileService getUserProfileService() {
        return userProfileService;
    }

    public OrganizerEventService getOrganizerEventService() {
        return organizerEventService;
    }

    public EntrantEventService getEntrantEventService() {
        return entrantEventService;
    }

    // =========================================================
    // US 01.01.01
    // Join Waiting List
    // =========================================================
    public boolean joinWaitingList(User user, Event event) {
        try {
            boolean joined = waitingListService.joinWaitingList(user, event);

            if (joined) {
                Toast.makeText(this, "Successfully joined waiting list", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to join waiting list", Toast.LENGTH_LONG).show();
            }

            return joined;
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    // =========================================================
    // US 01.01.02
    // Leave Waiting List
    // =========================================================
    public boolean leaveWaitingList(User user, Event event) {
        try {
            boolean removed = waitingListService.leaveWaitingList(user, event);

            if (removed) {
                Toast.makeText(this, "Removed from waiting list", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "User not in waiting list", Toast.LENGTH_LONG).show();
            }

            return removed;
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    // =========================================================
    // US 01.05.04
    // View Waiting List Count
    // =========================================================
    public int getWaitingListCount(Event event) {
        return waitingListService.getWaitingListCount(event);
    }

    // =========================================================
    // US 02.02.01
    // View Waiting List
    // =========================================================
    public List<User> getWaitingList(Event event) {
        return waitingListService.getWaitingList(event);
    }

    // =========================================================
    // US 02.03.01
    // Set Waiting List Capacity
    // =========================================================
    public void setWaitingListCapacity(Event event, Integer capacity) {
        waitingListService.setWaitingListCapacity(event, capacity);
    }

    // =========================================================
    // US 02.07.01
    // Notify Waiting List Entrants (Placeholder)
    // =========================================================
    public void notifyAllWaitingListEntrants(Event event, String message) {
        waitingListService.notifyAllEntrants(event, message);
    }
}