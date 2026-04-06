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

import java.util.ArrayList;
import java.util.Collections;

/**
 * Fragment that displays all events managed by the current organizer.
 */
public class OrganizerHistoryFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private View emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final ArrayList<Event> managedEvents = new ArrayList<>();

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
        adapter = new EventAdapter(managedEvents, event -> {
            ArrayList<String> tags = new ArrayList<>();
            tags.add("Organizer Mode");

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
        managedEvents.clear();

        String currentId = deviceId;
        if (db.getCurrentUser() != null) {
            currentId = db.getCurrentUser().getDeviceID();
        }

        for (Event event : db.getAllEvents()) {
            // Fetch all events where the current user is the organizer
            if (currentId != null && currentId.equals(event.getOrganizerID())) {
                managedEvents.add(event);
            }
        }

        // Sort by date (most recent first)
        Collections.sort(managedEvents, (e1, e2) -> {
            String t1 = e1.getTime();
            String t2 = e2.getTime();
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.compareTo(t1);
        });

        updateUI();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void updateUI() {
        if (managedEvents.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
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
