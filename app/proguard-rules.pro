# TAHAP 17: ProGuard rules for release builds
# Add project specific ProGuard rules here.
# You can control the set of configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# Firebase
# ============================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Analytics
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.android.gms.measurement.**

# Firestore
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.auth.**

# Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.messaging.**

# Firebase Functions
-keep class com.google.firebase.functions.** { *; }
-dontwarn com.google.firebase.functions.**

# ============================================
# Google Maps
# ============================================
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.maps.**
-dontwarn com.google.android.gms.location.**

# Maps Compose
-keep class com.google.maps.android.compose.** { *; }
-dontwarn com.google.maps.android.compose.**

# Maps Utils
-keep class com.google.maps.android.** { *; }
-dontwarn com.google.maps.android.**

# ============================================
# Kotlin Coroutines
# ============================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ============================================
# Jetpack Compose
# ============================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# ============================================
# Hilt (Dagger)
# ============================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**

# ============================================
# Data Classes (Firestore models)
# ============================================
-keep class com.tukanginAja.solusi.data.model.** { *; }
-keepclassmembers class com.tukanginAja.solusi.data.model.** {
    <fields>;
}

# ============================================
# Navigation Compose
# ============================================
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ============================================
# Coil (Image Loading)
# ============================================
-keep class coil.** { *; }
-dontwarn coil.**

# ============================================
# Timber (Logging)
# ============================================
-keep class timber.log.** { *; }

# ============================================
# Retrofit / OkHttp (if used)
# ============================================
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Retrofit interfaces
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ============================================
# Gson (if used)
# ============================================
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ============================================
# Application Classes
# ============================================
-keep class com.tukanginAja.solusi.TukanginApplication { *; }
-keep class com.tukanginAja.solusi.MainActivity { *; }

# ============================================
# ViewModels & Repositories
# ============================================
-keep class com.tukanginAja.solusi.ui.** { *; }
-keep class com.tukanginAja.solusi.data.repository.** { *; }

# ============================================
# Keep all Parcelable implementations
# ============================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ============================================
# Keep Serializable implementations
# ============================================
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================
# Keep annotations
# ============================================
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions

# ============================================
# Native methods
# ============================================
-keepclasseswithmembernames class * {
    native <methods>;
}

# ============================================
# Keep enum classes
# ============================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
