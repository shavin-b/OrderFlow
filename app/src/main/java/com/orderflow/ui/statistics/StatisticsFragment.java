package com.orderflow.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.orderflow.databinding.FragmentStatisticsBinding;

/**
 * STATISTICS FRAGMENT
 *
 * Renders real-time performance analytics and success metrics.
 */
public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private StatisticsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        observeViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void observeViewModel() {
        viewModel.getStatsState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.data == null) return;

            StatisticsViewModel.StatsData stats = resource.data;

            binding.tvSuccessRate.setText(stats.successRate + "%");
            binding.tvTotalSummary.setText("Based on " + stats.totalMessages + " total incoming messages");

            binding.tvRepliedCount.setText(String.valueOf(stats.totalReplied));
            binding.tvNoMatchCount.setText(String.valueOf(stats.totalNoMatch));
            binding.tvCooldownCount.setText(String.valueOf(stats.totalCooldown));
            binding.tvErrorCount.setText(String.valueOf(stats.totalError));
        });
    }
}
