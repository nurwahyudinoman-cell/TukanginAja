# Google Maps API Setup Guide

## ✅ Current Configuration Status

### 1. SHA-1 Fingerprint (Debug Keystore)
```
SHA1: 17:E8:7C:80:F2:A9:CE:F9:7D:E4:B2:E2:9B:9D:D8:FF:56:47:7D:95
```

**⚠️ IMPORTANT:** Add this SHA-1 fingerprint to your Google Cloud Console:
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project
3. Navigate to **APIs & Services** > **Credentials**
4. Find your **Maps API key** or **Android API key**
5. Click **Edit** and add this SHA-1 fingerprint under **Application restrictions** > **Android apps**
6. Add package name: `com.tukanginAja.solusi`

### 2. API Key Configuration

#### In `local.properties`:
```properties
MAPS_API_KEY=YOUR_API_KEY_HERE
```

#### In `AndroidManifest.xml`:
- ✅ Meta-data entry present: `com.google.android.geo.API_KEY`
- ✅ Uses `${MAPS_API_KEY}` placeholder

#### In `app/build.gradle.kts`:
- ✅ API key read from `local.properties`
- ✅ Injected into manifest via `manifestPlaceholders`
- ✅ Available in `BuildConfig.MAPS_API_KEY`

### 3. Required APIs (Enable in Google Cloud Console)

Ensure the following APIs are enabled in your Google Cloud project:

1. **Maps SDK for Android** ✅ Required
2. **Places API** ✅ Required
3. **Geocoding API** ✅ Required

**To enable:**
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Navigate to **APIs & Services** > **Library**
3. Search and enable each API listed above

### 4. Emulator Requirements

**⚠️ CRITICAL:** Use an emulator with Google Play Services:

- ✅ **Recommended:** Google Play ARM64 v8a system image
- ❌ **Do NOT use:** AOSP (Android Open Source Project) images without Google Play

**How to check:**
1. Open Android Studio
2. Go to **Tools** > **Device Manager**
3. When creating a new emulator, select system images with **"Google Play"** icon
4. Choose **ARM64 v8a** architecture for Apple Silicon (M4) compatibility

### 5. Google Play Services Check

The app includes automatic Google Play Services detection:

- If unavailable, shows error dialog with instructions
- Checks on app start using `GoogleApiAvailability`
- Provides helpful error messages

### 6. Location Permissions

The app requests location permissions at runtime:
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`

If denied, defaults to Jakarta coordinates: `(-6.2088, 106.8456)`

### 7. Firestore Real-time Updates

The map displays "tukang" locations from Firestore collection:
- Collection: `tukang_locations`
- Fields: `id`, `name`, `lat`, `lng`, `type`, `isAvailable`
- Updates in real-time via snapshot listeners

## 🧪 Testing Checklist

- [ ] API key is set in `local.properties`
- [ ] SHA-1 fingerprint added to Google Cloud Console
- [ ] Package name `com.tukanginAja.solusi` added to API key restrictions
- [ ] Maps SDK for Android enabled
- [ ] Places API enabled
- [ ] Geocoding API enabled
- [ ] Using Google Play ARM64 v8a emulator
- [ ] Location permissions granted
- [ ] Map displays without gray screen
- [ ] User location marker (blue) appears
- [ ] Tukang markers (red) sync from Firestore

## 🐛 Troubleshooting

### Map shows gray/blank screen:
1. Verify API key in `local.properties` matches Google Cloud Console
2. Check SHA-1 fingerprint is added correctly
3. Ensure Maps SDK for Android is enabled
4. Confirm using Google Play emulator image
5. Check Logcat for Google Maps errors:
   ```bash
   adb logcat | grep -i "maps\|google\|api"
   ```

### Google Play Services error:
- Use Google Play system image (not AOSP)
- Update Google Play Services in emulator:
  - Settings > Apps > Google Play Services > Update

### Location not showing:
- Grant location permissions when prompted
- Check emulator location settings:
  - Extended Controls (⋮) > Location > Set coordinates

### Firestore markers not appearing:
- Verify Firestore collection `tukang_locations` exists
- Check documents have `lat`, `lng`, `name`, `type`, `isAvailable` fields
- Ensure Firestore security rules allow read access

## 📱 Build Commands

```bash
# Clean build
./gradlew clean assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Build and run
./gradlew assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk
```

---

**Last Updated:** $(date)
**Project:** TukanginAja
**Package:** com.tukanginAja.solusi

