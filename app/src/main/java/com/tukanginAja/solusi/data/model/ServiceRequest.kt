package com.tukanginAja.solusi.data.model

/**
 * Data class representing a service request in Firestore
 * 
 * Example Firestore document:
 * {
 *   "id": "r001",
 *   "customerId": "u001",
 *   "tukangId": "t001",
 *   "tukangName": "Budi Tukang AC",
 *   "status": "pending",
 *   "timestamp": 1730400000000,
 *   "description": "Servis AC bocor di daerah Setiabudi"
 * }
 */
data class ServiceRequest(
    val id: String = "",
    val customerId: String = "",
    val tukangId: String = "",
    val tukangName: String = "",
    val status: String = "pending", // pending, accepted, declined, completed
    val timestamp: Long = 0L,
    val description: String = ""
) {
    /**
     * Helper properties for status checking
     */
    val isPending: Boolean
        get() = status == "pending"
    
    val isAccepted: Boolean
        get() = status == "accepted"
    
    val isDeclined: Boolean
        get() = status == "declined"
    
    val isCompleted: Boolean
        get() = status == "completed"
}

