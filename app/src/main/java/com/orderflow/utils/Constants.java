package com.orderflow.utils;

/**
 * CONSTANTS
 *
 * Purpose:
 * A single source of truth for all magic strings used across the app.
 * Never use string literals directly in code — always reference a constant from here.
 * This prevents typos and makes refactoring easier.
 *
 * Organization:
 * Constants are grouped into nested static classes by category.
 * Example usage: Constants.Firestore.COLLECTION_KEYWORDS
 *
 * Why a class instead of an interface?
 * Using a class with a private constructor prevents instantiation and is the
 * recommended pattern in Java for pure constant containers.
 */
public final class Constants {

    // Private constructor — this class should never be instantiated
    private Constants() {}

    // ─────────────────────────────────────────────────────────────────────────
    // FIRESTORE COLLECTION & DOCUMENT PATHS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * All Firestore collection names and document paths.
     * Structure:
     *   /users/{uid}/keywords/{keywordId}
     *   /users/{uid}/replies/{replyId}
     *   /users/{uid}/logs/{logId}
     *   /users/{uid}/settings/config
     */
    public static final class Firestore {

        private Firestore() {}

        /** Top-level collection containing one document per authenticated user */
        public static final String COLLECTION_USERS    = "users";

        /** Sub-collection of keyword rules under each user */
        public static final String COLLECTION_KEYWORDS = "keywords";

        /** Sub-collection of reply templates under each user */
        public static final String COLLECTION_REPLIES  = "replies";

        /** Sub-collection of activity logs under each user */
        public static final String COLLECTION_LOGS     = "logs";

        /** Sub-collection for user settings under each user */
        public static final String COLLECTION_SETTINGS = "settings";

        /**
         * The fixed document ID for the settings document.
         * Full path: /users/{uid}/settings/config
         */
        public static final String DOCUMENT_SETTINGS_CONFIG = "config";

        // Firestore field names — used in queries (e.g., .orderBy(FIELD_TIMESTAMP))
        public static final String FIELD_TIMESTAMP    = "timestamp";
        public static final String FIELD_CREATED_AT   = "createdAt";
        public static final String FIELD_UPDATED_AT   = "updatedAt";
        public static final String FIELD_IS_ENABLED   = "isEnabled";
        public static final String FIELD_PRIORITY     = "priority";
        public static final String FIELD_REPLY_ID     = "replyId";
        public static final String FIELD_STATUS       = "status";
        public static final String FIELD_PHONE_NUMBER = "phoneNumber";
        public static final String FIELD_CUSTOMER_NAME = "customerName";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SHARED PREFERENCES KEYS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Keys for SharedPreferences storage.
     * SharedPreferences is used for fast local access without a network call.
     */
    public static final class Prefs {

        private Prefs() {}

        /** Name of the SharedPreferences file */
        public static final String FILE_NAME = "orderflow_prefs";

        // Auth-related preferences
        public static final String KEY_REMEMBER_ME         = "pref_remember_me";
        public static final String KEY_SAVED_EMAIL         = "pref_saved_email";
        public static final String KEY_IS_LOGGED_IN        = "pref_is_logged_in";

        // Settings preferences (local cache of Firestore settings)
        public static final String KEY_AUTO_REPLY_ENABLED  = "pref_auto_reply_enabled";
        public static final String KEY_COOLDOWN_HOURS      = "pref_cooldown_hours";
        public static final String KEY_DARK_MODE_ENABLED   = "pref_dark_mode_enabled";
        public static final String KEY_WIZARD_COMPLETED    = "pref_wizard_completed";
        public static final String KEY_LAST_BACKUP_TIME    = "pref_last_backup_time";

        /**
         * Stores cooldown timestamps per customer.
         * Key format: "cooldown_{phoneNumber}"
         * Value: System.currentTimeMillis() when the last reply was sent.
         * Prefix used for lookup: Keys.COOLDOWN_PREFIX + phoneNumber
         */
        public static final String COOLDOWN_PREFIX = "cooldown_";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WHATSAPP BUSINESS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Package identifiers for WhatsApp variants.
     * Only WhatsApp Business notifications are processed — regular WhatsApp is ignored.
     */
    public static final class WhatsApp {

        private WhatsApp() {}

        /** WhatsApp Business app package name — the ONLY package we respond to */
        public static final String PACKAGE_WHATSAPP_BUSINESS = "com.whatsapp.w4b";

        /** Regular WhatsApp — we explicitly IGNORE notifications from this package */
        public static final String PACKAGE_WHATSAPP_REGULAR  = "com.whatsapp";

        /**
         * The resource ID name of the WhatsApp reply text input field.
         * Used by the Accessibility Service to find the message input box.
         * Note: This may change between WhatsApp Business versions.
         * If auto-reply stops working, this is the first thing to check.
         */
        public static final String REPLY_INPUT_RESOURCE_ID   = "com.whatsapp.w4b:id/entry";

        /**
         * The resource ID name of the WhatsApp send button.
         */
        public static final String SEND_BUTTON_RESOURCE_ID   = "com.whatsapp.w4b:id/send";

        /**
         * Maximum time (milliseconds) to wait for the WhatsApp chat to open
         * before the Accessibility Service gives up and logs an ERROR.
         */
        public static final long CHAT_OPEN_TIMEOUT_MS = 5000L;

        /**
         * Delay (milliseconds) between typing the reply and tapping Send.
         * A small delay ensures the text is fully entered before sending.
         */
        public static final long PRE_SEND_DELAY_MS = 500L;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTENT ACTIONS (for internal BroadcastReceiver communication)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Custom Intent actions used to communicate between services and activities.
     */
    public static final class Actions {

        private Actions() {}

        /** Sent by the Notification Listener when an incoming WA Business message is detected */
        public static final String ACTION_MESSAGE_RECEIVED =
                "com.orderflow.action.MESSAGE_RECEIVED";

        /** Sent by the Accessibility Service when a reply is successfully sent */
        public static final String ACTION_REPLY_SENT =
                "com.orderflow.action.REPLY_SENT";

        /** Sent when the user toggles auto-reply in Settings */
        public static final String ACTION_TOGGLE_AUTO_REPLY =
                "com.orderflow.action.TOGGLE_AUTO_REPLY";

        /** Extra key: the MessageLog object serialized as JSON */
        public static final String EXTRA_MESSAGE_LOG_JSON = "extra_message_log_json";

        /** Extra key: customer phone number (used in cooldown checks) */
        public static final String EXTRA_PHONE_NUMBER = "extra_phone_number";

        /** Extra key: reply text to send */
        public static final String EXTRA_REPLY_TEXT = "extra_reply_text";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI CONSTANTS
    // ─────────────────────────────────────────────────────────────────────────

    public static final class UI {

        private UI() {}

        /** Maximum length for a reply template (WhatsApp message length limit) */
        public static final int MAX_REPLY_LENGTH = 1000;

        /** Maximum number of trigger keywords per rule */
        public static final int MAX_KEYWORDS_PER_RULE = 20;

        /** Default priority for new keyword rules */
        public static final int DEFAULT_PRIORITY = 10;

        /** Min and max priority values (enforced by the priority input slider/field) */
        public static final int MIN_PRIORITY = 1;
        public static final int MAX_PRIORITY = 100;

        /** Animation durations in milliseconds */
        public static final int ANIM_DURATION_SHORT  = 150;
        public static final int ANIM_DURATION_MEDIUM = 300;
        public static final int ANIM_DURATION_LONG   = 500;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIREBASE STORAGE PATHS (for backup)
    // ─────────────────────────────────────────────────────────────────────────

    public static final class Storage {

        private Storage() {}

        /**
         * Storage path for user backup files.
         * Full path: /backups/{userId}/backup_{timestamp}.json
         */
        public static final String BACKUP_FOLDER = "backups/";
        public static final String BACKUP_FILE_PREFIX = "backup_";
        public static final String BACKUP_FILE_EXTENSION = ".json";
    }
}
