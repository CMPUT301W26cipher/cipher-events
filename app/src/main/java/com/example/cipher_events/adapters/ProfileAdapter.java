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
import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<Object> profiles;

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

        if (profile instanceof User) {
            User user = (User) profile;
            name = user.getName();
            identifier = user.getEmail() != null ? user.getEmail() : user.getDeviceID();
            role = "Entrant";
            imageUrl = user.getProfilePictureURL();
        } else if (profile instanceof Organizer) {
            Organizer organizer = (Organizer) profile;
            name = organizer.getName();
            identifier = organizer.getEmail() != null ? organizer.getEmail() : organizer.getDeviceID();
            role = "Organizer";
            imageUrl = organizer.getProfilePictureURL();
        } else if (profile instanceof Admin) {
            Admin admin = (Admin) profile;
            name = admin.getName();
            identifier = admin.getEmail() != null ? admin.getEmail() : admin.getDeviceID();
            role = "Admin";
            imageUrl = admin.getProfilePictureURL();
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
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfilePic;
        TextView tvName, tvIdentifier, tvRole;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.iv_profile_pic);
            tvName = itemView.findViewById(R.id.tv_profile_name);
            tvIdentifier = itemView.findViewById(R.id.tv_profile_identifier);
            tvRole = itemView.findViewById(R.id.tv_profile_role);
        }
    }
}