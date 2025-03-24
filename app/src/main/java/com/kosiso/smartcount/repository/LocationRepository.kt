package com.kosiso.foodshare.repository

import android.location.Location

interface LocationRepository {
    fun getLocationUpdates(callback: (Location) -> Unit, errorCallback: (Exception) -> Unit)
    fun stopLocationUpdates()

}