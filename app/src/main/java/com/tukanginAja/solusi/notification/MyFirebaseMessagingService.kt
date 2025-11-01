package com.tukanginAja.solusi.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tukanginAja.solusi.MainActivity
import com.tukanginAja.solusi.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase Cloud Messaging Service for handling push notifications
 * Handles receiving notifications and saving FCM tokens to Firestore
 */
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var firestore: FirebaseFirestore
    
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message received from: ${message.from}")
        Log.d(TAG, "Message data: ${message.data}")
        
        // Handle notification payload
        message.notification?.let { notification ->
            Log.d(TAG, "Notification title: ${notification.title}")
            Log.d(TAG, "Notification body: ${notification.body}")
            
            // Check if this is a chat message from data payload
            val messageType = message.data["type"] ?: ""
            if (messageType == "chat" || message.data.containsKey("chatId")) {
                // Handle chat notification
                val chatId = message.data["chatId"]
                val senderId = message.data["senderId"]
                
                NotificationHelper.showChatNotification(
                    context = this,
                    title = notification.title ?: "Pesan Baru",
                    message = notification.body ?: "Anda menerima pesan baru",
                    chatId = chatId,
                    senderId = senderId
                )
            } else {
                // Handle other notifications
                showNotification(
                    title = notification.title ?: "TukanginAja",
                    body = notification.body ?: "",
                    data = message.data
                )
            }
        }
        
        // TAHAP 16: Handle data-only payload (when app is in foreground, no notification is shown automatically)
        // Format: { "title": "...", "body": "...", "type": "chat|order|proximity", "chatId": "...", "orderId": "..."}
        if (message.data.isNotEmpty() && message.notification == null) {
            Log.d(TAG, "Data-only message received")
            
            val messageType = message.data["type"] ?: ""
            val title = message.data["title"] ?: "TukanginAja"
            val body = message.data["body"] ?: ""
            val chatId = message.data["chatId"]
            val orderId = message.data["orderId"]
            val senderId = message.data["senderId"]
            
            // TAHAP 16: Handle based on type (chat|order|proximity)
            when (messageType) {
                "chat" -> {
                    // Chat notification with deeplink
                    NotificationHelper.showChatNotification(
                        context = this,
                        title = title,
                        message = body,
                        chatId = chatId,
                        senderId = senderId
                    )
                    Log.d(TAG, "FCM: Chat notification shown - chatId: $chatId")
                }
                "order", "proximity" -> {
                    // Order or proximity notification with deeplink
                    NotificationHelper.showNotificationFromDataMessage(
                        context = this,
                        title = title,
                        body = body,
                        type = messageType,
                        orderId = orderId,
                        chatId = chatId,
                        payload = message.data
                    )
                    Log.d(TAG, "FCM: $messageType notification shown - orderId: $orderId")
                }
                else -> {
                    // Generic notification
                    NotificationHelper.showNotificationFromDataMessage(
                        context = this,
                        title = title,
                        body = body,
                        type = null,
                        payload = message.data
                    )
                    Log.d(TAG, "FCM: Generic notification shown")
                }
            }
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        Log.d(TAG, "New FCM token: $token")
        sendTokenToServer(token)
    }
    
    /**
     * Send FCM token to Firestore user document
     */
    private fun sendTokenToServer(token: String) {
        serviceScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId == null) {
                    Log.w(TAG, "Cannot save FCM token: User not logged in")
                    return@launch
                }
                
                // Save token to Firestore users collection
                firestore.collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "fcmToken" to token,
                            "fcmTokenUpdatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
                
                Log.d(TAG, "FCM token saved to Firestore for user: $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving FCM token to Firestore", e)
                
                // If update fails, try setting the document
                try {
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null) {
                        firestore.collection("users")
                            .document(userId)
                            .set(
                                mapOf(
                                    "fcmToken" to token,
                                    "fcmTokenUpdatedAt" to System.currentTimeMillis()
                                ),
                                com.google.firebase.firestore.SetOptions.merge()
                            )
                            .await()
                        Log.d(TAG, "FCM token set (merged) to Firestore for user: $userId")
                    }
                } catch (e2: Exception) {
                    Log.e(TAG, "Error setting FCM token to Firestore", e2)
                }
            }
        }
    }
    
    /**
     * Show system notification
     */
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        createNotificationChannel()
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add any extra data from notification
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    /**
     * Create notification channel for Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "tukanginaja_notifications"
        private const val CHANNEL_NAME = "TukanginAja Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for order updates and tukang location"
    }
}

