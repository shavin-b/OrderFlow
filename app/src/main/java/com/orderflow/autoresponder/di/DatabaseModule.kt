package com.orderflow.autoresponder.di

import android.content.Context
import androidx.room.Room
import com.orderflow.autoresponder.data.local.OrderFlowDatabase
import com.orderflow.autoresponder.data.local.dao.CustomerDao
import com.orderflow.autoresponder.data.local.dao.MessageLogDao
import com.orderflow.autoresponder.data.local.dao.RuleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OrderFlowDatabase {
        return Room.databaseBuilder(
            context,
            OrderFlowDatabase::class.java,
            "orderflow_autoresponder.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRuleDao(database: OrderFlowDatabase): RuleDao = database.ruleDao()

    @Provides
    fun provideMessageLogDao(database: OrderFlowDatabase): MessageLogDao = database.messageLogDao()

    @Provides
    fun provideCustomerDao(database: OrderFlowDatabase): CustomerDao = database.customerDao()
}
