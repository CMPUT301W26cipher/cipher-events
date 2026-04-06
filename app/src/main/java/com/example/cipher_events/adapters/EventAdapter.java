package com.example.cipher_events.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

import java.util.List;
import java.util.Locale;

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
    private OnFavoriteClickListener favoriteListener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Event event);
    }


    public EventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener favoriteListener) {
        this.favoriteListener = favoriteListener;
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

        // Bind Organizer info
        String organizerId = event.getOrganizerID();
        Organizer organizer = (organizerId != null) ? DBProxy.getInstance().getOrganizer(organizerId) : null;
        
        if (organizer != null) {
            holder.organizerName.setText("By " + organizer.getName());
            holder.organizerName.setVisibility(View.VISIBLE);
            
            if (organizer.getProfilePictureURL() != null && !organizer.getProfilePictureURL().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(organizer.getProfilePictureURL())
                        .placeholder(R.drawable.outline_account_circle_24)
                        .into(holder.organizerImage);
            } else {
                holder.organizerImage.setImageResource(R.drawable.outline_account_circle_24);
            }
            holder.organizerImage.setVisibility(View.VISIBLE);
        } else {
            holder.organizerName.setVisibility(View.GONE);
            holder.organizerImage.setVisibility(View.GONE);
        }

        // Bind Description
        String desc = event.getDescription();
        if (desc != null && !desc.isEmpty()) {
            holder.description.setText(desc);
            holder.description.setVisibility(View.VISIBLE);
        } else {
            holder.description.setVisibility(View.GONE);
        }

        // Bind Waitlist Count and Capacity
        int entrantsCount = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
        holder.waitlistCount.setText(String.format(Locale.getDefault(), "%d Waitlisted", entrantsCount));
        
        Integer capacity = event.getWaitingListCapacity();
        if (capacity != null && capacity > 0) {
            holder.capacity.setText(String.format(Locale.getDefault(), "%d Capacity", capacity));
            holder.capacity.setVisibility(View.VISIBLE);
        } else {
            holder.capacity.setVisibility(View.GONE);
        }

        // Privacy Badge
        if (holder.privacyBadge != null) {
            holder.privacyBadge.setVisibility(event.isPublicEvent() ? View.GONE : View.VISIBLE);
        }

        // Tags logic
        if (holder.tagsContainer != null) {
            holder.tagsContainer.removeAllViews();
            List<String> tags = event.getTags();
            if (tags != null && !tags.isEmpty()) {
                for (int i = 0; i < tags.size(); i++) {
                    String tag = tags.get(i);
                    TextView tagView = new TextView(holder.itemView.getContext());
                    tagView.setText(tag.toUpperCase());
                    tagView.setTextSize(9);
                    tagView.setTextColor(Color.WHITE);
                    tagView.setPadding(24, 8, 24, 8);
                    tagView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                    tagView.setLetterSpacing(0.06f);
                    
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 10, 0);
                    tagView.setLayoutParams(params);
                    
                    tagView.setBackgroundResource(R.drawable.bg_tag);
                    Drawable background = tagView.getBackground().mutate();
                    float hue = (i * 137.5f) % 360; 
                    int color = Color.HSVToColor(new float[]{hue, 0.65f, 0.65f});
                    background.setTint(color);
                    background.setAlpha(210);
                    
                    holder.tagsContainer.addView(tagView);
                }
                holder.tagsContainer.setVisibility(View.VISIBLE);
            } else {
                holder.tagsContainer.setVisibility(View.GONE);
            }
        }

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

        // Favourite logic
        User currentUser = DBProxy.getInstance().getCurrentUser();
        if (currentUser != null) {
            holder.favorite.setVisibility(View.VISIBLE);
            if (currentUser.isFavorite(event.getEventID())) {
                holder.favorite.setImageResource(R.drawable.baseline_star_24);
            } else {
                holder.favorite.setImageResource(R.drawable.baseline_star_border_24);
            }

            holder.favorite.setOnClickListener(v -> {
                if (favoriteListener != null) {
                    favoriteListener.onFavoriteClick(event);
                } else {
                    // Default behavior if no listener is set
                    if (currentUser.isFavorite(event.getEventID())) {
                        currentUser.removeFavoriteEvent(event.getEventID());
                    } else {
                        currentUser.addFavoriteEvent(event.getEventID());
                    }
                    DBProxy.getInstance().updateUser(currentUser);
                    notifyItemChanged(position);
                }
            });
        } else {
            holder.favorite.setVisibility(View.GONE);
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
        TextView title, date, location, organizerName, waitlistCount, capacity, description, privacyBadge;
        ImageView image, favorite, organizerImage;
        ViewGroup tagsContainer;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.event_image);
            favorite = itemView.findViewById(R.id.event_favourite);
            title = itemView.findViewById(R.id.event_title);
            date = itemView.findViewById(R.id.event_date);
            location = itemView.findViewById(R.id.event_location);
            organizerName = itemView.findViewById(R.id.event_organizer);
            organizerImage = itemView.findViewById(R.id.event_organizer_image);
            waitlistCount = itemView.findViewById(R.id.event_waitlist_count);
            capacity = itemView.findViewById(R.id.event_capacity);
            description = itemView.findViewById(R.id.event_description);
            privacyBadge = itemView.findViewById(R.id.event_privacy_badge);
            tagsContainer = itemView.findViewById(R.id.event_tags_container);
        }
    }
}
