#!/bin/bash

# CI Auto Repair Script
# This script is called when CI build fails and attempts to trigger
# Cursor AI's auto-repair process or outputs standardized instructions

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LOGS_DIR="$PROJECT_ROOT/logs"
REPAIR_LOG="$LOGS_DIR/build-fix-report.txt"
INSTRUCTION_FILE="$LOGS_DIR/ci-repair-instructions.txt"

# Create logs directory if it doesn't exist
mkdir -p "$LOGS_DIR"

# Log entry with timestamp
TIMESTAMP=$(date -u +"%Y-%m-%d %H:%M:%S UTC")
echo "[$TIMESTAMP] CI Auto Repair Triggered" | tee -a "$REPAIR_LOG"
echo "========================================" | tee -a "$REPAIR_LOG"
echo "" | tee -a "$REPAIR_LOG"

# Capture build error output
echo "Capturing build error details..." | tee -a "$REPAIR_LOG"
echo "" | tee -a "$REPAIR_LOG"

# Try to capture last build error
if [ -f "$PROJECT_ROOT/app/build/reports/tests/test/index.html" ]; then
    echo "Test report found" | tee -a "$REPAIR_LOG"
fi

# Check for common build error patterns
BUILD_LOG="$PROJECT_ROOT/build.log"
if [ -f "$BUILD_LOG" ]; then
    echo "Build log found" | tee -a "$REPAIR_LOG"
fi

# Create standardized instruction file for Cursor AI
cat > "$INSTRUCTION_FILE" << EOF
# CI Auto Repair Instructions for Cursor AI
# Generated: $TIMESTAMP
# Project: TukanginAja
# Build: Failed in CI

## Build Failure Detected

The CI build has failed. Cursor AI should analyze and fix the errors using the Auto Repair Loop policy.

## Required Actions

1. Analyze the build error logs
2. Identify root cause of the failure
3. Apply fixes following .cursorrules/auto_repair_loop.md policy
4. Log all fixes to $REPAIR_LOG
5. Ensure fixes do not require database schema changes without approval

## Build Context

- Java Version: 17
- Gradle Version: 8.13 (wrapper)
- Kotlin Version: 2.0.21
- Platform: Linux (GitHub Actions Ubuntu)
- Build Command: ./gradlew clean build

## Error Analysis

Run the following command to see build errors:
./gradlew clean build --stacktrace 2>&1 | tee build-error.log

## Fix Logging Format

Each fix must be logged to $REPAIR_LOG in this format:

[$TIMESTAMP] Build Fix Report
========================================
Error: [error description]
Files Changed:
  - [file path 1]
  - [file path 2]
Fix Applied:
  - [description of fix]
Rationale:
  - [why this fix addresses the root cause]
========================================

## Safety Checks

- ⚠️ NEVER change database schema without approval marker
- ⚠️ NEVER modify API keys or secrets
- ⚠️ NEVER modify signing configurations
- ⚠️ Flag major dependency version changes for review

## Next Steps

1. Run: ./gradlew clean build --stacktrace
2. Analyze error output
3. Apply fixes using Auto Repair Loop policy
4. Log fixes to $REPAIR_LOG
5. Rebuild to verify fixes

EOF

echo "Instruction file created: $INSTRUCTION_FILE" | tee -a "$REPAIR_LOG"
echo "" | tee -a "$REPAIR_LOG"

# Attempt to trigger Cursor AI auto-repair if Cursor CLI is available
if command -v cursor &> /dev/null; then
    echo "Cursor CLI detected. Attempting to trigger auto-repair..." | tee -a "$REPAIR_LOG"
    
    # Try to trigger Cursor auto-repair via CLI
    # Note: This is a placeholder - actual implementation depends on Cursor CLI API
    cursor auto-repair --project "$PROJECT_ROOT" --log "$REPAIR_LOG" || {
        echo "Cursor CLI auto-repair not available or failed" | tee -a "$REPAIR_LOG"
        echo "Falling back to instruction file generation" | tee -a "$REPAIR_LOG"
    }
else
    echo "Cursor CLI not available in CI environment" | tee -a "$REPAIR_LOG"
    echo "Instruction file generated for manual review:" | tee -a "$REPAIR_LOG"
    echo "  $INSTRUCTION_FILE" | tee -a "$REPAIR_LOG"
fi

# Capture error details from last gradlew run if possible
echo "" | tee -a "$REPAIR_LOG"
echo "Error Details:" | tee -a "$REPAIR_LOG"
echo "==============" | tee -a "$REPAIR_LOG"

# Try to run gradlew with error capture
if [ -f "$PROJECT_ROOT/gradlew" ]; then
    cd "$PROJECT_ROOT"
    ./gradlew clean build --stacktrace 2>&1 | tail -50 | tee -a "$REPAIR_LOG" || true
fi

echo "" | tee -a "$REPAIR_LOG"
echo "[$TIMESTAMP] CI Auto Repair Script Completed" | tee -a "$REPAIR_LOG"
echo "========================================" | tee -a "$REPAIR_LOG"
echo "" | tee -a "$REPAIR_LOG"

# Exit with success to allow CI to continue
# The actual build failure will be handled by the workflow
exit 0

