package com.example.cipher_events.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.User;

import java.util.ArrayList;
import java.util.List;

public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    public enum ListType { WAITLIST, INVITED, CANCELLED, ENROLLED }

    // Listener for removing enrolled users
    public interface OnEnrolledRemoveListener {
        void onRemoveFromEnrolled(User user);
    }

    // Listener for marking no-show
    public interface OnMarkNoShowListener {
        void onMarkNoShow(User user);
    }

    private List<User> users;
    private ListType listType;

    private OnEnrolledRemoveListener removeListener;
    private OnMarkNoShowListener noShowListener;

    public EntrantAdapter(List<User> users, ListType listType) {
        if (users != null) {
            this.users = users;
        }
        this.listType = listType;
    }

    public void setOnEnrolledRemoveListener(OnEnrolledRemoveListener listener) {
        this.removeListener = listener;
    }

    public void setOnMarkNoShowListener(OnMarkNoShowListener listener) {
        this.noShowListener = listener;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        User user = users.get(position);
        holder.name.setText(user.getName());
        
        // Load Avatar
        if (user.getProfilePictureURL() != null && !user.getProfilePictureURL().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfilePictureURL())
                    .placeholder(R.drawable.gray_placeholder)
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.gray_placeholder);
        }

        // Style status and indicator
        switch (listType) {
            case WAITLIST:
                holder.status.setText("Waitlisted");
                holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.text_hint));
                holder.actionIndicator.setVisibility(View.GONE);
                break;
            case INVITED:
                holder.status.setText("Invited");
                holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.button_purple));
                holder.actionIndicator.setVisibility(View.GONE);
                break;
            case CANCELLED:
                holder.status.setText("Cancelled");
                holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.remove_red));
                holder.actionIndicator.setVisibility(View.GONE);
                break;
            case ENROLLED:
                holder.status.setText("Enrolled");
                holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.role_organizer));
                holder.actionIndicator.setVisibility(removeListener != null ? View.VISIBLE : View.GONE);
                break;
        }

        // Only ENROLLED users have actions
        if (listType == ListType.ENROLLED) {
            holder.itemView.setOnClickListener(v -> {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Participant Options")
                        .setMessage("What do you want to do with " + user.getName() + "?")

                        .setPositiveButton("Remove", (dialog, which) -> {
                            if (removeListener != null) {
                                removeListener.onRemoveFromEnrolled(user);
                            }
                        })

                        .setNeutralButton("Mark No-Show", (dialog, which) -> {
                            if (noShowListener != null) {
                                noShowListener.onMarkNoShow(user);
                            }
                        })

                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            // IMPORTANT: prevent recycled click bugs
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    public void updateList(List<User> newUsers) {
        this.users = (newUsers != null) ? newUsers : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView name, status;
        ImageView avatar, actionIndicator;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.entrant_name);
            status = itemView.findViewById(R.id.entrant_status);
            avatar = itemView.findViewById(R.id.iv_entrant_avatar);
            actionIndicator = itemView.findViewById(R.id.iv_action_indicator);
        }
    }
}