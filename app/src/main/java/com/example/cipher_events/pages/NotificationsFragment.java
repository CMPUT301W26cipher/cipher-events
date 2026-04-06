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
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.User;
import com.example.cipher_events.logging.Logger;
import com.example.cipher_events.notifications.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * NotificationsFragment displays personal notifications for the current user.
 */
public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton btnBack;
    private NotificationAdapter adapter;
    private final Logger logger = Logger.getInstance();
    private final DBProxy db = DBProxy.getInstance();

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_notifications, container, false);

        // Reuse the layout but change the title
        TextView titleView = view.findViewById(R.id.tv_title);
        if (titleView != null) {
            titleView.setText("My Notifications");
        }

        btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        recyclerView = view.findViewById(R.id.rv_notifications);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        setupSwipeRefresh();
        setupRecyclerView();
        
        loadNotifications(true);

        return view;
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.button_purple);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.input_background);
        swipeRefreshLayout.setOnRefreshListener(() -> loadNotifications(false));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new NotificationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications(boolean showProgress) {
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

            String currentUID = currentUser.getDeviceID();
            List<Message> allLogs = logger.getNotificationLog();
            List<Message> userNotifications = new ArrayList<>();
            
            // Filter by recipientID
            for (Message msg : allLogs) {
                if (currentUID.equals(msg.getRecipientID())) {
                    userNotifications.add(msg);
                }
            }

            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            
            if (userNotifications.isEmpty()) {
                emptyStateContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setNotifications(userNotifications);
            }
        }, 600);
    }

    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<Message> notifications;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

        public NotificationAdapter(List<Message> notifications) {
            this.notifications = notifications;
        }

        public void setNotifications(List<Message> notifications) {
            this.notifications = notifications;
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
            Message notification = notifications.get(position);
            
            holder.tvTitle.setText(notification.getTitle() != null ? notification.getTitle() : "Event Update");
            holder.tvBody.setText(notification.getBody());
            
            String senderName = (notification.getOrganizer() != null && notification.getOrganizer().getName() != null) 
                    ? notification.getOrganizer().getName() 
                    : "Cipher Events";
            holder.tvSender.setText("From: " + senderName);
            
            if (notification.getDate() != null) {
                holder.tvDate.setText(dateFormat.format(notification.getDate()));
            } else {
                holder.tvDate.setText("Just now");
            }
        }

        @Override
        public int getItemCount() {
            return notifications.size();
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