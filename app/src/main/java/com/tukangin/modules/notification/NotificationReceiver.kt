package com.tukangin.modules.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationReceiver : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.data["title"] ?: message.notification?.title ?: "Tukangin Update"
        val body = message.data["body"] ?: message.notification?.body ?: "You have a new update!"
        NotificationService.showNotification(applicationContext, title, body)
    }
}

