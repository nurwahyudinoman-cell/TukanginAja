package com.tukangin.modules.tukang

import com.tukanginAja.solusi.data.firebase.FirestoreHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TukangRepositoryTest {

    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var repository: TukangRepository

    @Before
    fun setup() {
        firestoreHelper = mock()
        repository = TukangRepository(firestoreHelper)
    }

    @Test
    fun `getAllTukang returns data`() = runTest {
        val tukangList = listOf(TukangModel(id = "1", name = "Pak Budi", serviceCategory = "AC"))
        whenever(firestoreHelper.getAllTukang()).thenReturn(Result.success(tukangList))

        val result = repository.getAllTukang()

        assertTrue(result.isSuccess)
        assertEquals(tukangList, result.getOrNull())
    }

    @Test
    fun `createOrUpdateTukang delegates to helper`() = runTest {
        val tukang = TukangModel(id = "1", name = "Siti", serviceCategory = "Cleaning")
        whenever(firestoreHelper.createOrUpdateTukang(tukang)).thenReturn(Result.success(Unit))

        val result = repository.createOrUpdateTukang(tukang)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `setAvailability returns failure when helper fails`() = runTest {
        whenever(firestoreHelper.setTukangAvailability(any(), any())).thenReturn(Result.failure(Exception("error")))

        val result = repository.setAvailability("1", false)

        assertTrue(result.isFailure)
    }
}

