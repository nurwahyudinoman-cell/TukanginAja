package com.tukangin.modules.notification

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationServiceTest {

    private lateinit var context: Context
    private lateinit var firebaseMessaging: FirebaseMessaging
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var service: NotificationService

    @Before
    fun setup() {
        context = mock()
        firebaseMessaging = mock()
        firestore = mock()
        firebaseAuth = mock()
        service = NotificationService(context, firebaseMessaging, firestore, firebaseAuth)
    }

    @Test
    fun `registerToken stores token in firestore`() = runTest {
        val user: FirebaseUser = mock()
        whenever(user.uid).thenReturn("user-1")
        whenever(firebaseAuth.currentUser).thenReturn(user)

        val tokensCollection: CollectionReference = mock()
        val userDoc: DocumentReference = mock()
        val tokenDoc: DocumentReference = mock()
        val userCollection: CollectionReference = mock()

        whenever(firestore.collection("users")).thenReturn(userCollection)
        whenever(userCollection.document("user-1")).thenReturn(userDoc)
        whenever(userDoc.collection("fcmTokens")).thenReturn(tokensCollection)
        whenever(tokensCollection.document("token-123")).thenReturn(tokenDoc)
        whenever(tokenDoc.set(any<Map<String, Any>>())).thenReturn(Tasks.forResult(null))

        val result = service.registerToken("token-123")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `handleRemoteMessage triggers booking update handler`() {
        var capturedBookingId: String? = null
        var capturedStatus: String? = null
        service.setBookingUpdateHandler { bookingId, status ->
            capturedBookingId = bookingId
            capturedStatus = status
        }

        val message: RemoteMessage = mock {
            on { data } doReturn mapOf(
                "bookingId" to "booking-1",
                "status" to "accepted"
            )
            on { notification } doReturn null
        }

        service.handleRemoteMessage(message)

        assertEquals("booking-1", capturedBookingId)
        assertEquals("accepted", capturedStatus)
    }
}

