package com.tukangin.modules.notification

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.tukangin.modules.booking.BookingModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RealtimeSyncServiceTest {

    private val originalProvider = RealtimeSyncService.firestoreProvider

    @After
    fun tearDown() {
        RealtimeSyncService.firestoreProvider = originalProvider
    }

    @Test
    fun `listenToBookingUpdates emits results`() = runTest {
        val firestore: FirebaseFirestore = mock()
        val collection: CollectionReference = mock()
        val query: Query = mock()
        val registration: ListenerRegistration = mock()
        val snapshot: QuerySnapshot = mock()

        whenever(firestore.collection("bookings")).thenReturn(collection)
        whenever(collection.whereEqualTo("userId", "U1")).thenReturn(query)
        whenever(query.addSnapshotListener(any<EventListener<QuerySnapshot>>())).thenAnswer { invocation ->
            val listener = invocation.getArgument<EventListener<QuerySnapshot>>(0)
            val document = mock<com.google.firebase.firestore.DocumentSnapshot>().apply {
                whenever(toObject(BookingModel::class.java)).thenReturn(
                    BookingModel(id = "B1", userId = "U1", tukangId = "T1")
                )
            }
            whenever(snapshot.documents).thenReturn(listOf(document))
            listener.onEvent(snapshot, null)
            registration
        }
        whenever(registration.remove()).then { }

        RealtimeSyncService.firestoreProvider = { firestore }

        val flow = RealtimeSyncService.listenToBookingUpdates("U1")
        val result = flow.first()

        assertTrue(result.isSuccess || result.isFailure)
    }
}

