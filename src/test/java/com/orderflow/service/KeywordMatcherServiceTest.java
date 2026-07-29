package com.orderflow.service;

import com.orderflow.entity.Keyword;
import com.orderflow.entity.Keyword.MatchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class KeywordMatcherServiceTest {

    private KeywordMatcherService matcherService;

    @BeforeEach
    void setUp() {
        matcherService = new KeywordMatcherService();
    }

    @Test
    @DisplayName("Should return false when input text or keyword is null")
    void testNullInputs() {
        assertFalse(matcherService.matches(null, Keyword.builder().pattern("order").build()));
        assertFalse(matcherService.matches("hello", null));
        assertFalse(matcherService.matches("hello", Keyword.builder().pattern(null).build()));
    }

    @ParameterizedTest
    @CsvSource({
        "WHERE IS MY ORDER, order, true",
        "Order status, ORDER, true",
        "tracking number, track, true",
        "hello world, order, false"
    })
    @DisplayName("Should match CONTAINS mode ignoring case")
    void testContainsModeIgnoreCase(String input, String pattern, boolean expected) {
        Keyword keyword = Keyword.builder()
                .pattern(pattern)
                .matchType(MatchType.CONTAINS)
                .ignoreCase(true)
                .build();

        assertEquals(expected, matcherService.matches(input, keyword));
    }

    @Test
    @DisplayName("Should match EXACT mode case sensitively when ignoreCase is false")
    void testExactModeCaseSensitive() {
        Keyword matchCase = Keyword.builder()
                .pattern("ORDER")
                .matchType(MatchType.EXACT)
                .ignoreCase(false)
                .build();

        assertTrue(matcherService.matches("ORDER", matchCase));
        assertFalse(matcherService.matches("order", matchCase));
    }

    @Test
    @DisplayName("Should match STARTS_WITH mode correctly")
    void testStartsWithMode() {
        Keyword kw = Keyword.builder()
                .pattern("START")
                .matchType(MatchType.STARTS_WITH)
                .ignoreCase(true)
                .build();

        assertTrue(matcherService.matches("Start processing now", kw));
        assertFalse(matcherService.matches("Please start now", kw));
    }

    @Test
    @DisplayName("Should match ENDS_WITH mode correctly")
    void testEndsWithMode() {
        Keyword kw = Keyword.builder()
                .pattern("help")
                .matchType(MatchType.ENDS_WITH)
                .ignoreCase(true)
                .build();

        assertTrue(matcherService.matches("I need some help", kw));
        assertFalse(matcherService.matches("help me please", kw));
    }

    @Test
    @DisplayName("Should match REGEX mode correctly")
    void testRegexMode() {
        Keyword kw = Keyword.builder()
                .pattern("^ORD-\\d{4}$")
                .matchType(MatchType.REGEX)
                .ignoreCase(true)
                .build();

        assertTrue(matcherService.matches("ORD-1234", kw));
        assertFalse(matcherService.matches("ORD-12345", kw));
        assertFalse(matcherService.matches("INVALID", kw));
    }

    @Test
    @DisplayName("Should gracefully handle invalid REGEX pattern without throwing exception")
    void testInvalidRegex() {
        Keyword kw = Keyword.builder()
                .pattern("[invalid(regex")
                .matchType(MatchType.REGEX)
                .ignoreCase(true)
                .build();

        assertFalse(matcherService.matches("test string", kw));
    }
}
