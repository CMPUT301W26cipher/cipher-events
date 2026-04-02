package com.example.cipher_events.adapters;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<Object> profiles;
    private DBProxy db = DBProxy.getInstance();

    public ProfileAdapter(List<Object> profiles) {
        this.profiles = profiles;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Object profile = profiles.get(position);
        String name = "";
        String identifier = "";
        String role = "";
        String imageUrl = null;
        String deviceId = "";
        boolean isRemovable = false;

        if (profile instanceof User) {
            User user = (User) profile;
            name = user.getName();
            identifier = user.getEmail() != null ? user.getEmail() : user.getDeviceID();
            role = "Attendee";
            imageUrl = user.getProfilePictureURL();
            deviceId = user.getDeviceID();
            holder.tvRole.setTextColor(Color.GRAY);
            isRemovable = true;
        } else if (profile instanceof Organizer) {
            Organizer organizer = (Organizer) profile;
            name = organizer.getName();
            identifier = organizer.getEmail() != null ? organizer.getEmail() : organizer.getDeviceID();
            role = "ORGANIZER";
            imageUrl = organizer.getProfilePictureURL();
            deviceId = organizer.getDeviceID();
            holder.tvRole.setTextColor(Color.parseColor("#4CAF50")); // Green for organizers
            isRemovable = true;
        } else if (profile instanceof Admin) {
            Admin admin = (Admin) profile;
            name = admin.getName();
            identifier = admin.getEmail() != null ? admin.getEmail() : admin.getDeviceID();
            role = "Admin";
            imageUrl = admin.getProfilePictureURL();
            deviceId = admin.getDeviceID();
            holder.tvRole.setTextColor(Color.RED);
            isRemovable = false; // Admins can't remove each other from this simple list
        }

        holder.tvName.setText(name);
        holder.tvIdentifier.setText(identifier);
        holder.tvRole.setText(role);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivProfilePic);
        } else {
            holder.ivProfilePic.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        if (isRemovable) {
            holder.btnRemove.setVisibility(View.VISIBLE);
            final String finalDeviceId = deviceId;
            final String finalRole = role;
            final String finalName = name;
            holder.btnRemove.setOnClickListener(v -> {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Remove " + finalRole)
                        .setMessage("Are you sure you want to remove " + finalName + "?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            if (profile instanceof User) {
                                db.deleteUser(finalDeviceId);
                            } else if (profile instanceof Organizer) {
                                db.deleteOrganizer(finalDeviceId);
                            }
                            Toast.makeText(holder.itemView.getContext(), finalRole + " removed", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            holder.btnRemove.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    public void updateList(List<Object> newProfiles) {
        this.profiles = newProfiles;
        notifyDataSetChanged();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfilePic, btnRemove;
        TextView tvName, tvIdentifier, tvRole;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.iv_profile_pic);
            tvName = itemView.findViewById(R.id.tv_profile_name);
            tvIdentifier = itemView.findViewById(R.id.tv_profile_identifier);
            tvRole = itemView.findViewById(R.id.tv_profile_role);
            btnRemove = itemView.findViewById(R.id.btn_remove_profile);
        }
    }
}