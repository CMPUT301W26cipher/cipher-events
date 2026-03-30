package com.example.cipher_events.pages;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private ArrayList<Event> upcomingEvents = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadUpcomingEvents();

        // Pass click listener into adapter
        adapter = new EventAdapter(upcomingEvents, event -> {
            ArrayList<String> tags = new ArrayList<>();
            tags.add("Tech");
            tags.add("Free");
            tags.add("Outdoors");

            EventDetailsDialogFragment dialog = EventDetailsDialogFragment.newInstance(
                    event.getEventID(),
                    event.getName(),
                    event.getDescription(),
                    event.getTime(),
                    event.getLocation(),
                    event.getAttendees() != null ? event.getAttendees().size() : 0,
                    tags
            );

            dialog.show(getParentFragmentManager(), "EventDetailsDialog");
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    public void addEvent(Event event) {
        if (event != null) {
            upcomingEvents.add(event);
            if (adapter != null) {
                adapter.notifyItemInserted(upcomingEvents.size() - 1);
            }
        }
    }

    private void loadUpcomingEvents() {
        upcomingEvents.clear();
//        upcomingEvents.add(new Event(
//                "Tech Expo 2025",
//                "A huge tech showcase",
//                "2025-04-12 10:00 AM",
//                "Edmonton Convention Centre",
//                null,
//                new ArrayList<>(),
//                new ArrayList<>(),
//                "https://pinkcaviar.com.au/wp-content/uploads/2022/09/Professional-and-Stress-Free-Event-Management-Banner-1.jpg"
//        ));
//
//        upcomingEvents.add(new Event(
//                "Music Festival",
//                "Outdoor festival with multiple stages",
//                "2025-05-01 6:00 PM",
//                "Calgary",
//                null,
//                new ArrayList<>(),
//                new ArrayList<>(),
//                null
//        ));

        // Add events created during this session
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            DBProxy db = DBProxy.getInstance();
            upcomingEvents.addAll(db.getAllEvents());
        }
    }
}