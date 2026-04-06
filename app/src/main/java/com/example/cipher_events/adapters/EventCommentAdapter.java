package com.example.cipher_events.adapters;

import android.app.AlertDialog;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.comment.EventComment;

import java.util.ArrayList;
import java.util.List;

public class EventCommentAdapter extends RecyclerView.Adapter<EventCommentAdapter.CommentViewHolder> {

    private final List<EventComment> comments = new ArrayList<>();
    private String currentDeviceId;
    private boolean isAdmin;
    private boolean isOrganizer;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(EventComment comment);
    }

    public EventCommentAdapter() {}

    public void setup(String deviceId, boolean isAdmin, boolean isOrganizer, OnDeleteClickListener listener) {
        this.currentDeviceId = deviceId;
        this.isAdmin = isAdmin;
        this.isOrganizer = isOrganizer;
        this.deleteListener = listener;
    }

    public void setComments(List<EventComment> newComments) {
        comments.clear();
        if (newComments != null) {
            comments.addAll(newComments);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        EventComment comment = comments.get(position);
        holder.tvAuthor.setText(comment.getAuthorName());
        holder.tvMessage.setText(comment.getMessage());
        holder.tvTime.setText(comment.getCreatedAt());

        // Show delete button if author, admin, or organizer
        boolean canDelete = isAdmin || isOrganizer ||
                (currentDeviceId != null && currentDeviceId.equals(comment.getAuthorDeviceID()));

        if (canDelete) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Delete Comment")
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (deleteListener != null) {
                                deleteListener.onDeleteClick(comment);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvMessage, tvTime;
        ImageButton btnDelete;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvCommentAuthor);
            tvMessage = itemView.findViewById(R.id.tvCommentMessage);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }
    }
}