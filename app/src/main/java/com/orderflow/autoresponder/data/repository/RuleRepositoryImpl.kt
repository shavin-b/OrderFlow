package com.orderflow.autoresponder.data.repository

import com.orderflow.autoresponder.data.local.dao.RuleDao
import com.orderflow.autoresponder.data.local.entity.toDomainModel
import com.orderflow.autoresponder.data.local.entity.toEntity
import com.orderflow.autoresponder.domain.model.AutoReplyRule
import com.orderflow.autoresponder.domain.repository.RuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RuleRepositoryImpl @Inject constructor(
    private val ruleDao: RuleDao
) : RuleRepository {

    override fun getAllRules(): Flow<List<AutoReplyRule>> {
        return ruleDao.getAllRules().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getActiveRules(): Flow<List<AutoReplyRule>> {
        return ruleDao.getActiveRules().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getRuleById(id: Long): AutoReplyRule? {
        return ruleDao.getRuleById(id)?.toDomainModel()
    }

    override suspend fun insertRule(rule: AutoReplyRule): Long {
        return ruleDao.insertRule(rule.toEntity())
    }

    override suspend fun updateRule(rule: AutoReplyRule) {
        ruleDao.updateRule(rule.toEntity())
    }

    override suspend fun deleteRule(rule: AutoReplyRule) {
        ruleDao.deleteRule(rule.toEntity())
    }

    override suspend fun toggleRuleActive(ruleId: Long, isActive: Boolean) {
        ruleDao.updateRuleActiveState(ruleId, isActive)
    }
}
