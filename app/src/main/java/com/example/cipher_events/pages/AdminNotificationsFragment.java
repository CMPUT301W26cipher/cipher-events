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
    private TextView emptyStateText;
    private NotificationLogAdapter adapter;
    private Logger logger = Logger.getInstance();

    public AdminNotificationsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_notifications, container, false);

        recyclerView = view.findViewById(R.id.rv_notifications);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateText = view.findViewById(R.id.tv_empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationLogAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        
        loadLogs();

        return view;
    }

    private void loadLogs() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Simulating a small delay to ensure Firestore data is fetched
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<Message> logs = logger.getNotificationLog();
            progressBar.setVisibility(View.GONE);
            
            if (logs == null || logs.isEmpty()) {
                emptyStateText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setLogs(logs);
            }
        }, 500);
    }

    private static class NotificationLogAdapter extends RecyclerView.Adapter<NotificationLogAdapter.ViewHolder> {
        private List<Message> logs;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

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
            holder.tvTitle.setText(log.getTitle());
            holder.tvBody.setText(log.getBody());
            holder.tvSender.setText("Sent by: " + (log.getOrganizer() != null ? log.getOrganizer().getName() : "Unknown"));
            holder.tvDate.setText(log.getDate() != null ? dateFormat.format(log.getDate()) : "");
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