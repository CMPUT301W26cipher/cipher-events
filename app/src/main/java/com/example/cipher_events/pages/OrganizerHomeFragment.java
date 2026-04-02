package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;

import java.util.ArrayList;

public class OrganizerHomeFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private ArrayList<Event> organizedEvents = new ArrayList<>();
    private DBProxy db = DBProxy.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_organizer_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(organizedEvents, event -> {
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
        organizedEvents.clear();
        organizedEvents.addAll(db.getAllEvents());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

}