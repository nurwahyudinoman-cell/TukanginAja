# TukanginAja - Internal Testing Documentation
## Tahap 20: System Notification & Background Messaging Optimization

---

## 📦 Build Status

### ✅ Build Successful
- **Build Command:** `./gradlew clean && ./gradlew assembleRelease`
- **Build Time:** ~2m 48s
- **Output APK:** `app/build/outputs/apk/release/app-release-unsigned.apk`
- **APK Size:** 6.4 MB
- **Build Date:** 2024-11-01

### Warnings (Non-Critical)
- Deprecated API usage (Icons.Filled.List, Icons.Filled.Send, menuAnchor)
- NDK source.properties warning
- Kapt language version fallback to 1.9

**Status:** ✅ Ready for Testing

---

## 📁 Testing Files

### 1. Testing Checklist
**File:** `TESTING_CHECKLIST_TAHAP20.md`
- Complete checklist with 27 test categories
- Step-by-step testing instructions
- Expected results for each test

### 2. Testing Script
**File:** `testing_script_tahap20.sh`
- Automated testing script
- Device connection check
- APK installation
- Logcat monitoring
- Memory/CPU monitoring

### 3. Feedback Report Template
**File:** `internal_testing_feedback_tahap20.txt`
- Comprehensive test report template
- Sections for all test results
- Performance metrics
- Known issues tracking

---

## 🚀 Quick Start Testing

### Prerequisites
1. Android device/emulator (Android 14 recommended)
2. ADB installed and configured
3. Internet connection
4. Firebase project configured

### Installation Steps

```bash
# 1. Navigate to project directory
cd /Users/nurwahyudin/AndroidStudioProjects/TukanginAja

# 2. Check device connection
adb devices

# 3. Install APK
adb install -r app/build/outputs/apk/release/app-release-unsigned.apk

# 4. Launch application
adb shell am start -n com.tukanginAja.solusi/.MainActivity

# 5. Monitor logcat
adb logcat -s TukanginAja Firestore Maps FCMService BackgroundLocationService
```

### Automated Testing

```bash
# Run automated testing script
./reports/testing_script_tahap20.sh
```

---

## 📋 Testing Checklist Summary

### Core Modules to Test

1. **Authentication** (Login, Register, Logout, FCM Token)
2. **Maps & Location** (Google Maps, Real-time updates, Route tracking)
3. **Chat Module** (Send/Receive, Real-time sync, Notifications)
4. **Order Management** (Create, Accept, Complete, Status updates)
5. **Notification System** (FCM tokens, Chat notifications, Order notifications)
6. **Background Services** (Location tracking, Messaging)

### Performance Tests

1. **Memory Monitoring** (15-minute stress test)
2. **CPU Monitoring** (Baseline, Peak, Average)
3. **Firestore Listener Management** (No duplicates, Proper cleanup)
4. **Network Usage** (Firebase, Maps API connections)

### Error Scenarios

1. No internet connection
2. Weak internet connection
3. Invalid FCM token
4. Invalid chatId/orderId in notifications

---

## 🔔 FCM Testing Setup

### Prerequisites
- Firebase project ID: `tukanginaja-b1s4`
- Valid FCM tokens from test users
- Firebase Console access

### Testing Chat Notifications

Send test FCM message from Firebase Console:

```json
{
  "data": {
    "type": "chat",
    "chatId": "your_chat_id",
    "senderId": "sender_user_id",
    "title": "Pesan Baru",
    "body": "Test notification message"
  },
  "token": "recipient_fcm_token"
}
```

### Testing Order Notifications

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

---

## 📊 Expected Results

### Application Launch
- ✅ App opens without crash
- ✅ Splash screen displays correctly
- ✅ Navigation to Login/Home based on auth state

### Real-time Updates
- ✅ Firestore changes reflect within < 1 second
- ✅ Chat messages sync in real-time
- ✅ Order status updates instantly
- ✅ Map markers update smoothly

### Notifications
- ✅ FCM tokens saved to Firestore
- ✅ Notifications appear in foreground
- ✅ Notifications appear in background
- ✅ Clicking notification navigates to correct screen

### Performance
- ✅ Memory usage stable (< 50 MB increase in 15 min)
- ✅ CPU usage reasonable (< 30% average)
- ✅ No memory leaks
- ✅ No duplicate Firestore listeners

---

## 🐛 Known Issues (To Be Filled)

### Issue Tracking
All discovered issues should be documented in:
- `internal_testing_feedback_tahap20.txt` (Known Issues section)
- Separate issue reports if critical

---

## 📝 Reporting

### After Testing

1. **Fill in Test Results**
   - Update `internal_testing_feedback_tahap20.txt` with actual results
   - Mark all checkboxes in `TESTING_CHECKLIST_TAHAP20.md`
   - Document any issues found

2. **Performance Metrics**
   - Record memory usage (Initial, After 15 min)
   - Record CPU usage (Baseline, Peak, Average)
   - Record response times (Launch, Navigation, API calls)

3. **User Feedback** (if applicable)
   - Collect feedback from internal testers
   - Document satisfaction scores
   - Note any usability concerns

4. **Final Summary**
   - Calculate pass rate
   - List critical issues
   - Provide recommendations

---

## ✅ Post-Testing Actions

### If All Tests Pass
1. ✅ Mark Tahap 20 as COMPLETE
2. ✅ Archive APK: `cp app/build/outputs/apk/release/app-release-unsigned.apk releases/TukanginAja_v1.0.20.apk`
3. ✅ Archive report: `cp reports/internal_testing_feedback_tahap20.txt reports/archive/`

### If Issues Found
1. Document in `internal_testing_feedback_tahap20.txt`
2. Create separate issue reports for critical bugs
3. Proceed to Tahap 20B: Bug Diagnostics & Fixing

---

## 🔗 Related Documentation

- `TESTING_CHECKLIST_TAHAP20.md` - Complete testing checklist
- `testing_script_tahap20.sh` - Automated testing script
- `internal_testing_feedback_tahap20.txt` - Test report template

---

## 📞 Support

For questions or issues during testing:
1. Check Logcat output for errors
2. Review Firebase Console for connection issues
3. Verify ADB connection: `adb devices`
4. Check FCM token in Firestore users collection

---

**Last Updated:** 2024-11-01
**Build Version:** 1.0
**Target Platform:** Android 14 (API 34)

