package com.kosiso.foodshare.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.firebase.firestore.GeoPoint
import javax.inject.Inject

class LocationRepositoryImplementation @Inject constructor(
    private var fusedLocationProviderClient: FusedLocationProviderClient,
    private var locationRequest: LocationRequest
): LocationRepository {

    private lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(
        callback: (Location) -> Unit,
        errorCallback: (Exception) -> Unit
    ) {
        Log.i("mstartLocationUpdates", "mstartLocationUpdates")

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)

                Log.i("mlocationCallback", "mlocationCallback")
                result?.locations?.let {locations ->
                    for (location in locations){
                        callback.invoke(location)
                        val geoPoint: GeoPoint = GeoPoint(location.latitude, location.longitude)
                        Log.i("mlocation updates gotten ", "$geoPoint")
                    }
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.i("mstartLocationUpdates 1", "mstartLocationUpdates")
    }

    override fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            Log.i("LocationRepository", "locationCallback is initialized")
        }else{
            Log.i("LocationRepository", "locationCallback is not initialized")
        }
    }

}