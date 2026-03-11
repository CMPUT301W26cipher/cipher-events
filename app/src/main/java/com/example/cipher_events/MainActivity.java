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
import com.example.cipher_events.database.User;
import com.example.cipher_events.pages.FavouritesFragment;
import com.example.cipher_events.pages.HomeFragment;
import com.example.cipher_events.pages.ProfileFragment;
import com.example.cipher_events.pages.SearchFragment;
import com.example.cipher_events.user.Status;
import com.example.cipher_events.user.UserEventHistoryRecord;
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.user.UserProfileService;
import com.example.cipher_events.user.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.example.cipher_events.waitinglist.WaitingListService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //

    FragmentManager fragmentManager = getSupportFragmentManager();
    BottomNavigationView bottomNavigationView;

    private UserRepository userRepository;
    private UserEventHistoryRepository historyRepository;
    private UserProfileService userProfileService;

    private WaitingListService waitingListService;

    private final List<Event> allEvents = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        userRepository = new UserRepository();
        historyRepository = new UserEventHistoryRepository();
        userProfileService = new UserProfileService(userRepository, historyRepository);
        waitingListService = new WaitingListService(historyRepository);

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
