#!/bin/bash

# === FINAL VALIDATION STAGE: STAGING DEPLOYMENT TEST & VERIFICATION ===
# Script untuk validasi Firebase Staging Deployment

set -e

PROJECT_ID="tukanginaja-staging"
REGION="us-central1"
LOG_FILE="staging_validation_report.txt"

echo "🚀 Starting Staging Deployment Validation..." > $LOG_FILE
echo "==========================================" >> $LOG_FILE
echo "Project: $PROJECT_ID" >> $LOG_FILE
echo "Region: $REGION" >> $LOG_FILE
echo "Timestamp: $(date)" >> $LOG_FILE
echo "" >> $LOG_FILE

# 1️⃣ Check Firebase CLI is installed
echo "📦 Checking Firebase CLI..."
if ! command -v firebase &> /dev/null; then
    echo "❌ Firebase CLI not found. Please install: npm install -g firebase-tools"
    echo "❌ Firebase CLI not installed" >> $LOG_FILE
    exit 1
fi
echo "✅ Firebase CLI installed" >> $LOG_FILE

# 2️⃣ Check if logged in to Firebase
echo "🔐 Checking Firebase authentication..."
if ! firebase projects:list &> /dev/null; then
    echo "⚠️  Not logged in to Firebase. Please run: firebase login"
    echo "⚠️  Firebase authentication required" >> $LOG_FILE
else
    echo "✅ Firebase authenticated" >> $LOG_FILE
fi

# 3️⃣ List deployed functions
echo "📋 Listing deployed functions..."
echo "📋 Deployed Functions:" >> $LOG_FILE
firebase functions:list --project $PROJECT_ID >> $LOG_FILE 2>&1 || {
    echo "⚠️  Could not list functions. Check if project exists and functions are deployed."
    echo "⚠️  Function listing failed" >> $LOG_FILE
}
echo "" >> $LOG_FILE

# 4️⃣ Check recent function logs
echo "📊 Checking recent function logs..."
echo "📊 Recent Function Logs (last 50):" >> $LOG_FILE
firebase functions:log --project $PROJECT_ID --limit=50 >> $LOG_FILE 2>&1 || {
    echo "⚠️  Could not retrieve logs. Functions may not be deployed yet."
    echo "⚠️  Log retrieval failed" >> $LOG_FILE
}
echo "" >> $LOG_FILE

# 5️⃣ Test function endpoints (if functions are HTTP callable)
echo "🧪 Testing function endpoints..."
echo "🧪 Function Endpoint Tests:" >> $LOG_FILE

# Note: Cloud Functions are triggered by Firestore events, not HTTP calls
# These tests verify the functions are deployed, not directly callable via HTTP
echo "ℹ️  Cloud Functions are Firestore triggers (not HTTP endpoints)" >> $LOG_FILE
echo "ℹ️  Functions will trigger automatically on Firestore events" >> $LOG_FILE
echo "" >> $LOG_FILE

# 6️⃣ Check Firestore rules
echo "🔒 Checking Firestore rules..."
echo "🔒 Firestore Rules Status:" >> $LOG_FILE
if [ -f "firestore.rules" ]; then
    echo "✅ firestore.rules file exists" >> $LOG_FILE
    firebase deploy --only firestore:rules --project $PROJECT_ID --dry-run >> $LOG_FILE 2>&1 || echo "⚠️  Rules validation check" >> $LOG_FILE
else
    echo "⚠️  firestore.rules file not found" >> $LOG_FILE
fi
echo "" >> $LOG_FILE

# 7️⃣ Validation summary
echo "✅ Validation Summary:" >> $LOG_FILE
echo "==========================================" >> $LOG_FILE
echo "✅ Staging Deployment Validation Completed" >> $LOG_FILE
echo "📝 Full report saved to: $LOG_FILE" >> $LOG_FILE
echo "🔍 Review logs above for any warnings or errors" >> $LOG_FILE

# Display summary
echo ""
echo "=========================================="
echo "✅ Validation Complete!"
echo "📝 Full report: $LOG_FILE"
echo ""
echo "📋 Next Steps:"
echo "1. Review the validation report above"
echo "2. Check GitHub Actions for deployment status"
echo "3. Monitor Firebase Console for function logs"
echo "4. Test actual Firestore triggers by creating test documents"
echo ""
echo "🧪 To test functions manually:"
echo "   - Create a test order in Firestore: orders/{testId}"
echo "   - Watch function logs: firebase functions:log --project $PROJECT_ID"
echo "   - Check system_logs collection for event logs"
echo "=========================================="

cat $LOG_FILE

