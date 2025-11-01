package com.tukanginAja.solusi.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TAHAP 16: Notification Service untuk kirim push notification dua arah via FCM
 * 
 * Fungsi utama:
 * - sendNotificationToUser: Kirim notifikasi ke user
 * - sendNotificationToTukang: Kirim notifikasi ke tukang
 * 
 * Menggunakan Firebase Cloud Functions untuk kirim notifikasi FCM.
 * Alternatif: Gunakan Admin SDK di backend jika tersedia.
 */
@Singleton
class NotificationService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val functions: FirebaseFunctions = Firebase.functions
    
    companion object {
        private const val TAG = "NotificationService"
    }
    
    /**
     * TAHAP 16: Kirim notifikasi ke user
     * 
     * @param userId User ID yang akan menerima notifikasi
     * @param title Judul notifikasi
     * @param body Isi notifikasi
     * @param orderId Order ID terkait (optional)
     * @param data Data tambahan untuk notifikasi (optional)
     */
    suspend fun sendNotificationToUser(
        userId: String,
        title: String,
        body: String,
        orderId: String? = null,
        data: Map<String, String> = emptyMap()
    ): Result<Unit> {
        return try {
            if (userId.isEmpty()) {
                Log.w(TAG, "Cannot send notification: User ID is empty")
                return Result.failure(IllegalArgumentException("User ID cannot be empty"))
            }
            
            // Ambil FCM token user dari Firestore
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (!userDoc.exists()) {
                Log.w(TAG, "User document not found: $userId")
                return Result.failure(IllegalArgumentException("User not found"))
            }
            
            val fcmToken = userDoc.getString("fcmToken")
            if (fcmToken.isNullOrEmpty()) {
                Log.w(TAG, "User FCM token not found: $userId")
                return Result.failure(IllegalArgumentException("User FCM token not found"))
            }
            
            Log.d(TAG, "Sending notification to user $userId with token: ${fcmToken.take(20)}...")
            
            // TAHAP 16: Kirim notifikasi via Firebase Cloud Functions
            // Alternatif: Jika tidak ada Cloud Functions, gunakan REST API Firebase FCM langsung
            val notificationData = mutableMapOf<String, Any>(
                "token" to fcmToken,
                "title" to title,
                "body" to body
            )
            
            if (orderId != null) {
                notificationData["orderId"] = orderId
            }
            
            notificationData.putAll(data)
            
            // Method 1: Gunakan Cloud Functions (recommended)
            try {
                functions
                    .getHttpsCallable("sendNotification")
                    .call(notificationData)
                    .await()
                
                Log.d(TAG, "Notification sent to user $userId via Cloud Functions")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.w(TAG, "Cloud Functions not available, using direct FCM call: ${e.message}")
                
                // Method 2: Fallback ke direct call (memerlukan server key)
                // Untuk production, sebaiknya gunakan Cloud Functions atau backend server
                Log.w(TAG, "Direct FCM call requires server key. Using Firestore notification queue instead.")
                
                // Method 3: Simpan ke Firestore notification queue untuk diproses oleh Cloud Functions
                firestore.collection("notification_queue")
                    .add(notificationData)
                    .await()
                
                Log.d(TAG, "Notification queued for user $userId")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification to user $userId", e)
            Result.failure(e)
        }
    }
    
    /**
     * TAHAP 16: Kirim notifikasi ke tukang
     * 
     * @param tukangId Tukang ID yang akan menerima notifikasi
     * @param title Judul notifikasi
     * @param body Isi notifikasi
     * @param orderId Order ID terkait (optional)
     * @param data Data tambahan untuk notifikasi (optional)
     */
    suspend fun sendNotificationToTukang(
        tukangId: String,
        title: String,
        body: String,
        orderId: String? = null,
        data: Map<String, String> = emptyMap()
    ): Result<Unit> {
        return try {
            if (tukangId.isEmpty()) {
                Log.w(TAG, "Cannot send notification: Tukang ID is empty")
                return Result.failure(IllegalArgumentException("Tukang ID cannot be empty"))
            }
            
            // Ambil FCM token tukang dari Firestore (bisa dari collection users atau tukang_locations)
            val tukangDoc = firestore.collection("users")
                .document(tukangId)
                .get()
                .await()
            
            // Jika tidak ada di users, coba dari tukang_locations
            val fcmToken = if (tukangDoc.exists()) {
                tukangDoc.getString("fcmToken")
            } else {
                val tukangLocationDoc = firestore.collection("tukang_locations")
                    .document(tukangId)
                    .get()
                    .await()
                tukangLocationDoc.getString("fcmToken")
            }
            
            if (fcmToken.isNullOrEmpty()) {
                Log.w(TAG, "Tukang FCM token not found: $tukangId")
                return Result.failure(IllegalArgumentException("Tukang FCM token not found"))
            }
            
            Log.d(TAG, "Sending notification to tukang $tukangId with token: ${fcmToken.take(20)}...")
            
            // TAHAP 16: Kirim notifikasi via Firebase Cloud Functions
            val notificationData = mutableMapOf<String, Any>(
                "token" to fcmToken,
                "title" to title,
                "body" to body
            )
            
            if (orderId != null) {
                notificationData["orderId"] = orderId
            }
            
            notificationData.putAll(data)
            
            // Method 1: Gunakan Cloud Functions (recommended)
            try {
                functions
                    .getHttpsCallable("sendNotification")
                    .call(notificationData)
                    .await()
                
                Log.d(TAG, "Notification sent to tukang $tukangId via Cloud Functions")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.w(TAG, "Cloud Functions not available, using Firestore notification queue: ${e.message}")
                
                // Method 2: Simpan ke Firestore notification queue untuk diproses oleh Cloud Functions
                firestore.collection("notification_queue")
                    .add(notificationData)
                    .await()
                
                Log.d(TAG, "Notification queued for tukang $tukangId")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification to tukang $tukangId", e)
            Result.failure(e)
        }
    }
    
    /**
     * TAHAP 16: Helper function untuk kirim notifikasi saat order baru dibuat
     * Kirim notifikasi ke tukang bahwa ada order baru
     */
    fun sendNewOrderNotificationToTukang(
        tukangId: String,
        orderId: String,
        customerId: String,
        description: String
    ) {
        serviceScope.launch {
            val result = sendNotificationToTukang(
                tukangId = tukangId,
                title = "Order Baru Tersedia",
                body = "Anda menerima order baru: $description",
                orderId = orderId,
                data = mapOf(
                    "type" to "new_order",
                    "customerId" to customerId,
                    "description" to description
                )
            )
            
            result.onFailure { error ->
                Log.e(TAG, "Failed to send new order notification to tukang $tukangId", error)
            }
        }
    }
    
    /**
     * TAHAP 16: Helper function untuk kirim notifikasi saat tukang mendekati lokasi
     * Kirim notifikasi ke user bahwa tukang mendekati lokasi
     */
    fun sendTukangArrivingNotificationToUser(
        userId: String,
        orderId: String,
        tukangName: String
    ) {
        serviceScope.launch {
            val result = sendNotificationToUser(
                userId = userId,
                title = "Tukang Mendekati Lokasi",
                body = "$tukangName sedang mendekati lokasi Anda",
                orderId = orderId,
                data = mapOf(
                    "type" to "tukang_arriving",
                    "tukangName" to tukangName
                )
            )
            
            result.onFailure { error ->
                Log.e(TAG, "Failed to send arriving notification to user $userId", error)
            }
        }
    }
    
    /**
     * TAHAP 16: Helper function untuk kirim notifikasi saat order selesai
     * Kirim notifikasi ke user bahwa order telah selesai
     */
    fun sendOrderCompletedNotificationToUser(
        userId: String,
        orderId: String,
        tukangName: String
    ) {
        serviceScope.launch {
            val result = sendNotificationToUser(
                userId = userId,
                title = "Order Selesai",
                body = "Order Anda telah diselesaikan oleh $tukangName",
                orderId = orderId,
                data = mapOf(
                    "type" to "order_completed",
                    "tukangName" to tukangName
                )
            )
            
            result.onFailure { error ->
                Log.e(TAG, "Failed to send completed notification to user $userId", error)
            }
        }
    }
}

