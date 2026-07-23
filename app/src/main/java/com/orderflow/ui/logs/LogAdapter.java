package com.orderflow.ui.logs;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.orderflow.R;
import com.orderflow.data.model.MessageLog;
import com.orderflow.databinding.ItemLogBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * LOG ADAPTER
 *
 * Renders the list of MessageLog objects with formatted timestamps and color-coded status badges.
 */
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private List<MessageLog> logs = new ArrayList<>();

    public void setLogs(List<MessageLog> newLogs) {
        this.logs = newLogs != null ? newLogs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLogBinding binding = ItemLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new LogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(logs.get(position));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        private final ItemLogBinding binding;

        public LogViewHolder(ItemLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MessageLog log) {
            Context context = itemView.getContext();

            binding.tvCustomerName.setText(log.getDisplayName());
            binding.tvIncomingMessage.setText(log.getIncomingMessagePreview());

            // Timestamp formatting
            if (log.getTimestamp() != null) {
                Date date = log.getTimestamp().toDate();
                String formattedDate = DateFormat.getMediumDateFormat(context).format(date) + " " +
                        DateFormat.getTimeFormat(context).format(date);
                binding.tvTimestamp.setText(formattedDate);
            } else {
                binding.tvTimestamp.setText("Just now");
            }

            // Matched Keyword Section
            if (log.getMatchedKeyword() != null && !log.getMatchedKeyword().isEmpty()) {
                binding.containerMatched.setVisibility(View.VISIBLE);
                binding.tvMatchedKeyword.setText(log.getMatchedKeyword());
            } else {
                binding.containerMatched.setVisibility(View.GONE);
            }

            // Reply / Error Message Section
            if (log.getReplySent() != null && !log.getReplySent().isEmpty()) {
                binding.containerReply.setVisibility(View.VISIBLE);
                binding.tvReplyLabel.setText("Reply Sent: ");
                binding.tvReplySent.setText(log.getReplySent());
            } else if (log.getErrorMessage() != null && !log.getErrorMessage().isEmpty()) {
                binding.containerReply.setVisibility(View.VISIBLE);
                binding.tvReplyLabel.setText("Error Detail: ");
                binding.tvReplySent.setText(log.getErrorMessage());
            } else {
                binding.containerReply.setVisibility(View.GONE);
            }

            // Status Badge Styling
            String status = log.getStatus() != null ? log.getStatus() : MessageLog.STATUS_NO_MATCH;
            binding.tvStatusBadge.setText(status);

            int textColor;
            int bgDrawable;

            switch (status) {
                case MessageLog.STATUS_REPLIED:
                    textColor = ContextCompat.getColor(context, R.color.status_success);
                    break;
                case MessageLog.STATUS_COOLDOWN:
                    textColor = ContextCompat.getColor(context, R.color.status_warning);
                    break;
                case MessageLog.STATUS_ERROR:
                    textColor = ContextCompat.getColor(context, R.color.status_error);
                    break;
                case MessageLog.STATUS_NO_MATCH:
                default:
                    textColor = ContextCompat.getColor(context, R.color.text_tertiary);
                    break;
            }

            binding.tvStatusBadge.setTextColor(textColor);
        }
    }
}
