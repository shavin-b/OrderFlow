package com.orderflow.autoresponder.domain.model

data class MessageLog(
    val id: Long = 0,
    val senderPhone: String,
    val senderName: String,
    val incomingMessage: String,
    val replyMessage: String,
    val ruleId: Long?,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val errorMessage: String? = null
)

enum class MessageStatus {
    SENT,
    FAILED,
    QUEUED,
    IGNORED
}
