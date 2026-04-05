package com.example.cipher_events.pages;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.List;

public class WaitingListFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private String eventId;
    private String role;
    private WaitingListService waitingListService;
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private TabLayout tabLayout;
    private DBProxy db = DBProxy.getInstance();

    public WaitingListFragment() {}

    // UPDATED: pass role
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

        recyclerView = view.findViewById(R.id.entrants_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tabLayout = view.findViewById(R.id.tab_layout);

        Button btnDrawLottery = view.findViewById(R.id.btn_draw_lottery);
        Button btnDrawReplacement = view.findViewById(R.id.btn_draw_replacement);
        Button btnExportCsv = view.findViewById(R.id.btn_export_csv);

        // HIDE ORGANIZER FEATURES FOR ATTENDEE
        if ("attendee".equals(role)) {
            tabLayout.setVisibility(View.GONE);
            btnDrawLottery.setVisibility(View.GONE);
            btnDrawReplacement.setVisibility(View.GONE);
            btnExportCsv.setVisibility(View.GONE);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                refreshUI(getView());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Draw Lottery
        btnDrawLottery.setOnClickListener(v -> showDrawLotteryDialog());

        // Draw Replacement
        btnDrawReplacement.setOnClickListener(v -> {
            Event event = db.getEvent(eventId);
            if (event == null) {
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                return;
            }
            User replacement = waitingListService.drawReplacementEntrant(event);
            if (replacement != null) {
                db.updateEvent(event);
                Toast.makeText(getContext(),
                        "Replacement selected: " + replacement.getName(),
                        Toast.LENGTH_SHORT).show();
                refreshUI(getView());
            } else {
                Toast.makeText(getContext(),
                        "No eligible entrants for replacement",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Export CSV
        btnExportCsv.setOnClickListener(v -> exportCsv());

        refreshUI(view);
        return view;
    }

    private void showDrawLotteryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Draw Lottery Winners");

        final EditText input = new EditText(requireContext());
        input.setHint("Enter number of winners (N)");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Draw", (dialog, which) -> {
            String nStr = input.getText().toString().trim();
            if (nStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a number", Toast.LENGTH_SHORT).show();
                return;
            }
            int n = Integer.parseInt(nStr);
            Event event = db.getEvent(eventId);
            if (event == null) {
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                return;
            }
            List<User> winners = waitingListService.drawLotteryWinners(event, n);
            db.updateEvent(event);
            Toast.makeText(getContext(),
                    winners.size() + " winners selected!",
                    Toast.LENGTH_SHORT).show();
            refreshUI(getView());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void exportCsv() {
        Event event = db.getEvent(eventId);
        if (event == null) {
            Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String csv = waitingListService.exportEnrolledListAsCsv(event);

        try {
            File file = new File(requireContext().getCacheDir(), "enrolled_entrants.csv");
            FileWriter writer = new FileWriter(file);
            writer.write(csv);
            writer.close();

            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
            );

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

        TextView titleView = rootView.findViewById(R.id.title_events);
        if (titleView != null) {
            titleView.setText(event.getName() + " Waitlist");
        }

        // ATTENDEE VIEW (NO FULL WAITLIST)
        if ("attendee".equals(role)) {
            showAttendeeView(event);
            return;
        }

        // Organizer flow
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

    // ONLY SHOW CURRENT USER STATUS
    private void showAttendeeView(Event event) {
        User currentUser = db.getCurrentUser();

        List<User> waiting = waitingListService.getWaitingList(event);
        List<User> enrolled = waitingListService.getEnrolledEntrants(event);

        List<User> result;

        if (enrolled.contains(currentUser)) {
            result = List.of(currentUser);
        } else if (waiting.contains(currentUser)) {
            result = List.of(currentUser);
        } else {
            result = List.of();
        }

        adapter = new EntrantAdapter(result, EntrantAdapter.ListType.INVITED);
        recyclerView.setAdapter(adapter);
    }

    private void showList(EntrantAdapter.ListType listType) {
        Event event = db.getEvent(eventId);
        if (event == null) return;

        List<User> users;
        switch (listType) {
            case INVITED:   users = waitingListService.getInvitedEntrants(event); break;
            case CANCELLED: users = waitingListService.getCancelledEntrants(event); break;
            case ENROLLED:  users = waitingListService.getEnrolledEntrants(event); break;
            default:        users = waitingListService.getWaitingList(event); break;
        }

        adapter = new EntrantAdapter(users, listType);
        recyclerView.setAdapter(adapter);
    }
}