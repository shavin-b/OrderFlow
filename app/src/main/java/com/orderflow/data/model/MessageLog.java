package com.orderflow.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * MESSAGE LOG MODEL
 *
 * Purpose:
 * Records every incoming WhatsApp Business message that the app processes —
 * whether it resulted in an auto-reply, was skipped (no keyword match),
 * or was blocked by the cooldown period.
 *
 * Firestore Collection Path:
 * /users/{userId}/logs/{logId}
 *
 * Why "MessageLog" instead of "Log"?
 * The class is named MessageLog (not Log) to avoid a naming conflict with
 * android.util.Log — a system class used throughout the Android SDK.
 *
 * Architecture:
 * Pure POJO with Firestore annotations.
 * Logs are append-only — they are never edited, only created or deleted.
 * The Statistics screen reads from this collection to calculate daily/weekly metrics.
 *
 * Log Entry Lifecycle:
 * 1. WhatsAppNotificationListener detects an incoming message → creates a pending log
 * 2. KeywordMatcher checks the message → updates log with matched keyword info
 * 3. AutoReplyAccessibilityService sends the reply → updates log status to REPLIED
 *    or sets status to COOLDOWN / NO_MATCH if the reply was not sent
 */
public class MessageLog {

    /**
     * STATUS CONSTANTS
     *
     * REPLIED:   The keyword matched and the reply was successfully sent.
     * NO_MATCH:  No keyword matched the incoming message — no reply sent.
     * COOLDOWN:  A keyword matched, but the cooldown period for this customer
     *            has not expired yet — reply skipped to avoid spamming.
     * ERROR:     An unexpected error occurred while attempting to send the reply.
     */
    public static final String STATUS_REPLIED  = "REPLIED";
    public static final String STATUS_NO_MATCH = "NO_MATCH";
    public static final String STATUS_COOLDOWN = "COOLDOWN";
    public static final String STATUS_ERROR    = "ERROR";

    // ─────────────────────────────────────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Firestore document ID — auto-generated, never set manually.
     */
    @DocumentId
    private String id;

    /**
     * The name of the customer as shown in the WhatsApp notification.
     * Extracted from the notification's title text.
     * May be a contact name (if saved) or a phone number.
     */
    private String customerName;

    /**
     * The customer's WhatsApp phone number.
     * May be null if the contact is not saved in the device's address book
     * and WhatsApp doesn't expose it in the notification.
     */
    private String phoneNumber;

    /**
     * The full text of the incoming WhatsApp message.
     * Extracted from the notification's body text.
     */
    private String incomingMessage;

    /**
     * The specific keyword that triggered this log entry.
     * This is the single keyword (from the Keyword.keywords list) that was
     * found in the incoming message.
     * Null if status is NO_MATCH.
     */
    private String matchedKeyword;

    /**
     * The Firestore document ID of the Keyword rule that was triggered.
     * Null if status is NO_MATCH.
     */
    private String keywordId;

    /**
     * The full text of the reply that was sent (or attempted).
     * Null if status is NO_MATCH.
     */
    private String replySent;

    /**
     * The Firestore document ID of the Reply template that was used.
     * Null if status is NO_MATCH.
     */
    private String replyId;

    /**
     * The outcome of processing this message.
     * One of: REPLIED, NO_MATCH, COOLDOWN, ERROR.
     */
    private String status;

    /**
     * If status is ERROR, this field contains the error description.
     * Helps diagnose issues (e.g., "Accessibility service not running").
     */
    private String errorMessage;

    /**
     * Server-generated timestamp of when this log entry was created.
     * Used for: date filtering, statistics calculations, cooldown tracking.
     */
    @ServerTimestamp
    private Timestamp timestamp;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Required no-arg constructor for Firestore deserialization.
     */
    public MessageLog() {}

    /**
     * Full constructor used by the Notification Listener when creating a log entry.
     *
     * @param customerName    Name from notification title
     * @param phoneNumber     Phone number (may be null)
     * @param incomingMessage Message body text
     * @param status          Initial status (usually NO_MATCH until keyword is checked)
     */
    public MessageLog(String customerName, String phoneNumber,
                      String incomingMessage, String status) {
        this.customerName    = customerName;
        this.phoneNumber     = phoneNumber;
        this.incomingMessage = incomingMessage;
        this.status          = status;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GETTERS & SETTERS
    // ─────────────────────────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getIncomingMessage() { return incomingMessage; }
    public void setIncomingMessage(String incomingMessage) { this.incomingMessage = incomingMessage; }

    public String getMatchedKeyword() { return matchedKeyword; }
    public void setMatchedKeyword(String matchedKeyword) { this.matchedKeyword = matchedKeyword; }

    public String getKeywordId() { return keywordId; }
    public void setKeywordId(String keywordId) { this.keywordId = keywordId; }

    public String getReplySent() { return replySent; }
    public void setReplySent(String replySent) { this.replySent = replySent; }

    public String getReplyId() { return replyId; }
    public void setReplyId(String replyId) { this.replyId = replyId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITY METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if this log entry represents a successfully sent reply.
     */
    @com.google.firebase.firestore.Exclude
    public boolean isReplied() {
        return STATUS_REPLIED.equals(status);
    }

    /**
     * Returns true if the message was skipped due to cooldown.
     */
    @com.google.firebase.firestore.Exclude
    public boolean isCooldown() {
        return STATUS_COOLDOWN.equals(status);
    }

    /**
     * Returns true if no keyword matched this message.
     */
    @com.google.firebase.firestore.Exclude
    public boolean isNoMatch() {
        return STATUS_NO_MATCH.equals(status);
    }

    /**
     * Returns a display-friendly version of the customer name.
     * Falls back to phone number if name is unavailable.
     */
    @com.google.firebase.firestore.Exclude
    public String getDisplayName() {
        if (customerName != null && !customerName.isEmpty()) return customerName;
        if (phoneNumber != null && !phoneNumber.isEmpty()) return phoneNumber;
        return "Unknown Customer";
    }

    /**
     * Returns a truncated version of the incoming message for list item display.
     */
    @com.google.firebase.firestore.Exclude
    public String getIncomingMessagePreview() {
        if (incomingMessage == null || incomingMessage.isEmpty()) return "";
        if (incomingMessage.length() <= 60) return incomingMessage;
        return incomingMessage.substring(0, 60).trim() + "…";
    }

    @Override
    public String toString() {
        return "MessageLog{id='" + id + "', customer='" + customerName +
                "', status='" + status + "', timestamp=" + timestamp + "}";
    }
}
