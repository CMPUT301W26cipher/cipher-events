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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.user.UserEventHistoryRecord;
import com.example.cipher_events.user.UserEventHistoryRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the event history for the current user.
 */
public class UserHistoryFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private View emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final List<Event> historyEvents = new ArrayList<>();
    
    private final DBProxy db = DBProxy.getInstance();
    private final UserEventHistoryRepository historyRepository = new UserEventHistoryRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_history, container, false);

        recyclerView = view.findViewById(R.id.userHistoryRecycler);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

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
        adapter = new EventAdapter(historyEvents, event -> {
            EventDetailsDialogFragment dialog = EventDetailsDialogFragment.newInstance(
                    event.getEventID(),
                    event.getName(),
                    event.getDescription(),
                    event.getTime(),
                    event.getLocation(),
                    event.getEntrants() != null ? event.getEntrants().size() : 0,
                    event.getTags() != null ? event.getTags() : new ArrayList<>()
            );
            dialog.show(getParentFragmentManager(), "EventDetailsDialog");
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadHistory() {
        historyEvents.clear();
        if (db.getCurrentUser() != null) {
            List<UserEventHistoryRecord> records = historyRepository.getHistory(db.getCurrentUser().getDeviceID());
            for (UserEventHistoryRecord record : records) {
                historyEvents.add(record.getEvent());
            }
        }

        updateUI();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void updateUI() {
        if (historyEvents.isEmpty()) {
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