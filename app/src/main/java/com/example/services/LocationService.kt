package com.example.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationService(private val context: Context) {
    
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        continuation.resume(task.result)
                    } else {
                        continuation.resume(getFallbackLocation())
                    }
                }
            } catch (e: Exception) {
                continuation.resume(getFallbackLocation())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getFallbackLocation(): Location? {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }
            return bestLocation
        } catch (e: Exception) {
            return null
        }
    }

    fun makeGoogleMapsLink(latitude: Double, longitude: Double): String {
        return "https://www.google.com/maps?q=$latitude,$longitude"
    }
}
