package com.example.cipher_events.pages;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * HomeFragment displays a list of events with filtering options.
 */
public class HomeFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private final ArrayList<Event> allEvents = new ArrayList<>();
    private final ArrayList<Event> displayedEvents = new ArrayList<>();

    private MaterialButton btnToday, btnThisWeek, btnPublic, btnCapacity;
    
    private String currentFilter = "ALL"; // ALL, TODAY, THIS_WEEK, PUBLIC, CAPACITY

    private final DBProxy db = DBProxy.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        loadEvents();

        return view;
    }

    private void initializeViews(View view) {
        btnToday = view.findViewById(R.id.btn_filter_today);
        btnThisWeek = view.findViewById(R.id.btn_filter_this_week);
        btnPublic = view.findViewById(R.id.btn_filter_public);
        btnCapacity = view.findViewById(R.id.btn_filter_capacity);
        recyclerView = view.findViewById(R.id.recycler_events);
    }

    private void setupRecyclerView() {
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
        currentFilter = currentFilter.equals(filter) ? "ALL" : filter;
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
            if (matchesFilter(event, todayStr, today, nextWeek)) {
                displayedEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
        updateButtonUI();
    }

    private boolean matchesFilter(Event event, String todayStr, Date today, Date nextWeek) {
        if (currentFilter.equals("ALL")) return true;

        if (currentFilter.equals("PUBLIC")) {
            return event.isPublicEvent();
        }

        if (currentFilter.equals("CAPACITY")) {
            Integer capacity = event.getWaitingListCapacity();
            int currentEntrants = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
            return (capacity == null || currentEntrants < capacity);
        }

        if (currentFilter.equals("TODAY") || currentFilter.equals("THIS_WEEK")) {
            try {
                if (event.getTime() == null || event.getTime().length() < 10) return false;
                
                String eventDateStr = event.getTime().substring(0, 10);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date eventDate = sdf.parse(eventDateStr);
                
                if (currentFilter.equals("TODAY")) {
                    return eventDateStr.equals(todayStr);
                } else {
                    return eventDate != null && !eventDate.before(today) && eventDate.before(nextWeek);
                }
            } catch (Exception e) {
                return false; 
            }
        }
        return false;
    }

    private void updateButtonUI() {
        setButtonStyle(btnToday, "TODAY".equals(currentFilter));
        setButtonStyle(btnThisWeek, "THIS_WEEK".equals(currentFilter));
        setButtonStyle(btnPublic, "PUBLIC".equals(currentFilter));
        if (btnCapacity != null) {
            setButtonStyle(btnCapacity, "CAPACITY".equals(currentFilter));
        }
    }

    private void setButtonStyle(MaterialButton button, boolean isSelected) {
        int backgroundColor = isSelected ? R.color.button_purple : R.color.input_background;
        float alpha = isSelected ? 1.0f : 0.7f;
        
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), backgroundColor)));
        button.setAlpha(alpha);
        button.setStrokeWidth(isSelected ? 0 : 2);
        button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.button_purple)));
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