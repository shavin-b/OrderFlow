package com.orderflow.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.orderflow.R;
import com.orderflow.data.local.SharedPreferencesManager;
import com.orderflow.data.model.UserSettings;
import com.orderflow.databinding.FragmentSettingsBinding;
import com.orderflow.ui.auth.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SETTINGS FRAGMENT
 *
 * Provides control over master auto-reply switch, cooldown periods, dark theme, and account logout.
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private UserSettings currentSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        setupUserInfo();
        setupListeners();
        observeViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            binding.tvUserEmail.setText("Logged in as: " + user.getEmail());
        }
    }

    private void setupListeners() {
        // Master Switch Listener
        binding.switchMaster.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null && currentSettings.isAutoReplyEnabled() != isChecked) {
                currentSettings.setAutoReplyEnabled(isChecked);
                viewModel.updateSettings(currentSettings);
            }
        });

        // Cooldown Radio Group Listener
        binding.rgCooldown.setOnCheckedChangeListener((group, checkedId) -> {
            if (currentSettings == null) return;
            int hours = UserSettings.COOLDOWN_24_HOURS;
            if (checkedId == R.id.rb_12h) hours = UserSettings.COOLDOWN_12_HOURS;
            else if (checkedId == R.id.rb_24h) hours = UserSettings.COOLDOWN_24_HOURS;
            else if (checkedId == R.id.rb_48h) hours = UserSettings.COOLDOWN_48_HOURS;

            if (currentSettings.getCooldownHours() != hours) {
                currentSettings.setCooldownHours(hours);
                viewModel.updateSettings(currentSettings);
            }
        });

        // Dark Mode Switch Listener
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null && currentSettings.isDarkModeEnabled() != isChecked) {
                currentSettings.setDarkModeEnabled(isChecked);
                viewModel.updateSettings(currentSettings);

                AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            }
        });

        // Clear Cooldowns Button
        binding.btnClearCooldowns.setOnClickListener(v -> {
            viewModel.clearCooldowns();
            Toast.makeText(requireContext(), "All active cooldowns cleared", Toast.LENGTH_SHORT).show();
        });

        // Backup Now Button
        binding.btnBackupNow.setOnClickListener(v -> {
            String nowIso = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            if (currentSettings != null) {
                currentSettings.setLastBackupTimestamp(nowIso);
                viewModel.updateSettings(currentSettings);
                binding.tvLastBackup.setText("Last Backup: " + nowIso);
                Toast.makeText(requireContext(), "Settings backed up to Firebase", Toast.LENGTH_SHORT).show();
            }
        });

        // Logout Button
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferencesManager.getInstance().clearAll();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.getSettingsState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.data == null) return;
            currentSettings = resource.data;

            binding.switchMaster.setChecked(currentSettings.isAutoReplyEnabled());
            binding.switchDarkMode.setChecked(currentSettings.isDarkModeEnabled());

            int cooldownHours = currentSettings.getCooldownHours();
            if (cooldownHours == UserSettings.COOLDOWN_12_HOURS) binding.rgCooldown.check(R.id.rb_12h);
            else if (cooldownHours == UserSettings.COOLDOWN_48_HOURS) binding.rgCooldown.check(R.id.rb_48h);
            else binding.rgCooldown.check(R.id.rb_24h);

            if (currentSettings.getLastBackupTimestamp() != null) {
                binding.tvLastBackup.setText("Last Backup: " + currentSettings.getLastBackupTimestamp());
            } else {
                binding.tvLastBackup.setText("Last Backup: Never");
            }
        });
    }
}
