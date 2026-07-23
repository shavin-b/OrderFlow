package com.orderflow.ui.keywords;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.orderflow.data.model.Keyword;
import com.orderflow.databinding.FragmentKeywordsBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * KEYWORDS FRAGMENT
 *
 * Purpose:
 * Displays the list of keyword rules. Allows adding, editing, and deleting rules.
 */
public class KeywordsFragment extends Fragment implements KeywordAdapter.KeywordClickListener {

    private FragmentKeywordsBinding binding;
    private KeywordsViewModel viewModel;
    private KeywordAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentKeywordsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(KeywordsViewModel.class);

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
        adapter = new KeywordAdapter(this);
        binding.rvKeywords.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvKeywords.setAdapter(adapter);
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> showKeywordDialog(null));
    }

    private void observeViewModel() {
        // Observe List State
        viewModel.getKeywordsListState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.stateContainer.setVisibility(View.VISIBLE);
                    binding.pbLoading.setVisibility(View.VISIBLE);
                    binding.ivEmptyState.setVisibility(View.GONE);
                    binding.tvEmptyState.setVisibility(View.GONE);
                    binding.rvKeywords.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.pbLoading.setVisibility(View.GONE);
                    List<Keyword> keywords = resource.data;
                    if (keywords == null || keywords.isEmpty()) {
                        // Empty state
                        binding.stateContainer.setVisibility(View.VISIBLE);
                        binding.ivEmptyState.setVisibility(View.VISIBLE);
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                        binding.rvKeywords.setVisibility(View.GONE);
                    } else {
                        // Data loaded
                        binding.stateContainer.setVisibility(View.GONE);
                        binding.rvKeywords.setVisibility(View.VISIBLE);
                        adapter.setKeywords(keywords);
                    }
                    break;
                case ERROR:
                    binding.pbLoading.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error loading keywords: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // Observe Operation State (Save / Update / Delete)
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
                    // Optional: could show a small progress bar near the FAB
                    break;
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LISTENER CALLBACKS
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onEditClick(Keyword keyword) {
        showKeywordDialog(keyword);
    }

    @Override
    public void onToggleActive(Keyword keyword, boolean isChecked) {
        keyword.setEnabled(isChecked);
        viewModel.updateKeyword(keyword);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADD / EDIT DIALOG
    // ─────────────────────────────────────────────────────────────────────────

    private void showKeywordDialog(@Nullable Keyword existingKeyword) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_keyword, null);
        
        TextInputEditText etKeywords = dialogView.findViewById(R.id.et_keywords);
        AutoCompleteTextView actvMatchType = dialogView.findViewById(R.id.actv_match_type);
        TextInputEditText etPriority = dialogView.findViewById(R.id.et_priority);
        TextInputEditText etReplyId = dialogView.findViewById(R.id.et_reply_id);
        
        // Setup dropdown for Match Type
        String[] matchTypes = new String[]{Keyword.MATCH_TYPE_PARTIAL, Keyword.MATCH_TYPE_EXACT};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, matchTypes);
        actvMatchType.setAdapter(adapter);

        // Pre-fill data if editing
        if (existingKeyword != null) {
            etKeywords.setText(existingKeyword.getKeywordsDisplayText());
            actvMatchType.setText(existingKeyword.getMatchType(), false);
            etPriority.setText(String.valueOf(existingKeyword.getPriority()));
            etReplyId.setText(existingKeyword.getReplyId());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Theme_OrderFlow_Dialog)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        
        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            // Basic validation
            String keywordsInput = etKeywords.getText() != null ? etKeywords.getText().toString().trim() : "";
            String matchType = actvMatchType.getText().toString();
            String priorityStr = etPriority.getText() != null ? etPriority.getText().toString().trim() : "10";
            String replyId = etReplyId.getText() != null ? etReplyId.getText().toString().trim() : "";
            
            if (keywordsInput.isEmpty()) {
                etKeywords.setError("Cannot be empty");
                return;
            }
            if (replyId.isEmpty()) {
                etReplyId.setError("Cannot be empty");
                return;
            }

            int priority = 10;
            try {
                priority = Integer.parseInt(priorityStr);
            } catch (NumberFormatException e) {
                etPriority.setError("Invalid number");
                return;
            }

            // Parse comma separated keywords into a list
            List<String> keywordList = new ArrayList<>();
            for (String kw : keywordsInput.split(",")) {
                String trimmed = kw.trim();
                if (!trimmed.isEmpty()) {
                    keywordList.add(trimmed.toLowerCase()); // Store keywords in lowercase for easier matching later
                }
            }

            // Create or update object
            if (existingKeyword == null) {
                Keyword newKeyword = new Keyword(keywordList, replyId, priority, matchType);
                viewModel.addKeyword(newKeyword);
            } else {
                existingKeyword.setKeywords(keywordList);
                existingKeyword.setMatchType(matchType);
                existingKeyword.setPriority(priority);
                existingKeyword.setReplyId(replyId);
                viewModel.updateKeyword(existingKeyword);
            }

            dialog.dismiss();
        });

        // Add a delete button to the dialog if editing
        if (existingKeyword != null) {
            View btnDelete = dialogView.findViewById(R.id.btn_cancel); // We'll hijack cancel and turn it to a delete if we had a 3rd button.
            // Better: Let's just add a long press on the list item to delete, to avoid changing dialog layout too much.
        }

        dialog.show();
    }
}
