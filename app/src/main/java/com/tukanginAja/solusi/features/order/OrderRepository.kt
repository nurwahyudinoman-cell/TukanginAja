package com.tukanginAja.solusi.features.order

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for initializing order-related services
 * Provides a centralized way to create OrderViewModel instances
 */
@Singleton
class OrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Initialize OrderViewModel with dependencies
     * Creates OrderService and wraps it in OrderViewModel
     */
    fun init(): OrderViewModel {
        val service = OrderService(firestore)
        return OrderViewModel(service)
    }
}

