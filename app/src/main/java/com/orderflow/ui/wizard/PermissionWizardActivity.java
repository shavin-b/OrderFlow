package com.orderflow.ui.wizard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.orderflow.R;
import com.orderflow.data.local.SharedPreferencesManager;
import com.orderflow.databinding.ActivityPermissionWizardBinding;
import com.orderflow.service.AutoReplyAccessibilityService;
import com.orderflow.service.WhatsAppNotificationListener;
import com.orderflow.ui.home.HomeActivity;
import com.orderflow.utils.ServiceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * PERMISSION WIZARD ACTIVITY
 *
 * Purpose:
 * Guides the user through enabling the two critical system permissions required for the app:
 * 1. Notification Listener (to read incoming WhatsApp messages)
 * 2. Accessibility Service (to automate UI clicks for sending replies)
 *
 * It uses a ViewPager2 to show a step-by-step tutorial.
 * The user cannot proceed to the app until these are granted.
 */
public class PermissionWizardActivity extends AppCompatActivity {

    private ActivityPermissionWizardBinding binding;
    private WizardAdapter adapter;
    private SharedPreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPermissionWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        prefs = SharedPreferencesManager.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(binding.wizardRoot, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        setupViewPager();
        setupBottomBar();
    }

    /**
     * Re-check permissions every time the user returns to this Activity.
     * This handles the case where they come back from the Android Settings screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            setupViewPager(); // Rebuild the list with updated grant statuses
        }
    }

    private void setupViewPager() {
        List<WizardAdapter.WizardStep> steps = new ArrayList<>();

        // Step 1: Notification Access
        boolean isNotificationGranted = ServiceUtils.isNotificationServiceEnabled(this);
        steps.add(new WizardAdapter.WizardStep(
                R.drawable.ic_wizard_notification,
                "Notification Access",
                "We need to read incoming WhatsApp messages to know when to send an auto-reply.",
                "Enable Notification Access",
                isNotificationGranted,
                new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        ));

        // Step 2: Accessibility Service
        boolean isAccessibilityGranted = ServiceUtils.isAccessibilityServiceEnabled(this);
        steps.add(new WizardAdapter.WizardStep(
                R.drawable.ic_wizard_accessibility,
                "Accessibility Service",
                "We need accessibility access to automatically type and send the reply on your behalf.",
                "Enable Accessibility",
                isAccessibilityGranted,
                new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        ));

        // Step 3: All Done
        steps.add(new WizardAdapter.WizardStep(
                R.drawable.ic_wizard_done,
                "All Set!",
                "OrderFlow is now ready to automate your WhatsApp Business replies.",
                "",
                true,
                null
        ));

        adapter = new WizardAdapter(this, steps);
        binding.viewPager.setAdapter(adapter);

        // Keep the dots in sync with swipe gestures
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateStepIndicators(position);
            }
        });
    }

    private void setupBottomBar() {
        binding.btnNext.setOnClickListener(v -> {
            int currentItem = binding.viewPager.getCurrentItem();
            
            if (currentItem < 2) {
                // Move to next step
                binding.viewPager.setCurrentItem(currentItem + 1);
            } else {
                // Final step reached, finish wizard and go to Home
                prefs.setWizardCompleted(true);
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });
    }

    /**
     * Updates the UI state of the 3 dot indicators at the bottom.
     */
    private void updateStepIndicators(int position) {
        // Reset all dots to inactive
        binding.dot1.setBackgroundResource(R.drawable.bg_step_indicator_inactive);
        binding.dot2.setBackgroundResource(R.drawable.bg_step_indicator_inactive);
        binding.dot3.setBackgroundResource(R.drawable.bg_step_indicator_inactive);

        // Set the active dot
        if (position == 0) binding.dot1.setBackgroundResource(R.drawable.bg_step_indicator_active);
        else if (position == 1) binding.dot2.setBackgroundResource(R.drawable.bg_step_indicator_active);
        else if (position == 2) binding.dot3.setBackgroundResource(R.drawable.bg_step_indicator_active);

        // Change button text on the last step
        if (position == 2) {
            binding.btnNext.setText("Go to Dashboard");
        } else {
            binding.btnNext.setText("Next Step");
        }
    }

}
