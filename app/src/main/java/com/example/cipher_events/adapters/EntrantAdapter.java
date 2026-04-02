package com.example.cipher_events.adapters;

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

    public enum ListType { INVITED, CANCELLED, ENROLLED }

    private List<User> users;
    private ListType listType;

    public EntrantAdapter(List<User> users, ListType listType) {
        this.users = users;
        this.listType = listType;
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