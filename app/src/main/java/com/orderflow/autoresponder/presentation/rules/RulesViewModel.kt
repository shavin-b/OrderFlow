package com.orderflow.autoresponder.presentation.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orderflow.autoresponder.domain.model.AutoReplyRule
import com.orderflow.autoresponder.domain.model.MatchOption
import com.orderflow.autoresponder.domain.repository.RuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RulesUiState(
    val rules: List<AutoReplyRule> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val ruleRepository: RuleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RulesUiState())
    val uiState: StateFlow<RulesUiState> = _uiState.asStateFlow()

    init {
        loadRules()
    }

    private fun loadRules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            ruleRepository.getAllRules().collect { rulesList ->
                _uiState.value = RulesUiState(rules = rulesList, isLoading = false)
            }
        }
    }

    fun toggleRule(ruleId: Long, isActive: Boolean) {
        viewModelScope.launch {
            ruleRepository.toggleRuleActive(ruleId, isActive)
        }
    }

    fun deleteRule(rule: AutoReplyRule) {
        viewModelScope.launch {
            ruleRepository.deleteRule(rule)
        }
    }

    fun saveRule(
        id: Long = 0,
        ruleName: String,
        keywordsCsv: String,
        replyMessage: String,
        matchOption: MatchOption,
        delaySeconds: Int,
        replySequential: Boolean = false
    ) {
        viewModelScope.launch {
            val rule = AutoReplyRule(
                id = id,
                ruleName = ruleName.ifBlank { "Auto-Reply Rule" },
                keywordsCsv = keywordsCsv,
                replyMessagesJson = replyMessage,
                matchOption = matchOption,
                delaySeconds = delaySeconds,
                replySequential = replySequential,
                isActive = true
            )
            if (id == 0L) {
                ruleRepository.insertRule(rule)
            } else {
                ruleRepository.updateRule(rule)
            }
        }
    }
}
