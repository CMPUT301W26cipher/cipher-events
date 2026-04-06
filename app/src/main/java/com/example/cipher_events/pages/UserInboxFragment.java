package com.example.cipher_events.pages;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.MessageThreadAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.message.MessageThread;

import java.util.ArrayList;
import java.util.List;

/**
 * UserInboxFragment displays personal message threads for the current user.
 */
public class UserInboxFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton btnBack;
    private MessageThreadAdapter adapter;
    private final DBProxy db = DBProxy.getInstance();

    public UserInboxFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_inbox, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        recyclerView = view.findViewById(R.id.rv_inbox);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        setupSwipeRefresh();
        setupRecyclerView();
        
        loadThreads(true);

        return view;
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.button_purple);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.input_background);
        swipeRefreshLayout.setOnRefreshListener(() -> loadThreads(false));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new MessageThreadAdapter(new MessageThreadAdapter.OnThreadClickListener() {
            @Override
            public void onOpenThread(MessageThread thread) {
                openThread(thread);
            }

            @Override
            public void onCloseThread(MessageThread thread) {
                hideThread(thread);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadThreads(boolean showProgress) {
        if (showProgress) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            
            User currentUser = db.getCurrentUser();
            if (currentUser == null) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            List<MessageThread> userThreads = new ArrayList<>();
            List<Event> allEvents = db.getAllEvents();
            
            for (Event event : allEvents) {
                for (MessageThread thread : event.getMessageThreads()) {
                    if (currentUser.getDeviceID().equals(thread.getEntrantDeviceID())) {
                        if (!currentUser.isThreadHidden(thread.getThreadID())) {
                            userThreads.add(thread);
                        }
                    }
                }
            }

            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            
            if (userThreads.isEmpty()) {
                emptyStateContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setThreads(userThreads);
            }
        }, 600);
    }

    private void hideThread(MessageThread thread) {
        User currentUser = db.getCurrentUser();
        if (currentUser != null) {
            currentUser.hideThread(thread.getThreadID());
            db.updateUser(currentUser);
            loadThreads(false);
        }
    }

    private void openThread(MessageThread thread) {
        DirectChatFragment fragment = DirectChatFragment.newInstance(
                thread.getEventID(),
                thread.getThreadID(),
                db.getCurrentUser().getDeviceID(),
                false
        );

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
        loadThreads(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        db.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (isAdded()) {
            loadThreads(false);
        }
    }
}