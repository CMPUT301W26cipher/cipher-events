package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EntrantAdapter;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.waitinglist.WaitingListService;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class WaitingListFragment extends Fragment {

    private Event event;
    private WaitingListService waitingListService;
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private TabLayout tabLayout;

    public WaitingListFragment() {}

    public static WaitingListFragment newInstance(String eventId) {
        WaitingListFragment fragment = new WaitingListFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.waitinglist, container, false);

        // Get event ID and create dummy event for now
        if (getArguments() != null) {
            String eventId = getArguments().getString("eventId");
            // TODO: replace with real Firestore fetch using eventId
            event = new Event("Test Event", "", "", "", null, new ArrayList<>(), new ArrayList<>(), null);
            event.setInvitedEntrants(new ArrayList<>());
            event.setCancelledEntrants(new ArrayList<>());
            event.setEnrolledEntrants(new ArrayList<>());
        }

        // Set up service
        waitingListService = new WaitingListService(new UserEventHistoryRepository());

        // Set up views
        recyclerView = view.findViewById(R.id.entrants_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tabLayout = view.findViewById(R.id.tab_layout);

        // Default: show invited list
        showList(EntrantAdapter.ListType.INVITED);

        // Tab switching
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: showList(EntrantAdapter.ListType.INVITED); break;
                    case 1: showList(EntrantAdapter.ListType.CANCELLED); break;
                    case 2: showList(EntrantAdapter.ListType.ENROLLED); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }

    private void showList(EntrantAdapter.ListType listType) {
        if (event == null) return;

        List<User> users;
        switch (listType) {
            case CANCELLED: users = waitingListService.getCancelledEntrants(event); break;
            case ENROLLED:  users = waitingListService.getEnrolledEntrants(event); break;
            default:        users = waitingListService.getInvitedEntrants(event); break;
        }

        adapter = new EntrantAdapter(users, listType, user -> {
            boolean success = waitingListService.markAsNoShow(user, event);
            if (success) {
                Toast.makeText(getContext(), user.getName() + " marked as no-show", Toast.LENGTH_SHORT).show();
                showList(EntrantAdapter.ListType.INVITED);
            }
        });

        recyclerView.setAdapter(adapter);
    }
}