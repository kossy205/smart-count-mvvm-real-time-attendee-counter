package com.kosiso.smartcount.di

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.kosiso.smartcount.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import com.kosiso.smartcount.R
import com.kosiso.smartcount.ui.uiScreens.MainActivity

@Module
@InstallIn(ServiceComponent::class)
object TapCountForegroundModule {

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        application: Application): NotificationManager =
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent): NotificationCompat.Builder{
        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_capture1)
            .setContentTitle("Smart Count")
            .setContentIntent(pendingIntent)
    }

    @ServiceScoped
    @Provides
    fun provideNotificationChannel(): NotificationChannel{
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "This is the notification channel for tap screen foreground service"
        return channel
    }

    @ServiceScoped
    @Provides
    fun provideVolunteerActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TAP_SCREEN
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

}