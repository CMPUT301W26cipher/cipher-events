package com.example.cipher_events.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {

    private List<Event> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public AdminEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new AdminEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.title.setText(event.getName());
        int count = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
        holder.waitlistCount.setText(count + " people in waitlist");
        holder.dateLocation.setText(event.getLocation() + " • " + event.getTime());

        if (event.getPosterPictureURL() != null && !event.getPosterPictureURL().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getPosterPictureURL())
                    .into(holder.banner);
        } else {
            holder.banner.setImageResource(R.drawable.gray_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });

        Button deleteBtn = holder.itemView.findViewById(R.id.btn_delete_event);
        deleteBtn.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(holder.itemView.getContext(), R.style.CustomAlertDialog)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete \"" + event.getName() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        DBProxy.getInstance().deleteEvent(event.getEventID());
                        Toast.makeText(holder.itemView.getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class AdminEventViewHolder extends RecyclerView.ViewHolder {
        TextView title, waitlistCount, dateLocation;
        ImageView banner;

        public AdminEventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.admin_event_title);
            waitlistCount = itemView.findViewById(R.id.admin_event_waitlist_count);
            banner = itemView.findViewById(R.id.admin_event_banner);
            dateLocation = itemView.findViewById(R.id.admin_event_date_location);
        }
    }
}
