package com.orderflow;

import org.junit.Test;
import static org.junit.Assert.*;

import com.orderflow.data.model.MessageLog;

/**
 * MESSAGE LOG UNIT TESTS
 *
 * Tests display formatting and helper methods in MessageLog model.
 */
public class MessageLogTest {

    @Test
    public void testDisplayName_FallbackToPhone() {
        MessageLog log = new MessageLog(null, "+1234567890", "Hello", MessageLog.STATUS_NO_MATCH);
        assertEquals("+1234567890", log.getDisplayName());
    }

    @Test
    public void testDisplayName_FallbackToUnknown() {
        MessageLog log = new MessageLog("", "", "Hello", MessageLog.STATUS_NO_MATCH);
        assertEquals("Unknown Customer", log.getDisplayName());
    }

    @Test
    public void testIncomingMessagePreview_Truncation() {
        String longText = "This is a very long incoming message that exceeds sixty characters in length and should be truncated with an ellipsis.";
        MessageLog log = new MessageLog("Customer", null, longText, MessageLog.STATUS_NO_MATCH);
        
        String preview = log.getIncomingMessagePreview();
        assertTrue(preview.endsWith("…"));
        assertTrue(preview.length() <= 61);
    }
}
