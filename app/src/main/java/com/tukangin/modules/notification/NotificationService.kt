package com.tukangin.modules.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tukanginAja.solusi.R

object NotificationService {

    private const val CHANNEL_ID = "tukangin_notifications"
    private const val CHANNEL_NAME = "Tukangin Updates"
    private const val CHANNEL_DESCRIPTION = "Service updates and booking status"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val manager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getSystemService(NotificationManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                ContextCompat.getSystemService(context, NotificationManager::class.java)
            }
            manager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        createChannel(context)

        val notification = NotificationCompat.Builder(context.applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val managerCompat = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionState = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        if (!managerCompat.areNotificationsEnabled()) {
            return
        }

        runCatching {
            managerCompat.notify(
                (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                notification
            )
        }
    }
}

