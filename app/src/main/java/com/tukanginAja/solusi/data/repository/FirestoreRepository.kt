package com.tukanginAja.solusi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.tukanginAja.solusi.data.model.TukangLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Observe real-time updates from Firestore collection "tukang_locations"
     * Returns a Flow that emits List<TukangLocation> whenever the collection changes
     */
    fun observeTukangLocations(): Flow<List<TukangLocation>> = callbackFlow {
        val collectionRef = firestore.collection("tukang_locations")
        
        val listenerRegistration: ListenerRegistration = collectionRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Emit empty list on error
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val locations = snapshot.documents.mapNotNull { doc ->
                        try {
                            TukangLocation(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                lat = doc.getDouble("lat") ?: 0.0,
                                lng = doc.getDouble("lng") ?: 0.0,
                                status = doc.getString("status") ?: "offline",
                                updatedAt = doc.getLong("updatedAt") ?: 0L
                            )
                        } catch (e: Exception) {
                            // Log error and skip this document
                            null
                        }
                    }
                    trySend(locations)
                } else {
                    // Empty collection
                    trySend(emptyList())
                }
            }
        
        // Cleanup when flow is cancelled
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Observe a specific tukang by ID in real-time
     * Returns a Flow that emits TukangLocation whenever the document changes
     */
    fun observeTukangById(tukangId: String): Flow<TukangLocation> = callbackFlow {
        if (tukangId.isEmpty()) {
            trySend(TukangLocation())
            return@callbackFlow
        }
        
        val docRef = firestore.collection("tukang_locations").document(tukangId)
        
        val listenerRegistration: ListenerRegistration = docRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Emit empty location on error
                    trySend(TukangLocation())
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val tukang = TukangLocation(
                            id = snapshot.id,
                            name = snapshot.getString("name") ?: "",
                            lat = snapshot.getDouble("lat") ?: 0.0,
                            lng = snapshot.getDouble("lng") ?: 0.0,
                            status = snapshot.getString("status") ?: "offline",
                            updatedAt = snapshot.getLong("updatedAt") ?: 0L
                        )
                        trySend(tukang)
                    } catch (e: Exception) {
                        trySend(TukangLocation())
                    }
                } else {
                    // Document doesn't exist
                    trySend(TukangLocation())
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Add a new tukang to Firestore
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun addTukang(tukang: TukangLocation): Result<Unit> = try {
        val collectionRef = firestore.collection("tukang_locations")
        val docRef = collectionRef.document()
        
        val tukangData = tukang.copy(
            id = docRef.id,
            updatedAt = System.currentTimeMillis()
        )
        
        val dataMap = mapOf(
            "id" to tukangData.id,
            "name" to tukangData.name,
            "lat" to tukangData.lat,
            "lng" to tukangData.lng,
            "status" to tukangData.status,
            "updatedAt" to tukangData.updatedAt
        )
        
        docRef.set(dataMap).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Update an existing tukang in Firestore
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun updateTukang(tukang: TukangLocation): Result<Unit> {
        return try {
            if (tukang.id.isEmpty()) {
                return Result.failure(IllegalArgumentException("Tukang ID cannot be empty"))
            }
            
            val docRef = firestore.collection("tukang_locations").document(tukang.id)
            
            val updatedTukang = tukang.copy(updatedAt = System.currentTimeMillis())
            
            val dataMap = mapOf(
                "id" to updatedTukang.id,
                "name" to updatedTukang.name,
                "lat" to updatedTukang.lat,
                "lng" to updatedTukang.lng,
                "status" to updatedTukang.status,
                "updatedAt" to updatedTukang.updatedAt
            )
            
            docRef.update(dataMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a tukang from Firestore
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun deleteTukang(tukangId: String): Result<Unit> {
        return try {
            if (tukangId.isEmpty()) {
                return Result.failure(IllegalArgumentException("Tukang ID cannot be empty"))
            }
            
            val docRef = firestore.collection("tukang_locations").document(tukangId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

