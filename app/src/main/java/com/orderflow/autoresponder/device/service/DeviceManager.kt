package com.orderflow.autoresponder.device.service

import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Timestamp
import com.google.firebase.messaging.FirebaseMessaging
import com.orderflow.autoresponder.core.logger.StructuredLogger
import com.orderflow.autoresponder.core.security.SecureStorage
import com.orderflow.autoresponder.device.model.DeviceInfo
import com.orderflow.autoresponder.device.repository.DeviceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorage: SecureStorage,
    private val deviceRepository: DeviceRepository,
    private val firebaseMessaging: FirebaseMessaging
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        StructuredLogger.i("DeviceManager", "Initializing DeviceManager...")
        
        // Log current persistent state for diagnostics
        StructuredLogger.i("DeviceManager", "[DeviceControl] Current Lock State: ${if (secureStorage.isAdminLocked()) "LOCKED" else "UNLOCKED"}")
        StructuredLogger.i("DeviceManager", "[DeviceControl] Current Subscription: ${secureStorage.getSubscriptionStatus()}")

        serviceScope.launch {
            val deviceId = getOrCreateDeviceId()
            StructuredLogger.i("DeviceManager", "Device ID: $deviceId")
            
            // 1. Force synchronize the current FCM token
            synchronizeFcmToken(deviceId)

            // 2. Register/Update device info
            registerOrUpdateDevice(deviceId)
            
            // 3. Start background processes
            observeDeviceState(deviceId)
            scheduleHeartbeat()
        }
    }

    /**
     * Explicitly retrieves the current FCM token and updates Firestore if it changed.
     */
    suspend fun synchronizeFcmToken(deviceId: String) {
        try {
            val token = firebaseMessaging.token.await()
            if (!token.isNullOrBlank()) {
                val lastSavedToken = secureStorage.getFcmToken()
                if (token != lastSavedToken) {
                    StructuredLogger.i("DeviceManager", "FCM token synchronized (changed)")
                    secureStorage.saveFcmToken(token)
                    deviceRepository.updateDeviceInfo(deviceId, mapOf("fcmToken" to token))
                } else {
                    StructuredLogger.d("DeviceManager", "FCM token already up to date")
                }
            }
        } catch (e: Exception) {
            StructuredLogger.e("DeviceManager", "Failed to synchronize FCM token", e)
        }
    }

    private fun scheduleHeartbeat() {
        StructuredLogger.i("DeviceManager", "Scheduling heartbeat...")
        val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(15, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "device_heartbeat",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun getOrCreateDeviceId(): String {
        val existingId = secureStorage.getDeviceId()
        if (existingId != null) return existingId

        val newId = "OF-${UUID.randomUUID()}"
        secureStorage.saveDeviceId(newId)
        StructuredLogger.i("DeviceManager", "Generated new Device ID: $newId")
        return newId
    }

    private suspend fun registerOrUpdateDevice(deviceId: String) {
        StructuredLogger.i("DeviceManager", "Syncing real device info with Firestore...")
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        
        // Retrieve the current token from SecureStorage
        val currentToken = secureStorage.getFcmToken() ?: ""
        val credentials = secureStorage.getMetaCredentials()

        val deviceInfo = DeviceInfo(
            deviceId = deviceId,
            phoneModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            appVersion = packageInfo.versionName ?: "1.0.0",
            appBuildNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            },
            installationDate = packageInfo.firstInstallTime,
            userName = "User ${deviceId.takeLast(4)}",
            businessName = if (credentials.businessAccountId.isNotBlank()) "Biz ${credentials.businessAccountId}" else "OrderFlow User",
            firstSeen = null, 
            lastSeen = Timestamp.now(),
            lastSync = Timestamp.now(),
            fcmToken = currentToken,
            generatedUuid = deviceId, // Use full ID as UUID
            status = "Active",
            lockStatus = if (secureStorage.isAdminLocked()) "LOCKED" else "UNLOCKED",
            adminLock = secureStorage.isAdminLocked(),
            isLocked = secureStorage.isAdminLocked(),
            lockReason = secureStorage.getLockReason(),
            subscriptionStatus = secureStorage.getSubscriptionStatus()
        )

        val result = deviceRepository.registerDevice(deviceInfo)
        if (result.isSuccess) {
            StructuredLogger.i("DeviceManager", "Device info synced successfully")
        } else {
            StructuredLogger.e("DeviceManager", "Device info sync failed: ${result.exceptionOrNull()?.message}")
        }
    }

    private fun observeDeviceState(deviceId: String) {
        StructuredLogger.i("DeviceManager", "Starting device state observer (Raw Map)...")
        serviceScope.launch {
            deviceRepository.getDeviceRawFlow(deviceId)
                .catch { e -> 
                    StructuredLogger.e("DeviceManager", "Device state flow error", e)
                }
                .collect { data ->
                    if (data != null) {
                        StructuredLogger.d("DeviceManager", "[DeviceControl] Received RAW state sync: $data")

                        // 1. Handle Admin Lock State (Check ALL possible fields)
                        val statusField = data["status"]?.toString() ?: ""
                        val lockStatusField = data["lockStatus"]?.toString() ?: ""
                        val adminLockField = data["adminLock"]
                        val isLockedField = data["isLocked"]

                        val isLocked = statusField.contains("LOCKED", ignoreCase = true) || 
                                       statusField.contains("Suspended", ignoreCase = true) ||
                                       statusField.contains("Blocked", ignoreCase = true) ||
                                       lockStatusField.contains("LOCKED", ignoreCase = true) ||
                                       lockStatusField.contains("Admin Locked", ignoreCase = true) ||
                                       (adminLockField is Boolean && adminLockField) ||
                                       (isLockedField is Boolean && isLockedField) ||
                                       (adminLockField?.toString()?.equals("true", ignoreCase = true) ?: false)
                        
                        if (secureStorage.isAdminLocked() != isLocked) {
                            StructuredLogger.w("DeviceManager", "[DeviceControl] REMOTE CHANGE DETECTED: Lock Status -> $isLocked")
                            secureStorage.setAdminLocked(isLocked)
                            val reason = data["lockReason"]?.toString() ?: if (isLocked) "ADMIN" else "NONE"
                            secureStorage.setLockReason(reason)
                        }

                        // 2. Handle Subscription Status
                        val subStatus = data["subscriptionStatus"]?.toString() ?: "ACTIVE"
                        if (secureStorage.getSubscriptionStatus() != subStatus) {
                            StructuredLogger.i("DeviceManager", "[DeviceControl] REMOTE CHANGE DETECTED: Subscription -> $subStatus")
                            secureStorage.setSubscriptionStatus(subStatus)
                            
                            val subEnd = data["subscriptionEnd"]
                            if (subEnd is Timestamp) {
                                secureStorage.saveSubscriptionEnd(subEnd.toDate().time)
                            }
                        }
                    }
                }
        }
    }
}
