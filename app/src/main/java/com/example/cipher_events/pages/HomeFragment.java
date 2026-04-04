package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private ArrayList<Event> allEvents = new ArrayList<>();
    private ArrayList<Event> displayedEvents = new ArrayList<>();

    private Button btnToday, btnThisWeek, btnPublic, btnCapacity;
    
    private String currentFilter = "ALL"; // ALL, TODAY, THIS_WEEK, PUBLIC, CAPACITY

    private DBProxy db = DBProxy.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnToday = view.findViewById(R.id.btn_filter_today);
        btnThisWeek = view.findViewById(R.id.btn_filter_this_week);
        btnPublic = view.findViewById(R.id.btn_filter_public);
        btnCapacity = view.findViewById(R.id.btn_filter_capacity);
        
        recyclerView = view.findViewById(R.id.recycler_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(displayedEvents, event -> {
            EventDetailsDialogFragment dialog = EventDetailsDialogFragment.newInstance(
                    event.getEventID(),
                    event.getName(),
                    event.getDescription(),
                    event.getTime(),
                    event.getLocation(),
                    event.getEntrants() != null ? event.getEntrants().size() : 0,
                    new ArrayList<>() // Placeholder for tags
            );
            dialog.show(getParentFragmentManager(), "EventDetailsDialog");
        });
        recyclerView.setAdapter(adapter);

        setupListeners();
        loadEvents();

        return view;
    }

    private void setupListeners() {
        btnToday.setOnClickListener(v -> updateFilter("TODAY"));
        btnThisWeek.setOnClickListener(v -> updateFilter("THIS_WEEK"));
        btnPublic.setOnClickListener(v -> updateFilter("PUBLIC"));
        if (btnCapacity != null) {
            btnCapacity.setOnClickListener(v -> updateFilter("CAPACITY"));
        }
    }

    private void updateFilter(String filter) {
        if (currentFilter.equals(filter)) {
            currentFilter = "ALL"; // Toggle off
        } else {
            currentFilter = filter;
        }
        applyFilters();
    }

    private void loadEvents() {
        allEvents.clear();
        allEvents.addAll(db.getAllEvents());
        applyFilters();
    }

    private void applyFilters() {
        displayedEvents.clear();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        String todayStr = sdf.format(today);

        cal.add(Calendar.DAY_OF_YEAR, 7);
        Date nextWeek = cal.getTime();

        for (Event event : allEvents) {
            boolean matchesFilter = true;
            if (currentFilter.equals("PUBLIC")) {
                matchesFilter = event.isPublicEvent();
            } else if (currentFilter.equals("CAPACITY")) {
                // Event has capacity if it's null (unlimited) or current entrants < capacity
                Integer capacity = event.getWaitingListCapacity();
                int currentEntrants = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
                matchesFilter = (capacity == null || currentEntrants < capacity);
            } else if (currentFilter.equals("TODAY") || currentFilter.equals("THIS_WEEK")) {
                try {
                    String eventDateStr = event.getTime().substring(0, 10);
                    Date eventDate = sdf.parse(eventDateStr);
                    
                    if (currentFilter.equals("TODAY")) {
                        matchesFilter = eventDateStr.equals(todayStr);
                    } else {
                        matchesFilter = eventDate != null && !eventDate.before(today) && eventDate.before(nextWeek);
                    }
                } catch (Exception e) {
                    matchesFilter = false; 
                }
            }

            if (matchesFilter) {
                displayedEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
        
        // Update button visual states
        btnToday.setAlpha(currentFilter.equals("TODAY") ? 1.0f : 0.5f);
        btnThisWeek.setAlpha(currentFilter.equals("THIS_WEEK") ? 1.0f : 0.5f);
        btnPublic.setAlpha(currentFilter.equals("PUBLIC") ? 1.0f : 0.5f);
        if (btnCapacity != null) {
            btnCapacity.setAlpha(currentFilter.equals("CAPACITY") ? 1.0f : 0.5f);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
        loadEvents();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadEvents);
        }
    }
}