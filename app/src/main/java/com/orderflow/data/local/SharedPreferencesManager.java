package com.orderflow.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.orderflow.data.model.UserSettings;
import com.orderflow.utils.Constants;

/**
 * SHARED PREFERENCES MANAGER
 *
 * Purpose:
 * A type-safe, centralized wrapper around Android's SharedPreferences.
 * All local data persistence (that doesn't go to Firestore) happens here.
 *
 * Why use a wrapper instead of calling SharedPreferences directly?
 * - Prevents key typos (all keys are in Constants.Prefs)
 * - Type-safety (correct getter/setter per data type)
 * - Testable in isolation
 * - Centralized place to add encryption later if needed
 *
 * Singleton pattern:
 * This class uses a thread-safe singleton. In the app, it is initialized once
 * in OrderFlowApp.onCreate() and accessed via SharedPreferencesManager.getInstance().
 *
 * Usage:
 *   SharedPreferencesManager prefs = SharedPreferencesManager.getInstance();
 *   prefs.setAutoReplyEnabled(true);
 *   boolean isEnabled = prefs.isAutoReplyEnabled(); // → true
 */
public class SharedPreferencesManager {

    // ─────────────────────────────────────────────────────────────────────────
    // SINGLETON
    // ─────────────────────────────────────────────────────────────────────────

    private static volatile SharedPreferencesManager instance;

    private final SharedPreferences prefs;

    /**
     * Private constructor — use getInstance() to get the singleton.
     *
     * @param context Application context (NOT Activity context — avoids memory leaks)
     */
    private SharedPreferencesManager(Context context) {
        // Use application context to avoid leaking Activity context in a singleton
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.Prefs.FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Initializes the singleton. Call this ONCE in OrderFlowApp.onCreate().
     *
     * @param context Application context
     */
    public static void init(Context context) {
        if (instance == null) {
            synchronized (SharedPreferencesManager.class) {
                if (instance == null) {
                    instance = new SharedPreferencesManager(context);
                }
            }
        }
    }

    /**
     * Returns the singleton instance.
     * Must call init(context) before this — usually done in OrderFlowApp.
     *
     * @return Singleton instance
     * @throws IllegalStateException if init() was not called before this
     */
    public static SharedPreferencesManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                "SharedPreferencesManager not initialized. " +
                "Call SharedPreferencesManager.init(context) in your Application class.");
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTH PREFERENCES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Saves the user's email for the "Remember Me" feature.
     * Called when the user checks "Remember Me" before logging in.
     */
    public void saveEmail(String email) {
        prefs.edit().putString(Constants.Prefs.KEY_SAVED_EMAIL, email).apply();
    }

    /**
     * Returns the saved email, or null if not saved.
     */
    public String getSavedEmail() {
        return prefs.getString(Constants.Prefs.KEY_SAVED_EMAIL, null);
    }

    /**
     * Saves the "Remember Me" checkbox state.
     */
    public void setRememberMe(boolean rememberMe) {
        prefs.edit().putBoolean(Constants.Prefs.KEY_REMEMBER_ME, rememberMe).apply();
    }

    /**
     * Returns whether "Remember Me" was checked during the last login.
     */
    public boolean isRememberMe() {
        return prefs.getBoolean(Constants.Prefs.KEY_REMEMBER_ME, false);
    }

    /**
     * Clears all auth-related saved data (email, remember me flag).
     * Called when the user logs out.
     */
    public void clearAuthData() {
        prefs.edit()
                .remove(Constants.Prefs.KEY_SAVED_EMAIL)
                .remove(Constants.Prefs.KEY_REMEMBER_ME)
                .apply();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SETTINGS PREFERENCES (local cache of Firestore UserSettings)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Saves the auto-reply master switch state locally.
     * Called whenever the user toggles auto-reply in Settings.
     */
    public void setAutoReplyEnabled(boolean enabled) {
        prefs.edit().putBoolean(Constants.Prefs.KEY_AUTO_REPLY_ENABLED, enabled).apply();
    }

    /**
     * Returns the locally cached auto-reply state.
     * Default: true (enabled on first install).
     */
    public boolean isAutoReplyEnabled() {
        return prefs.getBoolean(Constants.Prefs.KEY_AUTO_REPLY_ENABLED, true);
    }

    /**
     * Saves the cooldown period (in hours) locally.
     */
    public void setCooldownHours(int hours) {
        prefs.edit().putInt(Constants.Prefs.KEY_COOLDOWN_HOURS, hours).apply();
    }

    /**
     * Returns the locally cached cooldown hours.
     * Default: 24 hours.
     */
    public int getCooldownHours() {
        return prefs.getInt(Constants.Prefs.KEY_COOLDOWN_HOURS, UserSettings.COOLDOWN_24_HOURS);
    }

    /**
     * Saves the dark mode preference locally.
     * Used in OrderFlowApp.onCreate() to apply the theme before any Activity loads.
     */
    public void setDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(Constants.Prefs.KEY_DARK_MODE_ENABLED, enabled).apply();
    }

    /**
     * Returns the locally cached dark mode preference.
     * Default: false (light mode).
     */
    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(Constants.Prefs.KEY_DARK_MODE_ENABLED, false);
    }

    /**
     * Marks the permission wizard as completed.
     * Called in the wizard's last step when the user taps "Finish".
     */
    public void setWizardCompleted(boolean completed) {
        prefs.edit().putBoolean(Constants.Prefs.KEY_WIZARD_COMPLETED, completed).apply();
    }

    /**
     * Returns whether the user has completed the permission setup wizard.
     * Default: false (show wizard on first launch).
     */
    public boolean isWizardCompleted() {
        return prefs.getBoolean(Constants.Prefs.KEY_WIZARD_COMPLETED, false);
    }

    /**
     * Saves the timestamp of the last Firebase backup.
     *
     * @param isoTimestamp ISO 8601 timestamp string
     */
    public void setLastBackupTimestamp(String isoTimestamp) {
        prefs.edit().putString(Constants.Prefs.KEY_LAST_BACKUP_TIME, isoTimestamp).apply();
    }

    /**
     * Returns the timestamp of the last Firebase backup.
     * Returns null if no backup has been made.
     */
    public String getLastBackupTimestamp() {
        return prefs.getString(Constants.Prefs.KEY_LAST_BACKUP_TIME, null);
    }

    /**
     * Applies all settings from a UserSettings object to SharedPreferences at once.
     * Used when syncing settings from Firestore to local storage on app launch.
     *
     * @param settings The UserSettings loaded from Firestore
     */
    public void applySettings(UserSettings settings) {
        if (settings == null) return;
        prefs.edit()
                .putBoolean(Constants.Prefs.KEY_AUTO_REPLY_ENABLED, settings.isAutoReplyEnabled())
                .putInt(Constants.Prefs.KEY_COOLDOWN_HOURS, settings.getCooldownHours())
                .putBoolean(Constants.Prefs.KEY_DARK_MODE_ENABLED, settings.isDarkModeEnabled())
                .putBoolean(Constants.Prefs.KEY_WIZARD_COMPLETED, settings.isWizardCompleted())
                .apply();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COOLDOWN TRACKING
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Records the timestamp of the last reply sent to a specific customer.
     * Called by the AutoReplyAccessibilityService after sending a reply.
     *
     * @param phoneNumber The customer's WhatsApp phone number (used as key)
     * @param timestampMillis System.currentTimeMillis() when the reply was sent
     */
    public void saveCooldownTimestamp(String phoneNumber, long timestampMillis) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return;
        String key = Constants.Prefs.COOLDOWN_PREFIX + phoneNumber;
        prefs.edit().putLong(key, timestampMillis).apply();
    }

    /**
     * Returns the timestamp (in milliseconds) of the last reply sent to a customer.
     *
     * @param phoneNumber The customer's WhatsApp phone number
     * @return Timestamp in milliseconds, or 0 if no reply has been sent yet
     */
    public long getCooldownTimestamp(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return 0L;
        String key = Constants.Prefs.COOLDOWN_PREFIX + phoneNumber;
        return prefs.getLong(key, 0L);
    }

    /**
     * Checks if the cooldown period has expired for a given customer.
     *
     * Logic:
     *   lastReplyTime = getCooldownTimestamp(phoneNumber)
     *   elapsed = currentTime - lastReplyTime
     *   if (elapsed >= cooldownMillis) → cooldown expired → can reply
     *   if (elapsed < cooldownMillis)  → still in cooldown → skip reply
     *
     * @param phoneNumber     Customer's phone number
     * @param cooldownMillis  Cooldown period in milliseconds (from UserSettings.getCooldownMillis())
     * @return true if the cooldown has expired (safe to reply), false if still in cooldown
     */
    public boolean isCooldownExpired(String phoneNumber, long cooldownMillis) {
        long lastReplyTime = getCooldownTimestamp(phoneNumber);
        if (lastReplyTime == 0L) return true; // Never replied before → can reply
        long elapsed = System.currentTimeMillis() - lastReplyTime;
        return elapsed >= cooldownMillis;
    }

    /**
     * Clears all cooldown timestamps for all customers.
     * Called from Settings > "Clear All Cooldowns" (optional feature).
     * We identify cooldown entries by their key prefix.
     */
    public void clearAllCooldowns() {
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(Constants.Prefs.COOLDOWN_PREFIX)) {
                editor.remove(key);
            }
        }
        editor.apply();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FULL CLEAR (used on logout)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Clears ALL SharedPreferences data.
     * Called when the user logs out to prevent data leakage between accounts.
     */
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
