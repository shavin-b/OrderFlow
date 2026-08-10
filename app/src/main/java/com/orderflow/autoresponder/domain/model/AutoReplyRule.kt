package com.orderflow.autoresponder.domain.model

data class AutoReplyRule(
    val id: Long = 0,
    val ruleName: String,
    val keywordsCsv: String,
    val replyMessagesJson: String,
    val matchOption: MatchOption,
    val delaySeconds: Int = 0,
    val replySequential: Boolean = false,
    val isActive: Boolean = true,
    val isPauseForContact: Boolean = false,
    val pauseDurationMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val businessHours: BusinessHours = BusinessHours()
)
