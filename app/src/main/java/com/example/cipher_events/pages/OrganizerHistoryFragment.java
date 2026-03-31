package com.example.cipher_events.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.Event;

import java.util.ArrayList;
import java.util.List;

public class OrganizerHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> pastEvents;

    public OrganizerHistoryFragment() {}

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_history, container, false);

        recyclerView = view.findViewById(R.id.organizerHistoryRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        pastEvents = new ArrayList<>();
        // Add dummy data for now
        pastEvents.add(new Event("Past Event 1", "Old description", "2024-01-01", "Old Venue", null, new ArrayList<>(), new ArrayList<>(), null, true));

        adapter = new EventAdapter(pastEvents, event -> {
            ArrayList<String> tags = new ArrayList<>();
            tags.add("Past Event");

            EventDetailsDialogFragment dialog = EventDetailsDialogFragment.newInstance(
                    event.getEventID(),
                    event.getName(),
                    event.getDescription(),
                    event.getTime(),
                    event.getLocation(),
                    event.getAttendees().size(),
                    tags,
                    true // Organizer view
            );

            dialog.show(getParentFragmentManager(), "EventDetailsDialog");
        });

        recyclerView.setAdapter(adapter);

        return view;
    }
}