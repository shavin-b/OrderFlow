package com.orderflow;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.orderflow.data.local.SharedPreferencesManager;

/**
 * ORDER FLOW APPLICATION CLASS
 *
 * Purpose:
 * The Application class is the entry point for the entire Android application.
 * It is instantiated before any Activity, Service, or BroadcastReceiver.
 *
 * What we do here:
 * 1. Initialize SharedPreferencesManager (singleton requires Application context)
 * 2. Apply the saved dark/light mode preference BEFORE any Activity inflates its layout
 *    (if we did this in an Activity, there would be a visible theme flash)
 * 3. Firebase is auto-initialized by the google-services plugin — no manual init needed
 * 4. Firestore offline persistence is enabled by default — no configuration needed
 *
 * AndroidManifest reference:
 * <application android:name=".OrderFlowApp" ... />
 * This line tells Android to use this class as the Application instance.
 *
 * Design Note:
 * We keep this class lean. Heavy initialization (like loading user settings from
 * Firestore) happens lazily in the respective ViewModel/Repository when needed,
 * not at app startup. This keeps the app launch fast.
 */
public class OrderFlowApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // ── Step 1: Initialize SharedPreferencesManager ───────────────────────
        // This singleton needs the application context.
        // All subsequent calls to SharedPreferencesManager.getInstance() work
        // because we initialize it here first.
        SharedPreferencesManager.init(this);

        // ── Step 2: Apply Dark Mode ────────────────────────────────────────────
        // Read the dark mode preference BEFORE any Activity starts.
        // This prevents a white flash when launching the app in dark mode.
        applyThemeFromPreferences();
    }

    /**
     * Reads the dark mode preference from SharedPreferences and applies the
     * corresponding AppCompat night mode globally.
     *
     * Mode options:
     * - MODE_NIGHT_NO:            Always use light theme
     * - MODE_NIGHT_YES:           Always use dark theme
     * - MODE_NIGHT_FOLLOW_SYSTEM: Follow the device's system dark mode setting (default)
     *
     * Note: AppCompatDelegate.setDefaultNightMode() affects ALL activities in the app.
     * Changing this at runtime causes all currently visible Activities to recreate.
     */
    private void applyThemeFromPreferences() {
        SharedPreferencesManager prefs = SharedPreferencesManager.getInstance();
        boolean isDarkMode = prefs.isDarkModeEnabled();

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            // Use system default — follows device dark mode toggle in Android settings
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
