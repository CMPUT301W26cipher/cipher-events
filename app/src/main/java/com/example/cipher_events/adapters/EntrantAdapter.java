package com.example.cipher_events.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private OnNoShowClickListener listener;

    public interface OnNoShowClickListener {
        void onNoShowClick(User user);
    }

    public EntrantAdapter(List<User> users, ListType listType, OnNoShowClickListener listener) {
        this.users = users;
        this.listType = listType;
        this.listener = listener;
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
                holder.btnNoShow.setVisibility(View.VISIBLE);
                holder.btnNoShow.setOnClickListener(v -> {
                    if (listener != null) listener.onNoShowClick(user);
                });
                break;
            case CANCELLED:
                holder.status.setText("Cancelled");
                holder.btnNoShow.setVisibility(View.GONE);
                break;
            case ENROLLED:
                holder.status.setText("Enrolled");
                holder.btnNoShow.setVisibility(View.GONE);
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
        Button btnNoShow;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.entrant_name);
            status = itemView.findViewById(R.id.entrant_status);
            btnNoShow = itemView.findViewById(R.id.btn_no_show);
        }
    }
}