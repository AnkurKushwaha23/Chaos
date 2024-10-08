package com.ankurkushwaha.chaos

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ankurkushwaha.chaos.services.CHANNEL_ID
import com.ankurkushwaha.chaos.services.CHANNEL_NAME
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class BaseApplication :Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }
}