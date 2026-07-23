package com.orderflow.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.orderflow.R;
import com.orderflow.databinding.FragmentDashboardBinding;
import com.orderflow.utils.ServiceUtils;

/**
 * DASHBOARD FRAGMENT
 *
 * Purpose:
 * The main overview screen shown when the app opens.
 * Displays system health (Notification and Accessibility service status),
 * quick statistics (messages today, active keywords), and recent activity logs.
 */
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate using ViewBinding
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupWelcomeHeader();
        setupServiceClickListeners();
        observeViewModel();
    }

    /**
     * Re-check service statuses every time this screen becomes visible.
     * This ensures the UI is accurate if the user just returned from Android Settings.
     */
    @Override
    public void onResume() {
        super.onResume();
        updateServiceStatuses();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI SETUP & DATA BINDING
    // ─────────────────────────────────────────────────────────────────────────

    private void setupWelcomeHeader() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Display part of email as name, e.g., "business@email.com" -> "Welcome, business"
            String email = user.getEmail();
            String name = email.split("@")[0];
            // Capitalize first letter
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            binding.tvWelcome.setText("Welcome, " + name);
        }
    }

    private void setupServiceClickListeners() {
        // Allow user to click the cards to go straight to settings if they need to fix permissions
        binding.cardNotification.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });

        binding.cardAccessibility.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
    }

    /**
     * Checks if the required background services are running and updates the UI cards.
     */
    private void updateServiceStatuses() {
        boolean notifActive = ServiceUtils.isNotificationServiceEnabled(requireContext());
        boolean accActive = ServiceUtils.isAccessibilityServiceEnabled(requireContext());

        // Update Notification Card
        if (notifActive) {
            binding.tvNotifStatus.setText("Active");
            binding.tvNotifStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.wa_green));
        } else {
            binding.tvNotifStatus.setText("Inactive - Tap to Fix");
            binding.tvNotifStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_error));
        }

        // Update Accessibility Card
        if (accActive) {
            binding.tvAccStatus.setText("Active");
            binding.tvAccStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.wa_green));
        } else {
            binding.tvAccStatus.setText("Inactive - Tap to Fix");
            binding.tvAccStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_error));
        }
    }

    /**
     * Observes LiveData from DashboardViewModel to populate stats and recent activity.
     */
    private void observeViewModel() {
        viewModel.getTodayReplies().observe(getViewLifecycleOwner(), count -> {
            binding.tvTodayReplies.setText(String.valueOf(count));
        });

        viewModel.getActiveKeywords().observe(getViewLifecycleOwner(), count -> {
            binding.tvActiveKeywords.setText(String.valueOf(count));
        });

        viewModel.getRecentActivity().observe(getViewLifecycleOwner(), activityText -> {
            binding.tvRecentActivity.setText(activityText);
        });
    }
}
