package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.AdminEventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseEventsFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private AdminEventAdapter adapter;
    private final List<Event> eventList = new ArrayList<>();
    private ProgressBar progressBar;
    private View emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DBProxy dbProxy;

    public AdminBrowseEventsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_events, container, false);

        recyclerView = view.findViewById(R.id.rv_events);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        dbProxy = DBProxy.getInstance();

        setupSwipeRefresh();
        setupRecyclerView();

        loadEvents();

        return view;
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.button_purple);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.input_background);
        swipeRefreshLayout.setOnRefreshListener(this::loadEvents);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new AdminEventAdapter(eventList, event -> {
            AdminEventDetailsDialogFragment dialog = AdminEventDetailsDialogFragment.newInstance(event.getEventID());
            dialog.show(getParentFragmentManager(), "AdminEventDetailsDialog");
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadEvents() {
        if (eventList.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Data is fetched synchronously from DBProxy in this architecture
        updateUI();
    }

    private void updateUI() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            eventList.clear();
            eventList.addAll(dbProxy.getAllEvents());

            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);

            if (eventList.isEmpty()) {
                emptyStateContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        dbProxy.addListener(this);
        loadEvents();
    }

    @Override
    public void onPause() {
        super.onPause();
        dbProxy.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (isAdded()) {
            loadEvents();
        }
    }
}