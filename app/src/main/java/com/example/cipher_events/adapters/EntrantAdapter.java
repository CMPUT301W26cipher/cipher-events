package com.example.cipher_events.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.database.User;

import java.util.List;

public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    public enum ListType { WAITLIST, INVITED, CANCELLED, ENROLLED }

    public interface OnEnrolledRemoveListener {
        void onRemoveFromEnrolled(User user);
    }

    private List<User> users;
    private ListType listType;
    private OnEnrolledRemoveListener removeListener;

    public EntrantAdapter(List<User> users, ListType listType) {
        this.users = users;
        this.listType = listType;
    }

    public void setOnEnrolledRemoveListener(OnEnrolledRemoveListener listener) {
        this.removeListener = listener;
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

        switch (listType) {
            case WAITLIST:
                holder.status.setText("Waitlisted");
                break;
            case INVITED:
                holder.status.setText("Invited");
                break;
            case CANCELLED:
                holder.status.setText("Cancelled");
                break;
            case ENROLLED:
                holder.status.setText("Enrolled");
                break;
        }

        // Only enrolled entrants are clickable for removal
        if (listType == ListType.ENROLLED && removeListener != null) {
            holder.itemView.setOnClickListener(v -> {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Remove Participant")
                        .setMessage("Do you want to remove " + user.getName() + " from enrolled and move them back to waitlist?")
                        .setPositiveButton("Yes", (dialog, which) -> removeListener.onRemoveFromEnrolled(user))
                        .setNegativeButton("No", null)
                        .show();
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateList(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView name, status;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.entrant_name);
            status = itemView.findViewById(R.id.entrant_status);
        }
    }
}