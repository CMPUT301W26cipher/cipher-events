package com.example.cipher_events.pages;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for administrators to browse and manage all uploaded images (event posters and profile pictures).
 */
public class AdminBrowseImagesFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private View emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageAdapter adapter;
    private final DBProxy db = DBProxy.getInstance();

    public AdminBrowseImagesFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_images, container, false);

        recyclerView = view.findViewById(R.id.rv_images);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ImageAdapter(new ArrayList<>(), this::onImageClick, this::onDeleteClick);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.button_purple);
        swipeRefreshLayout.setOnRefreshListener(this::loadImages);

        loadImages();

        return view;
    }

    private void loadImages() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        
        List<AdminImage> imageList = new ArrayList<>();

        // 1. Collect event posters
        List<Event> events = db.getAllEvents();
        for (Event event : events) {
            String url = event.getPosterPictureURL();
            if (url != null && !url.isEmpty()) {
                imageList.add(new AdminImage(url, "Event Poster", event));
            }
        }

        // 2. Collect entrant profile pictures
        List<User> users = db.getAllUsers();
        for (User user : users) {
            String url = user.getProfilePictureURL();
            if (url != null && !url.isEmpty()) {
                imageList.add(new AdminImage(url, "Profile (Entrant)", user));
            }
        }

        // 3. Collect organizer profile pictures
        List<Organizer> organizers = db.getAllOrganizers();
        for (Organizer organizer : organizers) {
            String url = organizer.getProfilePictureURL();
            if (url != null && !url.isEmpty()) {
                imageList.add(new AdminImage(url, "Profile (Organizer)", organizer));
            }
        }

        // 4. Collect admin profile pictures
        List<Admin> admins = db.getAllAdmins();
        for (Admin admin : admins) {
            String url = admin.getProfilePictureURL();
            if (url != null && !url.isEmpty()) {
                imageList.add(new AdminImage(url, "Profile (Admin)", admin));
            }
        }

        if (imageList.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setImages(imageList);
        }

        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
    }

    private void onImageClick(AdminImage image) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_image_preview, null);
        ImageView ivPreview = dialogView.findViewById(R.id.iv_preview);
        
        Glide.with(this)
                .load(image.url)
                .into(ivPreview);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        ivPreview.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void onDeleteClick(AdminImage image) {
        new MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
                .setTitle("Remove Image")
                .setMessage("Are you sure you want to remove this image? This action cannot be undone.")
                .setPositiveButton("Remove", (dialog, which) -> {
                    if (image.sourceObject instanceof Event) {
                        Event event = (Event) image.sourceObject;
                        event.setPosterPictureURL("");
                        db.updateEvent(event);
                    } else if (image.sourceObject instanceof User) {
                        User user = (User) image.sourceObject;
                        user.setProfilePictureURL("");
                        db.updateUser(user);
                    }
                    Toast.makeText(getContext(), "Image removed", Toast.LENGTH_SHORT).show();
                    loadImages();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        db.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadImages);
        }
    }

    /**
     * Data model for the adapter representing an image and its source.
     */
    private static class AdminImage {
        String url;
        String type;
        Object sourceObject;

        AdminImage(String url, String type, Object sourceObject) {
            this.url = url;
            this.type = type;
            this.sourceObject = sourceObject;
        }
    }

    private static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private List<AdminImage> images;
        private final OnImageClickListener clickListener;
        private final OnDeleteClickListener deleteListener;

        interface OnImageClickListener { void onImageClick(AdminImage image); }
        interface OnDeleteClickListener { void onDeleteClick(AdminImage image); }

        ImageAdapter(List<AdminImage> images, OnImageClickListener clickListener, OnDeleteClickListener deleteListener) {
            this.images = images;
            this.clickListener = clickListener;
            this.deleteListener = deleteListener;
        }

        void setImages(List<AdminImage> images) {
            this.images = images;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AdminImage image = images.get(position);
            
            Glide.with(holder.itemView.getContext())
                    .load(image.url)
                    .centerCrop()
                    .placeholder(R.drawable.gray_placeholder)
                    .into(holder.ivImage);
            
            holder.tvType.setText(image.type);
            
            String sourceName = "Unknown";
            if (image.sourceObject instanceof Event) {
                sourceName = ((Event) image.sourceObject).getName();
            } else if (image.sourceObject instanceof User) {
                sourceName = ((User) image.sourceObject).getName();
            }
            holder.tvSource.setText(sourceName);

            holder.itemView.setOnClickListener(v -> clickListener.onImageClick(image));
            holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(image));
        }

        @Override
        public int getItemCount() { return images.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvType, tvSource;
            ImageButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.iv_admin_image);
                tvType = itemView.findViewById(R.id.tv_image_type);
                tvSource = itemView.findViewById(R.id.tv_image_source);
                btnDelete = itemView.findViewById(R.id.btn_delete_image);
            }
        }
    }
}
