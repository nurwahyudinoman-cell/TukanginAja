# Monitoring Setup Guide

**Project:** TukanginAja  
**Last Updated:** 2025-11-01  
**Status:** ⚠️ Configuration Required

---

## Overview

This document provides configuration for monitoring and error tracking for the TukanginAja Android application. Monitoring helps detect issues early and provides insights into application performance.

---

## Monitoring Stack Recommendations

### Recommended Stack

1. **Error Tracking:** Sentry (Primary) + Firebase Crashlytics (Secondary)
2. **Performance Monitoring:** Firebase Performance Monitoring (Primary) + Prometheus/Grafana (Optional)
3. **Analytics:** Firebase Analytics (Primary)
4. **Logging:** Timber (Local) + Cloud Logging (Cloud Functions)

### Why This Stack?

- **Sentry:** Excellent error tracking with rich context, release tracking, and integrations
- **Firebase Crashlytics:** Native Firebase integration, automatic crash reporting
- **Firebase Performance:** Built-in Firebase performance monitoring
- **Prometheus/Grafana:** Advanced monitoring for Cloud Functions and infrastructure

---

## Error Tracking: Sentry

### 1. Project Setup

#### Create Sentry Project

1. Go to [Sentry.io](https://sentry.io/) and create an account
2. Create a new project:
   - **Platform:** Android (Kotlin)
   - **Project Name:** `tukanginaja-android`
   - **Organization:** Your organization

#### Get DSN (Data Source Name)

After project creation, Sentry provides a DSN:
```
https://<key>@<organization>.ingest.sentry.io/<project-id>
```

**Example:**
```
https://1234567890abcdef1234567890abcdef@123456.ingest.sentry.io/1234567
```

### 2. Android Integration

#### Add Dependency

**File:** `app/build.gradle.kts`
```kotlin
dependencies {
    // Sentry Android SDK
    implementation("io.sentry:sentry-android:7.0.0")
    
    // Sentry for Timber (logging integration)
    implementation("io.sentry:sentry-android-timber:7.0.0")
}
```

#### Initialize Sentry

**File:** `app/src/main/java/com/tukanginAja/solusi/TukanginApplication.kt`
```kotlin
import android.app.Application
import io.sentry.android.core.SentryAndroid
import io.sentry.android.timber.SentryTimberIntegration
import timber.log.Timber

class TukanginApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Sentry
        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.environment = BuildConfig.BUILD_TYPE // "debug" or "release"
            options.release = BuildConfig.VERSION_NAME
            options.enableAutoSessionTracking = true
            options.tracesSampleRate = 1.0 // 100% for now, adjust in production
            
            // Add Timber integration
            options.addIntegration(SentryTimberIntegration())
        }
        
        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(SentryTimberIntegration.Tree())
        }
    }
}
```

#### Add DSN to Build Configuration

**File:** `app/build.gradle.kts`
```kotlin
android {
    defaultConfig {
        // ... existing config ...
        
        // Sentry DSN (from secrets.xml or local.properties)
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val sentryDsn = localProperties.getProperty("SENTRY_DSN", "")
        buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
    }
}
```

#### Store DSN Securely

**Option 1: Use `local.properties` (Recommended)**
```properties
# local.properties (not committed to git)
SENTRY_DSN=https://<key>@<organization>.ingest.sentry.io/<project-id>
```

**Option 2: Use `secrets.xml`**
```xml
<!-- app/src/main/res/values/secrets.xml -->
<resources>
    <string name="sentry_dsn">https://<key>@<organization>.ingest.sentry.io/<project-id></string>
</resources>
```

**Note:** Ensure `secrets.xml` is in `.gitignore` if it contains sensitive data.

### 3. Usage Examples

#### Manual Error Reporting

```kotlin
import io.sentry.Sentry

try {
    // Your code
} catch (e: Exception) {
    Sentry.captureException(e)
    throw e // Re-throw if needed
}
```

#### Add Breadcrumbs (User Actions)

```kotlin
import io.sentry.Breadcrumb
import io.sentry.Sentry

// Track user actions
Sentry.addBreadcrumb(Breadcrumb().apply {
    message = "User clicked on service request"
    category = "user.action"
    level = SentryLevel.INFO
    data = mapOf(
        "requestId" to requestId,
        "screen" to "HomeScreen"
    )
})
```

#### Custom Context

```kotlin
import io.sentry.Sentry

// Set user context
Sentry.setUser(SentryUser().apply {
    id = firebaseUser.uid
    email = firebaseUser.email
    username = firebaseUser.displayName
})

// Set tags
Sentry.setTag("user_type", "customer") // or "tukang"

// Set extra context
Sentry.setExtra("device_info", mapOf(
    "model" to Build.MODEL,
    "android_version" to Build.VERSION.RELEASE
))
```

### 4. Release Tracking

#### Configure Release in Sentry

**File:** `app/build.gradle.kts`
```kotlin
android {
    defaultConfig {
        versionCode = 22
        versionName = "1.0.22"
    }
}
```

Sentry automatically tracks releases based on `versionName`.

#### Upload ProGuard Mapping Files

**File:** `app/build.gradle.kts`
```kotlin
plugins {
    id("io.sentry.android.gradle") version "4.0.0"
}

sentry {
    autoUploadProguardMapping = true
    uploadNativeSymbols = true
    includeSourceContext = true
}
```

---

## Performance Monitoring: Firebase Performance

### 1. Enable Firebase Performance

Firebase Performance is already configured in the project. To enable monitoring:

**File:** `app/build.gradle.kts` (Already configured)
```kotlin
dependencies {
    implementation(libs.firebase.analytics)
}
```

### 2. Custom Performance Traces

```kotlin
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

// Create custom trace
val trace = FirebasePerformance.getInstance().newTrace("service_request_create")
trace.start()

try {
    // Your code here
    createServiceRequest(request)
} finally {
    trace.stop()
}

// Add attributes
trace.putAttribute("customer_id", customerId)
trace.putAttribute("request_type", "service")

// Add metrics
trace.incrementMetric("requests_count", 1)
trace.putMetric("response_time_ms", responseTime)
```

### 3. Screen Performance Tracking

```kotlin
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

@Composable
fun HomeScreen() {
    val trace = remember { FirebasePerformance.getInstance().newTrace("home_screen_load") }
    
    LaunchedEffect(Unit) {
        trace.start()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            trace.stop()
        }
    }
    
    // Screen content
}
```

---

## Alternative: Prometheus + Grafana

### When to Use

- Advanced infrastructure monitoring
- Custom metrics for Cloud Functions
- Detailed dashboards and alerts

### Setup Steps

1. **Install Prometheus** (on a server or Kubernetes)
2. **Install Grafana** (for visualization)
3. **Expose Metrics** from Cloud Functions
4. **Configure Dashboards**

**Note:** For Android app monitoring, Firebase Performance is recommended. Prometheus/Grafana is better suited for infrastructure and backend services.

---

## Analytics: Firebase Analytics

### Already Configured

Firebase Analytics is already included in the project.

### Custom Events

```kotlin
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

val analytics = Firebase.analytics

// Log custom event
analytics.logEvent("service_request_created") {
    param("customer_id", customerId)
    param("request_type", "ac_repair")
    param("tukang_id", tukangId)
}
```

---

## Logging: Timber + Cloud Logging

### Local Logging (Timber)

**Already configured in project.**

```kotlin
import timber.log.Timber

Timber.d("Debug message")
Timber.i("Info message")
Timber.w("Warning message")
Timber.e("Error message", exception)
```

### Cloud Logging (Cloud Functions)

For Cloud Functions, logs are automatically sent to Cloud Logging.

---

## Monitoring Dashboard

### Recommended Metrics to Track

1. **Error Rate:**
   - Crashes per session
   - Error rate by screen
   - Error rate by user type (customer/tukang)

2. **Performance:**
   - App startup time
   - Screen load times
   - API response times
   - Firestore query performance

3. **User Engagement:**
   - Active users (DAU, MAU)
   - Session duration
   - Feature usage

4. **Business Metrics:**
   - Service requests created
   - Requests completed
   - Chat messages sent
   - Route tracking usage

---

## Alerting Configuration

### Sentry Alerts

1. **Go to Sentry Dashboard**
2. **Navigate to:** Alerts > Create Alert Rule
3. **Configure:**
   - **Trigger:** Error rate exceeds threshold
   - **Conditions:** > 5 errors in 5 minutes
   - **Actions:** Send email/Slack notification

### Firebase Crashlytics Alerts

1. **Go to Firebase Console**
2. **Navigate to:** Crashlytics > Alerts
3. **Configure:** Email notifications for new crashes

### Cloud Monitoring Alerts

For Cloud Functions and infrastructure:

```bash
# Create alert for high error rate
gcloud alpha monitoring policies create \
  --notification-channels=CHANNEL_ID \
  --display-name="High Error Rate Alert" \
  --condition-threshold-value=10 \
  --condition-threshold-duration=300s
```

---

## Environment Configuration

### Debug vs Release

```kotlin
// In build.gradle.kts
buildTypes {
    debug {
        buildConfigField("String", "SENTRY_DSN", "\"${debugSentryDsn}\"")
        buildConfigField("boolean", "ENABLE_MONITORING", "false") // Disable in debug
    }
    
    release {
        buildConfigField("String", "SENTRY_DSN", "\"${releaseSentryDsn}\"")
        buildConfigField("boolean", "ENABLE_MONITORING", "true")
    }
}
```

---

## API Keys and DSNs Summary

### Required Secrets

1. **Sentry DSN:**
   - **Location:** `local.properties` or `secrets.xml`
   - **Format:** `https://<key>@<organization>.ingest.sentry.io/<project-id>`
   - **Example:** `SENTRY_DSN=https://1234567890abcdef@123456.ingest.sentry.io/1234567`

2. **Firebase Configuration:**
   - **File:** `app/google-services.json`
   - **Status:** Already configured (verify presence)

3. **Firebase Analytics:**
   - **Status:** Automatic via `google-services.json`

### Security Best Practices

1. **Never commit DSNs to version control**
2. **Use `local.properties` for local development**
3. **Use environment variables in CI/CD**
4. **Rotate DSNs if compromised**

---

## Integration Checklist

### Sentry Setup
- [ ] Create Sentry account and project
- [ ] Get DSN from Sentry dashboard
- [ ] Add Sentry dependency to `build.gradle.kts`
- [ ] Initialize Sentry in `TukanginApplication`
- [ ] Add DSN to `local.properties`
- [ ] Test error reporting
- [ ] Configure release tracking
- [ ] Set up alert rules

### Firebase Performance
- [x] Firebase Performance dependency (already added)
- [ ] Add custom traces for critical operations
- [ ] Test performance monitoring
- [ ] Review performance reports

### Firebase Analytics
- [x] Firebase Analytics dependency (already added)
- [ ] Add custom events for key user actions
- [ ] Review analytics dashboard

### Monitoring Dashboard
- [ ] Set up Sentry dashboard
- [ ] Configure Firebase Crashlytics dashboard
- [ ] Create custom dashboards (optional)

---

## Next Steps

### Immediate Actions

1. **Set Up Sentry:**
   ```bash
   # Add Sentry dependency
   # Initialize in application class
   # Add DSN to local.properties
   ```

2. **Test Error Reporting:**
   ```kotlin
   // Add test error in debug build
   if (BuildConfig.DEBUG) {
       Sentry.captureException(Exception("Test error"))
   }
   ```

3. **Configure Alerts:**
   - Set up Sentry alert rules
   - Configure Firebase Crashlytics notifications

### Short Term Actions

1. Add custom performance traces
2. Implement custom analytics events
3. Set up monitoring dashboard
4. Document alert procedures

### Long Term Actions

1. Set up advanced Prometheus/Grafana monitoring (if needed)
2. Create monitoring runbook
3. Implement automated alert responses
4. Regular monitoring review meetings

---

## Troubleshooting

### Common Issues

1. **Sentry Not Reporting Errors:**
   - Verify DSN is correct
   - Check internet connection
   - Ensure Sentry is initialized before errors occur

2. **Firebase Performance Not Showing Data:**
   - Wait 24 hours for data to appear
   - Verify Firebase Analytics is enabled
   - Check Firebase Console configuration

3. **High Error Rate:**
   - Review error details in Sentry
   - Check recent code changes
   - Investigate specific user segments

---

**Configuration Status:** ⚠️ Requires Setup  
**Last Updated:** 2025-11-01  
**Next Review:** After initial setup

