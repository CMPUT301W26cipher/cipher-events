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
import com.example.cipher_events.adapters.EntrantAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.user.UserEventHistoryRepository;
import com.example.cipher_events.waitinglist.WaitingListService;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class WaitingListFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private String eventId;
    private WaitingListService waitingListService;
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private TabLayout tabLayout;
    private DBProxy db = DBProxy.getInstance();

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

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        // Set up service
        waitingListService = new WaitingListService(new UserEventHistoryRepository());

        // Set up views
        recyclerView = view.findViewById(R.id.entrants_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tabLayout = view.findViewById(R.id.tab_layout);

        // Tab switching
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                refreshUI();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        refreshUI();

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
        int selectedTab = tabLayout.getSelectedTabPosition();
        EntrantAdapter.ListType listType;
        switch (selectedTab) {
            case 1: listType = EntrantAdapter.ListType.CANCELLED; break;
            case 2: listType = EntrantAdapter.ListType.ENROLLED; break;
            default: listType = EntrantAdapter.ListType.INVITED; break;
        }
        showList(listType);
    }

    private void showList(EntrantAdapter.ListType listType) {
        Event event = db.getEvent(eventId);
        if (event == null) return;

        List<User> users;
        switch (listType) {
            case CANCELLED: users = waitingListService.getCancelledEntrants(event); break;
            case ENROLLED:  users = waitingListService.getEnrolledEntrants(event); break;
            default:        users = waitingListService.getWaitingList(event); break;
        }

        adapter = new EntrantAdapter(users, listType);
        recyclerView.setAdapter(adapter);
    }
}
