package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_events")
data class EmergencyEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String,
    val locationUrl: String,
    val latitude: Double,
    val longitude: Double,
    val batteryPercentage: Int,
    val selfieFilePath: String?,
    val backCameraFilePath: String? = null,
    val audioFilePath: String?,
    val distressMessage: String,
    val status: String
)
