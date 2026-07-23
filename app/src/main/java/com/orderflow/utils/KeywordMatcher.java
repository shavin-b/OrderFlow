package com.orderflow.utils;

import com.orderflow.data.model.Keyword;

import java.util.List;

/**
 * KEYWORD MATCHER ENGINE
 *
 * Purpose:
 * Evaluates an incoming WhatsApp message against a given Keyword rule
 * to determine if it is a match, based on the match type (EXACT vs PARTIAL).
 */
public class KeywordMatcher {

    /**
     * Checks if the incoming message matches the given keyword rule.
     *
     * @param rule    The Keyword rule containing a list of trigger words.
     * @param message The full text of the incoming WhatsApp message.
     * @return The specific trigger string that matched, or null if no match.
     */
    public static String findMatch(Keyword rule, String message) {
        if (rule == null || message == null || message.trim().isEmpty()) {
            return null;
        }

        List<String> triggers = rule.getKeywords();
        if (triggers == null || triggers.isEmpty()) {
            return null;
        }

        // Normalize the message (case-insensitive)
        String normalizedMessage = message.trim().toLowerCase();

        for (String trigger : triggers) {
            String normalizedTrigger = trigger.trim().toLowerCase();
            
            if (normalizedTrigger.isEmpty()) continue;

            if (Keyword.MATCH_TYPE_EXACT.equals(rule.getMatchType())) {
                // EXACT match: The entire message must be exactly the trigger word.
                if (normalizedMessage.equals(normalizedTrigger)) {
                    return trigger; // Return original trigger to log exactly what matched
                }
            } else {
                // PARTIAL match: The trigger word must be found *within* the message.
                // We add word boundaries around the trigger so that a trigger for "car" 
                // matches "I need a car" but does NOT match "I need a carpet".
                
                // Using regex for whole-word matching. \b means word boundary.
                // We use Pattern/Matcher implicitly via String.matches().
                // Escape special characters in the trigger just in case.
                String escapedTrigger = java.util.regex.Pattern.quote(normalizedTrigger);
                
                // Regex: (^|\W)escapedTrigger($|\W)
                // This means the trigger must be at the start of the string OR preceded by a non-word character (like a space),
                // AND it must be at the end of the string OR followed by a non-word character.
                String regex = "(^|.*\\W)" + escapedTrigger + "(\\W.*|$)";
                
                if (normalizedMessage.matches(regex)) {
                    return trigger;
                }
            }
        }

        return null;
    }
}
