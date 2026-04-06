package com.example.cipher_events.pages;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.comment.EntrantCommentService;
import com.example.cipher_events.comment.EventComment;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;

import java.util.ArrayList;

public class EventCommentsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_DEVICE_ID = "device_id";

    private String eventID;
    private String deviceID;

    private RecyclerView rvComments;
    private EditText etCommentInput;
    private Button btnPostComment;

    private com.example.cipher_events.adapters.EventCommentAdapter adapter;
    private EntrantCommentService commentService;

    public static EventCommentsFragment newInstance(String eventID, String deviceID) {
        EventCommentsFragment fragment = new EventCommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventID);
        args.putString(ARG_DEVICE_ID, deviceID);
        fragment.setArguments(args);
        return fragment;
    }

    public EventCommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            eventID = getArguments().getString(ARG_EVENT_ID);
            deviceID = getArguments().getString(ARG_DEVICE_ID);
        }

        rvComments = view.findViewById(R.id.rvComments);
        etCommentInput = view.findViewById(R.id.etCommentInput);
        btnPostComment = view.findViewById(R.id.btnPostComment);

        adapter = new com.example.cipher_events.adapters.EventCommentAdapter();
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setAdapter(adapter);

        // Check if admin
        boolean isAdmin = DBProxy.getInstance().getAdmin(deviceID) != null;
        
        // Check if organizer
        boolean isOrganizer = false;
        Event event = DBProxy.getInstance().getEvent(eventID);
        if (event != null && deviceID != null && deviceID.equals(event.getOrganizerID())) {
            isOrganizer = true;
        }

        adapter.setup(deviceID, isAdmin, isOrganizer, comment -> {
            commentService.deleteComment(eventID, comment.getCommentID());
            loadComments();
        });

        commentService = new EntrantCommentService();

        loadComments();

        btnPostComment.setOnClickListener(v -> postComment());
    }

    private void loadComments() {
        try {
            ArrayList<EventComment> comments = commentService.getComments(eventID);
            adapter.setComments(comments);
            if (!comments.isEmpty()) {
                rvComments.scrollToPosition(comments.size() - 1);
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void postComment() {
        String message = etCommentInput.getText().toString();

        if (TextUtils.isEmpty(message.trim())) {
            etCommentInput.setError("Comment cannot be empty");
            return;
        }

        if (message.trim().length() > 500) {
            etCommentInput.setError("Comment is too long (max 500 characters)");
            return;
        }

        User user = DBProxy.getInstance().getUser(deviceID);
        if (user == null) {
            Toast.makeText(requireContext(), "User not found.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            commentService.addComment(eventID, user, message);
            etCommentInput.setText("");
            loadComments();
            Toast.makeText(requireContext(), "Comment posted.", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}