package com.example.cipher_events.pages;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchFragment allows users to search for events by keyword and filter by public status.
 */
public class SearchFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private EditText etSearchBar;
    private Chip chipPublicOnly;
    private RecyclerView rvSearchResults;
    private View emptyStateContainer;
    
    private EventAdapter adapter;
    private final List<Event> filteredEvents = new ArrayList<>();
    private final DBProxy db = DBProxy.getInstance();

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupListeners();

        performSearch();

        return view;
    }

    private void initializeViews(View view) {
        etSearchBar = view.findViewById(R.id.et_search_bar);
        chipPublicOnly = view.findViewById(R.id.chip_public_only);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
    }

    private void setupRecyclerView() {
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(filteredEvents, event -> {
            ScannedEventDetailsDialogFragment dialog = ScannedEventDetailsDialogFragment.newInstance(event.getEventID());
            dialog.show(getParentFragmentManager(), "ScannedEventDetailsDialog");
        });
        rvSearchResults.setAdapter(adapter);
    }

    private void setupListeners() {
        etSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipPublicOnly.setOnCheckedChangeListener((buttonView, isChecked) -> performSearch());
    }

    private void performSearch() {
        String keyword = etSearchBar.getText().toString().toLowerCase().trim();
        boolean publicOnly = chipPublicOnly.isChecked();
        
        List<Event> allEvents = db.getAllEvents();
        filteredEvents.clear();

        for (Event event : allEvents) {
            boolean matchesKeyword = keyword.isEmpty() || 
                    (event.getName() != null && event.getName().toLowerCase().contains(keyword)) || 
                    (event.getDescription() != null && event.getDescription().toLowerCase().contains(keyword));
            
            boolean matchesPublicFilter = !publicOnly || event.isPublicEvent();

            if (matchesKeyword && matchesPublicFilter) {
                filteredEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredEvents.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
        performSearch();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::performSearch);
        }
    }
}