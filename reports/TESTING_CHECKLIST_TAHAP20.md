# TukanginAja - Internal Testing Checklist
## Tahap 20: System Notification & Background Messaging Optimization

---

## 📱 Pre-Testing Setup

### 1. Device/Emulator Setup
- [ ] Connect physical device OR start emulator
- [ ] Verify device is recognized: `adb devices`
- [ ] Check Android version (target: Android 14 / API 34)
- [ ] Ensure internet connection active

### 2. APK Installation
- [ ] Install release APK: `adb install -r app/build/outputs/apk/release/app-release-unsigned.apk`
- [ ] Verify installation successful
- [ ] Check app appears in app drawer

---

## 🚀 Launch & Basic Functionality

### 3. Application Launch
- [ ] Launch app: `adb shell am start -n com.tukanginAja.solusi/.MainActivity`
- [ ] Verify splash screen appears
- [ ] Verify app opens without crash
- [ ] Check initial screen loads correctly

### 4. Authentication Module
- [ ] Login with existing account
- [ ] Verify login successful
- [ ] Check FCM token generated (see Logcat: "FCM Token:")
- [ ] Verify token saved to Firestore
- [ ] Logout and verify session cleared

---

## 📍 Maps & Location Module

### 5. Google Maps Integration
- [ ] Navigate to Map Screen
- [ ] Verify Google Maps loads correctly
- [ ] Verify user location displayed (if permission granted)
- [ ] Check location permission request works (Android 13+)

### 6. Real-time Location Updates
- [ ] Verify tukang markers visible on map
- [ ] Edit tukang_locations in Firestore Console (change status)
- [ ] Verify marker updates within < 1 second
- [ ] Check status badge changes color/text

### 7. Route Tracking
- [ ] Navigate to Route Screen
- [ ] Verify route polyline renders
- [ ] Check distance & duration display
- [ ] Verify camera auto-follow works
- [ ] Test route caching (should not rebuild on every small update)

---

## 💬 Chat Module

### 8. Chat Functionality
- [ ] Navigate to Chat Screen
- [ ] Verify chat list loads
- [ ] Create new chat session OR open existing chat
- [ ] Send message from Account A
- [ ] Verify message appears in real-time on Account B
- [ ] Verify message timestamps correct
- [ ] Test message bubble UI (alignment, colors)

### 9. Chat Realtime Sync
- [ ] Open chat on two devices (or emulator + web Firestore)
- [ ] Send message from Device A
- [ ] Verify message appears instantly on Device B
- [ ] Test bidirectional messaging
- [ ] Verify no duplicate messages

---

## 🔔 Notification System

### 10. FCM Token Management
- [ ] Check Logcat for "FCM Token:" log
- [ ] Verify token format correct
- [ ] Check Firestore users collection for `fcmToken` field
- [ ] Verify `fcmTokenUpdatedAt` timestamp set

### 11. POST_NOTIFICATIONS Permission (Android 13+)
- [ ] Launch app first time
- [ ] Verify permission request dialog appears
- [ ] Grant permission
- [ ] Restart app and verify permission persists

### 12. Chat Notifications (Foreground)
- [ ] Keep app in foreground
- [ ] Send FCM message from Firebase Console:
  ```json
  {
    "data": {
      "type": "chat",
      "chatId": "your_chat_id",
      "senderId": "sender_user_id",
      "title": "Pesan Baru",
      "body": "Test notification from foreground"
    },
    "token": "recipient_fcm_token"
  }
  ```
- [ ] Verify notification appears in status bar
- [ ] Verify notification title and body correct
- [ ] Click notification
- [ ] Verify app navigates to ChatScreen
- [ ] Verify chatId passed correctly

### 13. Chat Notifications (Background)
- [ ] Put app in background (home button)
- [ ] Send FCM message from Firebase Console
- [ ] Verify notification appears in status bar
- [ ] Click notification
- [ ] Verify app opens to ChatScreen
- [ ] Verify chatId passed correctly

### 14. Order Notifications
- [ ] Send FCM message with type "order":
  ```json
  {
    "data": {
      "type": "order",
      "orderId": "your_order_id",
      "title": "Order Update",
      "body": "Your order has been accepted"
    },
    "token": "recipient_fcm_token"
  }
  ```
- [ ] Verify notification appears
- [ ] Click notification
- [ ] Verify navigation to correct screen

### 15. Proximity Notifications
- [ ] Send FCM message with type "proximity":
  ```json
  {
    "data": {
      "type": "proximity",
      "orderId": "your_order_id",
      "title": "Tukang Mendekat",
      "body": "Tukang Anda sudah dekat"
    },
    "token": "recipient_fcm_token"
  }
  ```
- [ ] Verify notification appears
- [ ] Test notification navigation

---

## 📦 Order Management

### 16. Order Request Flow
- [ ] Create service request (User)
- [ ] Verify request appears in TukangRequestScreen (Tukang)
- [ ] Accept request (Tukang)
- [ ] Verify status changes to "accepted" in real-time
- [ ] Start journey (Tukang) - status to "in_progress"
- [ ] Verify map tracking active
- [ ] Complete order (Tukang)
- [ ] Verify status changes to "completed"
- [ ] Verify route history saved to Firestore

### 17. Dashboard Tukang
- [ ] Navigate to TukangDashboardScreen
- [ ] Verify active orders list loads
- [ ] Click "Mulai Perjalanan" button
- [ ] Verify BackgroundLocationService starts
- [ ] Verify foreground notification appears
- [ ] Click "Selesaikan Order" button
- [ ] Verify confirmation dialog appears
- [ ] Confirm completion
- [ ] Verify order status updated to "completed"
- [ ] Verify route history saved

---

## 🔄 Background Services

### 18. Background Location Service
- [ ] Start background tracking from TukangDashboardScreen
- [ ] Verify foreground notification appears
- [ ] Verify notification title: "Melacak Lokasi Tukang"
- [ ] Minimize app (home button)
- [ ] Wait 30 seconds
- [ ] Check Firestore tukang_locations collection
- [ ] Verify location updates continue
- [ ] Verify location updates have reasonable intervals (>15m or >5s)
- [ ] Stop tracking
- [ ] Verify service stops
- [ ] Verify status set to "offline" in Firestore

### 19. Background Messaging
- [ ] Put app in background
- [ ] Send FCM message
- [ ] Verify notification appears
- [ ] Click notification
- [ ] Verify app opens to correct screen
- [ ] Verify app state restored correctly

---

## 📊 Performance & Stability

### 20. Memory Monitoring
- [ ] Run app for 15 minutes continuously
- [ ] Monitor memory: `adb shell dumpsys meminfo com.tukanginAja.solusi`
- [ ] Check for memory leaks:
  - Initial PSS: [TBD] MB
  - After 15 min: [TBD] MB
  - Memory increase: Should be < 50 MB
- [ ] Verify no OutOfMemoryError in Logcat

### 21. CPU Monitoring
- [ ] Monitor CPU usage: `adb shell top -n 1 | grep com.tukanginAja.solusi`
- [ ] Check CPU usage:
  - Baseline: [TBD] %
  - Peak: [TBD] %
  - Average: Should be < 30%

### 22. Firestore Listener Management
- [ ] Check Logcat for listener registration
- [ ] Navigate between screens multiple times
- [ ] Verify no duplicate listeners
- [ ] Verify listeners cleaned up properly
- [ ] Check Firestore read quota (should not spike)

### 23. Network Usage
- [ ] Monitor network connections: `adb shell netstat | grep ESTABLISHED`
- [ ] Verify Firebase connections established
- [ ] Verify Google Maps API connections
- [ ] Check for unnecessary network calls

---

## 🐛 Error Handling

### 24. Crash Detection
- [ ] Monitor Logcat for FATAL errors
- [ ] Monitor for AndroidRuntime crashes
- [ ] Check Crashlytics (if enabled)
- [ ] Verify no crashes during 15-minute test

### 25. Error Scenarios
- [ ] Test with no internet connection
- [ ] Test with weak internet connection
- [ ] Test with Firestore offline mode
- [ ] Test with invalid FCM token
- [ ] Test with invalid chatId/orderId in notification

---

## 📝 Final Checks

### 26. Logcat Review
- [ ] Review Logcat for errors
- [ ] Check FCM logs: "FCM:"
- [ ] Check Firestore logs
- [ ] Check Maps logs
- [ ] Check BackgroundLocationService logs

### 27. Documentation
- [ ] Fill in internal_testing_feedback_tahap20.txt
- [ ] Document all issues found
- [ ] Document performance metrics
- [ ] Document user feedback (if applicable)

---

## ✅ Test Summary

Total Tests: 27 categories
Tests Passed: [TBD]
Tests Failed: [TBD]
Critical Issues: [TBD]

---

## 📋 Notes

- All tests should be performed on Android 14 (API 34) device/emulator
- FCM testing requires valid Firebase project configuration
- Some tests require two devices or emulator + Firebase Console
- Performance tests require extended testing period (15 minutes)

---

**Last Updated:** $(date)
**Tester:** [Your Name]
**Build Version:** 1.0
**APK:** app-release-unsigned.apk (6.4 MB)

