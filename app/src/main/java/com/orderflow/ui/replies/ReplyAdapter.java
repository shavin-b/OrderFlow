package com.orderflow.ui.replies;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.orderflow.R;
import com.orderflow.data.model.Reply;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Replies RecyclerView.
 */
public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private List<Reply> replies = new ArrayList<>();
    private final ReplyClickListener listener;

    public interface ReplyClickListener {
        void onEditClick(Reply reply);
        void onToggleActive(Reply reply, boolean isChecked);
    }

    public ReplyAdapter(ReplyClickListener listener) {
        this.listener = listener;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reply, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        Reply reply = replies.get(position);
        holder.bind(reply, listener);
    }

    @Override
    public int getItemCount() {
        return replies != null ? replies.size() : 0;
    }

    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContentPreview;
        TextView tvReplyId;
        MaterialSwitch switchActive;

        ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContentPreview = itemView.findViewById(R.id.tv_content_preview);
            tvReplyId = itemView.findViewById(R.id.tv_reply_id);
            switchActive = itemView.findViewById(R.id.switch_active);
        }

        void bind(Reply reply, ReplyClickListener listener) {
            tvTitle.setText(reply.getTitle());
            tvContentPreview.setText(reply.getContentPreview(100)); // Show up to 100 chars
            tvReplyId.setText(reply.getId() != null ? reply.getId() : "Generating...");

            // Allow user to easily copy the Template ID (needed for Phase 4 Keyword linking)
            tvReplyId.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && reply.getId() != null) {
                    ClipData clip = ClipData.newPlainText("Template ID", reply.getId());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(v.getContext(), "Template ID copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            });

            switchActive.setOnCheckedChangeListener(null);
            switchActive.setChecked(reply.isEnabled());

            switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onToggleActive(reply, isChecked);
            });

            itemView.setOnClickListener(v -> listener.onEditClick(reply));
        }
    }
}
