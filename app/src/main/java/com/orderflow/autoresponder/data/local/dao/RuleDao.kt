package com.orderflow.autoresponder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.orderflow.autoresponder.data.local.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Query("SELECT * FROM auto_reply_rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM auto_reply_rules WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM auto_reply_rules WHERE id = :id LIMIT 1")
    suspend fun getRuleById(id: Long): RuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleEntity): Long

    @Update
    suspend fun updateRule(rule: RuleEntity)

    @Delete
    suspend fun deleteRule(rule: RuleEntity)

    @Query("UPDATE auto_reply_rules SET isActive = :isActive WHERE id = :ruleId")
    suspend fun updateRuleActiveState(ruleId: Long, isActive: Boolean)
}
