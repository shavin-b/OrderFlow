package com.orderflow.autoresponder.core.util

import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.orderflow.autoresponder.core.logger.StructuredLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalReplyHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Executes a direct reply using the provided notification action metadata.
     */
    fun sendDirectReply(
        pendingIntent: PendingIntent,
        remoteInput: RemoteInput,
        replyText: String
    ): Boolean {
        if (replyText.isBlank()) return false
        
        return try {
            val results = Bundle().apply {
                putString(remoteInput.resultKey, replyText)
            }
            
            val intent = Intent().apply {
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }
            
            RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, results)
            
            pendingIntent.send(context, 0, intent)
            StructuredLogger.i("LocalReplyHelper", "Successfully executed direct reply: $replyText")
            true
        } catch (e: Exception) {
            StructuredLogger.e("LocalReplyHelper", "Failed to send local reply to ${pendingIntent.creatorPackage}", e)
            false
        }
    }
}
