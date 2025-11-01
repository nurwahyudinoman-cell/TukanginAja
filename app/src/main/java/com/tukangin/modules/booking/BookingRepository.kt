package com.tukangin.modules.booking

import com.tukanginAja.solusi.data.firebase.FirestoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val firestoreHelper: FirestoreHelper
) {

    private val logFile: File by lazy {
        val dir = File("logs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        File(dir, FIRESTORE_LOG_FILE_NAME)
    }

    suspend fun createBooking(booking: BookingModel): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            firestoreHelper.saveBooking(booking)
        }.onFailure { logError("createBooking", it) }
    }

    suspend fun updateBookingStatus(id: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            firestoreHelper.updateModuleBookingStatus(id, status)
        }.onFailure { logError("updateBookingStatus", it) }
    }

    suspend fun getBookingsByUser(userId: String): Result<List<BookingModel>> = withContext(Dispatchers.IO) {
        runCatching {
            firestoreHelper.getBookingsByUser(userId)
        }.onFailure { logError("getBookingsByUser", it) }
    }

    suspend fun getBookingsByTukang(tukangId: String): Result<List<BookingModel>> = withContext(Dispatchers.IO) {
        runCatching {
            firestoreHelper.getBookingsByTukang(tukangId)
        }.onFailure { logError("getBookingsByTukang", it) }
    }

    private fun logError(action: String, throwable: Throwable) {
        try {
            FileWriter(logFile, true).use { writer ->
                writer.appendLine(
                    "${timestamp()} [BookingRepository] action=$action error=${throwable.message}"
                )
            }
        } catch (_: IOException) {
            // Ignore logging failures to avoid crashing business logic
        }
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

    companion object {
        private const val FIRESTORE_LOG_FILE_NAME = "firestore-ops.log"
    }
}
