package com.example.cipher_events.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
    private final DBProxy db = DBProxy.getInstance();

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
        int roleColor;
        String imageUrl = null;
        String deviceId = "";
        boolean isRemovable = false;

        if (profile instanceof User) {
            User user = (User) profile;
            name = user.getName();
            identifier = (user.getEmail() != null && !user.getEmail().isEmpty()) ? user.getEmail() : "ID: " + user.getDeviceID();
            role = "Attendee";
            imageUrl = user.getProfilePictureURL();
            deviceId = user.getDeviceID();
            roleColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.role_attendee);
            isRemovable = true;
        } else if (profile instanceof Organizer) {
            Organizer organizer = (Organizer) profile;
            name = organizer.getName();
            identifier = (organizer.getEmail() != null && !organizer.getEmail().isEmpty()) ? organizer.getEmail() : "ID: " + organizer.getDeviceID();
            role = "Organizer";
            imageUrl = organizer.getProfilePictureURL();
            deviceId = organizer.getDeviceID();
            roleColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.role_organizer);
            isRemovable = true;
        } else { // Admin
            Admin admin = (Admin) profile;
            name = admin.getName();
            identifier = (admin.getEmail() != null && !admin.getEmail().isEmpty()) ? admin.getEmail() : "ID: " + admin.getDeviceID();
            role = "Administrator";
            imageUrl = admin.getProfilePictureURL();
            deviceId = admin.getDeviceID();
            roleColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.role_admin);
            isRemovable = false;
        }

        holder.tvName.setText(name != null && !name.isEmpty() ? name : "Anonymous");
        holder.tvIdentifier.setText(identifier);
        holder.tvRole.setText(role);
        holder.tvRole.setTextColor(roleColor);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.outline_account_circle_24)
                    .into(holder.ivProfilePic);
            holder.ivProfilePic.setAlpha(1.0f);
        } else {
            holder.ivProfilePic.setImageResource(R.drawable.outline_account_circle_24);
            holder.ivProfilePic.setAlpha(0.3f);
        }

        if (isRemovable) {
            holder.btnRemove.setVisibility(View.VISIBLE);
            final String finalDeviceId = deviceId;
            final String finalRole = role;
            final String finalName = name != null ? name : "this profile";
            holder.btnRemove.setOnClickListener(v -> {
                new AlertDialog.Builder(holder.itemView.getContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                        .setTitle("Remove " + finalRole)
                        .setMessage("Are you sure you want to remove " + finalName + "? This action cannot be undone.")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            if (profile instanceof User) {
                                db.deleteUser(finalDeviceId);
                            } else {
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