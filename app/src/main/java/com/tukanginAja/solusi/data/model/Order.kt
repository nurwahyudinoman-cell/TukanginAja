package com.tukanginAja.solusi.data.model


/**
 * Data class representing an order in Firestore
 * 
 * Example Firestore document:
 * {
 *   "id": "ord001",
 *   "userId": "u001",
 *   "userName": "John Doe",
 *   "tukangId": "t001",
 *   "status": "requested",
 *   "serviceType": "AC Repair",
 *   "location": {
 *     "lat": -6.2088,
 *     "lng": 106.8456,
 *     "address": "Jl. Sudirman No. 123, Jakarta"
 *   },
 *   "price": 150000.0,
 *   "createdAt": 1730400000000,
 *   "updatedAt": 1730400000000
 * }
 */
data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val tukangId: String = "",
    val status: String = "requested", // requested, assigned, in_progress, done, cancelled
    val serviceType: String = "",
    val location: OrderLocation = OrderLocation(),
    val price: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Create Order from Firestore document with defensive parsing
         * Handles Timestamp, Long, String, or missing createdAt
         */
        fun fromMap(id: String, data: Map<String, Any?>): Order {
            // Defensive parsing: handle Timestamp, Long, String or missing createdAt
            // Pattern matches existing codebase (RouteHistoryRepository)
            val createdAtVal = data["createdAt"]
            val createdAtDate: Long = try {
                when {
                    createdAtVal == null -> System.currentTimeMillis()
                    createdAtVal is Long -> createdAtVal
                    createdAtVal is Number -> createdAtVal.toLong()
                    createdAtVal is String -> {
                        createdAtVal.toLongOrNull() ?: System.currentTimeMillis()
                    }
                    else -> {
                        // Handle Firestore Timestamp using reflection or cast
                        try {
                            // Check if it has a toDate method (Firestore Timestamp)
                            val timestampClass = createdAtVal.javaClass
                            if (timestampClass.simpleName == "Timestamp" || 
                                timestampClass.name.contains("Timestamp")) {
                                val toDateMethod = timestampClass.getMethod("toDate")
                                val date = toDateMethod.invoke(createdAtVal) as? java.util.Date
                                date?.time ?: System.currentTimeMillis()
                            } else {
                                System.currentTimeMillis()
                            }
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                    }
                }
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            val updatedAtVal = data["updatedAt"]
            val updatedAtDate: Long = try {
                when {
                    updatedAtVal == null -> System.currentTimeMillis()
                    updatedAtVal is Long -> updatedAtVal
                    updatedAtVal is Number -> updatedAtVal.toLong()
                    updatedAtVal is String -> {
                        updatedAtVal.toLongOrNull() ?: System.currentTimeMillis()
                    }
                    else -> {
                        // Handle Firestore Timestamp using reflection or cast
                        try {
                            val timestampClass = updatedAtVal.javaClass
                            if (timestampClass.simpleName == "Timestamp" || 
                                timestampClass.name.contains("Timestamp")) {
                                val toDateMethod = timestampClass.getMethod("toDate")
                                val date = toDateMethod.invoke(updatedAtVal) as? java.util.Date
                                date?.time ?: System.currentTimeMillis()
                            } else {
                                System.currentTimeMillis()
                            }
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                    }
                }
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            val locationData = data["location"] as? Map<String, Any>
            val location = if (locationData != null) {
                OrderLocation(
                    lat = (locationData["lat"] as? Number)?.toDouble() ?: 0.0,
                    lng = (locationData["lng"] as? Number)?.toDouble() ?: 0.0,
                    address = locationData["address"] as? String ?: ""
                )
            } else {
                OrderLocation()
            }

            return Order(
                id = id,
                userId = data["userId"] as? String ?: "",
                userName = data["userName"] as? String ?: "",
                tukangId = data["tukangId"] as? String ?: "",
                status = data["status"] as? String ?: "requested",
                serviceType = data["serviceType"] as? String ?: "",
                location = location,
                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                createdAt = createdAtDate,
                updatedAt = updatedAtDate
            )
        }
    }

    /**
     * Helper properties for status checking
     */
    val isRequested: Boolean
        get() = status == "requested" || status == "Menunggu"
    
    val isAssigned: Boolean
        get() = status == "assigned" || status == "Diterima"
    
    val isInProgress: Boolean
        get() = status == "in_progress" || status == "Dikerjakan"
    
    val isDone: Boolean
        get() = status == "done" || status == "Selesai"
    
    val isCancelled: Boolean
        get() = status == "cancelled" || status == "Dibatalkan"
}

/**
 * Data class representing order location
 */
data class OrderLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val address: String = ""
)

