package com.example.cipher_events.pages;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
import java.util.Date;
import java.util.Locale;

public class OrganizerHomeFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private final ArrayList<Event> allEvents = new ArrayList<>();
    private final ArrayList<Event> displayedEvents = new ArrayList<>();

    private MaterialButton btnAll, btnUpcoming, btnPast;
    private String currentFilter = "ALL"; // ALL, UPCOMING, PAST

    private final DBProxy db = DBProxy.getInstance();
    private String deviceId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_home, container, false);

        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        loadEvents();

        return view;
    }

    private void initializeViews(View view) {
        btnAll = view.findViewById(R.id.btn_filter_all);
        btnUpcoming = view.findViewById(R.id.btn_filter_upcoming);
        btnPast = view.findViewById(R.id.btn_filter_past);
        recyclerView = view.findViewById(R.id.recycler_organizer_events);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(displayedEvents, event -> {
            ArrayList<String> tags = new ArrayList<>();
            tags.add("Organizer View");

            EventDetailsDialogFragment dialog = EventDetailsDialogFragment.newInstance(
                    event.getEventID(),
                    event.getName(),
                    event.getDescription(),
                    event.getTime(),
                    event.getLocation(),
                    event.getEntrants() != null ? event.getEntrants().size() : 0,
                    tags,
                    true // Set as organizer view
            );

            dialog.show(getParentFragmentManager(), "EventDetailsDialog");
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAll.setOnClickListener(v -> updateFilter("ALL"));
        btnUpcoming.setOnClickListener(v -> updateFilter("UPCOMING"));
        btnPast.setOnClickListener(v -> updateFilter("PAST"));
    }

    private void updateFilter(String filter) {
        currentFilter = filter;
        applyFilters();
    }

    private void loadEvents() {
        allEvents.clear();
        // Only load events organized by this user
        for (Event event : db.getAllEvents()) {
            if (deviceId.equals(event.getOrganizerID())) {
                allEvents.add(event);
            }
        }
        applyFilters();
    }

    private void applyFilters() {
        displayedEvents.clear();
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Event event : allEvents) {
            if (matchesFilter(event, now, sdf)) {
                displayedEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
        updateButtonUI();
    }

    private boolean matchesFilter(Event event, Date now, SimpleDateFormat sdf) {
        if (currentFilter.equals("ALL")) return true;

        try {
            if (event.getTime() == null || event.getTime().length() < 10) return false;
            String eventDateStr = event.getTime().substring(0, 10);
            Date eventDate = sdf.parse(eventDateStr);
            
            if (eventDate == null) return false;

            if (currentFilter.equals("UPCOMING")) {
                return !eventDate.before(now);
            } else if (currentFilter.equals("PAST")) {
                return eventDate.before(now);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private void updateButtonUI() {
        setButtonStyle(btnAll, "ALL".equals(currentFilter));
        setButtonStyle(btnUpcoming, "UPCOMING".equals(currentFilter));
        setButtonStyle(btnPast, "PAST".equals(currentFilter));
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
