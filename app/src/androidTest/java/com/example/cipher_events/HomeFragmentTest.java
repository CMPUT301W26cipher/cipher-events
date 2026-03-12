package com.example.cipher_events;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.example.cipher_events.pages.HomeFragment;
import com.example.cipher_events.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HomeFragmentTest {

    @Test
    public void testEventsLoadAndClickOpensDialog() {
        // Launch the fragment
        FragmentScenario<HomeFragment> scenario =
                FragmentScenario.launchInContainer(HomeFragment.class);

        scenario.onFragment(fragment -> {
            RecyclerView recycler = fragment.getView().findViewById(R.id.recycler_events);

            // Check RecyclerView exists
            assertNotNull(recycler);

            // Check events were loaded
            assertTrue(recycler.getAdapter().getItemCount() > 0);
        });

        // Click the first event in the list
        Espresso.onView(ViewMatchers.withId(R.id.recycler_events))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

        // Check that the dialog title appears
        Espresso.onView(ViewMatchers.withText("Tech Expo 2025"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}