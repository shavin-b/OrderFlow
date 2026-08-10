package com.orderflow.autoresponder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.orderflow.autoresponder.data.local.dao.CustomerDao
import com.orderflow.autoresponder.data.local.dao.MessageLogDao
import com.orderflow.autoresponder.data.local.dao.RuleDao
import com.orderflow.autoresponder.data.local.entity.CustomerEntity
import com.orderflow.autoresponder.data.local.entity.MessageLogEntity
import com.orderflow.autoresponder.data.local.entity.RuleEntity

@Database(
    entities = [
        RuleEntity::class,
        MessageLogEntity::class,
        CustomerEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OrderFlowDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun messageLogDao(): MessageLogDao
    abstract fun customerDao(): CustomerDao
}
