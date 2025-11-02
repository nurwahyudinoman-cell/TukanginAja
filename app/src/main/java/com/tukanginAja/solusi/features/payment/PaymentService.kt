package com.tukanginAja.solusi.features.payment

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling payment operations with Firestore
 * Provides methods to process payments, calculate commissions, and update order status
 */
@Singleton
class PaymentService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val calculator: CommissionCalculator
) {
    private val transactionsCollection = firestore.collection("transactions")
    private val ordersCollection = firestore.collection("orders")
    
    /**
     * Process payment for an order
     * Calculates commission (90% tukang, 10% platform) and updates order status to PAID
     * Returns Result.success(TransactionModel) on success or Result.failure(exception) on error
     */
    suspend fun processPayment(
        orderId: String,
        userId: String,
        tukangId: String,
        amount: Double
    ): Result<TransactionModel> {
        return try {
            if (orderId.isEmpty() || userId.isEmpty() || tukangId.isEmpty()) {
                return Result.failure(IllegalArgumentException("Order ID, User ID, and Tukang ID cannot be empty"))
            }
            
            if (amount <= 0) {
                return Result.failure(IllegalArgumentException("Amount must be greater than 0"))
            }
            
            // Calculate commission and platform fee (90% tukang, 10% platform)
            val (tukangEarning, platformFee) = calculator.calculate(amount)
            
            // Create transaction
            val transaction = TransactionModel(
                orderId = orderId,
                userId = userId,
                tukangId = tukangId,
                amount = amount,
                commission = tukangEarning,
                platformFee = platformFee,
                status = "SUCCESS",
                createdAt = System.currentTimeMillis()
            )
            
            // Save transaction to Firestore
            val docRef = transactionsCollection.add(transaction.toMap()).await()
            
            val savedTransaction = transaction.copy(transactionId = docRef.id)
            
            // Update order status to PAID
            ordersCollection.document(orderId)
                .update(mapOf("paymentStatus" to "PAID")).await()
            
            Result.success(savedTransaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all transactions for a specific user
     * Returns List<TransactionModel> or empty list if no transactions found
     */
    suspend fun getTransactionsForUser(userId: String): List<TransactionModel> {
        return try {
            if (userId.isEmpty()) {
                return emptyList()
            }
            
            val snapshot = transactionsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: emptyMap()
                    TransactionModel.fromMap(doc.id, data)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get all transactions for a specific tukang
     * Returns List<TransactionModel> or empty list if no transactions found
     */
    suspend fun getTransactionsForTukang(tukangId: String): List<TransactionModel> {
        return try {
            if (tukangId.isEmpty()) {
                return emptyList()
            }
            
            val snapshot = transactionsCollection
                .whereEqualTo("tukangId", tukangId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: emptyMap()
                    TransactionModel.fromMap(doc.id, data)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get transaction by order ID
     * Returns TransactionModel or null if not found
     */
    suspend fun getTransactionByOrderId(orderId: String): TransactionModel? {
        return try {
            if (orderId.isEmpty()) {
                return null
            }
            
            val snapshot = transactionsCollection
                .whereEqualTo("orderId", orderId)
                .limit(1)
                .get()
                .await()
            
            if (!snapshot.isEmpty && snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents.first()
                val data = doc.data ?: emptyMap()
                TransactionModel.fromMap(doc.id, data)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Cancel a transaction
     * Updates transaction status to CANCELLED
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun cancelTransaction(transactionId: String): Result<Unit> {
        return try {
            if (transactionId.isEmpty()) {
                return Result.failure(IllegalArgumentException("Transaction ID cannot be empty"))
            }
            
            transactionsCollection.document(transactionId)
                .update(mapOf("status" to "CANCELLED"))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

