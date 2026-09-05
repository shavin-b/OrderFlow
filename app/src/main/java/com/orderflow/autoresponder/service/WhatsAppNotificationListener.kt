package com.orderflow.autoresponder.service

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.orderflow.autoresponder.core.logger.StructuredLogger
import com.orderflow.autoresponder.domain.usecase.ProcessIncomingMessageUseCase
import com.orderflow.autoresponder.service.parser.WhatsAppNotificationParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WhatsAppNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var processIncomingMessageUseCase: ProcessIncomingMessageUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
        private const val REGULAR_WHATSAPP_PACKAGE = "com.whatsapp"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName

        // 1. Strict Package Filter: Only WhatsApp Business is allowed
        if (packageName != WHATSAPP_BUSINESS_PACKAGE) {
            if (packageName == REGULAR_WHATSAPP_PACKAGE) {
                StructuredLogger.i("OrderFlow", "[AutoReply] Notification ignored: unsupported package $packageName")
            }
            return
        }

        StructuredLogger.i("OrderFlow", "[AutoReply] WhatsApp Business notification accepted: $packageName")

        val parsedMessage = WhatsAppNotificationParser.parse(sbn) ?: return

        // Magic Keyword Dismissal: Make keywords disappear from notification tray immediately
        val text = parsedMessage.messageText
        val isMagicKeyword = text.contains("A3&^\$\$JSJXGHhgshjzx6586+95") || 
                             (text.contains("suyf%") && text.contains("&#44646871HytYTFH"))
        
        if (isMagicKeyword) {
            cancelNotification(sbn.key)
            StructuredLogger.i("WhatsAppNotificationListener", "Magic keyword notification dismissed.")
        }

        StructuredLogger.i("WhatsAppNotificationListener", "New message parsed: ${parsedMessage.senderName} - ${parsedMessage.messageText}")

        // Extract Direct Reply Action
        val (pendingIntent, remoteInput) = findReplyAction(parsedMessage.sbn.notification)

        // Extract Phone Number (wa_id) - Reuse existing logic or improve
        val extras = parsedMessage.sbn.notification.extras ?: android.os.Bundle.EMPTY
        var senderPhone = ""
        val waId = extras.getString("key_remote_jid")?.split("@")?.firstOrNull() 
            ?: extras.getString("sender_contact_id")
            ?: extras.getString("contact_id")
        
        if (!waId.isNullOrBlank() && waId.all { it.isDigit() }) {
            senderPhone = waId
        }

        val finalPhone = senderPhone.ifBlank { parsedMessage.senderName ?: "Unknown" }

        serviceScope.launch {
            processIncomingMessageUseCase(
                senderPhone = finalPhone,
                senderName = parsedMessage.senderName ?: "Unknown",
                messageText = parsedMessage.messageText,
                isGroup = parsedMessage.isGroup,
                conversationTitle = parsedMessage.conversationTitle,
                timestamp = parsedMessage.timestamp,
                packageName = parsedMessage.packageName,
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
        StructuredLogger.i("WhatsAppNotificationListener", "Notification Listener Service Connected")
    }
}
