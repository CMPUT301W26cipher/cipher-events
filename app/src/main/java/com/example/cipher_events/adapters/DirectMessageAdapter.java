package com.example.cipher_events.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.message.DirectMessage;

import java.util.ArrayList;
import java.util.List;

public class DirectMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<DirectMessage> messages = new ArrayList<>();
    private final String currentDeviceID;

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
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessageContent;
        private final TextView tvMessageTime;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        void bind(DirectMessage message) {
            tvMessageContent.setText(message.getContent());
            tvMessageTime.setText(message.getTimestamp());
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSenderName;
        private final TextView tvMessageContent;
        private final TextView tvMessageTime;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        void bind(DirectMessage message) {
            tvSenderName.setText(message.getSenderName());
            tvMessageContent.setText(message.getContent());
            tvMessageTime.setText(message.getTimestamp());
        }
    }
}