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
                    secureStorage.setAdminLocked(true)
                    secureStorage.setAutoResponderEnabled(false)
                }
                "UNLOCK" -> {
                    secureStorage.setAdminLocked(false)
                    secureStorage.setAutoResponderEnabled(true)
                }
                "SYNC" -> {
                    // Subscription is already observed via Flow in DeviceManager
                    // This command can be used to force a refresh if needed in future
                }
                else -> {
                    success = false
                    error = "Unsupported command type"
                }
            }
        } catch (e: Exception) {
            success = false
            error = "Internal processing error"
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
        
        val result = deviceRepository.acknowledgeCommand(acknowledgement)
        if (result.isFailure) {
            StructuredLogger.e("OrderFlowFcmService", "Failed to acknowledge command to Firestore")
        }
    }
}
