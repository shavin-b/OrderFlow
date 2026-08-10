package com.orderflow.autoresponder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orderflow.autoresponder.domain.model.AutoReplyRule
import com.orderflow.autoresponder.domain.model.BusinessHours
import com.orderflow.autoresponder.domain.model.MatchOption

@Entity(tableName = "auto_reply_rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleName: String,
    val keywordsCsv: String,
    val replyMessagesJson: String,
    val matchOption: String,
    val delaySeconds: Int,
    val replySequential: Boolean,
    val isActive: Boolean,
    val isPauseForContact: Boolean,
    val pauseDurationMinutes: Int,
    val createdAt: Long,
    val businessHoursEnabled: Boolean,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val activeDaysCsv: String
)

fun RuleEntity.toDomainModel(): AutoReplyRule {
    return AutoReplyRule(
        id = id,
        ruleName = ruleName,
        keywordsCsv = keywordsCsv,
        replyMessagesJson = replyMessagesJson,
        matchOption = try { MatchOption.valueOf(matchOption) } catch (e: Exception) { MatchOption.EXACT },
        delaySeconds = delaySeconds,
        replySequential = replySequential,
        isActive = isActive,
        isPauseForContact = isPauseForContact,
        pauseDurationMinutes = pauseDurationMinutes,
        createdAt = createdAt,
        businessHours = BusinessHours(
            isEnabled = businessHoursEnabled,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            activeDaysCsv = activeDaysCsv
        )
    )
}

fun AutoReplyRule.toEntity(): RuleEntity {
    return RuleEntity(
        id = id,
        ruleName = ruleName,
        keywordsCsv = keywordsCsv,
        replyMessagesJson = replyMessagesJson,
        matchOption = matchOption.name,
        delaySeconds = delaySeconds,
        replySequential = replySequential,
        isActive = isActive,
        isPauseForContact = isPauseForContact,
        pauseDurationMinutes = pauseDurationMinutes,
        createdAt = createdAt,
        businessHoursEnabled = businessHours.isEnabled,
        startHour = businessHours.startHour,
        startMinute = businessHours.startMinute,
        endHour = businessHours.endHour,
        endMinute = businessHours.endMinute,
        activeDaysCsv = businessHours.activeDaysCsv
    )
}
