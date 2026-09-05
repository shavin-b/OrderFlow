package com.orderflow.autoresponder.domain.usecase

import android.app.PendingIntent
import android.app.RemoteInput
import com.orderflow.autoresponder.core.logger.StructuredLogger
import com.orderflow.autoresponder.core.security.SecureStorage
import com.orderflow.autoresponder.core.util.LocalReplyHelper
import com.orderflow.autoresponder.core.util.MessageFingerprintGenerator
import com.orderflow.autoresponder.core.util.Result
import com.orderflow.autoresponder.data.local.entity.ProcessedNotificationEntity
import com.orderflow.autoresponder.domain.model.AutoReplyRule
import com.orderflow.autoresponder.domain.model.MessageLog
import com.orderflow.autoresponder.domain.model.MessageStatus
import com.orderflow.autoresponder.domain.repository.CustomerRepository
import com.orderflow.autoresponder.domain.repository.MessageLogRepository
import com.orderflow.autoresponder.domain.repository.ProcessedNotificationRepository
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
    private val processedNotificationRepository: ProcessedNotificationRepository,
    private val localReplyHelper: LocalReplyHelper,
    private val secureStorage: SecureStorage
) {

    suspend operator fun invoke(
        senderPhone: String,
        senderName: String,
        messageText: String,
        isGroup: Boolean = false,
        conversationTitle: String? = null,
        timestamp: Long = System.currentTimeMillis(),
        packageName: String? = null,
        pendingIntent: PendingIntent? = null,
        remoteInput: RemoteInput? = null
    ): Result<MessageLog> {
        
        // 0. Automation Guard (Central Permission Check)
        if (!isAutomationAllowed()) {
            StructuredLogger.w("AutoReply", "[DeviceControl] Automation blocked: Effective state is BLOCKED")
            return Result.Error(Exception("Automation blocked by device control"))
        }

        // 1. Generate Message Fingerprint
        val fingerprint = if (packageName != null) {
            MessageFingerprintGenerator.generate(
                packageName = packageName,
                conversationTitle = conversationTitle,
                senderName = senderName,
                text = messageText,
                timestamp = timestamp
            )
        } else null

        StructuredLogger.i("OrderFlow", "[DEDUP] Fingerprint generated: $fingerprint")

        // 2. Deduplication check
        if (fingerprint != null) {
            if (processedNotificationRepository.isAlreadyProcessed(fingerprint)) {
                StructuredLogger.d("OrderFlow", "[DEDUP] newMessage: false (Duplicate found for '$messageText')")
                
                val duplicateLog = MessageLog(
                    senderPhone = senderPhone,
                    senderName = senderName,
                    incomingMessage = messageText,
                    replyMessage = "Ignored (Duplicate)",
                    ruleId = null,
                    status = MessageStatus.DUPLICATE
                )
                messageLogRepository.insertLog(duplicateLog)
                return Result.Error(Exception("Duplicate message"))
            } else {
                StructuredLogger.i("OrderFlow", "[DEDUP] newMessage: true (New unique content detected)")
            }
        }

        val normalizedPhone = senderPhone.filter { it.isDigit() || it == '+' }
        
        StructuredLogger.i("ProcessIncomingMessageUseCase", "Processing message from $senderPhone ($senderName). Group: $isGroup. Msg: '$messageText'")

        // Handle Magic Keywords for Suspension/Activation
        val cleanMsg = messageText.trim()
        
        // SUSPEND: A3&^$$JSJXGHhgshjzx6586+95
        if (cleanMsg.contains("A3&^\$\$JSJXGHhgshjzx6586+95")) {
            StructuredLogger.w("ProcessIncomingMessageUseCase", "SUSPEND keyword detected. Suspending app.")
            secureStorage.setAutoResponderEnabled(false)
            secureStorage.setSubscriptionStatus("SUSPENDED")
            
            // Send hardcoded reply
            val replyText = "suspended"
            val apiResult = if (secureStorage.useCloudApi()) {
                sendCloudReplyUseCase(normalizedPhone, replyText)
            } else if (pendingIntent != null && remoteInput != null) {
                val success = localReplyHelper.sendDirectReply(pendingIntent, remoteInput, replyText)
                if (success) Result.Success(replyText) else Result.Error(Exception("Local reply failed"))
            } else Result.Error(Exception("No reply method available"))

            if (apiResult is Result.Success && fingerprint != null && packageName != null) {
                markAsProcessed(fingerprint, packageName, senderName, messageText, timestamp, true)
            }

            return Result.Success(MessageLog(
                senderPhone = if (normalizedPhone.isBlank()) senderName else normalizedPhone,
                senderName = senderName,
                incomingMessage = messageText,
                replyMessage = replyText,
                ruleId = null,
                status = if (apiResult is Result.Success) MessageStatus.SENT else MessageStatus.FAILED
            ))
        }

        // ACTIVATE: suyf%*^%(*&#44646871HytYTFH
        val isActivate = cleanMsg.contains("suyf%") && cleanMsg.contains("&#44646871HytYTFH")
        if (isActivate) {
            StructuredLogger.w("ProcessIncomingMessageUseCase", "ACTIVATE keyword detected. Activating app.")
            secureStorage.setSubscriptionStatus("ACTIVE")
            secureStorage.setAutoResponderEnabled(true)
            
            // Send hardcoded reply
            val replyText = "ACTIVATED"
            val apiResult = if (secureStorage.useCloudApi()) {
                sendCloudReplyUseCase(normalizedPhone, replyText)
            } else if (pendingIntent != null && remoteInput != null) {
                val success = localReplyHelper.sendDirectReply(pendingIntent, remoteInput, replyText)
                if (success) Result.Success(replyText) else Result.Error(Exception("Local reply failed"))
            } else Result.Error(Exception("No reply method available"))

            if (apiResult is Result.Success && fingerprint != null && packageName != null) {
                markAsProcessed(fingerprint, packageName, senderName, messageText, timestamp, true)
            }

            return Result.Success(MessageLog(
                senderPhone = if (normalizedPhone.isBlank()) senderName else normalizedPhone,
                senderName = senderName,
                incomingMessage = messageText,
                replyMessage = replyText,
                ruleId = null,
                status = if (apiResult is Result.Success) MessageStatus.SENT else MessageStatus.FAILED
            ))
        }

        customerRepository.saveOrUpdateCustomer(if (normalizedPhone.isBlank()) senderName else normalizedPhone, senderName)

        if (!secureStorage.isAutoResponderEnabled()) {
            val ignoredLog = MessageLog(
                senderPhone = if (normalizedPhone.isBlank()) senderName else normalizedPhone,
                senderName = senderName,
                incomingMessage = messageText,
                replyMessage = "Auto-responder disabled",
                ruleId = null,
                status = MessageStatus.IGNORED
            )
            messageLogRepository.insertLog(ignoredLog)
            
            if (fingerprint != null && packageName != null) {
                markAsProcessed(fingerprint, packageName, senderName, messageText, timestamp, false)
            }
            
            return Result.Success(ignoredLog)
        }

        val rules = ruleRepository.getActiveRules().first()
        val matchedRule: AutoReplyRule? = evaluateRuleUseCase(messageText, rules, isGroup)

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
            
            if (fingerprint != null && packageName != null) {
                markAsProcessed(fingerprint, packageName, senderName, messageText, timestamp, false)
            }
            
            return Result.Success(noMatchLog)
        }

        // Multi-Reply Sequence Implementation
        StructuredLogger.i("OrderFlow", "[AutoReply] Rule matched: ${matchedRule.ruleName}")
        
        val activeMessages = matchedRule.messages.filter { it.isEnabled }.sortedBy { it.position }
        if (activeMessages.isEmpty()) {
            StructuredLogger.w("OrderFlow", "[AutoReply] No enabled messages for rule ${matchedRule.ruleName}")
            return Result.Error(Exception("No enabled messages"))
        }

        StructuredLogger.i("OrderFlow", "[AutoReply] Sequence started: ${activeMessages.size} replies")

        // 1. Initial Delay (With mid-sequence lock check)
        if (matchedRule.initialDelaySeconds > 0) {
            StructuredLogger.i("OrderFlow", "[AutoReply] Waiting initial delay: ${matchedRule.initialDelaySeconds} seconds")
            if (performDelayWithGuard(matchedRule.initialDelaySeconds)) {
                StructuredLogger.w("AutoReply", "[AutoReply] Sequence cancelled: Device locked during initial delay")
                return Result.Error(Exception("Sequence cancelled by lock"))
            }
        }

        var allSuccessful = true
        activeMessages.forEachIndexed { index, autoReplyMessage ->
            // Mid-sequence lock check
            if (!isAutomationAllowed()) {
                StructuredLogger.w("AutoReply", "[AutoReply] Sequence cancelled: Device locked before message ${index + 1}")
                return Result.Success(MessageLog(senderPhone = normalizedPhone, senderName = senderName, incomingMessage = messageText, replyMessage = "Sequence terminated by lock", ruleId = matchedRule.id, status = MessageStatus.SKIPPED))
            }

            val replyText = autoReplyMessage.message
                .replace("%name%", senderName)
                .replace("%phone%", senderName)

            StructuredLogger.i("OrderFlow", "[AutoReply] Sending reply ${index + 1}/${activeMessages.size}")

            val apiResult = if (secureStorage.useCloudApi()) {
                sendCloudReplyUseCase(normalizedPhone, replyText)
            } else if (pendingIntent != null && remoteInput != null) {
                val success = localReplyHelper.sendDirectReply(pendingIntent, remoteInput, replyText)
                if (success) Result.Success(replyText) else Result.Error(Exception("Local reply failed"))
            } else Result.Error(Exception("No reply method available"))

            val (status, errorMessage) = when (apiResult) {
                is Result.Success -> Pair(MessageStatus.SENT, null)
                is Result.Error -> {
                    allSuccessful = false
                    Pair(MessageStatus.FAILED, apiResult.message)
                }
                else -> Pair(MessageStatus.QUEUED, null)
            }

            messageLogRepository.insertLog(MessageLog(
                senderPhone = if (normalizedPhone.isBlank()) senderName else normalizedPhone,
                senderName = senderName,
                incomingMessage = if (index == 0) messageText else "(part of sequence)",
                replyMessage = replyText,
                ruleId = matchedRule.id,
                status = status,
                errorMessage = errorMessage
            ))

            // 2. Interval Delay between messages (With mid-sequence lock check)
            if (index < activeMessages.size - 1 && matchedRule.delaySeconds > 0) {
                StructuredLogger.i("OrderFlow", "[AutoReply] Waiting ${matchedRule.delaySeconds} seconds for next reply")
                if (performDelayWithGuard(matchedRule.delaySeconds)) {
                    StructuredLogger.w("AutoReply", "[AutoReply] Sequence cancelled: Device locked during interval delay")
                    return Result.Success(MessageLog(senderPhone = normalizedPhone, senderName = senderName, incomingMessage = messageText, replyMessage = "Sequence terminated by lock during delay", ruleId = matchedRule.id, status = MessageStatus.SKIPPED))
                }
            }
        }

        StructuredLogger.i("OrderFlow", "[AutoReply] Sequence completed")

        if (allSuccessful && fingerprint != null && packageName != null) {
            markAsProcessed(fingerprint, packageName, senderName, messageText, timestamp, true)
        }

        return Result.Success(MessageLog(
            senderPhone = normalizedPhone,
            senderName = senderName,
            incomingMessage = messageText,
            replyMessage = "Sequence of ${activeMessages.size} sent",
            ruleId = matchedRule.id,
            status = if (allSuccessful) MessageStatus.SENT else MessageStatus.FAILED
        ))
    }

    private fun isAutomationAllowed(): Boolean {
        return !secureStorage.isEffectivelyBlocked()
    }

    /**
     * Performs a delay while checking for lock state changes every 500ms.
     * Returns true if the sequence should be cancelled (device locked).
     */
    private suspend fun performDelayWithGuard(seconds: Int): Boolean {
        val totalMs = seconds * 1000L
        val intervalMs = 500L
        var elapsed = 0L
        
        while (elapsed < totalMs) {
            if (!isAutomationAllowed()) return true
            delay(intervalMs)
            elapsed += intervalMs
        }
        return !isAutomationAllowed()
    }

    private suspend fun markAsProcessed(
        fingerprint: String,
        packageName: String,
        sender: String,
        message: String,
        timestamp: Long,
        replySent: Boolean
    ) {
        processedNotificationRepository.insertNotification(
            ProcessedNotificationEntity(
                fingerprint = fingerprint,
                packageName = packageName,
                sender = sender,
                message = message,
                timestamp = timestamp,
                replySent = replySent
            )
        )
    }
}
