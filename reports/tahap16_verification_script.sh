#!/bin/bash

# TAHAP 16 Verification Script
# This script helps verify background tracking and FCM notifications

set -e

PROJECT_DIR="/Users/nurwahyudin/AndroidStudioProjects/TukanginAja"
REPORT_FILE="$PROJECT_DIR/reports/tahap16_verification_report.txt"
LOG_FILE="$PROJECT_DIR/runtime_reports/tahap16_logcat.txt"

mkdir -p "$PROJECT_DIR/reports"
mkdir -p "$PROJECT_DIR/runtime_reports"

echo "=========================================" > "$REPORT_FILE"
echo "TAHAP 16 VERIFICATION REPORT" >> "$REPORT_FILE"
echo "Generated: $(date)" >> "$REPORT_FILE"
echo "=========================================" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# 1. Build Status
echo "[1/8] Testing Build Status..." | tee -a "$REPORT_FILE"
cd "$PROJECT_DIR"
if ./gradlew clean assembleDebug > /tmp/build_output.log 2>&1; then
    echo "✅ BUILD STATUS: PASSED" >> "$REPORT_FILE"
    echo "Build completed successfully" >> "$REPORT_FILE"
else
    echo "❌ BUILD STATUS: FAILED" >> "$REPORT_FILE"
    cat /tmp/build_output.log >> "$REPORT_FILE"
fi
echo "" >> "$REPORT_FILE"

# 2. Check for required files
echo "[2/8] Checking Required Files..." | tee -a "$REPORT_FILE"
REQUIRED_FILES=(
    "app/src/main/java/com/tukanginAja/solusi/service/BackgroundLocationService.kt"
    "app/src/main/java/com/tukanginAja/solusi/service/NotificationService.kt"
    "app/src/main/java/com/tukanginAja/solusi/notification/NotificationHelper.kt"
    "app/src/main/java/com/tukanginAja/solusi/notification/MyFirebaseMessagingService.kt"
    "app/src/main/AndroidManifest.xml"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$PROJECT_DIR/$file" ]; then
        echo "✅ $file exists" >> "$REPORT_FILE"
    else
        echo "❌ $file MISSING" >> "$REPORT_FILE"
    fi
done
echo "" >> "$REPORT_FILE"

# 3. Check AndroidManifest permissions
echo "[3/8] Checking AndroidManifest Permissions..." | tee -a "$REPORT_FILE"
if grep -q "ACCESS_FINE_LOCATION" "$PROJECT_DIR/app/src/main/AndroidManifest.xml"; then
    echo "✅ ACCESS_FINE_LOCATION permission found" >> "$REPORT_FILE"
else
    echo "❌ ACCESS_FINE_LOCATION permission MISSING" >> "$REPORT_FILE"
fi

if grep -q "ACCESS_BACKGROUND_LOCATION" "$PROJECT_DIR/app/src/main/AndroidManifest.xml"; then
    echo "✅ ACCESS_BACKGROUND_LOCATION permission found" >> "$REPORT_FILE"
else
    echo "❌ ACCESS_BACKGROUND_LOCATION permission MISSING" >> "$REPORT_FILE"
fi

if grep -q "POST_NOTIFICATIONS" "$PROJECT_DIR/app/src/main/AndroidManifest.xml"; then
    echo "✅ POST_NOTIFICATIONS permission found" >> "$REPORT_FILE"
else
    echo "❌ POST_NOTIFICATIONS permission MISSING" >> "$REPORT_FILE"
fi

if grep -q "foregroundServiceType=\"location\"" "$PROJECT_DIR/app/src/main/AndroidManifest.xml"; then
    echo "✅ Foreground service type location configured" >> "$REPORT_FILE"
else
    echo "❌ Foreground service type location NOT configured" >> "$REPORT_FILE"
fi
echo "" >> "$REPORT_FILE"

# 4. Check for throttling logs in BackgroundLocationService
echo "[4/8] Checking Throttling Implementation..." | tee -a "$REPORT_FILE"
if grep -q "Tracking: skipped write" "$PROJECT_DIR/app/src/main/java/com/tukanginAja/solusi/service/BackgroundLocationService.kt"; then
    echo "✅ Throttling logs found in BackgroundLocationService" >> "$REPORT_FILE"
else
    echo "❌ Throttling logs NOT found" >> "$REPORT_FILE"
fi

if grep -q "writesAttempted\|writesExecuted\|writesSkipped" "$PROJECT_DIR/app/src/main/java/com/tukanginAja/solusi/service/BackgroundLocationService.kt"; then
    echo "✅ Stats tracking variables found" >> "$REPORT_FILE"
else
    echo "❌ Stats tracking variables NOT found" >> "$REPORT_FILE"
fi
echo "" >> "$REPORT_FILE"

# 5. Check for FCM data message handling
echo "[5/8] Checking FCM Data Message Handling..." | tee -a "$REPORT_FILE"
if grep -q "type.*chat\|order\|proximity" "$PROJECT_DIR/app/src/main/java/com/tukanginAja/solusi/notification/MyFirebaseMessagingService.kt"; then
    echo "✅ FCM data message type handling found" >> "$REPORT_FILE"
else
    echo "❌ FCM data message type handling NOT found" >> "$REPORT_FILE"
fi

if grep -q "showNotificationFromDataMessage" "$PROJECT_DIR/app/src/main/java/com/tukanginAja/solusi/notification/NotificationHelper.kt"; then
    echo "✅ showNotificationFromDataMessage method found" >> "$REPORT_FILE"
else
    echo "❌ showNotificationFromDataMessage method NOT found" >> "$REPORT_FILE"
fi
echo "" >> "$REPORT_FILE"

# 6. Check for GeoPoint usage
echo "[6/8] Checking GeoPoint Implementation..." | tee -a "$REPORT_FILE"
if grep -q "GeoPoint" "$PROJECT_DIR/app/src/main/java/com/tukanginAja/solusi/service/BackgroundLocationService.kt"; then
    echo "✅ GeoPoint usage found in BackgroundLocationService" >> "$REPORT_FILE"
else
    echo "❌ GeoPoint usage NOT found" >> "$REPORT_FILE"
fi
echo "" >> "$REPORT_FILE"

# 7. Check for auto-stop on completed
echo "[7/8] Checking Auto-Stop Implementation..." | tee -a "$REPORT_FILE"
if grep -q "completed\|cancelled" "$PROJECT_DIR/app/src/main/java/com/tukanginAja/solusi/service/BackgroundLocationService.kt" && grep -q "stopTracking" "$PROJECT_DIR/app/src/main/java/com/tukanginAja/solusi/service/BackgroundLocationService.kt"; then
    echo "✅ Auto-stop on completed/cancelled found" >> "$REPORT_FILE"
else
    echo "❌ Auto-stop on completed/cancelled NOT found" >> "$REPORT_FILE"
fi
echo "" >> "$REPORT_FILE"

# 8. Manual Testing Instructions
echo "[8/8] Manual Testing Instructions..." | tee -a "$REPORT_FILE"
cat >> "$REPORT_FILE" << 'INSTRUCTIONS'

MANUAL TESTING INSTRUCTIONS:
============================

A) Foreground Service Test:
   1. Start app as tukang (logged in)
   2. Navigate to Tukang Dashboard
   3. Start order tracking (tap "Mulai Perjalanan" on an order)
   4. Verify:
      - Foreground notification appears with "Melacak Lokasi Tukang"
      - Check logcat for "Tracking: service started at..."
      - Background the app (press home button)
      - Service should continue running (notification persists)
      - Location updates continue in Firestore (check tukang_locations collection)

B) Throttling Test:
   1. While tracking is active, monitor logcat for:
      - "Tracking: skipped write - distance X.XXm, time XXXXms"
      - "Tracking: updated location for tukang..."
   2. Verify writes are throttled (not every location update writes to Firestore)
   3. Check Firestore - location field should update every 12+ seconds or when moved >15m

C) Background Tracking Test:
   1. With app in background, simulate movement (emulator geo simulation or real device movement)
   2. Verify:
      - FusedLocationProvider continues to provide updates
      - Firestore location field updates (but throttled)
      - Notification persists in status bar

D) FCM Notification Test:
   1. Send chat message to tukang while app is in background
   2. Verify:
      - FCM message received (check logcat: "FCM: Chat notification shown")
      - System notification appears
      - Tapping notification opens ChatScreen (deeplink works)
   
   3. Update order status to "completed" via Firestore console
   4. Verify:
      - Service auto-stops (check logcat: "Order completed, stopping location tracking automatically")
      - Notification disappears
      - Firestore status updated to "offline"

E) Order Auto-Stop Test:
   1. While tracking is active, update order status in Firestore:
      - Set status to "completed" OR "cancelled"
   2. Verify:
      - Service stops automatically
      - Logcat shows: "Order completed, stopping location tracking automatically"
      - Listener cleanup occurs (no memory leaks)

F) Stats Tracking Test:
   1. After tracking session, check service stats:
      - writesAttempted should > writesExecuted
      - writesSkipped should reflect throttling
      - Check logcat for "Tracking: stats - attempted: X, executed: Y, skipped: Z"

LOG COLLECTION:
===============
To capture logs for verification, run:
  adb logcat -s "BackgroundLocationService:*" "FCM:*" "NotificationHelper:*" "Tracking:*" > runtime_reports/tahap16_logcat.txt

INSTRUCTIONS
echo "" >> "$REPORT_FILE"

# Final Summary
echo "=========================================" >> "$REPORT_FILE"
echo "VERIFICATION SUMMARY" >> "$REPORT_FILE"
echo "=========================================" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "Build Status: $(grep -o "BUILD STATUS:.*" "$REPORT_FILE" | head -1 | cut -d: -f2-)" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "✅ Code checks completed" >> "$REPORT_FILE"
echo "⚠️  Manual testing required - follow instructions above" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "Report saved to: $REPORT_FILE" >> "$REPORT_FILE"
echo "Logs should be saved to: $LOG_FILE" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

cat "$REPORT_FILE"

