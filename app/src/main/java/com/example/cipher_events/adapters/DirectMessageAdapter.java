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

import java.util.ArrayList;
import java.util.List;

public class DirectMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<DirectMessage> messages = new ArrayList<>();
    private final String currentDeviceID;
    private final DBProxy db = DBProxy.getInstance();

    public DirectMessageAdapter(String currentDeviceID) {
        this.currentDeviceID = currentDeviceID;
    }

    public void setMessages(List<DirectMessage> newMessages) {
        messages.clear();
        if (newMessages != null) {
            messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        DirectMessage message = messages.get(position);
        if (message.getSenderDeviceID() != null &&
                message.getSenderDeviceID().equals(currentDeviceID)) {
            return TYPE_SENT;
        }
        return TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DirectMessage message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message, db);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message, db);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessageContent;
        private final TextView tvMessageTime;
        private final ImageView ivSenderProfile;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            ivSenderProfile = itemView.findViewById(R.id.ivSenderProfile);
        }

        void bind(DirectMessage message, DBProxy db) {
            tvMessageContent.setText(message.getContent());
            tvMessageTime.setText(message.getTimestamp());
            
            // Fetch latest profile picture from DB
            User sender = db.getAnyUser(message.getSenderDeviceID());
            String photoUrl = (sender != null) ? sender.getProfilePictureURL() : message.getSenderProfilePictureURL();

            if (ivSenderProfile != null) {
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(photoUrl)
                            .placeholder(R.drawable.gray_placeholder)
                            .into(ivSenderProfile);
                } else {
                    ivSenderProfile.setImageResource(R.drawable.gray_placeholder);
                }
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessageContent;
        private final TextView tvMessageTime;
        private final ImageView ivSenderProfile;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            ivSenderProfile = itemView.findViewById(R.id.ivSenderProfile);
        }

        void bind(DirectMessage message, DBProxy db) {
            tvMessageContent.setText(message.getContent());
            tvMessageTime.setText(message.getTimestamp());

            // Fetch latest profile picture from DB
            User sender = db.getAnyUser(message.getSenderDeviceID());
            String photoUrl = (sender != null) ? sender.getProfilePictureURL() : message.getSenderProfilePictureURL();

            if (ivSenderProfile != null) {
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(photoUrl)
                            .placeholder(R.drawable.gray_placeholder)
                            .into(ivSenderProfile);
                } else {
                    ivSenderProfile.setImageResource(R.drawable.gray_placeholder);
                }
            }
        }
    }
}
