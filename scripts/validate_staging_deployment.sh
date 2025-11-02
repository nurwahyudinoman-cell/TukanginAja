#!/bin/bash

# ============================================
# Staging Deployment Validation Script
# ============================================
# Script untuk validasi Firebase Functions deployment di staging
# Usage: ./scripts/validate_staging_deployment.sh

set -e

PROJECT_ID="tukanginaja-staging"
REGION="us-central1"
LOG_LIMIT=100

echo "🔍 Starting Staging Deployment Validation..."
echo "Project: $PROJECT_ID"
echo "Region: $REGION"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Firebase CLI is installed
if ! command -v firebase &> /dev/null; then
    echo -e "${RED}❌ Firebase CLI not found. Please install: npm install -g firebase-tools${NC}"
    exit 1
fi

# Check if logged in
echo "🔐 Checking Firebase authentication..."
if ! firebase projects:list &> /dev/null; then
    echo -e "${YELLOW}⚠️  Not logged in to Firebase. Please run: firebase login${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Firebase CLI authenticated${NC}"
echo ""

# List deployed functions
echo "📦 Listing deployed Firebase Functions..."
echo "----------------------------------------"
firebase functions:list --project "$PROJECT_ID" 2>&1 || {
    echo -e "${RED}❌ Failed to list functions. Check if project '$PROJECT_ID' exists and you have access.${NC}"
    exit 1
}
echo ""

# Check function status
echo "📊 Checking function status..."
FUNCTIONS=$(firebase functions:list --project "$PROJECT_ID" 2>&1 | grep -E "^(onOrderStatusUpdate|onNewMessage|onNewRating|onPaymentSuccess)" || true)

if [ -z "$FUNCTIONS" ]; then
    echo -e "${YELLOW}⚠️  No functions found or functions not deployed yet.${NC}"
    echo -e "${YELLOW}   This might be expected if deployment hasn't completed.${NC}"
else
    echo -e "${GREEN}✅ Found deployed functions:${NC}"
    echo "$FUNCTIONS" | while IFS= read -r line; do
        echo "   - $line"
    done
fi
echo ""

# Check logs for errors
echo "📋 Checking recent function logs (last $LOG_LIMIT entries)..."
echo "----------------------------------------"
LOG_OUTPUT=$(firebase functions:log --project "$PROJECT_ID" --limit "$LOG_LIMIT" 2>&1 || echo "")

if [ -z "$LOG_OUTPUT" ]; then
    echo -e "${YELLOW}⚠️  No logs found or logging not available yet.${NC}"
else
    ERROR_COUNT=$(echo "$LOG_OUTPUT" | grep -i "error\|failed\|exception" | wc -l | tr -d ' ')
    
    if [ "$ERROR_COUNT" -gt 0 ]; then
        echo -e "${RED}⚠️  Found $ERROR_COUNT potential errors in logs:${NC}"
        echo "$LOG_OUTPUT" | grep -i "error\|failed\|exception" | head -10
    else
        echo -e "${GREEN}✅ No errors found in recent logs${NC}"
    fi
    
    # Show sample logs
    echo ""
    echo "Sample logs:"
    echo "$LOG_OUTPUT" | head -20
fi
echo ""

# Validate project configuration
echo "⚙️  Validating project configuration..."
if [ -f ".firebaserc" ]; then
    echo -e "${GREEN}✅ .firebaserc file exists${NC}"
    PROJECT_CONFIG=$(grep -A 1 "default" .firebaserc | grep -o '"[^"]*"' | tr -d '"')
    if [ "$PROJECT_CONFIG" == "$PROJECT_ID" ]; then
        echo -e "${GREEN}✅ Project ID matches: $PROJECT_CONFIG${NC}"
    else
        echo -e "${YELLOW}⚠️  Project ID mismatch. Expected: $PROJECT_ID, Found: $PROJECT_CONFIG${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  .firebaserc file not found${NC}"
fi
echo ""

# Validate GitHub Actions workflow
echo "🔧 Checking CI/CD configuration..."
if [ -f ".github/workflows/deploy-staging.yml" ]; then
    echo -e "${GREEN}✅ GitHub Actions workflow file exists${NC}"
    if grep -q "tukanginaja-staging" .github/workflows/deploy-staging.yml; then
        echo -e "${GREEN}✅ Workflow configured for staging project${NC}"
    else
        echo -e "${YELLOW}⚠️  Workflow might not be configured correctly${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  GitHub Actions workflow file not found${NC}"
fi
echo ""

# Summary
echo "==========================================="
echo "📊 Validation Summary"
echo "==========================================="
echo ""

# Generate report
REPORT_FILE="staging_validation_report.txt"
cat > "$REPORT_FILE" <<EOF
===========================================
STAGING DEPLOYMENT VALIDATION REPORT
===========================================
Date: $(date)
Project: $PROJECT_ID
Region: $REGION

VALIDATION CHECKLIST:
[✓] Firebase CLI installed and authenticated
[$(if [ -f ".firebaserc" ]; then echo '✓'; else echo '✗'; fi)] Project configuration file exists
[$(if [ -f ".github/workflows/deploy-staging.yml" ]; then echo '✓'; else echo '✗'; fi)] CI/CD workflow configured
[$(if [ ! -z "$FUNCTIONS" ]; then echo '✓'; else echo '⚠'; fi)] Functions deployed

FUNCTIONS STATUS:
$(if [ ! -z "$FUNCTIONS" ]; then echo "$FUNCTIONS"; else echo "No functions deployed or detected"; fi)

LOGS STATUS:
$(if [ ! -z "$LOG_OUTPUT" ]; then echo "Logs available - $(echo "$LOG_OUTPUT" | wc -l | tr -d ' ') entries checked"; else echo "No logs available yet"; fi)
ERROR COUNT: $ERROR_COUNT

NEXT STEPS:
1. Verify functions are deployed via Firebase Console
2. Test functions by triggering Firestore events
3. Monitor logs for any errors
4. Test end-to-end workflow with real data

NOTES:
- Firestore trigger functions cannot be tested directly via HTTP
- Functions will trigger automatically on Firestore events
- Use Firebase Console to monitor function executions
- Check GitHub Actions for deployment status

===========================================
EOF

echo -e "${GREEN}✅ Validation report generated: $REPORT_FILE${NC}"
echo ""
echo "📄 Report Contents:"
cat "$REPORT_FILE"
echo ""

echo -e "${GREEN}✅ Staging Deployment Validation Complete!${NC}"
echo ""
echo "📝 Next Steps:"
echo "   1. Review the validation report: $REPORT_FILE"
echo "   2. Check Firebase Console for function deployments"
echo "   3. Test functions by creating/updating Firestore documents"
echo "   4. Monitor logs in Firebase Console"
echo ""
