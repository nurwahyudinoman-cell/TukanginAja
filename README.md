# Tukangin — Local Daily Service Platform

A modern Android application built with Jetpack Compose, Firebase, and MVVM architecture for connecting users with local service providers.

## 🚀 Features

- **Authentication**: Email/Password login and registration with Firebase Auth
- **Home Screen**: Google Maps integration for location-based services
- **Orders**: Track and manage service orders
- **Chat**: Real-time messaging with service providers
- **Profile**: User profile management
- **More**: Additional settings and features

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt (Dagger)
- **Backend**: Firebase (Auth, Firestore, Storage, Messaging, Crashlytics)
- **Maps**: Google Maps SDK for Android
- **Navigation**: Navigation Compose
- **Image Loading**: Coil
- **Logging**: Timber
- **Coroutines**: Kotlin Coroutines

## 📋 Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK 26 (minimum)
- Google Firebase account
- Google Maps API Key

## 🔧 Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd TukanginAja
```

### 2. Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add an Android app with package name: `com.tukanginAja.solusi`
4. Download `google-services.json`
5. Place it in `app/google-services.json` (replace the placeholder file)

### 3. Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Enable Google Maps SDK for Android
3. Create an API key
4. Add the API key to `local.properties`:

```properties
MAPS_API_KEY=your_google_maps_api_key_here
```

### 4. Build the Project

```bash
./gradlew clean build
```

Or use Android Studio:
- Open the project in Android Studio
- Wait for Gradle sync to complete
- Click "Run" or press `Shift+F10`

## 📁 Project Structure

```
app/
├── src/
│   └── main/
│       ├── java/com/tukanginAja/solusi/
│       │   ├── data/
│       │   │   ├── model/          # Data models
│       │   │   ├── remote/         # Remote data sources (Firebase)
│       │   │   └── repository/     # Repository implementations
│       │   ├── domain/
│       │   │   └── usecase/        # Business logic use cases
│       │   ├── ui/
│       │   │   ├── screens/        # Screen composables
│       │   │   │   ├── auth/
│       │   │   │   ├── home/
│       │   │   │   ├── orders/
│       │   │   │   ├── chat/
│       │   │   │   ├── profile/
│       │   │   │   └── more/
│       │   │   ├── components/     # Reusable UI components
│       │   │   ├── navigation/     # Navigation setup
│       │   │   └── theme/          # App theme and colors
│       │   ├── di/                 # Dependency injection modules
│       │   └── utils/              # Utility classes
│       ├── res/                     # Resources
│       └── AndroidManifest.xml
└── google-services.json            # Firebase config (replace with your file)
```

## 🏗 Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with Repository pattern:

- **Data Layer**: Remote data sources and repositories
- **Domain Layer**: Use cases containing business logic
- **Presentation Layer**: UI screens (Compose) and ViewModels
- **DI Layer**: Hilt modules for dependency injection

## 🎨 Brand Colors

- **Primary**: `#FF9800` (Orange)
- **Secondary**: `#1565C0` (Blue)

## 🔐 Authentication

The app uses Firebase Authentication with:
- Email/Password authentication (implemented)
- Google Sign-In (stubbed for future implementation)

### Sample Credentials

To test the app, you'll need to create users via:
1. Firebase Console → Authentication → Users
2. Or use the Register screen in the app

## 📱 Screens

### 1. Splash Screen
- Shows app branding
- Checks authentication status
- Navigates to Login or Home based on auth state

### 2. Authentication Screens
- **Login**: Email/password login
- **Register**: Create new account

### 3. Main Screens (Bottom Navigation)
- **Home**: Placeholder for Google Maps integration
- **Orders**: Order management (placeholder)
- **Chat**: Messaging interface (placeholder)
- **Profile**: User profile and sign out
- **More**: Additional features (placeholder)

## 🧪 Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumentation tests:
```bash
./gradlew connectedAndroidTest
```

## 📦 Build Variants

- **Debug**: Development build with debug signing
- **Release**: Production build (configure signing config)

## 🔄 CI/CD

GitHub Actions workflow is configured for:
- Building the project on push to `main` branch
- Running tests (if configured)

See `.github/workflows/android.yml` for details.

## 📝 Dependencies

Key dependencies are managed in `gradle/libs.versions.toml`:

- Jetpack Compose BOM: 2024.09.00
- Hilt: 2.48
- Firebase BOM: 32.7.0
- Navigation Compose: 2.7.4
- Google Maps: 5.0.0
- Coroutines: 1.7.3

## 🐛 Troubleshooting

### Build Errors

1. **Google Services JSON missing**: Ensure `app/google-services.json` is present and valid
2. **Maps API Key**: Check `local.properties` has `MAPS_API_KEY` set
3. **Gradle sync fails**: Try `./gradlew clean` and invalidate caches in Android Studio

### Firebase Issues

1. Ensure Firebase project is properly configured
2. Check that Authentication is enabled in Firebase Console
3. Verify package name matches in Firebase and AndroidManifest

## 📄 License

[Add your license here]

## 👥 Contributors

[Add contributors here]

## 📞 Support

For issues and questions, please open an issue on GitHub.

---

**Note**: This is an initial implementation with placeholder screens. Additional features and full implementations will be added in future updates.
