package com.example.cipher_events.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.Event;

import java.util.List;

/**
 * Displays a list of Event objects
 * - fills in event title, date, location
 * - if URL is added for event poster, Glide is used to load it (no url = grey blank background)
 *
 * References for Glide:
 * https://www.geeksforgeeks.org/android/image-loading-caching-library-android-set-2/
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }


    public EventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.title.setText(event.getName());
        holder.date.setText(event.getTime());
        holder.location.setText(event.getLocation());

        String url = event.getPosterPictureURL();

        if (url != null && !url.isEmpty()) {
            // Load image normally
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .into(holder.image);
        } else {
            // Clear any previous image from recycled views
            Glide.with(holder.itemView.getContext()).clear(holder.image);

            // Keep XML background (grey)
            holder.image.setImageDrawable(null);
        }

        // debugging: testing if image url loads
        Log.d("EVENT_DEBUG", "Poster URL for " + event.getName() + ": " + url);


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });


    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, location;
        ImageView image;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.event_image);
            title = itemView.findViewById(R.id.event_title);
            date = itemView.findViewById(R.id.event_date);
            location = itemView.findViewById(R.id.event_location);
        }
    }
}