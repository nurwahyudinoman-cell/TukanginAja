package com.tukangin.modules.notification

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.tukanginAja.solusi.notification.NotificationHelper
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseMessaging: FirebaseMessaging,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    private var bookingUpdateHandler: ((bookingId: String, status: String) -> Unit)? = null

    fun setBookingUpdateHandler(handler: (bookingId: String, status: String) -> Unit) {
        bookingUpdateHandler = handler
    }

    suspend fun registerToken(token: String): Result<Unit> = runCatching {
        val currentUser = firebaseAuth.currentUser ?: throw IllegalStateException("User not logged in")
        val tokenRef = firestore.collection("users")
            .document(currentUser.uid)
            .collection("fcmTokens")
            .document(token)
        val data = mapOf(
            "token" to token,
            "createdAt" to System.currentTimeMillis()
        )
        tokenRef.set(data).await()
    }

    fun requestToken(onResult: (Result<String>) -> Unit) {
        firebaseMessaging.token
            .addOnSuccessListener { token -> onResult(Result.success(token)) }
            .addOnFailureListener { exception -> onResult(Result.failure(exception)) }
    }

    fun handleRemoteMessage(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "" 
        val body = message.notification?.body ?: message.data["body"] ?: ""
        if (title.isNotBlank() || body.isNotBlank()) {
            sendLocalNotification(title, body, message.data)
        }

        val bookingId = message.data["bookingId"]
        val status = message.data["status"]
        if (bookingId != null && status != null) {
            bookingUpdateHandler?.invoke(bookingId, status)
        }
    }

    fun sendLocalNotification(title: String, body: String, data: Map<String, String> = emptyMap()) {
        runCatching {
            NotificationHelper.showNotificationFromDataMessage(
                context = context,
                title = title,
                body = body,
                type = data["type"],
                chatId = data["chatId"],
                orderId = data["orderId"],
                payload = data
            )
        }
    }

    suspend fun sendNotificationRequest(targetUserId: String, payload: Map<String, Any?>): Result<Unit> = runCatching {
        val currentUser = firebaseAuth.currentUser ?: throw IllegalStateException("User not logged in")
        val request = mapOf(
            "toUserId" to targetUserId,
            "payload" to payload,
            "createdBy" to currentUser.uid,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("notification_requests").add(request).await()
    }
}

