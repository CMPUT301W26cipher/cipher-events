package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.List;

public class AdminBrowseEventsFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView emptyStateText;
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
        emptyStateText = view.findViewById(R.id.tv_empty_state);

        dbProxy = DBProxy.getInstance();
        dbProxy.addListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(eventList, event -> {
            // Show admin-exclusive event details dialog
            AdminEventDetailsDialogFragment dialog = AdminEventDetailsDialogFragment.newInstance(event.getEventID());
            dialog.show(getParentFragmentManager(), "AdminEventDetailsDialog");
        });
        recyclerView.setAdapter(adapter);

        updateUI();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dbProxy.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (isAdded()) {
            getActivity().runOnUiThread(this::updateUI);
        }
    }

    private void updateUI() {
        eventList.clear();
        eventList.addAll(dbProxy.getAllEvents());

        progressBar.setVisibility(View.GONE);
        if (eventList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }
}