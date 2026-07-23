package com.orderflow.ui.replies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.orderflow.R;
import com.orderflow.data.model.Reply;
import com.orderflow.databinding.FragmentRepliesBinding;

import java.util.List;

/**
 * REPLIES FRAGMENT
 *
 * Displays the list of reply templates. Allows adding, editing, and deleting templates.
 */
public class RepliesFragment extends Fragment implements ReplyAdapter.ReplyClickListener {

    private FragmentRepliesBinding binding;
    private RepliesViewModel viewModel;
    private ReplyAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRepliesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RepliesViewModel.class);

        setupRecyclerView();
        setupFab();
        observeViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new ReplyAdapter(this);
        binding.rvReplies.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReplies.setAdapter(adapter);
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> showReplyDialog(null));
    }

    private void observeViewModel() {
        // Observe List State
        viewModel.getRepliesListState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.stateContainer.setVisibility(View.VISIBLE);
                    binding.pbLoading.setVisibility(View.VISIBLE);
                    binding.ivEmptyState.setVisibility(View.GONE);
                    binding.tvEmptyState.setVisibility(View.GONE);
                    binding.rvReplies.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.pbLoading.setVisibility(View.GONE);
                    List<Reply> replies = resource.data;
                    if (replies == null || replies.isEmpty()) {
                        binding.stateContainer.setVisibility(View.VISIBLE);
                        binding.ivEmptyState.setVisibility(View.VISIBLE);
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                        binding.rvReplies.setVisibility(View.GONE);
                    } else {
                        binding.stateContainer.setVisibility(View.GONE);
                        binding.rvReplies.setVisibility(View.VISIBLE);
                        adapter.setReplies(replies);
                    }
                    break;
                case ERROR:
                    binding.pbLoading.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // Observe Operation State
        viewModel.getOperationState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case ERROR:
                    Snackbar.make(binding.getRoot(), resource.message, Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.status_error, requireContext().getTheme()))
                            .setTextColor(getResources().getColor(android.R.color.white, requireContext().getTheme()))
                            .show();
                    viewModel.clearOperationState();
                    break;
                case SUCCESS:
                    Snackbar.make(binding.getRoot(), "Saved successfully", Snackbar.LENGTH_SHORT).show();
                    viewModel.clearOperationState();
                    break;
                case LOADING:
                    break;
            }
        });
    }

    @Override
    public void onEditClick(Reply reply) {
        showReplyDialog(reply);
    }

    @Override
    public void onToggleActive(Reply reply, boolean isChecked) {
        reply.setEnabled(isChecked);
        viewModel.updateReply(reply);
    }

    private void showReplyDialog(@Nullable Reply existingReply) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reply, null);
        
        TextInputEditText etTitle = dialogView.findViewById(R.id.et_title);
        TextInputEditText etContent = dialogView.findViewById(R.id.et_content);
        
        if (existingReply != null) {
            etTitle.setText(existingReply.getTitle());
            etContent.setText(existingReply.getContent());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Theme_OrderFlow_Dialog)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        
        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String content = etContent.getText() != null ? etContent.getText().toString().trim() : "";
            
            if (title.isEmpty()) {
                etTitle.setError("Cannot be empty");
                return;
            }
            if (content.isEmpty()) {
                etContent.setError("Cannot be empty");
                return;
            }

            if (existingReply == null) {
                Reply newReply = new Reply(title, content);
                viewModel.addReply(newReply);
            } else {
                existingReply.setTitle(title);
                existingReply.setContent(content);
                viewModel.updateReply(existingReply);
            }

            dialog.dismiss();
        });

        dialog.show();
    }
}
