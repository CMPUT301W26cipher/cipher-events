package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.Event;

import java.util.ArrayList;

public class OrganizerHomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private ArrayList<Event> organizedEvents = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_organizer_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadOrganizedEvents();

        adapter = new EventAdapter(organizedEvents, event -> {
            ArrayList<String> tags = new ArrayList<>();
            tags.add("Organizer View");

            EventDetailsDialogFragment dialog = EventDetailsDialogFragment.newInstance(
                    event.getEventID(),
                    event.getName(),
                    event.getDescription(),
                    event.getTime(),
                    event.getLocation(),
                    event.getAttendees() != null ? event.getAttendees().size() : 0,
                    tags,
                    true // Set as organizer view
            );

            dialog.show(getParentFragmentManager(), "EventDetailsDialog");
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    public void addEvent(Event event) {
        if (event != null) {
            organizedEvents.add(event);
            if (adapter != null) {
                adapter.notifyItemInserted(organizedEvents.size() - 1);
            }
        }
    }

    private void loadOrganizedEvents() {
        organizedEvents.clear();
        // Placeholder for organizer's events
        organizedEvents.add(new Event(
                "Event 1",
                "Description ",
                "2025-06-12 10:00 AM",
                "Location",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null
        ));

        // Add events created during this session
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            organizedEvents.addAll(activity.getAllEvents());
        }
    }
}