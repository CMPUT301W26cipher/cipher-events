package com.example.cipher_events;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cipher_events.pages.FavouritesFragment;
import com.example.cipher_events.pages.HomeFragment;
import com.example.cipher_events.pages.ProfileFragment;
import com.example.cipher_events.pages.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    //

    FragmentManager fragmentManager = getSupportFragmentManager();
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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
}
