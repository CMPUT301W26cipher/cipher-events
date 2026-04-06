package com.example.cipher_events.pages;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WaitingListFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private String eventId;
    private String role;
    private WaitingListService waitingListService;
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private TabLayout tabLayout;
    private TextView titleView;
    private TextView listHeaderView;
    private TextView emptyStateView;
    private View organizerToolsCard;
    private ImageButton btnBack;
    
    private DBProxy db = DBProxy.getInstance();

    public WaitingListFragment() {}

    public static WaitingListFragment newInstance(String eventId, String role) {
        WaitingListFragment fragment = new WaitingListFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("role", role);
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
            role = getArguments().getString("role");
        }

        waitingListService = new WaitingListService(new UserEventHistoryRepository());

        // Initialize Views
        recyclerView = view.findViewById(R.id.entrants_recycler_view);
        tabLayout = view.findViewById(R.id.tab_layout);
        titleView = view.findViewById(R.id.title_events);
        listHeaderView = view.findViewById(R.id.list_header_text);
        emptyStateView = view.findViewById(R.id.tv_empty_state);
        organizerToolsCard = view.findViewById(R.id.organizer_tools_card);
        btnBack = view.findViewById(R.id.btn_back_waitinglist);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Button btnDrawLottery = view.findViewById(R.id.btn_draw_lottery);
        Button btnDrawReplacement = view.findViewById(R.id.btn_draw_replacement);
        Button btnExportCsv = view.findViewById(R.id.btn_export_csv);
        Button btnViewMap = view.findViewById(R.id.btn_view_map);

        // HIDE ORGANIZER FEATURES FOR ATTENDEE
        if ("attendee".equals(role)) {
            tabLayout.setVisibility(View.GONE);
            if (organizerToolsCard != null) organizerToolsCard.setVisibility(View.GONE);
            if (listHeaderView != null) listHeaderView.setText("My Status");
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                refreshUI(getView());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnDrawLottery.setOnClickListener(v -> showDrawLotteryDialog());

        btnDrawReplacement.setOnClickListener(v -> {
            Event event = db.getEvent(eventId);
            if (event == null) return;
            User replacement = waitingListService.drawReplacementEntrant(event);
            if (replacement != null) {
                db.updateEvent(event);
                Toast.makeText(getContext(), "Replacement selected!", Toast.LENGTH_SHORT).show();
                refreshUI(getView());
            }
        });

        btnExportCsv.setOnClickListener(v -> exportCsv());
        
        btnViewMap.setOnClickListener(v -> {
            EventMapFragment mapFragment = EventMapFragment.newInstance(eventId);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mapFragment)
                    .addToBackStack(null)
                    .commit();
        });

        refreshUI(view);
        return view;
    }

    private void showDrawLotteryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_draw_lottery, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etCount = dialogView.findViewById(R.id.et_lottery_count);
        Button btnCancel = dialogView.findViewById(R.id.btn_lottery_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_lottery_confirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String nStr = etCount.getText().toString().trim();
            if (nStr.isEmpty()) return;
            int n = Integer.parseInt(nStr);
            Event event = db.getEvent(eventId);
            if (event == null) return;
            waitingListService.drawLotteryWinners(event, n);
            db.updateEvent(event);
            dialog.dismiss();
            refreshUI(getView());
        });

        dialog.show();
    }

    private void exportCsv() {
        Event event = db.getEvent(eventId);
        if (event == null) return;
        String csv = waitingListService.exportEnrolledListAsCsv(event);
        try {
            File file = new File(requireContext().getCacheDir(), "enrolled_entrants.csv");
            FileWriter writer = new FileWriter(file);
            writer.write(csv);
            writer.close();
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share CSV"));
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to export CSV", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
        refreshUI(getView());
    }

    @Override
    public void onPause() {
        super.onPause();
        db.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> refreshUI(getView()));
        }
    }

    private void refreshUI(View rootView) {
        if (rootView == null) return;
        Event event = db.getEvent(eventId);
        if (event == null) return;
        if (titleView != null) titleView.setText(event.getName());
        if ("attendee".equals(role)) {
            showAttendeeView(event);
            return;
        }
        int selectedTab = tabLayout.getSelectedTabPosition();
        EntrantAdapter.ListType listType;
        switch (selectedTab) {
            case 1: listType = EntrantAdapter.ListType.INVITED; break;
            case 2: listType = EntrantAdapter.ListType.CANCELLED; break;
            case 3: listType = EntrantAdapter.ListType.ENROLLED; break;
            default: listType = EntrantAdapter.ListType.WAITLIST; break;
        }
        showList(listType);
    }

    private void showAttendeeView(Event event) {
        User currentUser = db.getCurrentUser();
        if (currentUser == null) return;
        List<User> result = new ArrayList<>();
        if (waitingListService.getEnrolledEntrants(event).contains(currentUser)) result.add(currentUser);
        else if (waitingListService.getInvitedEntrants(event).contains(currentUser)) result.add(currentUser);
        else if (waitingListService.getCancelledEntrants(event).contains(currentUser)) result.add(currentUser);
        else if (waitingListService.getWaitingList(event).contains(currentUser)) result.add(currentUser);
        updateRecycler(result, EntrantAdapter.ListType.WAITLIST);
    }

    private void showList(EntrantAdapter.ListType listType) {
        Event event = db.getEvent(eventId);
        if (event == null) return;
        List<User> users;
        switch (listType) {
            case INVITED: users = waitingListService.getInvitedEntrants(event); break;
            case CANCELLED: users = waitingListService.getCancelledEntrants(event); break;
            case ENROLLED: users = waitingListService.getEnrolledEntrants(event); break;
            default: users = waitingListService.getWaitingList(event); break;
        }
        updateRecycler(users, listType);
    }
    
    private void updateRecycler(List<User> users, EntrantAdapter.ListType listType) {
        if (users == null || users.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter = new EntrantAdapter(users, listType);
        recyclerView.setAdapter(adapter);
    }
}