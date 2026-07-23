package com.orderflow.ui.logs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.orderflow.data.model.MessageLog;
import com.orderflow.databinding.FragmentLogsBinding;

import java.util.List;

/**
 * LOGS FRAGMENT
 *
 * Displays the real-time activity log feed.
 */
public class LogsFragment extends Fragment {

    private FragmentLogsBinding binding;
    private LogsViewModel viewModel;
    private LogAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LogsViewModel.class);

        setupRecyclerView();
        observeViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new LogAdapter();
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLogs.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getLogsState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.stateContainer.setVisibility(View.VISIBLE);
                    binding.pbLoading.setVisibility(View.VISIBLE);
                    binding.ivEmptyState.setVisibility(View.GONE);
                    binding.tvEmptyState.setVisibility(View.GONE);
                    binding.rvLogs.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.pbLoading.setVisibility(View.GONE);
                    List<MessageLog> logs = resource.data;
                    if (logs == null || logs.isEmpty()) {
                        binding.stateContainer.setVisibility(View.VISIBLE);
                        binding.ivEmptyState.setVisibility(View.VISIBLE);
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                        binding.rvLogs.setVisibility(View.GONE);
                    } else {
                        binding.stateContainer.setVisibility(View.GONE);
                        binding.rvLogs.setVisibility(View.VISIBLE);
                        adapter.setLogs(logs);
                    }
                    break;
                case ERROR:
                    binding.pbLoading.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
