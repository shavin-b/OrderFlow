package com.orderflow.autoresponder.device.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.orderflow.autoresponder.device.model.DeviceCommand
import com.orderflow.autoresponder.device.model.DeviceInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDeviceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : DeviceRepository {

    override suspend fun registerDevice(deviceInfo: DeviceInfo): Result<Unit> {
        return try {
            firestore.collection("devices")
                .document(deviceInfo.deviceId)
                .set(deviceInfo, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDeviceInfo(deviceId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("devices")
                .document(deviceId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acknowledgeCommand(command: DeviceCommand): Result<Unit> {
        return try {
            // Acknowledge in global commands collection
            firestore.collection("commands")
                .document(command.commandId)
                .set(command)
                .await()
            
            // Also acknowledge in device sub-collection (common pattern for dashboards)
            firestore.collection("devices")
                .document(command.deviceId)
                .collection("commands")
                .document(command.commandId)
                .set(command)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDeviceSubscriptionFlow(deviceId: String): Flow<DeviceInfo?> = callbackFlow {
        val listener = firestore.collection("devices")
            .document(deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    com.orderflow.autoresponder.core.logger.StructuredLogger.d("FirebaseDeviceRepository", "[RAW] Snapshot data: $data")
                    trySend(snapshot.toObject(DeviceInfo::class.java))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getDeviceRawFlow(deviceId: String): Flow<Map<String, Any>?> = callbackFlow {
        val listener = firestore.collection("devices")
            .document(deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.data)
            }
        awaitClose { listener.remove() }
    }
}
