package com.example.cipher_events.pages;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment that displays the history of events organized by the user.
 * Shows only past events that the current organizer has managed.
 */
public class OrganizerHistoryFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private View emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final ArrayList<Event> pastEvents = new ArrayList<>();
    
    private final DBProxy db = DBProxy.getInstance();
    private String deviceId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_history, container, false);

        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        recyclerView = view.findViewById(R.id.organizerHistoryRecycler);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        
        setupSwipeRefresh();
        setupRecyclerView();
        loadHistory();

        return view;
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.button_purple);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.input_background);
        swipeRefreshLayout.setOnRefreshListener(this::loadHistory);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(pastEvents, event -> {
            ArrayList<String> tags = new ArrayList<>();
            tags.add("Past Event");
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

    private void loadHistory() {
        pastEvents.clear();
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Event event : db.getAllEvents()) {
            // Check if user is the organizer AND if the event is in the past
            if (deviceId.equals(event.getOrganizerID()) && isPastEvent(event, now, sdf)) {
                pastEvents.add(event);
            }
        }
        
        // Sort by date (most recent first)
        Collections.sort(pastEvents, (e1, e2) -> e2.getTime().compareTo(e1.getTime()));
        
        updateUI();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void updateUI() {
        if (pastEvents.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    private boolean isPastEvent(Event event, Date now, SimpleDateFormat sdf) {
        try {
            if (event.getTime() == null || event.getTime().length() < 10) return false;
            String eventDateStr = event.getTime().substring(0, 10);
            Date eventDate = sdf.parse(eventDateStr);
            return eventDate != null && eventDate.before(now);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
        loadHistory();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadHistory);
        }
    }
}
