package com.orderflow.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.orderflow.data.local.SharedPreferencesManager;
import com.orderflow.data.repository.AuthRepository;
import com.orderflow.databinding.ActivitySplashBinding;
import com.orderflow.ui.auth.LoginActivity;
import com.orderflow.ui.home.HomeActivity;
import com.orderflow.ui.wizard.PermissionWizardActivity;

/**
 * SPLASH ACTIVITY
 *
 * Purpose:
 * The entry point of the app (configured in AndroidManifest).
 * Displays a branding animation and handles routing logic.
 *
 * Routing Logic:
 * 1. Checks if a Firebase user is logged in (AuthRepository)
 * 2. If NO → route to LoginActivity
 * 3. If YES → checks if the Permission Wizard is completed (SharedPreferencesManager)
 * 4. If wizard NOT completed → route to PermissionWizardActivity
 * 5. If wizard IS completed → route to HomeActivity (Dashboard)
 *
 * Animation:
 * Uses a smooth property animation to scale and fade in the logo and text.
 */
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private AuthRepository authRepository;
    private SharedPreferencesManager prefs;

    // Minimum time to show splash screen (prevents flashing if routing is instant)
    private static final long SPLASH_DELAY_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Edge-to-edge support for modern Android devices
        ViewCompat.setOnApplyWindowInsetsListener(binding.splashRoot, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = new AuthRepository();
        prefs = SharedPreferencesManager.getInstance();

        startAnimations();
        routeNextScreen();
    }

    /**
     * Animates the UI elements.
     * Logo starts smaller (scale 0.8) and transparent, then scales to 1.0 and fades in.
     * Text fades in slightly after the logo.
     */
    private void startAnimations() {
        // Initial state: hide text, shrink logo
        binding.tvAppName.setAlpha(0f);
        binding.tvTagline.setAlpha(0f);
        binding.ivLogo.setScaleX(0.8f);
        binding.ivLogo.setScaleY(0.8f);
        binding.ivLogo.setAlpha(0f);

        // Logo animation
        binding.ivLogo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Text animation (delayed)
        binding.tvAppName.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(300)
                .start();

        binding.tvTagline.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(400)
                .start();
    }

    /**
     * Determines the next destination and launches it after a delay.
     */
    private void routeNextScreen() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            Intent intent;

            if (authRepository.isUserSignedIn()) {
                // User is logged in — check if they finished onboarding
                if (prefs.isWizardCompleted()) {
                    intent = new Intent(this, HomeActivity.class);
                } else {
                    intent = new Intent(this, PermissionWizardActivity.class);
                }
            } else {
                // Not logged in
                intent = new Intent(this, LoginActivity.class);
            }

            startActivity(intent);
            // Finish splash so the user can't press back to return here
            finish();

            // Custom transition: fade out splash, fade in next screen
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }, SPLASH_DELAY_MS);
    }
}
