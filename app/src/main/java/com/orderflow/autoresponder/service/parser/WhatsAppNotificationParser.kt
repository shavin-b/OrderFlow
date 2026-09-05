package com.orderflow.autoresponder.service.parser

import android.app.Notification
import android.app.Person
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.orderflow.autoresponder.core.logger.StructuredLogger
import com.orderflow.autoresponder.domain.model.IncomingWhatsAppMessage

object WhatsAppNotificationParser {

    private const val WHATSAPP_W4B_PKG = "com.whatsapp.w4b"

    fun parse(sbn: StatusBarNotification): IncomingWhatsAppMessage? {
        val packageName = sbn.packageName
        if (packageName != WHATSAPP_W4B_PKG) return null

        val notification = sbn.notification
        val extras = notification.extras ?: return null

        // 1. Filter out summary notifications
        val isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
        if (isGroupSummary) return null

        // 2. Extract Title and Text
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Loop Prevention: Ignore system updates or "You" sent messages
        if (title.isBlank() || title == "WhatsApp" || title == "WhatsApp Business" || 
            text.contains("new messages") || text.isBlank()) {
            return null
        }

        // 3. Extract detailed message info using MessagingStyle if available
        var messageText = text
        var senderName = title
        var conversationTitle = title
        var isGroup = false
        var messageTimestamp = sbn.postTime

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (messages != null && messages.isNotEmpty()) {
                val lastMessage = messages.last() as? Bundle
                if (lastMessage != null) {
                    messageText = lastMessage.getCharSequence("text")?.toString() ?: text
                    messageTimestamp = lastMessage.getLong("time", sbn.postTime)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val person = lastMessage.getParcelable<Person>("sender_person")
                        senderName = person?.name?.toString() ?: title
                    } else {
                        senderName = lastMessage.getCharSequence("sender")?.toString() ?: title
                    }
                }
            }
            
            // Determine if it's a group
            val isGroupConversation = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION)
            conversationTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString() ?: title
            isGroup = isGroupConversation || (conversationTitle != senderName && !conversationTitle.isNullOrBlank())
        }

        // Loop Prevention: Ignore self-replies
        if (senderName == "You" || senderName.startsWith("You ")) {
            return null
        }

        // WhatsApp specific: if title contains a colon, it's often a group message format "Group: Sender"
        if (!isGroup && title.contains(": ")) {
            isGroup = true
        }

        return IncomingWhatsAppMessage(
            packageName = packageName,
            conversationTitle = conversationTitle,
            senderName = senderName,
            messageText = messageText,
            timestamp = messageTimestamp,
            notificationKey = sbn.key,
            isGroup = isGroup,
            sbn = sbn
        )
    }
}
