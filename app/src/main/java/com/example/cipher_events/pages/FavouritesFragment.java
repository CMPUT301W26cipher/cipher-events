package com.example.cipher_events.pages;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;

import java.util.ArrayList;
import java.util.List;

public class FavouritesFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> filteredEvents = new ArrayList<>();
    private DBProxy db = DBProxy.getInstance();
    private String deviceId;
    
    private TextView tabWaitlist, tabFavourite;
    private boolean isWaitlistTab = true;

    public FavouritesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favourites, container, false);

        recyclerView = view.findViewById(R.id.favouritesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tabWaitlist = view.findViewById(R.id.tab_attending);
        tabFavourite = view.findViewById(R.id.tab_favourite);

        adapter = new EventAdapter(filteredEvents, event -> {
            // When user clicks an event in the Waitlist tab, show the management dialog (join/leave toggle)
            // instead of the scan dialog.
            ScannedEventDetailsDialogFragment dialog = ScannedEventDetailsDialogFragment.newInstance(event.getEventID());
            dialog.show(getParentFragmentManager(), "ScannedEventDetailsDialog");
        });

        recyclerView.setAdapter(adapter);

        tabWaitlist.setOnClickListener(v -> {
            isWaitlistTab = true;
            refreshUI();
        });

        tabFavourite.setOnClickListener(v -> {
            isWaitlistTab = false;
            refreshUI();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
        refreshUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::refreshUI);
        }
    }

    private void refreshUI() {
        filteredEvents.clear();
        ArrayList<Event> allEvents = db.getAllEvents();

        if (isWaitlistTab) {
            // Filter events where the user is in the entrants (waitlist) list
            for (Event event : allEvents) {
                if (isUserInWaitlist(event)) {
                    filteredEvents.add(event);
                }
            }
        } else {
            // Placeholder for "Favourite" logic
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        updateTabStyles();
    }

    private boolean isUserInWaitlist(Event event) {
        if (event.getEntrants() == null) return false;
        for (User u : event.getEntrants()) {
            if (u.getDeviceID() != null && u.getDeviceID().equals(deviceId)) {
                return true;
            }
        }
        return false;
    }

    private void updateTabStyles() {
        if (isWaitlistTab) {
            tabWaitlist.setAlpha(1.0f);
            tabFavourite.setAlpha(0.5f);
        } else {
            tabWaitlist.setAlpha(0.5f);
            tabFavourite.setAlpha(1.0f);
        }
    }
}