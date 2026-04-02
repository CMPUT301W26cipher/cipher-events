package com.example.cipher_events.pages;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.DirectMessageAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.message.DirectMessage;
import com.example.cipher_events.message.MessageThread;
import com.example.cipher_events.message.MessagingService;

import java.util.ArrayList;

public class DirectChatFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_THREAD_ID = "thread_id";
    private static final String ARG_CURRENT_DEVICE_ID = "current_device_id";
    private static final String ARG_IS_ORGANIZER = "is_organizer";

    private String eventID;
    private String threadID;
    private String currentDeviceID;
    private boolean isOrganizer;

    private MessagingService messagingService;
    private DBProxy db;

    private RecyclerView rvMessages;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;
    private TextView tvChatTitle;
    private TextView tvEventName;

    private DirectMessageAdapter adapter;

    public DirectChatFragment() { }

    public static DirectChatFragment newInstance(String eventID,
                                                 String threadID,
                                                 String currentDeviceID,
                                                 boolean isOrganizer) {
        DirectChatFragment fragment = new DirectChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventID);
        args.putString(ARG_THREAD_ID, threadID);
        args.putString(ARG_CURRENT_DEVICE_ID, currentDeviceID);
        args.putBoolean(ARG_IS_ORGANIZER, isOrganizer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_direct_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            eventID = getArguments().getString(ARG_EVENT_ID);
            threadID = getArguments().getString(ARG_THREAD_ID);
            currentDeviceID = getArguments().getString(ARG_CURRENT_DEVICE_ID);
            isOrganizer = getArguments().getBoolean(ARG_IS_ORGANIZER, false);
        }

        messagingService = new MessagingService();
        db = DBProxy.getInstance();

        tvChatTitle = view.findViewById(R.id.tvChatTitle);
        tvEventName = view.findViewById(R.id.tvEventName);
        rvMessages = view.findViewById(R.id.rvMessages);
        etMessageInput = view.findViewById(R.id.etMessageInput);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);

        adapter = new DirectMessageAdapter(currentDeviceID);
        rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMessages.setAdapter(adapter);

        loadHeader();
        loadMessages();

        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessages();
    }

    private void loadHeader() {
        Event event = db.getEvent(eventID);
        if (event != null) {
            tvEventName.setText(event.getName());
        }

        MessageThread thread = findThread();
        if (thread == null) {
            tvChatTitle.setText("Chat");
            return;
        }

        if (isOrganizer) {
            User entrant = db.getUser(thread.getEntrantDeviceID());
            tvChatTitle.setText(entrant != null ? entrant.getName() : "Entrant");
        } else {
            Organizer organizer = db.getOrganizer(thread.getOrganizerDeviceID());
            tvChatTitle.setText(organizer != null ? organizer.getName() : "Organizer");
        }
    }

    private void loadMessages() {
        try {
            ArrayList<DirectMessage> messages =
                    messagingService.getThreadMessages(eventID, threadID, currentDeviceID);
            adapter.setMessages(messages);

            if (!messages.isEmpty()) {
                rvMessages.scrollToPosition(messages.size() - 1);
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendMessage() {
        String message = etMessageInput.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(requireContext(), "Message cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (isOrganizer) {
                messagingService.sendMessageAsOrganizer(eventID, currentDeviceID, threadID, message);
            } else {
                messagingService.sendMessageAsEntrant(eventID, currentDeviceID, message);
            }

            etMessageInput.setText("");
            loadMessages();
        } catch (IllegalArgumentException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private MessageThread findThread() {
        Event event = db.getEvent(eventID);
        if (event == null) {
            return null;
        }

        for (MessageThread thread : event.getMessageThreads()) {
            if (thread != null && threadID.equals(thread.getThreadID())) {
                return thread;
            }
        }
        return null;
    }
}