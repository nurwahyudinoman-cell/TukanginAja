# Backup Configuration Guide

**Project:** TukanginAja  
**Last Updated:** 2025-11-01  
**Status:** ⚠️ Configuration Required

---

## Overview

This document provides configuration for automated Firestore backups and storage retention policies for the TukanginAja project. Backups are critical for disaster recovery and data protection.

---

## Firestore Backup Configuration

### Backup Schedule

**Recommended:** Weekly automated backups with monthly retention

- **Frequency:** Weekly (every Sunday at 2:00 AM UTC)
- **Retention:** 
  - Daily backups: Last 7 days
  - Weekly backups: Last 4 weeks
  - Monthly backups: Last 12 months

### Storage Bucket Configuration

#### Bucket Naming Convention

```
tukanginaja-backups
├── firestore/
│   ├── daily/
│   │   └── YYYY-MM-DD/
│   │       └── firestore-export-YYYY-MM-DD-HHMMSS
│   ├── weekly/
│   │   └── YYYY-MM-DD/
│   │       └── firestore-export-YYYY-MM-DD-HHMMSS
│   └── monthly/
│       └── YYYY-MM/
│           └── firestore-export-YYYY-MM-DD-HHMMSS
└── metadata/
    └── backup-index.json
```

**Bucket Name:** `tukanginaja-backups`  
**Location:** `asia-southeast2` (or preferred region)  
**Storage Class:** `STANDARD` (for frequent access), `NEARLINE` (for weekly/monthly)

### Firestore Export Commands

#### Using Firebase CLI

```bash
# Install Firebase CLI (if not installed)
npm install -g firebase-tools

# Login to Firebase
firebase login

# Set Firebase project
firebase use tukanginaja-b1s4  # Replace with your actual project ID

# Manual export command
firebase firestore:export gs://tukanginaja-backups/firestore/$(date +%Y-%m-%d)/firestore-export-$(date +%Y-%m-%d-%H%M%S)
```

#### Using gcloud CLI

```bash
# Install gcloud CLI (if not installed)
# https://cloud.google.com/sdk/docs/install

# Authenticate
gcloud auth login

# Set project
gcloud config set project tukanginaja-b1s4  # Replace with your actual project ID

# Create backup bucket (one-time setup)
gsutil mb -p tukanginaja-b1s4 -c STANDARD -l asia-southeast2 gs://tukanginaja-backups

# Manual export command
gcloud firestore export gs://tukanginaja-backups/firestore/$(date +%Y-%m-%d)/firestore-export-$(date +%Y-%m-%d-%H%M%S) \
  --project=tukanginaja-b1s4 \
  --collection-ids=tukang_locations,service_requests,chats,route_history

# Export all collections (default)
gcloud firestore export gs://tukanginaja-backups/firestore/$(date +%Y-%m-%d)/firestore-export-$(date +%Y-%m-%d-%H%M%S) \
  --project=tukanginaja-b1s4
```

### Automated Backup Scheduling

#### Option 1: Cloud Functions (Recommended)

Create a Cloud Function that triggers weekly:

**Function Code:**
```javascript
const {Firestore} = require('@google-cloud/firestore');
const {Storage} = require('@google-cloud/storage');
const admin = require('firebase-admin');

admin.initializeApp();

exports.weeklyFirestoreBackup = functions.pubsub
  .schedule('0 2 * * 0') // Every Sunday at 2:00 AM UTC
  .timeZone('UTC')
  .onRun(async (context) => {
    const projectId = admin.app().options.projectId;
    const bucketName = 'tukanginaja-backups';
    const date = new Date().toISOString().split('T')[0];
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const backupPath = `firestore/weekly/${date}/firestore-export-${timestamp}`;
    
    const exportPath = `gs://${bucketName}/${backupPath}`;
    
    // Use Admin SDK to export Firestore
    // Note: This requires appropriate IAM permissions
    console.log(`Starting Firestore backup to ${exportPath}`);
    
    // For actual export, use gcloud command via Cloud Functions
    // or use Firestore Admin SDK export method if available
    
    return { success: true, path: exportPath };
  });
```

**Deployment:**
```bash
firebase deploy --only functions:weeklyFirestoreBackup
```

#### Option 2: Cloud Scheduler + Cloud Functions

**Create Scheduler Job:**
```bash
gcloud scheduler jobs create pubsub weekly-firestore-backup \
  --schedule="0 2 * * 0" \
  --time-zone="UTC" \
  --topic=firestore-backup \
  --message-body='{"action":"backup","type":"weekly"}' \
  --project=tukanginaja-b1s4
```

**Cloud Function Trigger:**
```javascript
exports.firestoreBackupTrigger = functions.pubsub
  .topic('firestore-backup')
  .onPublish(async (message) => {
    const { action, type } = message.json;
    // Execute backup based on type
  });
```

#### Option 3: Cron Job (Linux Server)

Create a cron job on a server with gcloud CLI:

```bash
# Edit crontab
crontab -e

# Add weekly backup (every Sunday at 2:00 AM UTC)
0 2 * * 0 /usr/local/bin/firestore-backup.sh >> /var/log/firestore-backup.log 2>&1
```

**Backup Script:** `/usr/local/bin/firestore-backup.sh`
```bash
#!/bin/bash

PROJECT_ID="tukanginaja-b1s4"
BUCKET="tukanginaja-backups"
DATE=$(date +%Y-%m-%d)
TIMESTAMP=$(date +%Y-%m-%d-%H%M%S)
BACKUP_PATH="firestore/weekly/${DATE}/firestore-export-${TIMESTAMP}"

# Export Firestore
gcloud firestore export gs://${BUCKET}/${BACKUP_PATH} \
  --project=${PROJECT_ID}

# Verify export
if [ $? -eq 0 ]; then
  echo "Backup successful: ${BACKUP_PATH}"
  
  # Send notification (optional)
  # curl -X POST https://hooks.slack.com/services/YOUR/WEBHOOK/URL \
  #   -d "{\"text\":\"Firestore backup completed: ${BACKUP_PATH}\"}"
else
  echo "Backup failed!"
  exit 1
fi
```

### Retention Policy

#### Storage Lifecycle Rules

Create lifecycle rules in Cloud Storage to automatically delete old backups:

**Using gcloud CLI:**
```bash
# Create lifecycle configuration file
cat > lifecycle.json << EOF
{
  "lifecycle": {
    "rule": [
      {
        "action": {"type": "Delete"},
        "condition": {
          "age": 30,
          "matchesPrefix": ["firestore/daily/"]
        }
      },
      {
        "action": {"type": "Delete"},
        "condition": {
          "age": 90,
          "matchesPrefix": ["firestore/weekly/"]
        }
      },
      {
        "action": {"type": "SetStorageClass"},
        "condition": {
          "age": 30,
          "matchesPrefix": ["firestore/weekly/"]
        },
        "action": {
          "type": "SetStorageClass",
          "storageClass": "NEARLINE"
        }
      },
      {
        "action": {"type": "SetStorageClass"},
        "condition": {
          "age": 90,
          "matchesPrefix": ["firestore/monthly/"]
        },
        "action": {
          "type": "SetStorageClass",
          "storageClass": "COLDLINE"
        }
      },
      {
        "action": {"type": "Delete"},
        "condition": {
          "age": 365,
          "matchesPrefix": ["firestore/monthly/"]
        }
      }
    ]
  }
}
EOF

# Apply lifecycle rules
gsutil lifecycle set lifecycle.json gs://tukanginaja-backups
```

**Retention Summary:**
- **Daily backups:** 30 days
- **Weekly backups:** 90 days (moved to NEARLINE after 30 days)
- **Monthly backups:** 365 days (moved to COLDLINE after 90 days)

### Backup Verification

#### Manual Verification

```bash
# List recent backups
gsutil ls -r gs://tukanginaja-backups/firestore/

# Verify backup integrity
gsutil stat gs://tukanginaja-backups/firestore/weekly/2025-11-01/firestore-export-2025-11-01-020000/

# Download and inspect backup
gsutil cp -r gs://tukanginaja-backups/firestore/weekly/2025-11-01/firestore-export-2025-11-01-020000/ ./local-backup/
```

#### Automated Verification Script

```bash
#!/bin/bash
# verify-backup.sh

BUCKET="tukanginaja-backups"
DATE=$(date +%Y-%m-%d)

# Find latest weekly backup
LATEST_BACKUP=$(gsutil ls -r gs://${BUCKET}/firestore/weekly/${DATE}/ | tail -1)

if [ -z "$LATEST_BACKUP" ]; then
  echo "ERROR: No backup found for ${DATE}"
  exit 1
fi

# Check if backup is complete (should contain metadata file)
if gsutil ls ${LATEST_BACKUP}firestore_export/metadata > /dev/null 2>&1; then
  echo "Backup verified: ${LATEST_BACKUP}"
  exit 0
else
  echo "ERROR: Backup incomplete: ${LATEST_BACKUP}"
  exit 1
fi
```

### Backup Restoration

#### Restore from Backup

```bash
# Restore specific collection
gcloud firestore import gs://tukanginaja-backups/firestore/weekly/2025-11-01/firestore-export-2025-11-01-020000 \
  --project=tukanginaja-b1s4 \
  --collection-ids=service_requests

# Restore all collections
gcloud firestore import gs://tukanginaja-backups/firestore/weekly/2025-11-01/firestore-export-2025-11-01-020000 \
  --project=tukanginaja-b1s4

# Restore to different database (for testing)
gcloud firestore import gs://tukanginaja-backups/firestore/weekly/2025-11-01/firestore-export-2025-11-01-020000 \
  --project=tukanginaja-b1s4 \
  --database-id=backup-test
```

### Monitoring Backup Status

#### Cloud Monitoring Alerts

Create alerts for backup failures:

```bash
# Create alert policy for backup failures
gcloud alpha monitoring policies create \
  --notification-channels=CHANNEL_ID \
  --display-name="Firestore Backup Failure Alert" \
  --condition-display-name="Backup job failed" \
  --condition-threshold-value=1 \
  --condition-threshold-duration=300s \
  --condition-filter='resource.type="cloud_function" AND resource.labels.function_name="weeklyFirestoreBackup" AND metric.type="cloudfunctions.googleapis.com/function/execution_count" AND metric.labels.status="error"'
```

### Cost Optimization

#### Storage Costs

- **STANDARD Storage:** ~$0.020 per GB/month
- **NEARLINE Storage:** ~$0.010 per GB/month (30-day minimum)
- **COLDLINE Storage:** ~$0.004 per GB/month (90-day minimum)

**Recommendation:**
- Use STANDARD for recent backups (0-30 days)
- Use NEARLINE for weekly backups (30-90 days)
- Use COLDLINE for monthly backups (90-365 days)

### Security Best Practices

1. **IAM Permissions:**
   - Grant minimal permissions for backup service account
   - Use service account for automated backups
   - Restrict bucket access to authorized users only

2. **Encryption:**
   - Enable bucket-level encryption
   - Use customer-managed encryption keys (CMEK) for sensitive data

3. **Access Control:**
   - Limit bucket access to backup service accounts
   - Use signed URLs for temporary access if needed

---

## Next Steps

### Immediate Actions

1. **Create Backup Bucket:**
   ```bash
   gsutil mb -p tukanginaja-b1s4 -c STANDARD -l asia-southeast2 gs://tukanginaja-backups
   ```

2. **Set Up Service Account:**
   ```bash
   # Create service account for backups
   gcloud iam service-accounts create firestore-backup \
     --display-name="Firestore Backup Service Account" \
     --project=tukanginaja-b1s4
   
   # Grant necessary permissions
   gcloud projects add-iam-policy-binding tukanginaja-b1s4 \
     --member="serviceAccount:firestore-backup@tukanginaja-b1s4.iam.gserviceaccount.com" \
     --role="roles/datastore.exportAdmin"
   ```

3. **Test Manual Backup:**
   ```bash
   gcloud firestore export gs://tukanginaja-backups/firestore/test/firestore-export-test \
     --project=tukanginaja-b1s4
   ```

4. **Configure Lifecycle Rules:**
   ```bash
   gsutil lifecycle set lifecycle.json gs://tukanginaja-backups
   ```

### Short Term Actions

1. Set up automated weekly backup schedule
2. Configure monitoring alerts
3. Document restoration procedures
4. Test backup restoration

### Long Term Actions

1. Implement backup verification automation
2. Set up backup reporting dashboard
3. Create disaster recovery runbook
4. Regular backup restoration drills

---

## Troubleshooting

### Common Issues

1. **Permission Denied:**
   - Verify service account has `datastore.exportAdmin` role
   - Check bucket IAM permissions

2. **Backup Timeout:**
   - Large databases may require longer timeout
   - Consider exporting collections individually

3. **Storage Costs:**
   - Monitor storage usage regularly
   - Adjust retention policy if needed

---

**Configuration Status:** ⚠️ Requires Setup  
**Last Verified:** 2025-11-01  
**Next Review:** After initial setup

