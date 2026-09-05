package com.orderflow.autoresponder.domain.usecase

import com.orderflow.autoresponder.core.logger.StructuredLogger
import com.orderflow.autoresponder.domain.model.AutoReplyRule
import com.orderflow.autoresponder.domain.model.BusinessHours
import com.orderflow.autoresponder.domain.model.MatchOption
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class EvaluateRuleUseCase @Inject constructor() {

    operator fun invoke(
        incomingMessage: String,
        rules: List<AutoReplyRule>,
        isGroup: Boolean = false
    ): AutoReplyRule? {
        val trimmedInput = incomingMessage.trim()
        val activeRules = rules.filter { it.isActive }
            .sortedByDescending { it.priority }

        for (rule in activeRules) {
            // Check group chat support
            if (isGroup && !rule.enabledForGroups) {
                continue
            }

            if (!isWithinBusinessHours(rule.businessHours)) {
                StructuredLogger.d("EvaluateRuleUseCase", "Rule '${rule.ruleName}' skipped: outside business hours")
                continue
            }

            if (isMatch(trimmedInput, rule)) {
                StructuredLogger.i("EvaluateRuleUseCase", "Matched rule '${rule.ruleName}' for message: $trimmedInput")
                return rule
            }
        }

        // Check for fallback ALL or AWAY rule if no exact match (also respecting priority)
        return activeRules.firstOrNull { 
            (it.matchOption == MatchOption.ALL || it.matchOption == MatchOption.AWAY) && 
            (!isGroup || it.enabledForGroups) 
        }
    }

    private fun isMatch(input: String, rule: AutoReplyRule): Boolean {
        val keywords = rule.keywordsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val ignoreCase = !rule.caseSensitive
        
        return when (rule.matchOption) {
            MatchOption.EXACT -> keywords.any { it.equals(input, ignoreCase = ignoreCase) }
            MatchOption.CONTAINS -> keywords.any { input.contains(it, ignoreCase = ignoreCase) }
            MatchOption.STARTS_WITH -> keywords.any { input.startsWith(it, ignoreCase = ignoreCase) }
            MatchOption.ENDS_WITH -> keywords.any { input.endsWith(it, ignoreCase = ignoreCase) }
            MatchOption.REGEX -> keywords.any { keyword ->
                try {
                    val options = if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()
                    Regex(keyword, options).containsMatchIn(input)
                } catch (e: Exception) {
                    false
                }
            }
            MatchOption.ALL -> true
            MatchOption.AWAY -> true
        }
    }

    private fun isWithinBusinessHours(hours: BusinessHours): Boolean {
        if (!hours.isEnabled) return true

        val calendar = Calendar.getInstance()
        val currentDay = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> ""
        }

        val activeDays = hours.activeDaysCsv.split(",").map { it.trim().uppercase(Locale.getDefault()) }
        if (!activeDays.contains(currentDay)) {
            return false
        }

        val currentMinuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMinuteOfDay = hours.startHour * 60 + hours.startMinute
        val endMinuteOfDay = hours.endHour * 60 + hours.endMinute

        return currentMinuteOfDay in startMinuteOfDay..endMinuteOfDay
    }
}
