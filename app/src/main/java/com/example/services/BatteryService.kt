package com.example.services

import android.content.Context
import android.os.BatteryManager
import android.util.Log

class BatteryService(private val context: Context) {
    fun getBatteryPercentage(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 80
        } catch (e: Exception) {
            Log.e("BatteryService", "Failed to retrieve battery percentage", e)
            80
        }
    }
}
