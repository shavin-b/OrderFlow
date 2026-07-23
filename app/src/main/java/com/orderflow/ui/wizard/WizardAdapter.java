package com.orderflow.ui.wizard;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.orderflow.R;

import java.util.List;

/**
 * Adapter for the Permission Wizard ViewPager2.
 * Inflates the item_wizard_step.xml layout and binds the specific data for each step.
 */
public class WizardAdapter extends RecyclerView.Adapter<WizardAdapter.WizardViewHolder> {

    private final List<WizardStep> steps;
    private final Context context;

    public WizardAdapter(Context context, List<WizardStep> steps) {
        this.context = context;
        this.steps = steps;
    }

    @NonNull
    @Override
    public WizardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wizard_step, parent, false);
        return new WizardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WizardViewHolder holder, int position) {
        WizardStep step = steps.get(position);

        holder.ivIcon.setImageResource(step.iconResId);
        holder.tvTitle.setText(step.title);
        holder.tvDesc.setText(step.description);

        // Update the status badge based on whether the permission is granted
        if (step.isGranted) {
            holder.tvStatus.setText("Permission Active");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.wa_green));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_permission_active);
            
            // Hide the action button if already granted
            holder.btnAction.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setText("Permission Required");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_warning));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_permission_pending);
            
            // Show the action button and set its click listener
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText(step.buttonText);
            holder.btnAction.setOnClickListener(v -> {
                if (step.actionIntent != null) {
                    context.startActivity(step.actionIntent);
                }
            });
        }
        
        // Special case for the final "All Done" step — it never shows a status badge or action button
        if (position == 2) {
            holder.tvStatus.setVisibility(View.GONE);
            holder.btnAction.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    /**
     * Data class holding the configuration for a single wizard step.
     */
    public static class WizardStep {
        int iconResId;
        String title;
        String description;
        String buttonText;
        boolean isGranted;
        Intent actionIntent; // The Android settings intent to launch

        public WizardStep(int iconResId, String title, String description, String buttonText, boolean isGranted, Intent actionIntent) {
            this.iconResId = iconResId;
            this.title = title;
            this.description = description;
            this.buttonText = buttonText;
            this.isGranted = isGranted;
            this.actionIntent = actionIntent;
        }
    }

    static class WizardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDesc;
        TextView tvStatus;
        Button btnAction;

        WizardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_step_icon);
            tvTitle = itemView.findViewById(R.id.tv_step_title);
            tvDesc = itemView.findViewById(R.id.tv_step_desc);
            tvStatus = itemView.findViewById(R.id.tv_status_badge);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}
