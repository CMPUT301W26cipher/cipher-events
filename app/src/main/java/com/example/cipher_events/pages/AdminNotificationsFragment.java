package com.example.cipher_events.pages;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cipher_events.R;
import com.example.cipher_events.logging.Logger;
import com.example.cipher_events.notifications.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminNotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NotificationLogAdapter adapter;
    private final Logger logger = Logger.getInstance();

    public AdminNotificationsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_notifications, container, false);

        recyclerView = view.findViewById(R.id.rv_notifications);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        setupSwipeRefresh();
        setupRecyclerView();
        
        loadLogs(true);

        return view;
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.button_purple);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.input_background);
        swipeRefreshLayout.setOnRefreshListener(() -> loadLogs(false));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new NotificationLogAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void loadLogs(boolean showProgress) {
        if (showProgress) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Simulating a small delay to ensure Firestore data is fetched (as per original logic)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            
            List<Message> logs = logger.getNotificationLog();
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            
            if (logs == null || logs.isEmpty()) {
                emptyStateContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setLogs(logs);
            }
        }, 600);
    }

    private static class NotificationLogAdapter extends RecyclerView.Adapter<NotificationLogAdapter.ViewHolder> {
        private List<Message> logs;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

        public NotificationLogAdapter(List<Message> logs) {
            this.logs = logs;
        }

        public void setLogs(List<Message> logs) {
            this.logs = logs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Message log = logs.get(position);
            
            holder.tvTitle.setText(log.getTitle() != null ? log.getTitle() : "Notification Sent");
            holder.tvBody.setText(log.getBody());
            
            String senderName = (log.getOrganizer() != null && log.getOrganizer().getName() != null) 
                    ? log.getOrganizer().getName() 
                    : "Unknown Organizer";
            holder.tvSender.setText("Sent by: " + senderName);
            
            if (log.getDate() != null) {
                holder.tvDate.setText(dateFormat.format(log.getDate()));
            } else {
                holder.tvDate.setText("Just now");
            }
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvBody, tvSender, tvDate;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_log_title);
                tvBody = itemView.findViewById(R.id.tv_log_body);
                tvSender = itemView.findViewById(R.id.tv_log_sender);
                tvDate = itemView.findViewById(R.id.tv_log_date);
            }
        }
    }
}