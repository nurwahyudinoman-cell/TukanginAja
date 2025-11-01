package com.tukangin.modules.tukang

import com.tukanginAja.solusi.data.firebase.FirestoreHelper
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TukangRepository @Inject constructor(
    private val firestoreHelper: FirestoreHelper
) {

    suspend fun getAllTukang(): Result<List<TukangModel>> =
        firestoreHelper.getAllTukang().also { result ->
            result.onFailure { logFailure("getAllTukang", it) }
        }

    suspend fun getTukangById(id: String): Result<TukangModel?> =
        firestoreHelper.getTukangModelById(id).also { result ->
            result.onFailure { logFailure("getTukangById", it) }
        }

    suspend fun createOrUpdateTukang(tukang: TukangModel): Result<Unit> =
        firestoreHelper.createOrUpdateTukang(tukang).also { result ->
            result.onFailure { logFailure("createOrUpdateTukang", it) }
        }

    suspend fun setAvailability(id: String, available: Boolean): Result<Unit> =
        firestoreHelper.setTukangAvailability(id, available).also { result ->
            result.onFailure { logFailure("setAvailability", it) }
        }

    suspend fun deleteTukang(id: String): Result<Unit> =
        firestoreHelper.deleteTukangModel(id).also { result ->
            result.onFailure { logFailure("deleteTukang", it) }
        }

    private fun logFailure(operation: String, throwable: Throwable) {
        try {
            val logDir = File("logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            val logFile = File(logDir, FIRESTORE_LOG_FILE_NAME)
            FileWriter(logFile, true).use { writer ->
                writer.appendLine(
                    "${timestamp()} [TukangRepository] Operation=$operation, error=${throwable.message}"
                )
            }
        } catch (_: IOException) {
            // Swallow logging errors to avoid crashing calling code
        }
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

    companion object {
        private const val FIRESTORE_LOG_FILE_NAME = "firestore-ops.log"
    }
}

