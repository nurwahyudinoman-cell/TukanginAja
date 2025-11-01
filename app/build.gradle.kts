import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    // TAHAP 17: Firebase Crashlytics plugin
    id("com.google.firebase.crashlytics") version "2.9.9"
    id("org.jetbrains.kotlin.kapt") version "2.0.21"
}

android {
    namespace = "com.tukanginAja.solusi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tukanginAja.solusi"
        minSdk = 26
        targetSdk = 34
        // TAHAP 22: Version update for Final Build & Play Console Deployment
        versionCode = 22
        versionName = "1.0.22"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Google Maps API Key - should be added to local.properties
        // Add: MAPS_API_KEY=your_api_key_here
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY", "")
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
        
        // ARM64 configuration for Apple Silicon compatibility
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    // TAHAP 22B: Keystore configuration for release signing
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))
            
            // Use rootProject.file() to resolve path relative to root project
            storeFile = rootProject.file(keystoreProperties["storeFile"]!!.toString())
            storePassword = keystoreProperties["storePassword"]!!.toString()
            keyAlias = keystoreProperties["keyAlias"]!!.toString()
            keyPassword = keystoreProperties["keyPassword"]!!.toString()
        }
    }

    buildTypes {
        getByName("debug") {
            // TAHAP 19B: Debug build configuration
            // Note: applicationIdSuffix removed to match Firebase google-services.json
            // If debug variant needed, add client in Firebase Console for com.tukanginAja.solusi.debug
            // applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isDebuggable = true
        }
        
        getByName("release") {
            // TAHAP 22B: Release build configuration
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    
    // Configure JVM toolchain for Java 17 (Apple Silicon compatible)
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    // TAHAP 16: Firebase Functions untuk kirim FCM notification
    implementation("com.google.firebase:firebase-functions-ktx")
    
    // TAHAP 17: Firebase Crashlytics & Analytics
    // Crashlytics - only for release builds to prevent debug runtime crashes
    releaseImplementation(libs.firebase.crashlytics)
    // Analytics - available in all builds for tracking
    implementation(libs.firebase.analytics)
    
    // Google Maps
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.utils)
    implementation(libs.maps.ktx)
    implementation(libs.play.services.maps)
    // Additional maps compose dependency for compatibility
    implementation("com.google.maps.android:maps-compose:4.3.3")
    // Google Maps Utils for Polyline decoding
    implementation("com.google.maps.android:android-maps-utils:3.8.2")
    
    // Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")
    
    // Google Play Services Base (for availability checks)
    implementation("com.google.android.gms:play-services-base:18.5.0")
    
    // Firebase Firestore (already included via BOM, but ensure ktx version)
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Timber (Logging)
    implementation(libs.timber)
    
    // Coil (Image Loading)
    implementation(libs.coil.compose)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}