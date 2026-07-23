package com.orderflow.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.android.material.snackbar.Snackbar;
import com.orderflow.data.local.SharedPreferencesManager;
import com.orderflow.databinding.ActivityLoginBinding;
import com.orderflow.ui.home.HomeActivity;
import com.orderflow.ui.wizard.PermissionWizardActivity;

/**
 * LOGIN ACTIVITY
 *
 * Purpose:
 * UI for the user to sign in using their Firebase email and password.
 * Routes to PermissionWizard on first login, or HomeActivity on subsequent logins.
 *
 * MVVM Implementation:
 * This Activity contains NO business logic. It simply reads input from the UI,
 * passes it to LoginViewModel, and observes the ViewModel's state to update
 * the UI (show loading, show error, or route to next screen).
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private SharedPreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        prefs = SharedPreferencesManager.getInstance();

        // Edge-to-edge layout support
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginRoot, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(LoginViewModel.class);

        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        // Pre-fill "Remember Me" data if it exists
        if (viewModel.getRememberMeState()) {
            binding.cbRememberMe.setChecked(true);
            String savedEmail = viewModel.getSavedEmail();
            if (savedEmail != null) {
                binding.etEmail.setText(savedEmail);
                binding.etPassword.requestFocus(); // Move focus to password field automatically
            }
        }

        // Sign In Button Click
        binding.btnSignIn.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
            boolean rememberMe = binding.cbRememberMe.isChecked();

            viewModel.login(email, password, rememberMe);
        });

        // Forgot Password Text Click
        binding.btnForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    setLoadingState(true);
                    break;
                
                case ERROR:
                    setLoadingState(false);
                    showError(resource.message);
                    break;
                
                case SUCCESS:
                    setLoadingState(false);
                    routeToNextScreen();
                    break;
            }
        });
    }

    /**
     * Toggles UI interactions and changes button text during network request.
     */
    private void setLoadingState(boolean isLoading) {
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.cbRememberMe.setEnabled(!isLoading);
        binding.btnForgotPassword.setEnabled(!isLoading);
        
        if (isLoading) {
            binding.btnSignIn.setEnabled(false);
            binding.btnSignIn.setText("Signing in...");
        } else {
            binding.btnSignIn.setEnabled(true);
            binding.btnSignIn.setText(com.orderflow.R.string.auth_login_button);
        }
    }

    private void showError(String message) {
        // Use Snackbar for a modern error display
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(com.orderflow.R.color.status_error, getTheme()))
                .setTextColor(getResources().getColor(android.R.color.white, getTheme()))
                .show();
    }

    private void routeToNextScreen() {
        Intent intent;
        if (prefs.isWizardCompleted()) {
            intent = new Intent(this, HomeActivity.class);
        } else {
            intent = new Intent(this, PermissionWizardActivity.class);
        }
        startActivity(intent);
        finish(); // Prevent going back to login screen
    }
}
