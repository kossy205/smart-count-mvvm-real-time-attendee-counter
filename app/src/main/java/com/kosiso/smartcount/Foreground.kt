package com.kosiso.smartcount

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.kosiso.foodshare.repository.LocationRepository
import com.kosiso.smartcount.repository.MainRepository
import com.kosiso.smartcount.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class Foreground: LifecycleService() {

    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var notificationBuilder: NotificationCompat.Builder
    @Inject lateinit var notificationChannel: NotificationChannel
    @Inject lateinit var mainRepository: MainRepository


    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        serviceScope.launch{
            updateNotification()
        }
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                Constants.ACTION_START -> {
                    startForegroundService()
                }
                Constants.ACTION_STOP -> killService()
                Constants.ACTION_START_LOCATION_UPDATE ->{
//                    getCurrentLocationUpdate()
                }
                Constants.ACTION_STOP_LOCATION_UPDATE ->{
//                    stopLocationUpdates()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        Log.i("service 1","start service")

        val displayCount = mainRepository.count.value

        createNotificationChannel()
        val notification = buildNotification(displayCount)

        startForeground(Constants.NOTIFICATION_ID, notification)
        Log.i("service 2","start service")
    }

    private fun createNotificationChannel(){
        // WE USED NOTIFICATION OF LOW IMPORTANCE BECAUSE WE DON'T WANT IT TO MK SOUNDS WHENEVER WE UPDATE NOTIFICATION
        // WE WOULD BE UPDATING NOTIFICATION EVERY TIME VOLUME BUTTON IS CLICKED
        // CHECK DEPENDENCY MODULE TO SEE WHAT I'M TALKING ABOUT.
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun buildNotification(count: Int): Notification{
        return notificationBuilder
            .setContentText("Counts: $count ppl")
            .build()
    }

    private suspend fun updateNotification(){
        mainRepository.count.collect{
            val updatedNotification = buildNotification(it)
            notificationManager.notify(Constants.NOTIFICATION_ID, updatedNotification)
        }
    }

    private fun handleVolumeUp() {
        mainRepository.increment()
    }
    private fun handleVolumeDown() {
        mainRepository.decrement()
    }

    private fun killService(){
        Log.i("service 1","stop service")
//        stopLocationUpdates()
        serviceScope.cancel()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        killService()
    }

    // Kill the service when the app is removed from recent tasks or recent apps
//    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//        killService()
//    }

//    private fun getCurrentLocationUpdate(){
//        locationRepository.getLocationUpdates(
//            {location ->
//                val geoPoint = GeoPoint(location.latitude, location.longitude)
//                serviceScope.launch{
//                    mainRepository.setLocationUsingGeoFirestore(mainRepository.getCurrentUser()!!.uid, geoPoint).apply {
//                        onSuccess {
//                            Log.i("geofirestore location", "Location set successfully $geoPoint")
//                        }
//                        onFailure {
//                            Log.i("geofirestore location", "error: ${it.message}")
//                        }
//                    }
//                }
//                Log.i("service location","$geoPoint")
//            },{exception ->
//                Log.i("service location exception","$exception")
//            }
//        )
//    }
//
//    private fun stopLocationUpdates() {
//        Log.i("service location stopped", "stopped")
//        locationRepository.stopLocationUpdates()
//    }

}