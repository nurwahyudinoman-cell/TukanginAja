package com.tukanginAja.solusi.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.tukanginAja.solusi.MainActivity
import com.tukanginAja.solusi.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Background location tracking service for tukang
 * Continuously updates tukang position in Firestore even when app is in background
 */
@AndroidEntryPoint
class BackgroundLocationService : Service() {
    
    @Inject
    lateinit var firestore: FirebaseFirestore
    
    private val binder = LocalBinder()
    private var tukangId: String = ""
    private var tukangName: String = ""
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    
    private var isTracking = false
    private var lastUpdateTime = 0L
    // TAHAP 16: Update interval dari 5 detik ke 12 detik (10-15 detik sesuai spesifikasi)
    private val MIN_UPDATE_INTERVAL = 12000L // 12 seconds minimum interval between updates
    private val MIN_DISTANCE_METERS = 15.0 // Minimum distance change to trigger update (15 meters)
    private var lastLocation: Location? = null
    // TAHAP 16: Track current order ID untuk monitoring status
    private var currentOrderId: String? = null
    private var orderStatusListener: com.google.firebase.firestore.ListenerRegistration? = null
    
    // TAHAP 16: Stats tracking for verification report
    private var writesAttempted = 0
    private var writesExecuted = 0
    private var writesSkipped = 0
    private var serviceStartTime = 0L
    private var serviceStopTime = 0L
    
    inner class LocalBinder : Binder() {
        fun getService(): BackgroundLocationService = this@BackgroundLocationService
    }
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallback()
        createNotificationChannel()
        Log.d(TAG, "BackgroundLocationService created")
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                val id = intent.getStringExtra(EXTRA_TUKANG_ID) ?: ""
                val name = intent.getStringExtra(EXTRA_TUKANG_NAME) ?: ""
                // TAHAP 16: Ambil orderId dari intent jika ada
                val orderId = intent.getStringExtra(EXTRA_ORDER_ID)
                startTracking(id, name, orderId)
            }
            ACTION_STOP_TRACKING -> {
                stopTracking()
            }
        }
        return START_STICKY
    }
    
    /**
     * Start tracking tukang location
     * TAHAP 16: Tambahkan parameter orderId untuk monitoring status order
     */
    fun startTracking(id: String, name: String, orderId: String? = null) {
        if (id.isEmpty()) {
            Log.w(TAG, "Cannot start tracking: tukang ID is empty")
            return
        }
        
        tukangId = id
        tukangName = name
        currentOrderId = orderId
        
        if (isTracking) {
            Log.d(TAG, "Already tracking, updating tukang info")
            // TAHAP 16: Update order listener jika ada order baru
            if (orderId != null) {
                startOrderStatusListener(orderId)
            }
            return
        }
        
        isTracking = true
        serviceStartTime = System.currentTimeMillis()
        startForeground(NOTIFICATION_ID, createNotification())
        
        Log.d(TAG, "Tracking: service started at ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date(serviceStartTime))}")
        
        // TAHAP 16: Start monitoring order status jika ada orderId
        if (orderId != null) {
            startOrderStatusListener(orderId)
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Started location tracking for tukang: $tukangName ($tukangId)")
            if (orderId != null) {
                Log.d(TAG, "Monitoring order status: $orderId")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
            stopSelf()
        }
    }
    
    /**
     * Stop tracking tukang location
     * TAHAP 16: Stop order status listener juga
     */
    fun stopTracking() {
        if (!isTracking) {
            return
        }
        
        isTracking = false
        serviceStopTime = System.currentTimeMillis()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        
        // TAHAP 16: Stop monitoring order status
        stopOrderStatusListener()
        
        // TAHAP 16: Log service stop dengan stats
        val duration = serviceStopTime - serviceStartTime
        Log.d(TAG, "Tracking: service stopped at ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date(serviceStopTime))}")
        Log.d(TAG, "Tracking: stats - attempted: $writesAttempted, executed: $writesExecuted, skipped: $writesSkipped, duration: ${duration}ms")
        
        // Update status to offline in Firestore
        serviceScope.launch {
            try {
                firestore.collection("tukang_locations")
                    .document(tukangId)
                    .update(
                        mapOf(
                            "status" to "offline",
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                Log.d(TAG, "Stopped tracking and set status to offline for tukang: $tukangId")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating offline status", e)
            }
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    /**
     * TAHAP 16: Start monitoring order status untuk stop otomatis saat completed
     */
    private fun startOrderStatusListener(orderId: String) {
        // Stop existing listener jika ada
        stopOrderStatusListener()
        
        orderStatusListener = firestore.collection("service_requests")
            .document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to order status", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status") ?: ""
                    Log.d(TAG, "Order $orderId status changed to: $status")
                    
                    // TAHAP 16: Stop tracking otomatis jika order completed atau cancelled
                    if (status == "completed" || status == "cancelled") {
                        Log.d(TAG, "Order $status, stopping location tracking automatically")
                        stopTracking()
                    }
                }
            }
    }
    
    /**
     * TAHAP 16: Stop monitoring order status
     */
    private fun stopOrderStatusListener() {
        orderStatusListener?.remove()
        orderStatusListener = null
        currentOrderId = null
    }
    
    /**
     * Create location request configuration
     */
    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            MIN_UPDATE_INTERVAL
        ).apply {
            // TAHAP 16: Convert Double to Float for setMinUpdateDistanceMeters
            setMinUpdateDistanceMeters(MIN_DISTANCE_METERS.toFloat())
            setMaxUpdateDelayMillis(10000L) // Maximum delay between updates
        }.build()
    }
    
    /**
     * Create location callback for handling location updates
     */
    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.lastLocation?.let { location ->
                    updateTukangLocation(location)
                }
            }
        }
    }
    
    /**
     * Update tukang location in Firestore
     * TAHAP 16: Pastikan GeoPoint digunakan sesuai spesifikasi
     * Enhanced dengan detailed throttling logs untuk observability
     */
    private fun updateTukangLocation(location: Location) {
        val currentTime = System.currentTimeMillis()
        writesAttempted++
        
        // Throttling: Skip if update is too frequent or distance change is too small
        if (lastLocation != null) {
            val distance = lastLocation!!.distanceTo(location)
            val timeSinceLastUpdate = currentTime - lastUpdateTime
            
            if (distance < MIN_DISTANCE_METERS && timeSinceLastUpdate < MIN_UPDATE_INTERVAL) {
                // TAHAP 16: Log skipped writes untuk observability
                writesSkipped++
                Log.d(TAG, "Tracking: skipped write - distance ${String.format("%.2f", distance)}m, time ${timeSinceLastUpdate}ms")
                return
            }
        }
        
        lastLocation = location
        lastUpdateTime = currentTime
        writesExecuted++
        
        serviceScope.launch {
            try {
                // TAHAP 16: Gunakan GeoPoint untuk menyimpan posisi di Firestore (sesuai spesifikasi)
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                
                firestore.collection("tukang_locations")
                    .document(tukangId)
                    .update(
                        mapOf(
                            "lat" to location.latitude,
                            "lng" to location.longitude,
                            "location" to geoPoint, // TAHAP 16: Tambahkan GeoPoint field
                            "status" to "online",
                            "updatedAt" to currentTime
                        )
                    )
                
                Log.d(TAG, "Tracking: updated location for tukang $tukangId: ${location.latitude}, ${location.longitude} (GeoPoint: $geoPoint)")
                
                // Update notification with latest location info
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, createNotification())
            } catch (e: Exception) {
                Log.e(TAG, "Error updating tukang location", e)
            }
        }
    }
    
    /**
     * TAHAP 16: Get tracking stats for verification report
     */
    fun getTrackingStats(): Map<String, Any> {
        return mapOf(
            "writesAttempted" to writesAttempted,
            "writesExecuted" to writesExecuted,
            "writesSkipped" to writesSkipped,
            "serviceStartTime" to serviceStartTime,
            "serviceStopTime" to serviceStopTime,
            "isTracking" to isTracking,
            "currentOrderId" to (currentOrderId ?: "")
        )
    }
    
    /**
     * Create notification channel for Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create foreground service notification
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Melacak Lokasi Tukang")
            .setContentText("Memperbarui lokasi: $tukangName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // TAHAP 16: Pastikan listener dihentikan
        stopOrderStatusListener()
        if (isTracking) {
            stopTracking()
        }
        Log.d(TAG, "BackgroundLocationService destroyed")
    }
    
    companion object {
        private const val TAG = "BackgroundLocationService"
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Location Tracking"
        private const val CHANNEL_DESCRIPTION = "Shows notification when tracking tukang location"
        private const val NOTIFICATION_ID = 1
        
        const val ACTION_START_TRACKING = "com.tukanginAja.solusi.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.tukanginAja.solusi.STOP_TRACKING"
        const val EXTRA_TUKANG_ID = "tukang_id"
        const val EXTRA_TUKANG_NAME = "tukang_name"
        // TAHAP 16: Tambahkan extra untuk orderId
        const val EXTRA_ORDER_ID = "order_id"
    }
}
