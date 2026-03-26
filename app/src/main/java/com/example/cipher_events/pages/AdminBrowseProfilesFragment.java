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
import com.example.cipher_events.adapters.ProfileAdapter;
import com.example.cipher_events.database.DBProxy;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseProfilesFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<Object> profileList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private DBProxy dbProxy;

    public AdminBrowseProfilesFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_profiles, container, false);

        recyclerView = view.findViewById(R.id.rv_profiles);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateText = view.findViewById(R.id.tv_empty_state);

        dbProxy = DBProxy.getInstance();
        dbProxy.addListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfileAdapter(profileList);
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
        profileList.clear();
        profileList.addAll(dbProxy.getAllUsers());
        profileList.addAll(dbProxy.getAllOrganizers());
        profileList.addAll(dbProxy.getAllAdmins());

        progressBar.setVisibility(View.GONE);
        if (profileList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }
}