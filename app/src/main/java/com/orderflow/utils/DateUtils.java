package com.orderflow.utils;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * DATE UTILS
 *
 * Purpose:
 * Centralized date and time formatting helpers.
 * All date/time rendering in the app uses these methods for consistency.
 *
 * Why centralize date formatting?
 * - Ensures consistent date display across all screens (logs, stats, settings)
 * - Prevents format string duplication (reducing bugs from inconsistent patterns)
 * - Easy to change the date format for all screens in one place
 *
 * Note on thread safety:
 * SimpleDateFormat is NOT thread-safe. Each method creates a new instance
 * rather than sharing a static field — this is intentional.
 */
public final class DateUtils {

    // Private constructor — utility class, not meant to be instantiated
    private DateUtils() {}

    // ─────────────────────────────────────────────────────────────────────────
    // FORMAT PATTERNS
    // ─────────────────────────────────────────────────────────────────────────

    /** Full date + time for log entry detail view: "Jul 23, 2024 • 10:45 AM" */
    private static final String PATTERN_FULL       = "MMM dd, yyyy • hh:mm a";

    /** Date only for log list items: "Jul 23, 2024" */
    private static final String PATTERN_DATE_ONLY  = "MMM dd, yyyy";

    /** Time only for notification/activity feeds: "10:45 AM" */
    private static final String PATTERN_TIME_ONLY  = "hh:mm a";

    /** Compact date for chart labels: "Jul 23" */
    private static final String PATTERN_SHORT_DATE = "MMM dd";

    /** For backup file names: "20240723_104500" */
    private static final String PATTERN_FILE_STAMP = "yyyyMMdd_HHmmss";

    /** For display in Settings "Last Backup": "Jul 23, 2024 at 10:45 AM" */
    private static final String PATTERN_BACKUP     = "MMM dd, yyyy 'at' hh:mm a";

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC FORMAT METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Formats a Firestore Timestamp into a full date + time string.
     * Example output: "Jul 23, 2024 • 10:45 AM"
     * Used in: Log detail view
     *
     * @param timestamp Firestore Timestamp (may be null before server sets it)
     * @return Formatted string, or "—" if timestamp is null
     */
    public static String formatFull(Timestamp timestamp) {
        if (timestamp == null) return "—";
        return format(timestamp.toDate(), PATTERN_FULL);
    }

    /**
     * Formats a Firestore Timestamp to date only.
     * Example output: "Jul 23, 2024"
     * Used in: Log list date separators, filter labels
     *
     * @param timestamp Firestore Timestamp
     * @return Formatted date string
     */
    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "—";
        return format(timestamp.toDate(), PATTERN_DATE_ONLY);
    }

    /**
     * Formats a Firestore Timestamp to time only.
     * Example output: "10:45 AM"
     * Used in: Log list item subtitles, recent activity feed
     *
     * @param timestamp Firestore Timestamp
     * @return Formatted time string
     */
    public static String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "—";
        return format(timestamp.toDate(), PATTERN_TIME_ONLY);
    }

    /**
     * Returns a relative or absolute time description for recent events.
     * Logic:
     *   - Less than 1 minute ago → "Just now"
     *   - Less than 60 minutes ago → "X minutes ago"
     *   - Less than 24 hours ago → "X hours ago"
     *   - Older than 24 hours → Full date (e.g., "Jul 22, 2024")
     *
     * Used in: Dashboard recent activity feed, log list items
     *
     * @param timestamp Firestore Timestamp
     * @return Relative or absolute time string
     */
    public static String formatRelative(Timestamp timestamp) {
        if (timestamp == null) return "—";

        long nowMillis = System.currentTimeMillis();
        long thenMillis = timestamp.toDate().getTime();
        long diffMillis = nowMillis - thenMillis;

        if (diffMillis < 0) {
            // Timestamp is in the future (clock skew) — show "Just now"
            return "Just now";
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
        long hours   = TimeUnit.MILLISECONDS.toHours(diffMillis);
        long days    = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (minutes < 1)   return "Just now";
        if (minutes < 60)  return minutes + "m ago";
        if (hours < 24)    return hours + "h ago";
        if (days < 7)      return days + "d ago";

        // Older than 7 days — show the actual date
        return format(timestamp.toDate(), PATTERN_DATE_ONLY);
    }

    /**
     * Formats a timestamp for use as a backup file name suffix.
     * Example output: "20240723_104500"
     * Used in: Firebase Storage file naming during backup
     *
     * @return Current time formatted as a file-safe timestamp string
     */
    public static String formatFileTimestamp() {
        return format(new Date(), PATTERN_FILE_STAMP);
    }

    /**
     * Formats a timestamp for display in the Settings "Last Backup" row.
     * Example output: "Jul 23, 2024 at 10:45 AM"
     *
     * @param isoTimestamp ISO 8601 timestamp string stored in UserSettings
     * @return Formatted string, or "Never" if null/empty
     */
    public static String formatBackupTime(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isEmpty()) return "Never";
        try {
            // Parse ISO 8601 format
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            Date date = isoFormat.parse(isoTimestamp);
            if (date == null) return "Never";
            return format(date, PATTERN_BACKUP);
        } catch (Exception e) {
            return "Never";
        }
    }

    /**
     * Returns the current ISO 8601 timestamp string.
     * Used when saving a backup timestamp to UserSettings.
     *
     * @return Current time as ISO 8601 string: "2024-07-23T08:30:00Z"
     */
    public static String getCurrentIsoTimestamp() {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return isoFormat.format(new Date());
    }

    /**
     * Checks if a given Firestore Timestamp falls within "today" (current calendar day).
     * Used in: Dashboard "Today's Messages" and "Today's Auto Replies" counters.
     *
     * @param timestamp Firestore Timestamp to check
     * @return true if the timestamp is from today
     */
    public static boolean isToday(Timestamp timestamp) {
        if (timestamp == null) return false;

        Calendar today = Calendar.getInstance();
        Calendar tsCalendar = Calendar.getInstance();
        tsCalendar.setTime(timestamp.toDate());

        return today.get(Calendar.YEAR)         == tsCalendar.get(Calendar.YEAR)
            && today.get(Calendar.DAY_OF_YEAR)  == tsCalendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Checks if a Firestore Timestamp falls within the current calendar week (Mon-Sun).
     * Used in: Statistics weekly report.
     *
     * @param timestamp Firestore Timestamp to check
     * @return true if the timestamp is from this week
     */
    public static boolean isThisWeek(Timestamp timestamp) {
        if (timestamp == null) return false;

        Calendar today = Calendar.getInstance();
        Calendar tsCalendar = Calendar.getInstance();
        tsCalendar.setTime(timestamp.toDate());

        return today.get(Calendar.YEAR)         == tsCalendar.get(Calendar.YEAR)
            && today.get(Calendar.WEEK_OF_YEAR) == tsCalendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Checks if a Firestore Timestamp falls within the current calendar month.
     * Used in: Statistics monthly report.
     *
     * @param timestamp Firestore Timestamp to check
     * @return true if the timestamp is from this month
     */
    public static boolean isThisMonth(Timestamp timestamp) {
        if (timestamp == null) return false;

        Calendar today = Calendar.getInstance();
        Calendar tsCalendar = Calendar.getInstance();
        tsCalendar.setTime(timestamp.toDate());

        return today.get(Calendar.YEAR)  == tsCalendar.get(Calendar.YEAR)
            && today.get(Calendar.MONTH) == tsCalendar.get(Calendar.MONTH);
    }

    /**
     * Returns the start of the current day (midnight) as a Firestore Timestamp.
     * Used in Firestore queries to filter logs from today:
     * .whereGreaterThan(FIELD_TIMESTAMP, DateUtils.startOfToday())
     *
     * @return Firestore Timestamp at midnight of today
     */
    public static Timestamp startOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTime());
    }

    /**
     * Returns the start of the current week (Monday midnight) as a Firestore Timestamp.
     */
    public static Timestamp startOfThisWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTime());
    }

    /**
     * Returns the start of the current month (1st day, midnight) as a Firestore Timestamp.
     */
    public static Timestamp startOfThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTime());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Core formatting method. Creates a new SimpleDateFormat per call (thread-safe).
     *
     * @param date    Java Date object to format
     * @param pattern SimpleDateFormat pattern string
     * @return Formatted date string
     */
    private static String format(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }
}
