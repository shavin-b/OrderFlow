package com.orderflow.data.model;

import com.google.firebase.firestore.DocumentId;

/**
 * USER SETTINGS MODEL
 *
 * Purpose:
 * Stores the user's configuration preferences for the OrderFlow app.
 * This is a single document per user — not a collection of multiple documents.
 *
 * Firestore Document Path:
 * /users/{userId}/settings/config
 *
 * Note the path ends with "config" — a fixed document ID (not auto-generated).
 * This is intentional: there is only ONE settings document per user, so we
 * use a known document ID instead of an auto-generated one.
 *
 * Architecture:
 * Settings are stored in Firestore for cloud sync, but are ALSO cached locally
 * in SharedPreferences via SharedPreferencesManager for fast access without
 * a network round-trip.
 *
 * Sync Strategy:
 * - On app launch: load from SharedPreferences first (instant), then sync from Firestore
 * - On settings change: save to SharedPreferences (immediate) + Firestore (async)
 * - The Firestore copy acts as a backup for when the user installs the app on a new device
 */
public class UserSettings {

    /**
     * Cooldown period constants (in hours).
     * After sending a reply to a customer, the app will not send another reply
     * to the same customer until the cooldown period expires.
     */
    public static final int COOLDOWN_12_HOURS = 12;
    public static final int COOLDOWN_24_HOURS = 24;
    public static final int COOLDOWN_48_HOURS = 48;

    // ─────────────────────────────────────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fixed document ID = "config".
     * Populated by Firestore via @DocumentId when the settings document is fetched.
     */
    @DocumentId
    private String id;

    /**
     * Master switch for the entire auto-reply system.
     * When false: the Notification Listener still runs (reads messages),
     * but NO replies are sent regardless of keyword matches.
     * Default: true
     */
    private boolean autoReplyEnabled;

    /**
     * Cooldown period in hours.
     * Prevents sending duplicate replies to the same customer within this window.
     * Valid values: 12, 24, 48 (use constants COOLDOWN_12_HOURS, etc.)
     * Default: 24
     */
    private int cooldownHours;

    /**
     * Whether dark mode is enabled.
     * When true: AppCompatDelegate.MODE_NIGHT_YES is applied on app start.
     * When false: AppCompatDelegate.MODE_NIGHT_NO is applied.
     * Default: follows system setting (MODE_NIGHT_FOLLOW_SYSTEM)
     */
    private boolean darkModeEnabled;

    /**
     * Whether the user has completed the permission setup wizard.
     * Used to decide whether to show the wizard on first launch.
     * Default: false
     */
    private boolean wizardCompleted;

    /**
     * ISO 8601 timestamp of the last successful Firebase backup.
     * Displayed in the Settings screen under "Last Backup".
     * Example: "2024-07-23T08:30:00Z"
     * Null if no backup has been made yet.
     */
    private String lastBackupTimestamp;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Required no-arg constructor for Firestore deserialization.
     * Sets sensible production defaults.
     */
    public UserSettings() {
        this.autoReplyEnabled   = true;
        this.cooldownHours      = COOLDOWN_24_HOURS;
        this.darkModeEnabled    = false;
        this.wizardCompleted    = false;
        this.lastBackupTimestamp = null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GETTERS & SETTERS
    // ─────────────────────────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isAutoReplyEnabled() { return autoReplyEnabled; }
    public void setAutoReplyEnabled(boolean autoReplyEnabled) {
        this.autoReplyEnabled = autoReplyEnabled;
    }

    public int getCooldownHours() { return cooldownHours; }
    public void setCooldownHours(int cooldownHours) { this.cooldownHours = cooldownHours; }

    public boolean isDarkModeEnabled() { return darkModeEnabled; }
    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }

    public boolean isWizardCompleted() { return wizardCompleted; }
    public void setWizardCompleted(boolean wizardCompleted) {
        this.wizardCompleted = wizardCompleted;
    }

    public String getLastBackupTimestamp() { return lastBackupTimestamp; }
    public void setLastBackupTimestamp(String lastBackupTimestamp) {
        this.lastBackupTimestamp = lastBackupTimestamp;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITY METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the cooldown period in milliseconds.
     * Used by the cooldown check logic in the Notification Listener:
     * if (System.currentTimeMillis() - lastReplyTime < settings.getCooldownMillis()) → skip
     */
    public long getCooldownMillis() {
        return (long) cooldownHours * 60 * 60 * 1000L;
    }

    /**
     * Returns a display-friendly label for the cooldown setting.
     * Used in the Settings screen.
     */
    public String getCooldownDisplayLabel() {
        return cooldownHours + " Hours";
    }

    @Override
    public String toString() {
        return "UserSettings{autoReply=" + autoReplyEnabled +
                ", cooldown=" + cooldownHours + "h" +
                ", darkMode=" + darkModeEnabled +
                ", wizardCompleted=" + wizardCompleted + "}";
    }
}
