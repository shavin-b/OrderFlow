package com.orderflow.autoresponder.device.service

import android.content.Context
import com.google.firebase.Timestamp
import com.orderflow.autoresponder.core.security.SecureStorage
import com.orderflow.autoresponder.device.repository.DeviceRepository
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HeartbeatWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val deviceRepository: DeviceRepository,
    private val secureStorage: SecureStorage
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val deviceId = secureStorage.getDeviceId() ?: return Result.success()

        val updates = mapOf(
            "lastSeen" to Timestamp.now(),
            "lastSync" to Timestamp.now()
        )

        return try {
            deviceRepository.updateDeviceInfo(deviceId, updates)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
