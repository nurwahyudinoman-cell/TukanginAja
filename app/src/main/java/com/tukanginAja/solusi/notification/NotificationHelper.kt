package com.tukanginAja.solusi.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tukanginAja.solusi.MainActivity
import com.tukanginAja.solusi.R

/**
 * Helper class for creating and showing notifications
 * Specifically designed for chat notifications
 */
object NotificationHelper {
    
    private const val CHANNEL_ID_CHAT = "chat_channel"
    private const val CHANNEL_NAME_CHAT = "Chat Notifications"
    private const val CHANNEL_DESCRIPTION_CHAT = "Notifications for new chat messages"
    
    /**
     * Show a chat notification
     * @param context Application context
     * @param title Notification title
     * @param message Notification message body
     * @param chatId Chat ID to navigate to when notification is clicked
     * @param senderId Sender user ID
     */
    fun showChatNotification(
        context: Context,
        title: String,
        message: String,
        chatId: String? = null,
        senderId: String? = null
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_CHAT,
                CHANNEL_NAME_CHAT,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION_CHAT
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open MainActivity and navigate to ChatScreen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (chatId != null) {
                putExtra("chatId", chatId)
            }
            if (senderId != null) {
                putExtra("senderId", senderId)
            }
            putExtra("navigateTo", "chat")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CHAT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
        
        // Show notification with unique ID based on timestamp
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * TAHAP 16: Show notification from FCM data message with deeplink navigation
     * Supports types: chat, order, proximity
     */
    fun showNotificationFromDataMessage(
        context: Context,
        title: String,
        body: String,
        type: String? = null,
        chatId: String? = null,
        orderId: String? = null,
        payload: Map<String, String> = emptyMap()
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O+
        val channelId = "tukanginaja_notifications"
        val channelName = "TukanginAja Notifications"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General notifications from TukanginAja"
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent with deeplink based on type
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            when (type) {
                "chat" -> {
                    putExtra("navigateTo", "chat")
                    chatId?.let { putExtra("chatId", it) }
                    payload["senderId"]?.let { putExtra("senderId", it) }
                }
                "order" -> {
                    putExtra("navigateTo", "order")
                    orderId?.let { putExtra("orderId", it) }
                }
                "proximity" -> {
                    putExtra("navigateTo", "route")
                    orderId?.let { putExtra("orderId", it) }
                }
                else -> {
                    // Generic notification
                    putExtra("navigateTo", "home")
                }
            }
            
            // Add all payload extras
            payload.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    /**
     * Show a generic notification (for order updates, etc.)
     */
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = "tukanginaja_notifications",
        channelName: String = "TukanginAja Notifications"
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General notifications from TukanginAja"
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
