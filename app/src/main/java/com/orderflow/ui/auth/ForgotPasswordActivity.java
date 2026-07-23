package com.orderflow.ui.auth;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.android.material.snackbar.Snackbar;
import com.orderflow.databinding.ActivityForgotPasswordBinding;

/**
 * FORGOT PASSWORD ACTIVITY
 *
 * Purpose:
 * Allows the user to request a password reset email via Firebase Auth.
 * Re-uses the LoginViewModel since the authentication logic is closely related.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(binding.forgotRoot, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        // Setup Toolbar back button
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // We use the same ViewModel as LoginActivity because it handles Auth tasks
        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(LoginViewModel.class);

        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        binding.btnSendReset.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString() : "";
            viewModel.sendPasswordReset(email);
        });
    }

    private void observeViewModel() {
        viewModel.getResetState().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    setLoadingState(true);
                    break;

                case ERROR:
                    setLoadingState(false);
                    showError(resource.message);
                    viewModel.clearResetState(); // Clear so it doesn't fire again on rotation
                    break;

                case SUCCESS:
                    setLoadingState(false);
                    // Use Toast instead of Snackbar here because we are finishing the Activity
                    Toast.makeText(this, "Password reset email sent. Check your inbox.", Toast.LENGTH_LONG).show();
                    viewModel.clearResetState();
                    finish(); // Go back to login screen automatically
                    break;
            }
        });
    }

    /**
     * Toggles UI interactions during network request.
     */
    private void setLoadingState(boolean isLoading) {
        binding.etEmail.setEnabled(!isLoading);
        if (isLoading) {
            binding.btnSendReset.setEnabled(false);
            binding.btnSendReset.setText("Sending...");
        } else {
            binding.btnSendReset.setEnabled(true);
            binding.btnSendReset.setText("Send Reset Link");
        }
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(com.orderflow.R.color.status_error, getTheme()))
                .setTextColor(getResources().getColor(android.R.color.white, getTheme()))
                .show();
    }
}
