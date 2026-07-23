package com.orderflow.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;

/**
 * KEYWORD MODEL
 *
 * Purpose:
 * Represents a single "keyword rule" — a set of trigger words that,
 * when matched in an incoming WhatsApp Business message, will trigger
 * an automatic reply using the linked reply template.
 *
 * Firestore Collection Path:
 * /users/{userId}/keywords/{keywordId}
 *
 * Architecture:
 * This is a pure POJO (Plain Old Java Object) — no Android dependencies.
 * Firestore uses reflection to map document fields to this class, so:
 * - All fields must be public OR have public getters+setters.
 * - A no-argument constructor is required by Firestore.
 * - The @DocumentId annotation tells Firestore to auto-populate the 'id' field
 *   with the document's Firestore document ID.
 *
 * Example:
 * A keyword rule for "pricing inquiries" might have:
 *   keywords = ["price", "cost", "amount", "how much", "rate"]
 *   replyId  = "abc123" (references a document in /replies collection)
 *   priority = 1 (highest priority, checked first)
 *   matchType = PARTIAL (message only needs to CONTAIN the keyword, not equal it)
 *   isEnabled = true
 */
public class Keyword {

    /**
     * Match type constants.
     * PARTIAL: the incoming message only needs to CONTAIN the keyword anywhere.
     *          Example: keyword="price" matches "What is the price of item A?"
     * EXACT:   the incoming message must equal the keyword exactly (case-insensitive).
     *          Example: keyword="price" only matches "price" — not "the price"
     */
    public static final String MATCH_TYPE_PARTIAL = "PARTIAL";
    public static final String MATCH_TYPE_EXACT   = "EXACT";

    // ─────────────────────────────────────────────────────────────────────────
    // FIELDS (all public so Firestore can map them without needing getters)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Auto-populated by Firestore with the document's unique ID.
     * Never set this manually — Firestore handles it.
     */
    @DocumentId
    private String id;

    /**
     * List of trigger words/phrases.
     * Any one of these, when found in an incoming message, triggers this rule.
     * Example: ["price", "cost", "amount", "how much"]
     * Stored as a Firestore array field.
     */
    private List<String> keywords;

    /**
     * The Firestore document ID of the Reply template to send when this rule matches.
     * References /users/{userId}/replies/{replyId}
     */
    private String replyId;

    /**
     * Priority determines which rule wins when multiple rules match the same message.
     * Lower number = higher priority. Rule with priority 1 is checked before priority 2.
     * Range: 1 to 100 (enforced by the UI).
     */
    private int priority;

    /**
     * Whether this keyword rule is active.
     * Disabled rules are stored in Firestore but skipped during keyword matching.
     */
    private boolean isEnabled;

    /**
     * How to match the keyword: "PARTIAL" or "EXACT".
     * See MATCH_TYPE_PARTIAL and MATCH_TYPE_EXACT constants above.
     */
    private String matchType;

    /**
     * Server-generated creation timestamp.
     * @ServerTimestamp tells Firestore to set this to the server time when
     * the document is first created — we never set this manually.
     */
    @ServerTimestamp
    private Timestamp createdAt;

    /**
     * Last updated timestamp — set manually whenever the keyword is edited.
     */
    private Timestamp updatedAt;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Required no-arg constructor for Firestore deserialization.
     * Firestore creates instances of this class using reflection, so a
     * no-argument constructor must always exist.
     */
    public Keyword() {
        this.keywords = new ArrayList<>();
        this.isEnabled = true;
        this.matchType = MATCH_TYPE_PARTIAL;
        this.priority = 10;
    }

    /**
     * Convenience constructor for creating a new keyword rule programmatically
     * (used in the ViewModel when saving from the Add Keyword screen).
     *
     * @param keywords  List of trigger words/phrases
     * @param replyId   Firestore ID of the linked reply template
     * @param priority  Matching priority (1 = highest)
     * @param matchType "PARTIAL" or "EXACT"
     */
    public Keyword(List<String> keywords, String replyId, int priority, String matchType) {
        this.keywords  = keywords != null ? keywords : new ArrayList<>();
        this.replyId   = replyId;
        this.priority  = priority;
        this.matchType = matchType;
        this.isEnabled = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GETTERS & SETTERS
    // ─────────────────────────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getReplyId() { return replyId; }
    public void setReplyId(String replyId) { this.replyId = replyId; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    @com.google.firebase.firestore.PropertyName("enabled")
    public boolean isEnabled() { return isEnabled; }
    @com.google.firebase.firestore.PropertyName("enabled")
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITY METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a comma-separated display string of all trigger words.
     * Used in the RecyclerView list item to show all triggers in one line.
     * Example: "price, cost, amount, how much"
     */
    @com.google.firebase.firestore.Exclude
    public String getKeywordsDisplayText() {
        if (keywords == null || keywords.isEmpty()) return "";
        return String.join(", ", keywords);
    }

    /**
     * Checks if this is a partial match type.
     */
    @com.google.firebase.firestore.Exclude
    public boolean isPartialMatch() {
        return MATCH_TYPE_PARTIAL.equals(matchType);
    }

    @Override
    public String toString() {
        return "Keyword{id='" + id + "', keywords=" + keywords +
                ", replyId='" + replyId + "', priority=" + priority +
                ", isEnabled=" + isEnabled + ", matchType='" + matchType + "'}";
    }
}
