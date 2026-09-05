package com.orderflow.autoresponder.device.messaging

import com.google.firebase.Timestamp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orderflow.autoresponder.core.logger.StructuredLogger
import com.orderflow.autoresponder.core.security.SecureStorage
import com.orderflow.autoresponder.device.model.DeviceCommand
import com.orderflow.autoresponder.device.repository.DeviceRepository
import com.orderflow.autoresponder.device.service.DeviceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrderFlowFcmService : FirebaseMessagingService() {

    @Inject
    lateinit var secureStorage: SecureStorage

    @Inject
    lateinit var deviceRepository: DeviceRepository

    @Inject
    lateinit var deviceManager: DeviceManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        StructuredLogger.i("OrderFlowFcmService", "New FCM Token received")
        
        val deviceId = secureStorage.getDeviceId() ?: return
        serviceScope.launch {
            deviceManager.synchronizeFcmToken(deviceId)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        
        // 1. Strict field presence validation
        val command = data["command"]
        val targetDeviceId = data["deviceId"]
        val commandId = data["commandId"]

        if (command.isNullOrBlank() || targetDeviceId.isNullOrBlank() || commandId.isNullOrBlank()) {
            StructuredLogger.w("OrderFlowFcmService", "Ignored FCM message with missing required fields")
            return
        }

        // 2. Identity verification
        val localDeviceId = secureStorage.getDeviceId()
        if (targetDeviceId != localDeviceId) {
            StructuredLogger.w("OrderFlowFcmService", "Ignored command for different device: $targetDeviceId")
            return
        }

        // 3. Persistent duplicate protection
        if (secureStorage.isCommandProcessed(commandId)) {
            StructuredLogger.d("OrderFlowFcmService", "Command $commandId already processed. Skipping.")
            return
        }

        StructuredLogger.i("OrderFlowFcmService", "Processing command: $command")
        
        serviceScope.launch {
            processCommand(command, commandId)
        }
    }

    private suspend fun processCommand(command: String, commandId: String) {
        var success = true
        var error: String? = null

        val deviceId = secureStorage.getDeviceId()
        if (deviceId == null) {
            StructuredLogger.e("OrderFlowFcmService", "Cannot process command: local deviceId is null")
            return
        }

        try {
            when (command) {
                "LOCK" -> {
                    StructuredLogger.w("OrderFlowFcmService", "[DeviceControl] Lock command received via FCM")
                    secureStorage.setAdminLocked(true)
                    secureStorage.setLockReason("ADMIN")
                    // Note: Auto-responder guard in ProcessIncomingMessageUseCase will handle blocking
                }
                "UNLOCK" -> {
                    StructuredLogger.i("OrderFlowFcmService", "[DeviceControl] Unlock command received via FCM")
                    secureStorage.setAdminLocked(false)
                    secureStorage.setLockReason("NONE")
                }
                "SYNC" -> {
                    StructuredLogger.i("OrderFlowFcmService", "[DeviceControl] Sync command received via FCM")
                    // Snapshot listener in DeviceManager will handle state updates automatically
                    // But we can trigger a manual sync of the FCM token if requested
                    deviceManager.synchronizeFcmToken(deviceId)
                }
                else -> {
                    success = false
                    error = "Unsupported command type: $command"
                }
            }
        } catch (e: Exception) {
            success = false
            error = "Internal processing error: ${e.message}"
            StructuredLogger.e("OrderFlowFcmService", "Command execution failed", e)
        }

        if (success) {
            secureStorage.markCommandAsProcessed(commandId)
        }

        // 4. Acknowledge to Firestore
        val acknowledgement = DeviceCommand(
            commandId = commandId,
            deviceId = deviceId,
            command = command,
            status = if (success) "SUCCESS" else "FAILED",
            processedAt = Timestamp.now(),
            errorMessage = error
        )
        
        // Update both the specific command document and the device summary
        deviceRepository.acknowledgeCommand(acknowledgement)
        deviceRepository.updateDeviceInfo(deviceId, mapOf(
            "lastCommandId" to commandId,
            "lastCommandStatus" to if (success) "SUCCESS" else "FAILED",
            "lastSync" to Timestamp.now()
        ))
    }
}
