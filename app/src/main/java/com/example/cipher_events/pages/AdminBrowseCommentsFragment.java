package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cipher_events.R;
import com.example.cipher_events.comment.EntrantCommentService;
import com.example.cipher_events.comment.EventComment;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseCommentsFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private View emptyStateContainer;
    private CommentAdapter adapter;
    private final DBProxy db = DBProxy.getInstance();
    private final EntrantCommentService commentService = new EntrantCommentService();

    public AdminBrowseCommentsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_comments, container, false);

        recyclerView = view.findViewById(R.id.rv_comments);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);

        View backBtn = view.findViewById(R.id.btn_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentAdapter(new ArrayList<>(), this::onDeleteClick);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.button_purple);
        swipeRefreshLayout.setOnRefreshListener(this::loadComments);

        loadComments();

        return view;
    }

    private void loadComments() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        
        List<AdminCommentWrapper> commentList = new ArrayList<>();
        List<Event> events = db.getAllEvents();
        for (Event event : events) {
            if (event.getComments() != null) {
                for (EventComment comment : event.getComments()) {
                    commentList.add(new AdminCommentWrapper(event, comment));
                }
            }
        }

        if (commentList.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setComments(commentList);
        }

        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void onDeleteClick(AdminCommentWrapper wrapper) {
        new MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        commentService.deleteComment(wrapper.event.getEventID(), wrapper.comment.getCommentID());
                        Toast.makeText(getContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                        loadComments();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error deleting comment: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
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
            getActivity().runOnUiThread(this::loadComments);
        }
    }

    private static class AdminCommentWrapper {
        Event event;
        EventComment comment;

        AdminCommentWrapper(Event event, EventComment comment) {
            this.event = event;
            this.comment = comment;
        }
    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
        private List<AdminCommentWrapper> comments;
        private final OnDeleteClickListener deleteListener;

        interface OnDeleteClickListener { void onDeleteClick(AdminCommentWrapper wrapper); }

        CommentAdapter(List<AdminCommentWrapper> comments, OnDeleteClickListener deleteListener) {
            this.comments = comments;
            this.deleteListener = deleteListener;
        }

        void setComments(List<AdminCommentWrapper> comments) {
            this.comments = comments;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_comment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AdminCommentWrapper wrapper = comments.get(position);
            EventComment comment = wrapper.comment;
            
            holder.tvAuthor.setText(comment.getAuthorName() + " @ " + wrapper.event.getName());
            holder.tvMessage.setText(comment.getMessage());
            holder.tvTime.setText(comment.getCreatedAt());

            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(wrapper));
        }

        @Override
        public int getItemCount() { return comments.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAuthor, tvMessage, tvTime;
            ImageButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvAuthor = itemView.findViewById(R.id.tvCommentAuthor);
                tvMessage = itemView.findViewById(R.id.tvCommentMessage);
                tvTime = itemView.findViewById(R.id.tvCommentTime);
                btnDelete = itemView.findViewById(R.id.btnDeleteComment);
            }
        }
    }
}