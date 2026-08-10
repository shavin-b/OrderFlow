package com.orderflow.autoresponder.core.security

import android.content.Context
import android.content.SharedPreferences
import com.orderflow.autoresponder.domain.model.MetaCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveMetaCredentials(credentials: MetaCredentials) {
        prefs.edit()
            .putString(KEY_PHONE_ID, credentials.phoneNumberId)
            .putString(KEY_ACCESS_TOKEN, credentials.accessToken)
            .putString(KEY_BUSINESS_ID, credentials.businessAccountId)
            .putString(KEY_WEBHOOK_TOKEN, credentials.webhookVerifyToken)
            .apply()
    }

    fun getMetaCredentials(): MetaCredentials {
        return MetaCredentials(
            phoneNumberId = prefs.getString(KEY_PHONE_ID, "") ?: "",
            accessToken = prefs.getString(KEY_ACCESS_TOKEN, "") ?: "",
            businessAccountId = prefs.getString(KEY_BUSINESS_ID, "") ?: "",
            webhookVerifyToken = prefs.getString(KEY_WEBHOOK_TOKEN, "") ?: ""
        )
    }

    fun setAutoResponderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RESPONDER_ENABLED, enabled).apply()
    }

    fun isAutoResponderEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_RESPONDER_ENABLED, true)
    }

    fun setUseCloudApi(useCloud: Boolean) {
        prefs.edit().putBoolean(KEY_USE_CLOUD_API, useCloud).apply()
    }

    fun useCloudApi(): Boolean {
        return prefs.getBoolean(KEY_USE_CLOUD_API, false)
    }

    fun setAppSuspended(suspended: Boolean) {
        prefs.edit().putBoolean(KEY_APP_SUSPENDED, suspended).apply()
    }

    fun isAppSuspended(): Boolean {
        return prefs.getBoolean(KEY_APP_SUSPENDED, false)
    }

    fun isAppSuspendedFlow(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == KEY_APP_SUSPENDED) {
                trySend(sharedPreferences.getBoolean(KEY_APP_SUSPENDED, false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(isAppSuspended()) }

    fun setAdminLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_ADMIN_LOCKED, locked).apply()
    }

    fun isAdminLocked(): Boolean {
        return prefs.getBoolean(KEY_ADMIN_LOCKED, false)
    }

    fun isAdminLockedFlow(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == KEY_ADMIN_LOCKED) {
                trySend(sharedPreferences.getBoolean(KEY_ADMIN_LOCKED, false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(isAdminLocked()) }

    fun getDeviceId(): String? {
        return prefs.getString(KEY_DEVICE_ID, null)
    }

    fun saveDeviceId(deviceId: String) {
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }

    fun getFcmToken(): String? {
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    fun saveFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getSubscriptionEnd(): Long {
        return prefs.getLong(KEY_SUBSCRIPTION_END, 0L)
    }

    fun saveSubscriptionEnd(timestamp: Long) {
        prefs.edit().putLong(KEY_SUBSCRIPTION_END, timestamp).apply()
    }

    fun getSubscriptionStatus(): String {
        return prefs.getString(KEY_SUBSCRIPTION_STATUS, "ACTIVE") ?: "ACTIVE"
    }

    fun saveSubscriptionStatus(status: String) {
        prefs.edit().putString(KEY_SUBSCRIPTION_STATUS, status).apply()
    }

    fun isCommandProcessed(commandId: String): Boolean {
        val processed = prefs.getStringSet(KEY_PROCESSED_COMMANDS, emptySet()) ?: emptySet()
        return processed.contains(commandId)
    }

    fun markCommandAsProcessed(commandId: String) {
        val processed = prefs.getStringSet(KEY_PROCESSED_COMMANDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        processed.add(commandId)
        // Keep only last 100 commands to prevent unlimited growth
        val trimmed = if (processed.size > 100) processed.toList().takeLast(100).toSet() else processed
        prefs.edit().putStringSet(KEY_PROCESSED_COMMANDS, trimmed).apply()
    }

    companion object {
        private const val PREFS_NAME = "orderflow_secure_prefs"
        private const val KEY_PHONE_ID = "phone_number_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_BUSINESS_ID = "business_account_id"
        private const val KEY_WEBHOOK_TOKEN = "webhook_verify_token"
        private const val KEY_AUTO_RESPONDER_ENABLED = "auto_responder_enabled"
        private const val KEY_USE_CLOUD_API = "use_cloud_api"
        private const val KEY_APP_SUSPENDED = "app_suspended"
        private const val KEY_ADMIN_LOCKED = "admin_locked"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_SUBSCRIPTION_END = "subscription_end"
        private const val KEY_SUBSCRIPTION_STATUS = "subscription_status"
        private const val KEY_PROCESSED_COMMANDS = "processed_command_ids"
    }
}
