package com.example.services

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.util.Log

class ContactService(private val context: Context) {
    
    fun sendSms(phoneNumber: String, message: String): Boolean {
        if (phoneNumber.isBlank()) return false
        return try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            Log.d("ContactService", "SMS sent successfully to $phoneNumber: $message")
            true
        } catch (e: Exception) {
            Log.e("ContactService", "Failed to send SMS to $phoneNumber", e)
            false
        }
    }
}
