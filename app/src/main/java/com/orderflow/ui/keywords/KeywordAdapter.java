package com.orderflow.ui.keywords;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.orderflow.R;
import com.orderflow.data.model.Keyword;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Keywords RecyclerView.
 */
public class KeywordAdapter extends RecyclerView.Adapter<KeywordAdapter.KeywordViewHolder> {

    private List<Keyword> keywords = new ArrayList<>();
    private final KeywordClickListener listener;

    public interface KeywordClickListener {
        void onEditClick(Keyword keyword);
        void onToggleActive(Keyword keyword, boolean isChecked);
    }

    public KeywordAdapter(KeywordClickListener listener) {
        this.listener = listener;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
        notifyDataSetChanged(); // For production with large lists, use DiffUtil. For now this is fine.
    }

    @NonNull
    @Override
    public KeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_keyword, parent, false);
        return new KeywordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeywordViewHolder holder, int position) {
        Keyword keyword = keywords.get(position);
        holder.bind(keyword, listener);
    }

    @Override
    public int getItemCount() {
        return keywords != null ? keywords.size() : 0;
    }

    static class KeywordViewHolder extends RecyclerView.ViewHolder {
        TextView tvKeywords;
        TextView tvMeta;
        TextView tvReplyId;
        MaterialSwitch switchActive;

        KeywordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKeywords = itemView.findViewById(R.id.tv_keywords);
            tvMeta = itemView.findViewById(R.id.tv_meta);
            tvReplyId = itemView.findViewById(R.id.tv_reply_id);
            switchActive = itemView.findViewById(R.id.switch_active);
        }

        void bind(Keyword keyword, KeywordClickListener listener) {
            tvKeywords.setText(keyword.getKeywordsDisplayText());
            tvMeta.setText("Match: " + keyword.getMatchType() + "  •  Priority: " + keyword.getPriority());
            
            // In a future phase, we would resolve this ID to a real Reply Title.
            // For now, just show the ID.
            tvReplyId.setText(keyword.getReplyId() != null && !keyword.getReplyId().isEmpty() ? keyword.getReplyId() : "No Reply Linked");

            // Prevent the OnCheckedChangeListener from firing during recycle binding
            switchActive.setOnCheckedChangeListener(null);
            switchActive.setChecked(keyword.isEnabled());

            switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onToggleActive(keyword, isChecked);
            });

            itemView.setOnClickListener(v -> listener.onEditClick(keyword));
        }
    }
}
