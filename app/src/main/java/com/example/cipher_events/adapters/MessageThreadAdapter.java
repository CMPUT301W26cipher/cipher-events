package com.example.cipher_events.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        String entrantName = thread.getEntrantDeviceID();
        User entrant = db.getUser(thread.getEntrantDeviceID());
        if (entrant != null && entrant.getName() != null) {
            entrantName = entrant.getName();
        }

        holder.tvParticipantName.setText(entrantName);

        List<DirectMessage> messages = thread.getMessages();
        if (messages != null && !messages.isEmpty()) {
            DirectMessage lastMessage = messages.get(messages.size() - 1);
            holder.tvLastMessage.setText(lastMessage.getContent());
            holder.tvLastMessageTime.setText(lastMessage.getTimestamp());
        } else {
            holder.tvLastMessage.setText("No messages yet");
            holder.tvLastMessageTime.setText("");
        }

        holder.btnOpenThread.setOnClickListener(v -> listener.onOpenThread(thread));
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
        Button btnOpenThread;

        ThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParticipantName = itemView.findViewById(R.id.tvParticipantName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvLastMessageTime = itemView.findViewById(R.id.tvLastMessageTime);
            btnOpenThread = itemView.findViewById(R.id.btnOpenThread);
        }
    }
}