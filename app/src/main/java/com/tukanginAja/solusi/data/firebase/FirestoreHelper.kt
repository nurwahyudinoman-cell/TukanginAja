package com.tukanginAja.solusi.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tukanginAja.solusi.data.model.Booking
import com.tukanginAja.solusi.data.model.Tukang
import com.tukanginAja.solusi.data.model.User
import com.tukangin.modules.booking.BookingModel
import com.tukangin.modules.tukang.TukangModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class that encapsulates Firestore CRUD operations for core collections.
 */
@Singleton
class FirestoreHelper @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val usersCollection get() = firestore.collection("users")
    private val tukangCollection get() = firestore.collection("tukang")
    private val bookingsCollection get() = firestore.collection("bookings")
    private val serviceCategoriesCollection get() = firestore.collection("service_categories")
    private val tukangModuleCollection get() = firestore.collection("module_tukang")
    private val bookingModuleCollection get() = firestore.collection("module_bookings")
    private val bookingsAuditCollection get() = firestore.collection("bookings_audit")

    // region User CRUD
    suspend fun createUser(user: User): Result<Unit> = runCatching {
        require(user.id.isNotBlank()) { "User ID cannot be empty" }
        val userData = user.copy(
            createdAt = user.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
        )
        usersCollection.document(user.id).set(userData).await()
    }

    suspend fun updateUser(user: User): Result<Unit> = runCatching {
        require(user.id.isNotBlank()) { "User ID cannot be empty" }
        usersCollection.document(user.id)
            .set(user.copy(createdAt = user.createdAt))
            .await()
    }

    suspend fun deleteUser(userId: String): Result<Unit> = runCatching {
        require(userId.isNotBlank()) { "User ID cannot be empty" }
        usersCollection.document(userId).delete().await()
    }

    suspend fun getUser(userId: String): Result<User?> = runCatching {
        require(userId.isNotBlank()) { "User ID cannot be empty" }
        val snapshot = usersCollection.document(userId).get().await()
        if (snapshot.exists()) snapshot.toObject(User::class.java) else null
    }

    suspend fun getUserByEmail(email: String): Result<User?> = runCatching {
        require(email.isNotBlank()) { "Email cannot be empty" }
        val snapshot = usersCollection
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()
        if (!snapshot.isEmpty) {
            val doc = snapshot.documents.first()
            doc.toObject(User::class.java)?.copy(id = doc.id)
        } else {
            null
        }
    }

    suspend fun getUserRole(email: String): Result<String?> = runCatching {
        val userResult = getUserByEmail(email)
        userResult.getOrNull()?.role  // Don't default to "user" - let AuthViewModel handle null
    }
    
    /**
     * Get user role by UID (recommended method - uses users/{uid} path)
     */
    suspend fun getUserRoleByUid(uid: String): Result<String?> = runCatching {
        val userResult = getUser(uid)
        userResult.getOrNull()?.role  // Don't default to "user" - let AuthViewModel handle null
    }
    // endregion

    // region Tukang CRUD
    suspend fun createTukang(tukang: Tukang): Result<Unit> = runCatching {
        val id = tukang.id.ifBlank { tukangCollection.document().id }
        val tukangData = tukang.copy(
            id = id,
            createdAt = tukang.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        tukangCollection.document(id).set(tukangData).await()
    }

    suspend fun updateTukang(tukang: Tukang): Result<Unit> = runCatching {
        require(tukang.id.isNotBlank()) { "Tukang ID cannot be empty" }
        tukangCollection.document(tukang.id)
            .set(tukang.copy(updatedAt = System.currentTimeMillis()))
            .await()
    }

    suspend fun deleteTukang(tukangId: String): Result<Unit> = runCatching {
        require(tukangId.isNotBlank()) { "Tukang ID cannot be empty" }
        tukangCollection.document(tukangId).delete().await()
    }

    suspend fun getTukang(tukangId: String): Result<Tukang?> = runCatching {
        require(tukangId.isNotBlank()) { "Tukang ID cannot be empty" }
        val snapshot = tukangCollection.document(tukangId).get().await()
        if (snapshot.exists()) snapshot.toObject(Tukang::class.java) else null
    }
    // endregion

    // region Booking CRUD
    suspend fun createBooking(booking: Booking): Result<String> = runCatching {
        val id = booking.id.ifBlank { bookingsCollection.document().id }
        val bookingData = booking.copy(
            id = id,
            createdAt = booking.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        bookingsCollection.document(id).set(bookingData).await()
        id
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> = runCatching {
        require(bookingId.isNotBlank()) { "Booking ID cannot be empty" }
        bookingsCollection.document(bookingId)
            .update(mapOf("status" to status, "updatedAt" to System.currentTimeMillis()))
            .await()
    }

    suspend fun getBookingByUser(userId: String): Result<List<Booking>> = runCatching {
        require(userId.isNotBlank()) { "User ID cannot be empty" }
        val snapshot = bookingsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toObject(Booking::class.java) }
    }
    // endregion

    // region Module Tukang
    suspend fun getAllTukang(): Result<List<TukangModel>> = runCatching {
        val snapshot = tukangModuleCollection.get().await()
        snapshot.documents.mapNotNull { doc ->
            doc.toObject(TukangModel::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun getTukangModelById(id: String): Result<TukangModel?> = runCatching {
        require(id.isNotBlank()) { "Tukang ID cannot be empty" }
        val snapshot = tukangModuleCollection.document(id).get().await()
        if (snapshot.exists()) snapshot.toObject(TukangModel::class.java)?.copy(id = snapshot.id) else null
    }

    suspend fun createOrUpdateTukang(tukang: TukangModel): Result<Unit> = runCatching {
        val id = tukang.id.ifBlank { tukangModuleCollection.document().id }
        val data = tukang.copy(id = id, createdAt = tukang.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis())
        tukangModuleCollection.document(id).set(data).await()
    }

    suspend fun setTukangAvailability(id: String, available: Boolean): Result<Unit> = runCatching {
        require(id.isNotBlank()) { "Tukang ID cannot be empty" }
        tukangModuleCollection.document(id)
            .update("available", available)
            .await()
    }

    suspend fun deleteTukangModel(id: String): Result<Unit> = runCatching {
        require(id.isNotBlank()) { "Tukang ID cannot be empty" }
        tukangModuleCollection.document(id).delete().await()
    }
    // endregion

    // region Module Booking
    suspend fun saveBooking(booking: BookingModel) {
        val id = booking.id.ifBlank { bookingModuleCollection.document().id }
        val data = booking.copy(
            id = id,
            scheduledAt = booking.scheduledAt,
            completedAt = booking.completedAt
        )
        bookingModuleCollection.document(id).set(data).await()
        logBookingAudit(id, data.status, actorId = booking.userId)
    }

    suspend fun updateModuleBookingStatus(bookingId: String, status: String) {
        require(bookingId.isNotBlank()) { "Booking ID cannot be empty" }
        val updates = mutableMapOf<String, Any?>(
            "status" to status,
            "updatedAt" to System.currentTimeMillis()
        )
        if (status.equals("completed", ignoreCase = true)) {
            updates["completedAt"] = System.currentTimeMillis()
        } else {
            updates["completedAt"] = null
        }
        bookingModuleCollection.document(bookingId).update(updates).await()
        logBookingAudit(bookingId, status, actorId = null)
    }

    suspend fun getBookingsByUser(userId: String): List<BookingModel> {
        require(userId.isNotBlank()) { "User ID cannot be empty" }
        val snapshot = bookingModuleCollection
            .whereEqualTo("userId", userId)
            .orderBy("scheduledAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(BookingModel::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun getBookingsByTukang(tukangId: String): List<BookingModel> {
        require(tukangId.isNotBlank()) { "Tukang ID cannot be empty" }
        val snapshot = bookingModuleCollection
            .whereEqualTo("tukangId", tukangId)
            .orderBy("scheduledAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(BookingModel::class.java)?.copy(id = doc.id)
        }
    }

    private suspend fun logBookingAudit(bookingId: String, status: String, actorId: String?) {
        val logEntry = mapOf(
            "bookingId" to bookingId,
            "status" to status,
            "actorId" to actorId,
            "timestamp" to System.currentTimeMillis()
        )
        bookingsAuditCollection.add(logEntry).await()
    }
    // endregion

    // region Service Categories Helpers (optional for future use)
    suspend fun createServiceCategory(categoryId: String, data: Map<String, Any?>): Result<Unit> = runCatching {
        require(categoryId.isNotBlank()) { "Category ID cannot be empty" }
        serviceCategoriesCollection.document(categoryId).set(data).await()
    }

    suspend fun deleteServiceCategory(categoryId: String): Result<Unit> = runCatching {
        require(categoryId.isNotBlank()) { "Category ID cannot be empty" }
        serviceCategoriesCollection.document(categoryId).delete().await()
    }
    // endregion
}

