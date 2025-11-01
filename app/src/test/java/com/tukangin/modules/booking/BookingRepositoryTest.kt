package com.tukangin.modules.booking

import com.tukanginAja.solusi.data.firebase.FirestoreHelper
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
class BookingRepositoryTest {

    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var repository: BookingRepository

    @Before
    fun setup() {
        firestoreHelper = mock()
        repository = BookingRepository(firestoreHelper)
    }

    @Test
    fun `createBooking should call firestore save`() = runTest {
        val booking = BookingModel(id = "B001", userId = "U1", tukangId = "T1", serviceType = "AC Repair")
        whenever(firestoreHelper.saveBooking(booking)).thenReturn(Unit)

        val result = repository.createBooking(booking)

        assertTrue(result.isSuccess)
        verify(firestoreHelper).saveBooking(booking)
    }

    @Test
    fun `getBookingsByUser returns list`() = runTest {
        val expected = listOf(BookingModel(id = "B002", userId = "U2", tukangId = "T3", serviceType = "Cleaning"))
        whenever(firestoreHelper.getBookingsByUser("U2")).thenReturn(expected)

        val result = repository.getBookingsByUser("U2")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `updateBookingStatus returns failure when helper throws`() = runTest {
        whenever(firestoreHelper.updateModuleBookingStatus(any(), any())).thenThrow(RuntimeException("error"))

        val result = repository.updateBookingStatus("B003", "accepted")

        assertTrue(result.isFailure)
    }
}
