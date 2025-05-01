package com.kosiso.smartcount.repository

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.firebase.firestore.GeoPoint
import com.kosiso.foodshare.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImplementation @Inject constructor(
    private var fusedLocationProviderClient: FusedLocationProviderClient,
    private var locationRequest: LocationRequest
): LocationRepository {

    // Initialize locationCallback immediately to prevent "not initialized" issues
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            Log.i("LocationRepository", "Location callback triggered")
            result.locations.forEach { location ->
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                Log.i("LocationRepository", "Location update: $geoPoint")
                callback?.invoke(location)
            }
        }
    }

    // Keep track of callback state
    private var isTracking = false
    private var callback: ((Location) -> Unit)? = null

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(
        callback: (Location) -> Unit,
        errorCallback: (Exception) -> Unit
    ) {
        if (isTracking) {
            Log.i("LocationRepository", "Already tracking location - stopping previous updates")
            stopLocationUpdates()
        }

        Log.i("LocationRepository", "Starting location updates")
        this.callback = callback

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            isTracking = true
            Log.i("LocationRepository", "Location updates requested successfully")
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error requesting location updates: ${e.message}")
            errorCallback(e)
            isTracking = false
        }
    }

    override fun stopLocationUpdates() {
        Log.i("LocationRepository", "Stopping location updates, isTracking=$isTracking")

        if (isTracking) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("LocationRepository", "Location updates stopped successfully")
                    } else {
                        Log.e("LocationRepository", "Failed to stop location updates: ${task.exception?.message}")
                    }
                }

            callback = null
            isTracking = false
        }
    }
}