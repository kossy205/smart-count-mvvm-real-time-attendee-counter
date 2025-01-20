package com.kosiso.smartcount

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.kosiso.smartcount.repository.MainRepository
import com.kosiso.smartcount.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TapCountForeground: LifecycleService() {

    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var notificationBuilder: NotificationCompat.Builder
    @Inject lateinit var notificationChannel: NotificationChannel
    @Inject lateinit var mainRepository: MainRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var volumeReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        serviceScope.launch{
            updateNotification()
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                Constants.ACTION_START -> {
                    startForegroundService()
                }
                Constants.ACTION_STOP -> killService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        Log.i("service 1","start service")

        createNotificationChannel()
        val notification = buildNotification(0)

        startForeground(Constants.NOTIFICATION_ID, notification)
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
        serviceScope.cancel()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }

    // Kill the service when the app is removed from recent tasks or recent apps
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        killService()
    }


}