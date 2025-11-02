# 🚀 Staging Deployment Validation Guide

## 📋 Overview

Guide ini menjelaskan langkah-langkah untuk melakukan validasi deployment Firebase Functions ke staging environment.

## ⚙️ Prerequisites

### 1. Setup GitHub Secrets

Sebelum menjalankan workflow, pastikan secret berikut sudah diatur di GitHub:

**Lokasi:** `Settings → Secrets and variables → Actions`

**Secret yang diperlukan:**
- `FIREBASE_DEPLOY_TOKEN`: Token dari `firebase login:ci`
  ```bash
  firebase login:ci
  # Copy token yang dihasilkan dan paste sebagai secret value
  ```

### 2. Setup Firebase Project

Pastikan project `tukanginaja-staging` sudah dibuat di Firebase Console:
- [Firebase Console](https://console.firebase.google.com/)
- Create new project dengan ID: `tukanginaja-staging`

### 3. Install Dependencies

```bash
# Install Firebase CLI globally
npm install -g firebase-tools

# Login to Firebase
firebase login
```

## 🔄 Workflow Execution

### Manual Trigger (Recommended untuk Testing)

1. Masuk ke GitHub Repository
2. Klik tab **"Actions"**
3. Pilih workflow **"🚀 Deploy TukanginAja Staging"**
4. Klik **"Run workflow"** dropdown
5. Pilih branch: `main`
6. Klik **"Run workflow"**

### Automatic Trigger

Workflow akan otomatis berjalan ketika:
- Push ke `main` branch
- Pull request ke `main` branch

## ✅ Validation Steps

### Step 1: Run Validation Script

```bash
chmod +x scripts/validate_staging_deployment.sh
./scripts/validate_staging_deployment.sh
```

Script akan:
- ✅ Check Firebase authentication
- ✅ List deployed functions
- ✅ Check function logs
- ✅ Validate project configuration
- ✅ Generate validation report

### Step 2: Verify Functions Deployment

```bash
# List all deployed functions
firebase functions:list --project tukanginaja-staging

# Expected output should include:
# - onOrderStatusUpdate
# - onNewMessage
# - onNewRating
# - onPaymentSuccess
```

### Step 3: Check Function Logs

```bash
# View recent logs
firebase functions:log --project tukanginaja-staging --limit=50

# Monitor logs in real-time
firebase functions:log --project tukanginaja-staging --limit=100 | grep -i "error\|success"
```

### Step 4: Test Functions via Firestore Events

Firestore trigger functions **tidak bisa di-test langsung via HTTP**. Mereka harus di-trigger oleh Firestore events.

#### Test onOrderStatusUpdate

1. Buka Firebase Console → Firestore
2. Navigate ke collection `orders`
3. Create atau update document dengan mengubah field `status`
4. Function akan otomatis trigger
5. Check logs untuk verify execution

#### Test onNewMessage

1. Navigate ke `orders/{orderId}/messages`
2. Create new message document
3. Function akan otomatis trigger
4. Check logs dan notifications

#### Test onNewRating

1. Navigate ke collection `ratings`
2. Create new rating document
3. Function akan update trust score
4. Verify trust score di `tukang_locations`

#### Test onPaymentSuccess

1. Navigate ke collection `transactions`
2. Update transaction status dari non-"SUCCESS" ke "SUCCESS"
3. Function akan trigger
4. Check logs dan notifications

## 🧪 Manual Testing Guide

### Testing Order Status Update

```javascript
// Di Firestore Console atau via Firebase Admin SDK
// Update order status
await admin.firestore()
  .collection('orders')
  .doc('TEST_ORDER_123')
  .update({
    status: 'Selesai',
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  });

// Check logs
firebase functions:log --project tukanginaja-staging --limit=10
```

### Testing New Rating

```javascript
// Create rating
await admin.firestore()
  .collection('ratings')
  .add({
    orderId: 'TEST_ORDER_123',
    userId: 'TEST_USER',
    tukangId: 'TEST_TUKANG',
    score: 4.5,
    comment: 'Test rating',
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });

// Verify trust score updated
const tukangDoc = await admin.firestore()
  .collection('tukang_locations')
  .doc('TEST_TUKANG')
  .get();

console.log('Trust Score:', tukangDoc.data().trustScore);
```

### Testing Payment Success

```javascript
// Update transaction status
await admin.firestore()
  .collection('transactions')
  .doc('TEST_TX_123')
  .update({
    status: 'SUCCESS',
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  });

// Check logs for notification sent
```

## 📊 Monitoring & Debugging

### Firebase Console Monitoring

1. **Functions Dashboard:**
   - Firebase Console → Functions
   - Check deployment status
   - View execution count
   - Monitor error rate

2. **Logs Viewer:**
   - Firebase Console → Functions → Logs
   - Filter by function name
   - Search for errors

3. **Firestore Console:**
   - Verify data changes
   - Check document updates
   - Monitor real-time updates

### GitHub Actions Monitoring

1. **Workflow Runs:**
   - GitHub → Actions tab
   - View workflow execution history
   - Check build logs
   - Monitor deployment status

2. **Build Artifacts:**
   - Check build summary
   - Review test results
   - Verify deployment success

## ⚠️ Troubleshooting

### Common Issues

**1. Functions not deployed:**
```bash
# Check if deployment completed
firebase functions:list --project tukanginaja-staging

# Re-deploy manually if needed
firebase deploy --only functions --project tukanginaja-staging
```

**2. Authentication errors:**
```bash
# Re-authenticate
firebase login --reauth

# Check token
firebase projects:list
```

**3. Missing secrets:**
- Verify `FIREBASE_DEPLOY_TOKEN` is set in GitHub Secrets
- Re-generate token if needed: `firebase login:ci`

**4. Function execution errors:**
- Check logs: `firebase functions:log --project tukanginaja-staging`
- Verify Firestore rules allow function access
- Check function code for errors

## 📝 Validation Report

Setelah menjalankan validation script, file `staging_validation_report.txt` akan di-generate dengan:
- ✅ Validation checklist
- ✅ Functions status
- ✅ Logs analysis
- ✅ Next steps recommendations

## 🎯 Success Criteria

Deployment dianggap berhasil jika:

- ✅ All functions deployed successfully
- ✅ No errors in recent logs
- ✅ Functions trigger on Firestore events
- ✅ Trust score calculation works
- ✅ Notifications are sent
- ✅ Event logging works

## 📞 Support

Jika menemukan issues:
1. Check GitHub Actions logs
2. Review Firebase Functions logs
3. Verify Firestore rules
4. Check function permissions
5. Contact development team

---

**Last Updated:** $(date)
**Version:** 1.0.0
