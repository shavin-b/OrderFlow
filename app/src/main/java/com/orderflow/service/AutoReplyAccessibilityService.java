package com.orderflow.service;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.orderflow.data.local.SharedPreferencesManager;
import com.orderflow.data.repository.LogRepository;

import java.util.List;

/**
 * AUTO REPLY ACCESSIBILITY SERVICE
 *
 * Simulates user UI interactions to automatically paste and send the reply
 * on the WhatsApp Business chat screen.
 */
public class AutoReplyAccessibilityService extends AccessibilityService {

    private static final String TAG = "AutoReplyService";
    private LogRepository logRepository;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        logRepository = new LogRepository();
        Log.d(TAG, "Accessibility Service Connected!");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // We only care when the window state changes (e.g., chat screen opens)
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        // Check if we have a pending reply task waiting to be executed
        ReplyQueue.ReplyTask task = ReplyQueue.getInstance().getPendingTask();
        if (task == null) {
            return; 
        }

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }

        Log.d(TAG, "Executing pending reply task...");

        // 1. Find the Text Input field (EditText)
        AccessibilityNodeInfo inputNode = findNodeByClassName(rootNode, "android.widget.EditText");
        if (inputNode == null) {
            Log.e(TAG, "Could not find text input field.");
            return;
        }

        // 2. Paste the reply content
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, task.replyContent);
        boolean textSet = inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

        if (!textSet) {
            Log.e(TAG, "Failed to set text in the input field.");
            return;
        }
        
        Log.d(TAG, "Text pasted successfully.");

        // Wait a tiny bit for the Send button to appear (UI update)
        try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }

        // Refresh root node since UI changed (Send button replaces Voice button)
        rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // 3. Find and Click the Send Button
        // WhatsApp usually sets the contentDescription of the send button to "Send"
        AccessibilityNodeInfo sendNode = findNodeByContentDescription(rootNode, "Send");
        if (sendNode == null) {
            Log.e(TAG, "Could not find the Send button.");
            // Optional: fallback to searching by view ID if needed in the future
            return;
        }

        boolean clicked = sendNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        if (clicked) {
            Log.i(TAG, "Send button clicked! Reply sent successfully.");
            
            // 4. Save the completed log to Firestore
            if (task.partialLog != null && logRepository != null) {
                logRepository.addLog(task.partialLog, new LogRepository.OperationCallback() {
                    @Override
                    public void onSuccess(String logId) {
                        Log.d(TAG, "Successfully logged the sent reply.");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to log sent reply: " + errorMessage);
                    }
                });
                
                // --- PHASE 9: COOLDOWN TRACKING ---
                // Record the time this reply was sent so we don't spam this customer
                SharedPreferencesManager.getInstance()
                        .saveCooldownTimestamp(task.partialLog.getCustomerName(), System.currentTimeMillis());
            }

            // 5. Clear the queue so we don't spam
            ReplyQueue.getInstance().clear();

            // 6. Press the hardware BACK button to return to the background
            try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
            performGlobalAction(GLOBAL_ACTION_BACK);
            try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
            performGlobalAction(GLOBAL_ACTION_BACK); // Double back to ensure we leave the app

        } else {
            Log.e(TAG, "Failed to click the Send button.");
        }
    }

    /**
     * Recursively searches the node tree for the first node matching the class name.
     */
    private AccessibilityNodeInfo findNodeByClassName(AccessibilityNodeInfo node, String className) {
        if (node == null) return null;
        if (node.getClassName() != null && node.getClassName().toString().equals(className)) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo result = findNodeByClassName(node.getChild(i), className);
            if (result != null) return result;
        }
        return null;
    }

    /**
     * Recursively searches the node tree for a node matching the content description.
     */
    private AccessibilityNodeInfo findNodeByContentDescription(AccessibilityNodeInfo node, String desc) {
        if (node == null) return null;
        if (node.getContentDescription() != null && 
            node.getContentDescription().toString().equalsIgnoreCase(desc)) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo result = findNodeByContentDescription(node.getChild(i), desc);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Service interrupted.");
    }
}
