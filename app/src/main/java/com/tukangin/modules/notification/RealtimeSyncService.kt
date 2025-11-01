package com.tukangin.modules.notification

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.tukangin.modules.booking.BookingModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object RealtimeSyncService {

    internal var firestoreProvider: () -> FirebaseFirestore = {
        FirebaseFirestore.getInstance()
    }

    fun listenToBookingUpdates(userId: String) = callbackFlow<Result<BookingModel>> {
        if (userId.isBlank()) {
            trySend(Result.failure(IllegalArgumentException("User ID cannot be blank")))
            close()
            return@callbackFlow
        }

        val firestore = try {
            firestoreProvider()
        } catch (exception: Exception) {
            trySend(Result.failure(exception))
            close(exception)
            return@callbackFlow
        }

        val registration: ListenerRegistration = firestore
            .collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                snapshot?.documents.orEmpty().forEach { document ->
                    val booking = document.toObject(BookingModel::class.java)
                    if (booking != null) {
                        trySend(Result.success(booking))
                    }
                }
            }

        awaitClose { registration.remove() }
    }
}

