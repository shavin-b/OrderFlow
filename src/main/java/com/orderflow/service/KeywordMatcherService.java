package com.orderflow.service;

import com.orderflow.entity.Keyword;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Service responsible for evaluating keyword matching rules.
 * Supports CONTAINS, EXACT, STARTS_WITH, ENDS_WITH, and REGEX matching modes.
 */
@Service
@Slf4j
public class KeywordMatcherService {

    /**
     * Tests if an inbound text message matches a given Keyword rule.
     *
     * @param text    the raw message text from the user
     * @param keyword the keyword rule definition
     * @return true if the text satisfies the keyword match condition
     */
    public boolean matches(String text, Keyword keyword) {
        if (text == null || keyword == null || keyword.getPattern() == null) {
            return false;
        }

        String input = text.trim();
        String pattern = keyword.getPattern().trim();
        boolean ignoreCase = Boolean.TRUE.equals(keyword.getIgnoreCase());

        if (keyword.getMatchType() == Keyword.MatchType.REGEX) {
            return matchRegex(input, pattern, ignoreCase);
        }

        if (ignoreCase) {
            input = input.toLowerCase();
            pattern = pattern.toLowerCase();
        }

        return switch (keyword.getMatchType()) {
            case EXACT       -> input.equals(pattern);
            case CONTAINS    -> input.contains(pattern);
            case STARTS_WITH -> input.startsWith(pattern);
            case ENDS_WITH   -> input.endsWith(pattern);
            default          -> false;
        };
    }

    private boolean matchRegex(String input, String patternStr, boolean ignoreCase) {
        try {
            int flags = ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0;
            Pattern pattern = Pattern.compile(patternStr, flags);
            return pattern.matcher(input).find();
        } catch (PatternSyntaxException e) {
            log.warn("Invalid regex pattern syntax '{}': {}", patternStr, e.getMessage());
            return false;
        }
    }
}
