package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

/**
 * FavouritesFragment displays events the user has joined (Waitlist) or marked as favourite.
 */
public class FavouritesFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private final List<Event> filteredEvents = new ArrayList<>();
    private final DBProxy db = DBProxy.getInstance();
    
    private TextView tabWaitlist, tabFavourite;
    private View emptyStateContainer;
    private TextView tvEmptyMsg;
    private ImageView emptyIcon;
    
    private boolean isWaitlistTab = true;

    public FavouritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favourites, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.favouritesRecycler);
        tabWaitlist = view.findViewById(R.id.tab_attending);
        tabFavourite = view.findViewById(R.id.tab_favourite);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        tvEmptyMsg = view.findViewById(R.id.tv_empty_msg);
        emptyIcon = view.findViewById(R.id.empty_icon);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(filteredEvents, event -> {
            // Consistent with other fragments, showing detailed event view
            ScannedEventDetailsDialogFragment dialog = ScannedEventDetailsDialogFragment.newInstance(event.getEventID());
            dialog.show(getParentFragmentManager(), "ScannedEventDetailsDialog");
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        tabWaitlist.setOnClickListener(v -> {
            if (!isWaitlistTab) {
                isWaitlistTab = true;
                refreshUI();
            }
        });

        tabFavourite.setOnClickListener(v -> {
            if (isWaitlistTab) {
                isWaitlistTab = false;
                refreshUI();
            }
        });
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
        User currentUser = db.getCurrentUser();

        if (isWaitlistTab) {
            for (Event event : allEvents) {
                if (isUserInWaitlist(event, currentUser)) {
                    filteredEvents.add(event);
                }
            }
            tvEmptyMsg.setText("You haven't joined any waitlists yet.");
            emptyIcon.setImageResource(R.drawable.baseline_notifications_none_24);
        } else {
            if (currentUser != null) {
                ArrayList<String> favorites = currentUser.getFavoriteEventIds();
                for (Event event : allEvents) {
                    if (favorites.contains(event.getEventID())) {
                        filteredEvents.add(event);
                    }
                }
            }
            tvEmptyMsg.setText("No favourite events yet.");
            emptyIcon.setImageResource(R.drawable.baseline_star_border_24);
        }

        adapter.notifyDataSetChanged();
        
        if (filteredEvents.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        
        updateTabStyles();
    }

    private boolean isUserInWaitlist(Event event, User currentUser) {
        if (event.getEntrants() == null || currentUser == null) return false;
        String currentId = currentUser.getDeviceID();
        for (User u : event.getEntrants()) {
            if (u.getDeviceID() != null && u.getDeviceID().equals(currentId)) {
                return true;
            }
        }
        return false;
    }

    private void updateTabStyles() {
        if (isWaitlistTab) {
            tabWaitlist.setBackgroundResource(R.drawable.filter_button_bg);
            tabWaitlist.setAlpha(1.0f);
            tabFavourite.setBackground(null);
            tabFavourite.setAlpha(0.5f);
        } else {
            tabWaitlist.setBackground(null);
            tabWaitlist.setAlpha(0.5f);
            tabFavourite.setBackgroundResource(R.drawable.filter_button_bg);
            tabFavourite.setAlpha(1.0f);
        }
    }
}
