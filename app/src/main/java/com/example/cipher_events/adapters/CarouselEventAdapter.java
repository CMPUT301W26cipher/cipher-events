package com.example.cipher_events.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.Event;

import java.util.List;

/**
 * Adapter for the featured events carousel on the home screen.
 * Supports infinite looping by returning a large item count.
 */
public class CarouselEventAdapter extends RecyclerView.Adapter<CarouselEventAdapter.CarouselViewHolder> {

    private final List<Event> events;
    private final OnEventClickListener listener;
    // Use a smaller number than Integer.MAX_VALUE to avoid memory issues with CarouselLayoutManager
    private static final int LOOP_COUNT = 10000;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public CarouselEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_carousel, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        if (events.isEmpty()) return;
        Event event = events.get(position % events.size());
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        // Return a large number to simulate infinite looping, but not MAX_VALUE
        return events.isEmpty() ? 0 : LOOP_COUNT;
    }

    /**
     * Helper to get the actual starting position in the middle of the large item count
     */
    public int getStartingPosition() {
        if (events.isEmpty()) return 0;
        int half = LOOP_COUNT / 2;
        return half - (half % events.size());
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final TextView title;
        private final TextView date;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.event_image);
            title = itemView.findViewById(R.id.event_title);
            date = itemView.findViewById(R.id.event_date);
        }

        public void bind(Event event, OnEventClickListener listener) {
            title.setText(event.getName());
            date.setText(event.getTime());

            String url = event.getPosterPictureURL();
            if (url != null && !url.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(url)
                        .centerCrop()
                        .into(image);
            } else {
                image.setImageResource(R.drawable.outline_account_circle_24);
                image.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.button_purple));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}