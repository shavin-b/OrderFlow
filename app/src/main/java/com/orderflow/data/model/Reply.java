package com.orderflow.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * REPLY MODEL
 *
 * Purpose:
 * Represents a reply template — a pre-written message that is automatically
 * sent to customers when a matching keyword rule is triggered.
 *
 * Firestore Collection Path:
 * /users/{userId}/replies/{replyId}
 *
 * Architecture:
 * Pure POJO with Firestore annotations. Multiple Keyword documents can reference
 * the same Reply document using the replyId field — this is the one-reply-to-many-
 * keywords relationship in the data model.
 *
 * Example:
 * A reply template for "Price List" might contain:
 *   title   = "Price List Reply"
 *   content = "Thank you for contacting us! Here is our current price list:\n
 *              - Product A: $10\n- Product B: $25\n..."
 *   isEnabled = true
 */
public class Reply {

    // ─────────────────────────────────────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Firestore document ID — auto-populated, never set manually.
     */
    @DocumentId
    private String id;

    /**
     * A short human-readable name for this template.
     * Shown in the Reply Manager list and in the Keyword editor dropdown.
     * Example: "Price List Reply", "Welcome Message", "Working Hours"
     */
    private String title;

    /**
     * The actual reply text that will be typed and sent in WhatsApp Business.
     * Supports newlines (\n) and Unicode emoji.
     * Maximum recommended length: 1000 characters (WhatsApp message limit).
     */
    private String content;

    /**
     * Whether this reply template is active.
     * Disabled templates cannot be selected when adding new keyword rules,
     * and existing keyword rules linked to a disabled template will not fire.
     */
    private boolean isEnabled;

    /**
     * Server timestamp of when this template was first created.
     */
    @ServerTimestamp
    private Timestamp createdAt;

    /**
     * Timestamp of the last edit — updated manually in the repository
     * whenever the content or title changes.
     */
    private Timestamp updatedAt;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Required no-arg constructor for Firestore deserialization.
     */
    public Reply() {
        this.isEnabled = true;
    }

    /**
     * Convenience constructor for creating a new reply template.
     *
     * @param title   Human-readable template name
     * @param content The reply message text
     */
    public Reply(String title, String content) {
        this.title     = title;
        this.content   = content;
        this.isEnabled = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GETTERS & SETTERS
    // ─────────────────────────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    @com.google.firebase.firestore.PropertyName("enabled")
    public boolean isEnabled() { return isEnabled; }
    @com.google.firebase.firestore.PropertyName("enabled")
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITY METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a truncated preview of the reply content for display in list items.
     * Keeps only the first 80 characters to fit in a card subtitle.
     *
     * @param maxLength Maximum characters before truncating with "…"
     * @return Truncated preview string
     */
    @com.google.firebase.firestore.Exclude
    public String getContentPreview(int maxLength) {
        if (content == null || content.isEmpty()) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength).trim() + "…";
    }

    /**
     * Convenience method — returns a standard 80-char preview.
     */
    @com.google.firebase.firestore.Exclude
    public String getContentPreview() {
        return getContentPreview(80);
    }

    /**
     * Returns the character count of the content for the UI counter.
     */
    @com.google.firebase.firestore.Exclude
    public int getCharacterCount() {
        return content != null ? content.length() : 0;
    }

    @Override
    public String toString() {
        return "Reply{id='" + id + "', title='" + title + "', isEnabled=" + isEnabled + "}";
    }
}
