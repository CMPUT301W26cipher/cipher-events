package com.example.cipher_events.pages;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.Event;

import java.util.ArrayList;
import java.util.List;

public class FavouritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> favouriteEvents;
    private TextView tabAttending, tabFavourite;  // 1. Add these fields

    public FavouritesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favourites, container, false);

        // 2. Find the tab views
        tabAttending = view.findViewById(R.id.tab_attending);
        tabFavourite = view.findViewById(R.id.tab_favourite);

        // 3. Set initial state — Favourites tab selected since this is FavouritesFragment
        selectTab(tabFavourite, tabAttending);

        // 4. Set click listeners
        tabAttending.setOnClickListener(v -> {
            selectTab(tabAttending, tabFavourite);
            // Load attending events (e.g. switch fragment or filter list)
        });

        tabFavourite.setOnClickListener(v -> {
            selectTab(tabFavourite, tabAttending);
            // Load favourite events
        });

        recyclerView = view.findViewById(R.id.favouritesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        favouriteEvents = new ArrayList<>();
        adapter = new EventAdapter(favouriteEvents, event -> {
            // handle click
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    // 5. Add the selectTab helper method
    private void selectTab(TextView selected, TextView unselected) {
        selected.setSelected(true);
        selected.setTextColor(Color.parseColor("#B388FF"));

        unselected.setSelected(false);
        unselected.setTextColor(Color.parseColor("#CFCFCF"));
    }
}