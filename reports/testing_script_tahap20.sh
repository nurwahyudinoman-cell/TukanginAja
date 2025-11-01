#!/bin/bash

# TukanginAja - Internal Testing Script for Tahap 20
# Usage: ./testing_script_tahap20.sh

echo "=================================================================================="
echo "TUKANGINAJA - INTERNAL TESTING SCRIPT"
echo "TAHAP 20: SYSTEM NOTIFICATION & BACKGROUND MESSAGING OPTIMIZATION"
echo "=================================================================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if device is connected
echo "[1] Checking device connection..."
if adb devices | grep -q "device$"; then
    DEVICE=$(adb devices | grep "device$" | awk '{print $1}')
    echo -e "${GREEN}✅ Device connected: $DEVICE${NC}"
else
    echo -e "${RED}❌ No device connected. Please connect a device or start an emulator.${NC}"
    exit 1
fi

# Check if APK exists
echo ""
echo "[2] Checking release APK..."
APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
    echo -e "${GREEN}✅ APK found: $APK_PATH (Size: $APK_SIZE)${NC}"
else
    echo -e "${RED}❌ APK not found. Please run: ./gradlew assembleRelease${NC}"
    exit 1
fi

# Install APK
echo ""
echo "[3] Installing APK..."
adb install -r "$APK_PATH"
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ APK installed successfully${NC}"
else
    echo -e "${RED}❌ APK installation failed${NC}"
    exit 1
fi

# Launch application
echo ""
echo "[4] Launching application..."
adb shell am start -n com.tukanginAja.solusi/.MainActivity
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Application launched${NC}"
    sleep 3
else
    echo -e "${RED}❌ Failed to launch application${NC}"
    exit 1
fi

# Start logcat monitoring
echo ""
echo "[5] Starting logcat monitoring..."
echo -e "${YELLOW}Press Ctrl+C to stop logcat and continue with tests${NC}"
echo ""
adb logcat -c  # Clear logcat
adb logcat -s TukanginAja Firestore Maps FCMService BackgroundLocationService | tee logs/test_$(date +%Y%m%d_%H%M%S).log &
LOGCAT_PID=$!

# Wait for user to stop logcat
echo ""
read -p "Press Enter when you've finished monitoring logcat (Ctrl+C to stop early)..."

# Stop logcat
kill $LOGCAT_PID 2>/dev/null

# Memory check
echo ""
echo "[6] Checking memory usage..."
mkdir -p reports
adb shell dumpsys meminfo com.tukanginAja.solusi > reports/memory_info_$(date +%Y%m%d_%H%M%S).txt
echo -e "${GREEN}✅ Memory info saved to reports/${NC}"

# CPU check
echo ""
echo "[7] Checking CPU usage..."
adb shell top -n 1 -b | grep com.tukanginAja.solusi > reports/cpu_info_$(date +%Y%m%d_%H%M%S).txt
echo -e "${GREEN}✅ CPU info saved to reports/${NC}"

# Check for crashes
echo ""
echo "[8] Checking for crashes..."
adb logcat -d | grep -i "FATAL\|AndroidRuntime\|crash" > reports/crash_check_$(date +%Y%m%d_%H%M%S).txt
if [ -s reports/crash_check_*.txt ]; then
    echo -e "${RED}⚠️  Possible crashes detected. Check reports/crash_check_*.txt${NC}"
else
    echo -e "${GREEN}✅ No crashes detected in logcat${NC}"
fi

# Get app info
echo ""
echo "[9] Getting application info..."
adb shell dumpsys package com.tukanginAja.solusi | grep -A 5 "versionName\|versionCode" > reports/app_info_$(date +%Y%m%d_%H%M%S).txt
echo -e "${GREEN}✅ App info saved${NC}"

# Network check
echo ""
echo "[10] Checking network connections..."
adb shell netstat | grep ESTABLISHED | grep -E "firebase|google" > reports/network_check_$(date +%Y%m%d_%H%M%S).txt
echo -e "${GREEN}✅ Network connections logged${NC}"

echo ""
echo "=================================================================================="
echo "Testing script completed!"
echo "=================================================================================="
echo ""
echo "Reports generated in: reports/"
echo "Logs saved in: logs/"
echo ""
echo "Next steps:"
echo "1. Review the logcat output"
echo "2. Check memory and CPU reports"
echo "3. Test all modules manually (Auth, Maps, Chat, Orders, Notifications)"
echo "4. Fill in the internal_testing_feedback_tahap20.txt report"
echo ""

