# TukanginAja - Local Daily Service Platform

A modern Android application built with Jetpack Compose, Firebase, and MVVM architecture for connecting users with local service providers (tukang) for daily services.

## 🚀 Features

- **Authentication**: Email/Password login and registration with Firebase Auth
- **Location Services**: Google Maps integration for real-time location tracking
- **Service Requests**: Create and manage service orders
- **Real-time Chat**: Messaging with service providers
- **Route Tracking**: Track service provider routes in real-time
- **Profile Management**: User profile and preferences

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Node.js** (v16 or higher) - For Firebase CLI and potential frontend tools
- **pnpm/yarn/npm** - Package manager (for Firebase CLI and any Node-based tools)
- **Java JDK** (17 or higher) - Required for Android development
- **Gradle** (8.13+) - Build tool (included via wrapper)
- **Firebase CLI** - For Firebase emulators and deployment
- **Android Studio** (Hedgehog 2023.1.1 or later) - Recommended IDE
- **Android SDK** (API 26 minimum, API 34 target)

## 🔧 Quickstart

### 1. Clone the Repository

```bash
git clone https://github.com/nurwahyudinoman-cell/TukanginAja.git
cd TukanginAja
```

### 2. Environment Setup

Create environment configuration files:

```bash
# Copy example environment file (if exists)
cp .env.example .env.local

# Or create local.properties for Android-specific config
# Add the following to local.properties:
MAPS_API_KEY=your_google_maps_api_key_here
```

**Required Environment Variables:**

- `MAPS_API_KEY` - Google Maps API key (add to `local.properties`)
- Firebase configuration via `google-services.json` (download from Firebase Console)

### 3. Install Dependencies & Build

#### Frontend (if applicable):
```bash
# If there's a frontend directory with package.json
npm install
# or
yarn install
# or
pnpm install
```

#### Backend (Android):
```bash
./gradlew clean build
```

### 4. Firebase Emulators (if applicable)

For local development with Firebase emulators:

```bash
firebase emulators:start
```

**Note:** Ensure `firebase.json` is configured with emulator settings.

### 5. Run Tests & Lint

#### Run Tests:
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

#### Lint:
```bash
# Android Lint
./gradlew lint

# For Node.js projects (if applicable)
npm run lint
# or
yarn lint
```

## 🏗️ Project Structure

```
TukanginAja/
├── app/                          # Android application module
│   ├── src/
│   │   ├── main/                 # Main source code
│   │   │   ├── java/com/tukanginAja/solusi/
│   │   │   │   ├── data/         # Data layer (models, repositories)
│   │   │   │   ├── ui/           # UI layer (screens, components)
│   │   │   │   ├── di/           # Dependency injection
│   │   │   │   └── utils/        # Utilities
│   │   │   ├── res/              # Resources (layouts, drawables)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                  # Unit tests
│   │   └── androidTest/           # Instrumented tests
│   └── build.gradle.kts
├── docs/                          # Documentation
│   ├── system-audit.md
│   └── ...
├── logs/                          # Build and fix logs
├── .cursorrules/                   # Cursor AI rules
│   └── auto_repair_loop.md
├── build.gradle.kts               # Root build configuration
├── gradle/                        # Gradle wrapper and version catalog
└── README.md
```

## 🔐 Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create or select your project
3. Add Android app with package name: `com.tukanginAja.solusi`
4. Download `google-services.json`
5. Place it in `app/google-services.json`

### Required Firebase Services:
- **Authentication** - User auth
- **Firestore** - Real-time database
- **Storage** - File storage
- **Cloud Messaging (FCM)** - Push notifications
- **Cloud Functions** - Backend functions
- **Crashlytics** - Crash reporting (release builds)
- **Analytics** - User analytics

## 🗺️ Google Maps Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Enable **Maps SDK for Android**
3. Enable **Places API** and **Geocoding API**
4. Create API key with Android app restrictions
5. Add SHA-1 fingerprint and package name restrictions
6. Add API key to `local.properties`:

```properties
MAPS_API_KEY=your_api_key_here
```

## 🧪 Testing

### Run All Tests:
```bash
./gradlew test connectedAndroidTest
```

### Test Coverage:
- **Unit Tests**: JUnit 4.13.2
- **Instrumented Tests**: AndroidX Test (JUnit 1.5.1, Espresso 3.1.5)
- **Compose Tests**: Compose UI Test

### Current Test Status:
- ✅ All tests passing
- ⚠️ Coverage can be improved (currently example tests only)

## 🔄 Auto Repair Loop via Cursor

Cursor AI can automatically fix build errors using the Auto Repair Loop policy.

### How to Use:

1. **Trigger Auto Repair:**
   - When build fails, Cursor AI will automatically:
     - Run `./gradlew clean build`
     - Analyze error logs
     - Apply fixes automatically
     - Rebuild until success or max iterations

2. **Manual Trigger:**
   - Ask Cursor AI: "Fix build errors using auto repair loop"
   - Or: "Run auto repair loop for current build failures"

3. **View Fix Logs:**
   - Check `/logs/build-fix-report.txt` for all fixes applied
   - Each fix includes timestamp, files changed, and explanation

### Policy Location:
See `.cursorrules/auto_repair_loop.md` for complete policy details.

**Note:** Cursor AI will never modify database schemas or critical configurations without flagging for Senior Dev approval.

## 👥 Contributing

We welcome contributions! Please refer to **[COLLAB_GUIDE.md](./COLLAB_GUIDE.md)** for detailed collaboration guidelines between Senior Developers and Cursor AI.

### Quick Contribution Guidelines:

1. **Follow Architecture:**
   - MVVM + Repository Pattern
   - Hilt for Dependency Injection
   - Kotlin Coroutines + Flow for async operations

2. **Code Style:**
   - Follow official Kotlin code style
   - Use Android Lint for code quality
   - Write tests for new features

3. **Commit Messages:**
   - Use conventional commits: `type: description`
   - Types: `feat`, `fix`, `docs`, `refactor`, `test`, `ci`

4. **Pull Requests:**
   - Create feature branch from `main`
   - Ensure all tests pass
   - Request review from Senior Developer

## 📚 Documentation

- **[COLLAB_GUIDE.md](./COLLAB_GUIDE.md)** - Collaboration guide for Senior Dev x Cursor AI
- **[docs/system-audit.md](./docs/system-audit.md)** - System audit report
- **[.cursorrules/auto_repair_loop.md](./.cursorrules/auto_repair_loop.md)** - Auto repair policy

## 🛠️ Tech Stack

### Frontend/UI:
- **Kotlin** 2.0.21
- **Jetpack Compose** (BOM 2024.09.00)
- **Material 3**
- **Navigation Compose** 2.7.4

### Backend/Services:
- **Firebase BOM** 32.7.0
  - Auth, Firestore, Storage, Messaging
  - Functions, Crashlytics, Analytics
- **Google Maps SDK** 5.0.0
- **Google Play Services** (Maps, Location)

### Architecture:
- **MVVM** + Repository Pattern
- **Hilt (Dagger)** 2.48 - Dependency Injection
- **Kotlin Coroutines** 1.7.3 + Flow
- **Timber** 5.1.1 - Logging

### Build Tools:
- **Gradle** 8.13
- **Android Gradle Plugin** 8.13.0
- **Kotlin** 2.0.21

## 🚀 Build Variants

- **Debug**: Development build, debuggable, no minification
- **Release**: Production build, minified, ProGuard enabled, signed

### Release Build:
```bash
./gradlew assembleRelease
```

**Note:** Requires `keystore.properties` configured for signing.

## 🐛 Troubleshooting

### Common Issues:

1. **Build Fails:**
   - Run: `./gradlew clean build`
   - Check `/logs/build-fix-report.txt` if using Auto Repair Loop
   - Verify `google-services.json` exists

2. **Maps Not Loading:**
   - Verify `MAPS_API_KEY` in `local.properties`
   - Check API key restrictions in Google Cloud Console
   - Ensure SHA-1 fingerprint is added to API key

3. **Firebase Not Initializing:**
   - Verify `google-services.json` is in `app/` directory
   - Check Firebase project configuration
   - Ensure package name matches: `com.tukanginAja.solusi`

4. **Tests Failing:**
   - Run: `./gradlew clean test`
   - Check test logs for specific failures
   - Verify test dependencies are up to date

## 📞 Support

- **Issues:** Open an issue on [GitHub](https://github.com/nurwahyudinoman-cell/TukanginAja/issues)
- **Documentation:** See `/docs` directory
- **Collaboration Guide:** See [COLLAB_GUIDE.md](./COLLAB_GUIDE.md)

## 📄 License

[Add your license here]

---

**Project Status:** ✅ Active Development  
**Version:** 1.0.22 (versionCode 22)  
**Last Updated:** November 2025
