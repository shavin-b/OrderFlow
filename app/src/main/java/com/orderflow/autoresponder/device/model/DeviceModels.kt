package com.orderflow.autoresponder.device.model

import com.google.firebase.Timestamp

data class DeviceInfo(
    val deviceId: String = "",
    val phoneModel: String = "",
    val manufacturer: String = "",
    val androidVersion: String = "",
    val appVersion: String = "",
    val appBuildNumber: Int = 0,
    val installationDate: Long = 0,
    val userName: String = "",
    val businessName: String = "",
    val firstSeen: Timestamp? = null,
    val lastSeen: Timestamp? = null,
    val lastSync: Timestamp? = null,
    val status: String = "Active", // Overall status
    val lockStatus: String = "UNLOCKED",
    val lockReason: String = "NONE",
    val adminLock: Boolean = false, // Check if the website uses a boolean
    val isLocked: Boolean = false,
    val generatedUuid: String = "",
    val lastCommandId: String? = null,
    val lastCommandStatus: String? = null,
    val fcmToken: String = "",
    val subscriptionStart: Timestamp? = null,
    val subscriptionEnd: Timestamp? = null,
    val subscriptionStatus: String = "ACTIVE"
)

data class DeviceCommand(
    val commandId: String = "",
    val deviceId: String = "",
    val command: String = "",
    val status: String = "PENDING",
    val processedAt: Timestamp? = null,
    val errorMessage: String? = null
)
