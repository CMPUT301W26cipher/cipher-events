package com.example.cipher_events.pages;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.MessageThreadAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.message.MessageThread;
import com.example.cipher_events.message.MessagingService;

import java.util.List;

public class MessageThreadsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_ORGANIZER_DEVICE_ID = "organizer_device_id";

    private String eventID;
    private String organizerDeviceID;

    private MessagingService messagingService;
    private DBProxy db;

    private TextView tvEventTitle;
    private TextView tvEmptyState;
    private RecyclerView rvThreads;

    private MessageThreadAdapter adapter;

    public MessageThreadsFragment() { }

    public static MessageThreadsFragment newInstance(String eventID, String organizerDeviceID) {
        MessageThreadsFragment fragment = new MessageThreadsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventID);
        args.putString(ARG_ORGANIZER_DEVICE_ID, organizerDeviceID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message_threads, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            eventID = getArguments().getString(ARG_EVENT_ID);
            organizerDeviceID = getArguments().getString(ARG_ORGANIZER_DEVICE_ID);
        }

        messagingService = new MessagingService();
        db = DBProxy.getInstance();

        tvEventTitle = view.findViewById(R.id.tvEventTitle);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        rvThreads = view.findViewById(R.id.rvThreads);

        adapter = new MessageThreadAdapter(this::openThread);
        rvThreads.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvThreads.setAdapter(adapter);

        loadHeader();
        loadThreads();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadThreads();
    }

    private void loadHeader() {
        Event event = db.getEvent(eventID);
        if (event != null && !TextUtils.isEmpty(event.getName())) {
            tvEventTitle.setText(event.getName());
        } else {
            tvEventTitle.setText("Event Messages");
        }
    }

    private void loadThreads() {
        try {
            List<MessageThread> threads =
                    messagingService.getThreadsForOrganizer(eventID, organizerDeviceID);

            adapter.setThreads(threads);

            if (threads == null || threads.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                rvThreads.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvThreads.setVisibility(View.VISIBLE);
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openThread(MessageThread thread) {
        DirectChatFragment fragment = DirectChatFragment.newInstance(
                eventID,
                thread.getThreadID(),
                organizerDeviceID,
                true
        );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}