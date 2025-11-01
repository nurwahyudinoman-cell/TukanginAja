package com.tukanginAja.solusi.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.tukangin.modules.booking.BookingRepository
import com.tukangin.modules.notification.NotificationService
import com.tukangin.modules.notification.RealtimeSyncService
import com.tukangin.modules.tukang.TukangRepository
import com.tukanginAja.solusi.data.firebase.FirestoreHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFirestoreHelper(firestore: FirebaseFirestore): FirestoreHelper = FirestoreHelper(firestore)

    @Provides
    @Singleton
    fun provideTukangRepository(firestoreHelper: FirestoreHelper): TukangRepository = TukangRepository(firestoreHelper)

    @Provides
    @Singleton
    fun provideBookingRepository(firestoreHelper: FirestoreHelper): BookingRepository = BookingRepository(firestoreHelper)

    @Provides
    @Singleton
    fun provideRealtimeSyncService(): RealtimeSyncService = RealtimeSyncService

    @Provides
    @Singleton
    fun provideNotificationService(): NotificationService = NotificationService
}

