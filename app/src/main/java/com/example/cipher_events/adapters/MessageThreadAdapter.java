package com.example.cipher_events.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.User;
import com.example.cipher_events.message.DirectMessage;
import com.example.cipher_events.message.MessageThread;

import java.util.ArrayList;
import java.util.List;

public class MessageThreadAdapter extends RecyclerView.Adapter<MessageThreadAdapter.ThreadViewHolder> {

    public interface OnThreadClickListener {
        void onOpenThread(MessageThread thread);
    }

    private final List<MessageThread> threads = new ArrayList<>();
    private final OnThreadClickListener listener;
    private final DBProxy db;

    public MessageThreadAdapter(OnThreadClickListener listener) {
        this.listener = listener;
        this.db = DBProxy.getInstance();
    }

    public void setThreads(List<MessageThread> newThreads) {
        threads.clear();
        if (newThreads != null) {
            threads.addAll(newThreads);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThreadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_thread, parent, false);
        return new ThreadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThreadViewHolder holder, int position) {
        MessageThread thread = threads.get(position);

        User entrant = db.getUser(thread.getEntrantDeviceID());
        String entrantName = thread.getEntrantDeviceID();
        String avatarUrl = null;

        if (entrant != null) {
            if (entrant.getName() != null) {
                entrantName = entrant.getName();
            }
            avatarUrl = entrant.getProfilePictureURL();
        }

        holder.tvParticipantName.setText(entrantName);

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.gray_placeholder)
                    .into(holder.ivParticipantAvatar);
        } else {
            holder.ivParticipantAvatar.setImageResource(R.drawable.gray_placeholder);
        }

        List<DirectMessage> messages = thread.getMessages();
        if (messages != null && !messages.isEmpty()) {
            DirectMessage lastMessage = messages.get(messages.size() - 1);
            holder.tvLastMessage.setText(lastMessage.getContent());
            
            // Format time if possible, otherwise use full timestamp
            String time = lastMessage.getTimestamp();
            if (time != null && time.contains(" ")) {
                String[] parts = time.split(" ");
                if (parts.length > 1) {
                    holder.tvLastMessageTime.setText(parts[1]); // Just show time
                } else {
                    holder.tvLastMessageTime.setText(time);
                }
            } else {
                holder.tvLastMessageTime.setText(time);
            }
        } else {
            holder.tvLastMessage.setText("No messages yet");
            holder.tvLastMessageTime.setText("");
        }

        holder.itemView.setOnClickListener(v -> listener.onOpenThread(thread));
    }

    @Override
    public int getItemCount() {
        return threads.size();
    }

    static class ThreadViewHolder extends RecyclerView.ViewHolder {
        TextView tvParticipantName;
        TextView tvLastMessage;
        TextView tvLastMessageTime;
        ImageView ivParticipantAvatar;

        ThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParticipantName = itemView.findViewById(R.id.tvParticipantName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvLastMessageTime = itemView.findViewById(R.id.tvLastMessageTime);
            ivParticipantAvatar = itemView.findViewById(R.id.ivParticipantAvatar);
        }
    }
}