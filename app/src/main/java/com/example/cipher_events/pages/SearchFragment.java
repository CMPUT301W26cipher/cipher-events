package com.example.cipher_events.pages;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private EditText etSearchBar;
    private CheckBox cbFilterPublic;
    private RecyclerView rvSearchResults;
    private TextView tvNoResults;
    
    private EventAdapter adapter;
    private List<Event> filteredEvents = new ArrayList<>();
    private DBProxy db = DBProxy.getInstance();

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

        etSearchBar = view.findViewById(R.id.et_search_bar);
        cbFilterPublic = view.findViewById(R.id.cb_filter_public);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        tvNoResults = view.findViewById(R.id.tv_no_results);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(filteredEvents, event -> {
            EventDetailsDialogFragment dialog = EventDetailsDialogFragment.newInstance(
                    event.getEventID(),
                    event.getName(),
                    event.getDescription(),
                    event.getTime(),
                    event.getLocation(),
                    (event.getEntrants() != null ? event.getEntrants().size() : 0),
                    new ArrayList<>() // Tags placeholder
            );
            dialog.show(getParentFragmentManager(), "EventDetailsDialog");
        });
        rvSearchResults.setAdapter(adapter);

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

        cbFilterPublic.setOnCheckedChangeListener((buttonView, isChecked) -> performSearch());

        performSearch();

        return view;
    }

    private void performSearch() {
        String keyword = etSearchBar.getText().toString().toLowerCase().trim();
        boolean publicOnly = cbFilterPublic.isChecked();
        
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
            tvNoResults.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
        performSearch(); // Refresh list in case data changed while away
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