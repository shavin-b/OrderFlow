package com.orderflow;

import org.junit.Test;
import static org.junit.Assert.*;

import com.orderflow.data.model.Keyword;
import com.orderflow.utils.KeywordMatcher;

import java.util.Arrays;

/**
 * KEYWORD MATCHER UNIT TESTS
 *
 * Tests the core text-matching engine for both EXACT and PARTIAL rules.
 */
public class KeywordMatcherTest {

    @Test
    public void testExactMatch_Success() {
        Keyword rule = new Keyword();
        rule.setMatchType(Keyword.MATCH_TYPE_EXACT);
        rule.setKeywords(Arrays.asList("price", "cost", "how much"));

        // Exact match should pass
        String matched = KeywordMatcher.findMatch(rule, "price");
        assertEquals("price", matched);

        // Case insensitive check
        matched = KeywordMatcher.findMatch(rule, "HOW MUCH");
        assertEquals("how much", matched);
    }

    @Test
    public void testExactMatch_FailureWhenExtraWords() {
        Keyword rule = new Keyword();
        rule.setMatchType(Keyword.MATCH_TYPE_EXACT);
        rule.setKeywords(Arrays.asList("price", "cost"));

        // Extra words should fail exact match
        String matched = KeywordMatcher.findMatch(rule, "what is the price");
        assertNull(matched);
    }

    @Test
    public void testPartialMatch_Success() {
        Keyword rule = new Keyword();
        rule.setMatchType(Keyword.MATCH_TYPE_PARTIAL);
        rule.setKeywords(Arrays.asList("order", "buy", "pizza"));

        // Sentence containing keyword should match
        String matched = KeywordMatcher.findMatch(rule, "Hello, I would like to order a pizza please");
        assertNotNull(matched);
    }

    @Test
    public void testPartialMatch_WordBoundarySafety() {
        Keyword rule = new Keyword();
        rule.setMatchType(Keyword.MATCH_TYPE_PARTIAL);
        rule.setKeywords(Arrays.asList("car"));

        // Whole word "car" should match
        String matched = KeywordMatcher.findMatch(rule, "I need a car today");
        assertEquals("car", matched);

        // Substring "carpet" should NOT match "car"
        matched = KeywordMatcher.findMatch(rule, "I need a carpet today");
        assertNull(matched);
    }
}
