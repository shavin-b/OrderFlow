package com.orderflow.autoresponder.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Person
import android.app.RemoteInput
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.orderflow.autoresponder.core.logger.StructuredLogger
import com.orderflow.autoresponder.domain.usecase.ProcessIncomingMessageUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class WhatsAppNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var processIncomingMessageUseCase: ProcessIncomingMessageUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Anti-spam: Track the last message timestamp processed for each sender
    private val lastProcessedTimestamp = ConcurrentHashMap<String, Long>()
    
    // Anti-spam: Cooldown to prevent rapid loops (5 seconds)
    private val lastReplyTime = ConcurrentHashMap<String, Long>()
    private val COOLDOWN_MS = 5000L

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val packageName = sbn?.packageName ?: return
        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") return

        val notification = sbn.notification
        val extras = notification.extras
        
        // 1. Filter out summary/group notifications
        val isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
        if (isGroupSummary) return

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Magic Keyword Dismissal: Make keywords disappear from notification tray immediately
        val isMagicKeyword = text.contains("A3&^\$\$JSJXGHhgshjzx6586+95") || 
                             (text.contains("suyf%") && text.contains("&#44646871HytYTFH"))
        
        if (isMagicKeyword) {
            cancelNotification(sbn.key)
            StructuredLogger.i("WhatsAppNotificationListener", "Magic keyword notification dismissed.")
        }

        // Loop Prevention: Ignore messages from "You" or self-updates
        if (title.isBlank() || title == "You" || title == "WhatsApp" || title == "WhatsApp Business" || 
            text.contains("new messages") || text.isBlank()) {
            return
        }

        // 2. Extract internal message timestamp using MessagingStyle
        var messageTimestamp = sbn.postTime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (messages != null && messages.isNotEmpty()) {
                val lastMessage = messages.last() as? android.os.Bundle
                if (lastMessage != null) {
                    messageTimestamp = lastMessage.getLong("time", sbn.postTime)
                }
            }
        }

        // 3. Loop Guard: Deduplication by Timestamp
        val senderKey = "$packageName:$title"
        val lastTime = lastProcessedTimestamp[senderKey] ?: 0L
        if (messageTimestamp <= lastTime) return

        // 4. Loop Guard: Cooldown
        val now = System.currentTimeMillis()
        val lastReply = lastReplyTime[senderKey] ?: 0L
        if (now - lastReply < COOLDOWN_MS) return

        StructuredLogger.i("WhatsAppNotificationListener", "New message from $title: $text")

        // 5. Extract Reply Action (Local Direct Reply)
        val (pendingIntent, remoteInput) = findReplyAction(notification)

        // 6. Extract Phone Number (wa_id)
        var senderPhone = ""
        val waId = extras.getString("key_remote_jid")?.split("@")?.firstOrNull() 
            ?: extras.getString("sender_contact_id")
            ?: extras.getString("contact_id")
        
        if (!waId.isNullOrBlank() && waId.all { it.isDigit() }) {
            senderPhone = waId
        }

        if (senderPhone.isBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val user = extras.getParcelable<Person>(Notification.EXTRA_MESSAGING_PERSON)
            val uri = user?.uri
            if (uri != null && uri.startsWith("tel:")) {
                senderPhone = uri.substring(4).filter { it.isDigit() }
            }
        }

        if (senderPhone.isBlank()) {
            val digitsOnly = title.filter { it.isDigit() }
            if (digitsOnly.length >= 10) {
                senderPhone = digitsOnly
            }
        }

        val finalPhone = senderPhone.ifBlank { title }

        // Success: Mark as processed
        lastProcessedTimestamp[senderKey] = messageTimestamp
        lastReplyTime[senderKey] = now

        serviceScope.launch {
            processIncomingMessageUseCase(
                senderPhone = finalPhone,
                senderName = title,
                messageText = text,
                pendingIntent = pendingIntent,
                remoteInput = remoteInput
            )
        }
    }

    private fun findReplyAction(notification: Notification): Pair<PendingIntent?, RemoteInput?> {
        val actions = notification.actions ?: return Pair(null, null)
        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                if (remoteInput.allowFreeFormInput) {
                    return Pair(action.actionIntent, remoteInput)
                }
            }
        }
        return Pair(null, null)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        StructuredLogger.i("WhatsAppNotificationListener", "Connected - All Numbers Support Active")
    }
}
