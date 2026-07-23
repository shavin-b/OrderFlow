package com.orderflow.service;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.orderflow.data.model.Keyword;
import com.orderflow.data.model.MessageLog;
import com.orderflow.data.model.Reply;
import com.orderflow.data.local.SharedPreferencesManager;
import com.orderflow.data.repository.KeywordRepository;
import com.orderflow.data.repository.LogRepository;
import com.orderflow.data.repository.ReplyRepository;
import com.orderflow.utils.KeywordMatcher;

import java.util.List;

/**
 * WHATSAPP NOTIFICATION LISTENER
 *
 * Listens to device notifications, filters for WhatsApp Business, 
 * extracts the customer name and message content.
 */
public class WhatsAppNotificationListener extends NotificationListenerService {

    private static final String TAG = "WhatsAppListener";
    private static final String WA_BUSINESS_PACKAGE = "com.whatsapp.w4b";
    
    private LogRepository logRepository;
    private KeywordRepository keywordRepository;
    private ReplyRepository replyRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        logRepository = new LogRepository();
        keywordRepository = new KeywordRepository();
        replyRepository = new ReplyRepository();
        Log.d(TAG, "WhatsAppNotificationListener created.");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getPackageName() == null) return;

        // 1. Package Filtering: Only process WhatsApp Business notifications
        if (!sbn.getPackageName().equals(WA_BUSINESS_PACKAGE)) {
            return; 
        }

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        Bundle extras = notification.extras;
        if (extras == null) return;

        // 2. Extract Customer Name (Title) and Message Content (Text)
        String customerName = extras.getString(Notification.EXTRA_TITLE);
        CharSequence textSequence = extras.getCharSequence(Notification.EXTRA_TEXT);
        String messageBody = textSequence != null ? textSequence.toString() : null;

        if (customerName == null || messageBody == null || messageBody.isEmpty()) {
            return;
        }

        // 3. Ignore system/sync notifications (e.g. "Checking for new messages", "WhatsApp Web is active")
        if (customerName.equals("WhatsApp Business") || messageBody.contains("new messages")) {
            return;
        }

        // 4. Ignore Group Chats (Optional, but usually a good idea for auto-replies)
        // Group chat titles usually contain a colon (e.g., "Group Name: John")
        if (customerName.contains(":")) {
            Log.d(TAG, "Ignored group chat message from: " + customerName);
            return;
        }
        
        // 5. Ignore outgoing messages (e.g. "You: Hello")
        if (messageBody.startsWith("You: ")) {
            Log.d(TAG, "Ignored outgoing message.");
            return;
        }

        Log.i(TAG, "Incoming WA Business Message! Customer: " + customerName + " | Msg: " + messageBody);

        // Make sure user is logged in before hitting Firestore
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "User not logged in. Cannot process message.");
            return;
        }

        SharedPreferencesManager prefs = SharedPreferencesManager.getInstance();

        // --- PHASE 11 PREVIEW: Check Master Switch ---
        if (!prefs.isAutoReplyEnabled()) {
            Log.d(TAG, "Auto-reply is globally disabled in Settings. Ignoring message.");
            // We can optionally log it, but usually if it's disabled, we don't spam the logs.
            return;
        }

        // --- PHASE 9: Cooldown System ---
        int cooldownHours = prefs.getCooldownHours();
        long cooldownMillis = (long) cooldownHours * 60 * 60 * 1000L;
        
        // We use customerName as the unique key since phone numbers aren't always exposed by WhatsApp
        if (!prefs.isCooldownExpired(customerName, cooldownMillis)) {
            Log.i(TAG, "Customer '" + customerName + "' is in cooldown period. Skipping reply.");
            
            MessageLog log = new MessageLog(customerName, null, messageBody, MessageLog.STATUS_COOLDOWN);
            saveLog(log);
            return;
        }

        // --- PHASE 7: Keyword Matching Engine ---
        Log.d(TAG, "Querying active keywords for matching...");
        keywordRepository.getActiveKeywords(new KeywordRepository.KeywordListCallback() {
            @Override
            public void onDataLoaded(List<Keyword> keywords) {
                if (keywords == null || keywords.isEmpty()) {
                    Log.d(TAG, "No active keywords found. Logging as NO_MATCH.");
                    saveLog(new MessageLog(customerName, null, messageBody, MessageLog.STATUS_NO_MATCH));
                    return;
                }

                boolean matchFound = false;

                // Loop through keywords (already ordered by priority)
                for (Keyword rule : keywords) {
                    String matchedTrigger = KeywordMatcher.findMatch(rule, messageBody);
                    if (matchedTrigger != null) {
                        Log.i(TAG, "MATCH FOUND! Rule ID: " + rule.getId() + " | Trigger: " + matchedTrigger);
                        matchFound = true;
                        
                        // Proceed to fetch the Reply Template for this rule
                        fetchReplyAndProcess(customerName, messageBody, rule, matchedTrigger, notification);
                        break; // Stop after first (highest priority) match
                    }
                }

                if (!matchFound) {
                    Log.d(TAG, "Message checked against all rules. No match found. Logging as NO_MATCH.");
                    saveLog(new MessageLog(customerName, null, messageBody, MessageLog.STATUS_NO_MATCH));
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error fetching keywords: " + errorMessage);
                MessageLog errorLog = new MessageLog(customerName, null, messageBody, MessageLog.STATUS_ERROR);
                errorLog.setErrorMessage("Failed to fetch keywords: " + errorMessage);
                saveLog(errorLog);
            }
        });
    }

    /**
     * Fetches the linked Reply template and prepares it for sending.
     */
    private void fetchReplyAndProcess(String customerName, String incomingMsg, Keyword rule, String matchedTrigger, Notification notification) {
        String replyId = rule.getReplyId();
        if (replyId == null || replyId.isEmpty()) {
            MessageLog err = new MessageLog(customerName, null, incomingMsg, MessageLog.STATUS_ERROR);
            err.setMatchedKeyword(matchedTrigger);
            err.setKeywordId(rule.getId());
            err.setErrorMessage("Keyword rule has no linked Reply ID.");
            saveLog(err);
            return;
        }

        replyRepository.getReply(replyId, new ReplyRepository.ReplyFetchCallback() {
            @Override
            public void onSuccess(Reply reply) {
                if (!reply.isEnabled()) {
                    MessageLog err = new MessageLog(customerName, null, incomingMsg, MessageLog.STATUS_ERROR);
                    err.setMatchedKeyword(matchedTrigger);
                    err.setKeywordId(rule.getId());
                    err.setReplyId(reply.getId());
                    err.setErrorMessage("Linked reply template is disabled.");
                    saveLog(err);
                    return;
                }

                // We have a match and a valid reply!
                String replyContent = reply.getContent();
                Log.i(TAG, "Preparing to send reply: " + replyContent);

                // --- PHASE 8: HANDOFF TO ACCESSIBILITY SERVICE ---
                // 1. Prepare the partial log (Status: REPLIED). We let the AccessibilityService save it 
                //    once it actually successfully clicks the Send button.
                MessageLog partialLog = new MessageLog(customerName, null, incomingMsg, MessageLog.STATUS_REPLIED);
                partialLog.setMatchedKeyword(matchedTrigger);
                partialLog.setKeywordId(rule.getId());
                partialLog.setReplyId(reply.getId());
                partialLog.setReplySent(replyContent);

                // 2. Put the task into our singleton queue
                ReplyQueue.getInstance().setPendingTask(new ReplyQueue.ReplyTask(replyContent, partialLog));

                // 3. Fire the PendingIntent attached to this WhatsApp notification.
                // This physically opens the exact WhatsApp chat on the user's screen.
                // The AutoReplyAccessibilityService will detect the screen change and execute the queue.
                if (notification.contentIntent != null) {
                    try {
                        notification.contentIntent.send();
                        Log.d(TAG, "Fired PendingIntent to open WhatsApp chat.");
                    } catch (android.app.PendingIntent.CanceledException e) {
                        Log.e(TAG, "Failed to open chat. PendingIntent canceled.", e);
                        ReplyQueue.getInstance().clear();
                        
                        partialLog.setStatus(MessageLog.STATUS_ERROR);
                        partialLog.setErrorMessage("Failed to open chat: Intent canceled.");
                        saveLog(partialLog);
                    }
                } else {
                    Log.e(TAG, "Notification has no contentIntent. Cannot open chat.");
                    ReplyQueue.getInstance().clear();
                    
                    partialLog.setStatus(MessageLog.STATUS_ERROR);
                    partialLog.setErrorMessage("Notification has no contentIntent.");
                    saveLog(partialLog);
                }
            }

            @Override
            public void onError(String errorMessage) {
                MessageLog err = new MessageLog(customerName, null, incomingMsg, MessageLog.STATUS_ERROR);
                err.setMatchedKeyword(matchedTrigger);
                err.setKeywordId(rule.getId());
                err.setErrorMessage("Failed to fetch reply template: " + errorMessage);
                saveLog(err);
            }
        });
    }

    /**
     * Helper to save a log to Firestore.
     */
    private void saveLog(MessageLog log) {
        logRepository.addLog(log, new LogRepository.OperationCallback() {
            @Override
            public void onSuccess(String logId) {
                Log.d(TAG, "Log saved: " + logId + " [Status: " + log.getStatus() + "]");
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to save log: " + errorMessage);
            }
        });
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Not needed for our use case. We only care when the notification is posted (arrives).
    }
}
