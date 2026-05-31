package com.example.services

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.example.data.entity.EmergencyEvent
import com.example.data.repository.SosRepository
import com.example.utils.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SosService(
    private val context: Context,
    private val repository: SosRepository,
    private val locationService: LocationService,
    private val batteryService: BatteryService,
    private val contactService: ContactService,
    private val cameraService: CameraService,
    private val audioService: AudioService
) {
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    fun triggerSos(lifecycleOwner: LifecycleOwner, scope: CoroutineScope) {
        scope.launch {
            try {
                // 1. Get Battery percentage
                val battery = batteryService.getBatteryPercentage()
                
                // 2. Get Location
                val location = locationService.getCurrentLocation()
                val latitude = location?.latitude ?: 0.0
                val longitude = location?.longitude ?: 0.0
                val locationUrl = if (location != null) {
                    locationService.makeGoogleMapsLink(latitude, longitude)
                } else {
                    "Unavailable"
                }

                // 3. Make Distress Message & Format Time
                val timestamp = System.currentTimeMillis()
                val formattedTime = TimeFormatter.format(timestamp)
                
                val userDistressMessage = "SOS ALERT! I need help."
                val smsMessage = buildString {
                    appendLine(userDistressMessage)
                    appendLine("Time: $formattedTime")
                    appendLine("Battery: $battery%")
                    append("Location: $locationUrl")
                }

                // 4. Send SMS to all configured contacts immediately
                val contacts = repository.getAllContacts()
                var smsSuccess = false
                if (contacts.isNotEmpty()) {
                    for (contact in contacts) {
                        launch(Dispatchers.IO) {
                            contactService.sendSms(contact.phoneNumber, smsMessage)
                        }
                    }
                    smsSuccess = true
                } else {
                    Log.w("SosService", "No emergency contacts configured to send SMS.")
                }

                // 5. Check if Selfie or Audio is enabled
                val isSelfieEnabled = repository.getIsSelfieEnabled()
                val isAudioEnabled = repository.getIsAudioEnabled()

                var selfiePath: String? = null
                var audioPath: String? = null

                // Launch Audio Recording if enabled (30 seconds)
                if (isAudioEnabled) {
                    audioPath = audioService.startRecording()
                    if (audioPath != null) {
                        val finalAudioPath = audioPath
                        launch(Dispatchers.IO) {
                            delay(30000)
                            audioService.stopRecording()
                        }
                    }
                }

                // Database insert helper
                suspend fun insertDbEvent(finalSelfie: String?, finalBackPhoto: String?, finalAudio: String?) {
                    val event = EmergencyEvent(
                        formattedTime = formattedTime,
                        locationUrl = locationUrl,
                        latitude = latitude,
                        longitude = longitude,
                        batteryPercentage = battery,
                        selfieFilePath = finalSelfie,
                        backCameraFilePath = finalBackPhoto,
                        audioFilePath = finalAudio,
                        distressMessage = smsMessage,
                        status = if (smsSuccess) "Dispatched" else "Stored Offline"
                    )
                    repository.insertEvent(event)
                }

                // Process selfie and back photo capture sequentially
                if (isSelfieEnabled) {
                    withContext(Dispatchers.Main) {
                        cameraService.takeSelfie(lifecycleOwner) { frontPath ->
                            cameraService.takeBackPhoto(lifecycleOwner) { backPath ->
                                serviceScope.launch {
                                    insertDbEvent(frontPath, backPath, audioPath)
                                }
                            }
                        }
                    }
                } else {
                    insertDbEvent(null, null, audioPath)
                }

            } catch (e: Exception) {
                Log.e("SosService", "Error during SOS alert flow execution", e)
            }
        }
    }

    fun stopSos(scope: CoroutineScope, onFinished: (Boolean) -> Unit) {
        scope.launch {
            try {
                // stop audio recording immediately
                audioService.stopRecording()

                // Send "I Am Safe" messages to all contacts
                val contacts = repository.getAllContacts()
                if (contacts.isNotEmpty()) {
                    for (contact in contacts) {
                        launch(Dispatchers.IO) {
                            contactService.sendSms(contact.phoneNumber, "Safe now. Previous SOS can be ignored.")
                        }
                    }
                    onFinished(true)
                } else {
                    onFinished(false)
                }
            } catch (e: Exception) {
                Log.e("SosService", "Error during Stop SOS logic execution", e)
                onFinished(false)
            }
        }
    }
}
