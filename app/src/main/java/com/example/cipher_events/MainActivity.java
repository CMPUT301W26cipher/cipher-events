package com.example.cipher_events;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.organizer.OrganizerEventCreationResult;
import com.example.cipher_events.organizer.OrganizerEventService;
import com.example.cipher_events.pages.FavouritesFragment;
import com.example.cipher_events.pages.HomeFragment;
import com.example.cipher_events.pages.ProfileFragment;
import com.example.cipher_events.pages.SearchFragment;
import com.example.cipher_events.user.EntrantEventService;
import com.example.cipher_events.user.EntrantQrScanResult;
import com.example.cipher_events.user.Status;
import com.example.cipher_events.user.UserEventHistoryRecord;
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.user.UserProfileService;
import com.example.cipher_events.user.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //

    FragmentManager fragmentManager = getSupportFragmentManager();
    BottomNavigationView bottomNavigationView;

    private UserRepository userRepository;
    private UserEventHistoryRepository historyRepository;
    private UserProfileService userProfileService;

    private OrganizerEventService organizerEventService;
    private EntrantEventService entrantEventService;

    private final List<Event> allEvents = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        //User-related services
        userRepository = new UserRepository();
        historyRepository = new UserEventHistoryRepository();
        userProfileService = new UserProfileService(userRepository, historyRepository);

        //QR / Event-related services
        organizerEventService = new OrganizerEventService();
        entrantEventService = new EntrantEventService();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        replaceFragment(new HomeFragment());

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;
                int id = menuItem.getItemId();

                if (id == R.id.menu_home) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.menu_search) {
                    selectedFragment = new SearchFragment();
                } else if (id == R.id.menu_favourites) {
                    selectedFragment = new FavouritesFragment();
                } else if (id == R.id.menu_profile) {
                    selectedFragment = new ProfileFragment();
                }

                replaceFragment(selectedFragment);

                return true;
            }
        });
    }

    private void replaceFragment(Fragment fragment){
        if (fragment != null) {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
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
    // Add one event history record
    // =========================================================
    public void addUserEventHistory(String deviceId, Event event, Status status) {
        try {
            userProfileService.addEventHistory(deviceId, event, status);
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
            userProfileService.deleteUserProfile(deviceId, allEvents);
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
            //Keep a local reference for profile deletion history operations
            Event createdEvent = result.getEvent();
            if (createdEvent != null && !allEvents.contains(createdEvent)) {
                allEvents.add(createdEvent);
            }

            Toast.makeText(this, "Event created and QR generated", Toast.LENGTH_SHORT).show();
            return result;
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        } catch (WriterException e) {
            Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    // =========================================================
    // US 01.06.01
    // Entrant scans QR and views event details
    // =========================================================
    public EntrantQrScanResult getEventDetailsFromScannedQr(String scannedQRTest) {
        try {
            return entrantEventService.getEventDetailsFromScannedQr(scannedQRTest);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    // =========================================================
    // US 01.06.02
    // Entrant signs up for event from event details
    // =========================================================\
    public void signUpForEventDetails(String eventId, User entrant) {
        try {
            entrantEventService.signUpForEventFromDetails(eventId, entrant);
            Toast.makeText(this, "Successfully joined the event waiting list", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public User getUserByDeviceId(String deviceId) {
        return userRepository.findByDeviceId(deviceId);
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
}
