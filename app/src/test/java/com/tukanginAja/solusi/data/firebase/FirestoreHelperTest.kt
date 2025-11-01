package com.tukanginAja.solusi.data.firebase

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.tukanginAja.solusi.data.model.Booking
import com.tukanginAja.solusi.data.model.Tukang
import com.tukanginAja.solusi.data.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreHelperTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var helper: FirestoreHelper

    private val usersCollection: CollectionReference = mock()
    private val tukangCollection: CollectionReference = mock()
    private val bookingsCollection: CollectionReference = mock()

    @Before
    fun setup() {
        firestore = mock()
        helper = FirestoreHelper(firestore)

        whenever(firestore.collection("users")).thenReturn(usersCollection)
        whenever(firestore.collection("tukang")).thenReturn(tukangCollection)
        whenever(firestore.collection("bookings")).thenReturn(bookingsCollection)
        whenever(firestore.collection("service_categories")).thenReturn(mock())
    }

    @Test
    fun `createUser should succeed when firestore returns success`() = runTest {
        val user = User(id = "user_1", email = "test@domain.com", name = "Test User", phoneNumber = "")
        val documentReference: DocumentReference = mock()
        whenever(usersCollection.document(user.id)).thenReturn(documentReference)
        whenever(documentReference.set(user)).thenReturn(Tasks.forResult(null))

        val result = helper.createUser(user)

        assertTrue(result.isSuccess)
        verify(documentReference).set(user)
    }

    @Test
    fun `getUser should return user when snapshot exists`() = runTest {
        val userId = "user_2"
        val user = User(id = userId, email = "user@domain.com", name = "User Two", phoneNumber = "")
        val documentReference: DocumentReference = mock()
        val snapshot: DocumentSnapshot = mock()

        whenever(usersCollection.document(userId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(snapshot))
        whenever(snapshot.exists()).thenReturn(true)
        whenever(snapshot.toObject(User::class.java)).thenReturn(user)

        val result = helper.getUser(userId)

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }

    @Test
    fun `createTukang should write tukang to firestore`() = runTest {
        val tukang = Tukang(id = "tukang_1", name = "Pak Budi", phoneNumber = "08000000", skills = listOf("AC"))
        val documentReference: DocumentReference = mock()

        whenever(tukangCollection.document(tukang.id)).thenReturn(documentReference)
        whenever(documentReference.set(any<Tukang>())).thenReturn(Tasks.forResult(null))

        val result = helper.createTukang(tukang)

        assertTrue(result.isSuccess)
        verify(documentReference).set(any<Tukang>())
    }

    @Test
    fun `getBookingByUser returns bookings list`() = runTest {
        val userId = "user_booking"
        val query: Query = mock()
        val snapshot: QuerySnapshot = mock()
        val documentSnapshot: DocumentSnapshot = mock()
        val booking = Booking(id = "booking_1", userId = userId, tukangId = "tukang_1")

        whenever(bookingsCollection.whereEqualTo("userId", userId)).thenReturn(query)
        whenever(query.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forResult(snapshot))
        whenever(snapshot.documents).thenReturn(listOf(documentSnapshot))
        whenever(documentSnapshot.toObject(Booking::class.java)).thenReturn(booking)

        val result = helper.getBookingByUser(userId)

        assertTrue(result.isSuccess)
        assertEquals(listOf(booking), result.getOrNull())
    }

    @Test
    fun `createBooking returns failure when firestore throws`() = runTest {
        val booking = Booking(id = "booking_fail", userId = "user", tukangId = "tukang")
        val documentReference: DocumentReference = mock()

        whenever(bookingsCollection.document(booking.id)).thenReturn(documentReference)
        whenever(documentReference.set(any<Booking>())).thenReturn(Tasks.forException(Exception("Firestore error")))

        val result = helper.createBooking(booking)

        assertTrue(result.isFailure)
    }
}

