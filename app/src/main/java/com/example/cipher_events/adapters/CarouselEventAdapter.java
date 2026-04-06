package com.example.cipher_events.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;

import java.util.List;

/**
 * Adapter for the featured events carousel on the home screen.
 * Supports infinite looping by returning a large item count.
 */
public class CarouselEventAdapter extends RecyclerView.Adapter<CarouselEventAdapter.CarouselViewHolder> {

    private final List<Event> events;
    private final OnEventClickListener listener;
    private OnFavoriteClickListener favoriteListener;
    // Use a smaller number than Integer.MAX_VALUE to avoid memory issues with CarouselLayoutManager
    private static final int LOOP_COUNT = 10000;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Event event);
    }

    public CarouselEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener favoriteListener) {
        this.favoriteListener = favoriteListener;
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
        holder.bind(event, listener, favoriteListener);
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
        private final ImageView favorite;
        private final TextView title;
        private final TextView date;
        private final ViewGroup tagsContainer;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.event_image);
            favorite = itemView.findViewById(R.id.event_favourite);
            title = itemView.findViewById(R.id.event_title);
            date = itemView.findViewById(R.id.event_date);
            tagsContainer = itemView.findViewById(R.id.event_tags_container);
        }

        public void bind(Event event, OnEventClickListener listener, OnFavoriteClickListener favoriteListener) {
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

            // Tags logic
            if (tagsContainer != null) {
                tagsContainer.removeAllViews();
                List<String> tags = event.getTags();
                if (tags != null && !tags.isEmpty()) {
                    for (int i = 0; i < tags.size(); i++) {
                        String tag = tags.get(i);
                        TextView tagView = new TextView(itemView.getContext());
                        tagView.setText(tag);
                        tagView.setTextSize(10);
                        tagView.setTextColor(itemView.getContext().getResources().getColor(R.color.white));
                        tagView.setPadding(20, 10, 20, 10);
                        
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 12, 0);
                        tagView.setLayoutParams(params);
                        
                        tagView.setBackgroundResource(R.drawable.button_background_purple);
                        Drawable background = tagView.getBackground().mutate();
                        float hue = (i * 137.5f) % 360; // Use golden angle for distinct colors
                        int color = Color.HSVToColor(new float[]{hue, 0.65f, 0.75f});
                        background.setTint(color);
                        background.setAlpha(200);
                        
                        tagsContainer.addView(tagView);
                    }
                    tagsContainer.setVisibility(View.VISIBLE);
                } else {
                    tagsContainer.setVisibility(View.GONE);
                }
            }

            // Favourite logic
            User currentUser = DBProxy.getInstance().getCurrentUser();
            if (currentUser != null && favorite != null) {
                favorite.setVisibility(View.VISIBLE);
                if (currentUser.isFavorite(event.getEventID())) {
                    favorite.setImageResource(R.drawable.baseline_star_24);
                } else {
                    favorite.setImageResource(R.drawable.baseline_star_border_24);
                }

                favorite.setOnClickListener(v -> {
                    if (favoriteListener != null) {
                        favoriteListener.onFavoriteClick(event);
                    } else {
                        if (currentUser.isFavorite(event.getEventID())) {
                            currentUser.removeFavoriteEvent(event.getEventID());
                        } else {
                            currentUser.addFavoriteEvent(event.getEventID());
                        }
                        DBProxy.getInstance().updateUser(currentUser);
                        // Using a simple refresh of the favorite icon since it's a carousel
                        if (currentUser.isFavorite(event.getEventID())) {
                            favorite.setImageResource(R.drawable.baseline_star_24);
                        } else {
                            favorite.setImageResource(R.drawable.baseline_star_border_24);
                        }
                    }
                });
            } else if (favorite != null) {
                favorite.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}
