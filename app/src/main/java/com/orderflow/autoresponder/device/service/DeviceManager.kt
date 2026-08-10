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
        
        // Log current persistent state
        if (secureStorage.isAdminLocked()) {
            StructuredLogger.w("DeviceManager", "App suspension state restored: SUSPENDED (Admin Lock)")
        } else if (secureStorage.isAppSuspended()) {
            StructuredLogger.w("DeviceManager", "App suspension state restored: SUSPENDED (Subscription)")
        } else {
            StructuredLogger.i("DeviceManager", "App suspension state restored: ACTIVE")
        }

        serviceScope.launch {
            val deviceId = getOrCreateDeviceId()
            StructuredLogger.i("DeviceManager", "Device ID: $deviceId")
            
            // 1. Force synchronize the current FCM token
            synchronizeFcmToken(deviceId)

            // 2. Register/Update device info
            registerOrUpdateDevice(deviceId)
            
            // 3. Start background processes
            observeSubscription(deviceId)
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
        StructuredLogger.i("DeviceManager", "Syncing device info with Firestore...")
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        
        // Retrieve the current token from SecureStorage (populated by synchronizeFcmToken)
        val currentToken = secureStorage.getFcmToken() ?: ""

        val deviceInfo = DeviceInfo(
            deviceId = deviceId,
            phoneModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            appVersion = packageInfo.versionName ?: "unknown",
            appBuildNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            },
            installationDate = packageInfo.firstInstallTime,
            // Only set firstSeen if we are creating a new record. 
            // Firestore 'set with merge' will handle the rest.
            firstSeen = null, 
            lastSeen = Timestamp.now(),
            lastSync = Timestamp.now(),
            fcmToken = currentToken,
            status = "Active",
            subscriptionStatus = secureStorage.getSubscriptionStatus()
        )

        val result = deviceRepository.registerDevice(deviceInfo)
        if (result.isSuccess) {
            StructuredLogger.i("DeviceManager", "Device info synced successfully")
        } else {
            StructuredLogger.e("DeviceManager", "Device info sync failed: ${result.exceptionOrNull()?.message}")
        }
    }

    private fun observeSubscription(deviceId: String) {
        StructuredLogger.i("DeviceManager", "Starting subscription observer...")
        serviceScope.launch {
            deviceRepository.getDeviceSubscriptionFlow(deviceId)
                .catch { e -> 
                    StructuredLogger.e("DeviceManager", "Subscription flow error", e)
                }
                .collect { deviceInfo ->
                    if (deviceInfo != null) {
                    StructuredLogger.i("DeviceManager", "Subscription update received: ${deviceInfo.subscriptionStatus}")
                    secureStorage.saveSubscriptionStatus(deviceInfo.subscriptionStatus)
                    val expiry = deviceInfo.subscriptionEnd?.toDate()?.time ?: 0L
                    secureStorage.saveSubscriptionEnd(expiry)
                    
                    // Trigger suspension if status is SUSPENDED or EXPIRED
                    val shouldSuspend = deviceInfo.subscriptionStatus == "SUSPENDED" || 
                                        deviceInfo.subscriptionStatus == "EXPIRED"
                    
                    if (secureStorage.isAppSuspended() != shouldSuspend) {
                        StructuredLogger.w("DeviceManager", "Remote command: Setting app suspended to $shouldSuspend")
                        secureStorage.setAppSuspended(shouldSuspend)
                    }
                }
            }
        }
    }
}
