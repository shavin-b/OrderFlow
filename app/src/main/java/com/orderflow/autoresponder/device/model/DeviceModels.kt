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
    val firstSeen: Timestamp? = null,
    val lastSeen: Timestamp? = null,
    val lastSync: Timestamp? = null,
    val status: String = "Active",
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
