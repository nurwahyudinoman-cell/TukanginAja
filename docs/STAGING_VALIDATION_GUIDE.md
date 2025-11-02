# 📋 Staging Deployment Validation Guide

## 🎯 Tujuan

Memastikan semua komponen sistem ATM TukanginAja berfungsi dengan baik di Staging Environment setelah deployment melalui CI/CD pipeline.

## ✅ Checklist Validasi

### 1️⃣ Pre-Deployment Setup

- [ ] GitHub Secrets sudah diatur (`FIREBASE_DEPLOY_TOKEN`)
- [ ] Firebase project `tukanginaja-staging` sudah dibuat
- [ ] Firebase CLI terinstall dan user sudah login
- [ ] Repository sudah di-push ke `main` branch

### 2️⃣ Trigger Deployment

**Cara Manual Trigger:**

1. Masuk ke GitHub Repository: `https://github.com/nurwahyudinoman-cell/TukanginAja`
2. Buka tab **Actions**
3. Pilih workflow: **🚀 Deploy TukanginAja Staging**
4. Klik **Run workflow** (dropdown di kanan)
5. Pilih branch: `main`
6. Klik **Run workflow** (button hijau)

**Automatic Trigger:**

- Workflow akan otomatis berjalan saat ada push ke `main` branch
- Atau saat ada pull request ke `main` branch

### 3️⃣ Monitor Deployment

**Di GitHub Actions:**

1. Klik workflow run yang sedang berjalan
2. Monitor setiap step:
   - ✅ Checkout repository
   - ✅ Setup Node.js & Java
   - ✅ Install dependencies
   - ✅ Run tests
   - ✅ Build Android app
   - ✅ Deploy Firebase Functions

**Di Firebase Console:**

1. Masuk ke [Firebase Console](https://console.firebase.google.com)
2. Pilih project: `tukanginaja-staging`
3. Buka: **Functions** → **Dashboard**
4. Cek deployed functions dan statusnya

### 4️⃣ Verifikasi Deployment

**Jalankan Validation Script:**

```bash
# Berikan execute permission
chmod +x scripts/validate_staging_deployment.sh

# Jalankan script
./scripts/validate_staging_deployment.sh
```

Script akan:
- ✅ Cek Firebase CLI installation
- ✅ Cek Firebase authentication
- ✅ List deployed functions
- ✅ Check function logs
- ✅ Generate validation report

**Manual Verification:**

```bash
# 1. List deployed functions
firebase functions:list --project tukanginaja-staging

# 2. Check recent logs
firebase functions:log --project tukanginaja-staging --limit=50

# 3. Check Firestore rules
firebase deploy --only firestore:rules --project tukanginaja-staging --dry-run
```

### 5️⃣ Test Functions

**Note:** Cloud Functions yang dibuat adalah Firestore Triggers, bukan HTTP endpoints.

**Cara Test:**

1. **Test Order Status Update:**
   ```javascript
   // Di Firestore Console atau via Firebase SDK
   // Update order status
   db.collection('orders').doc('TEST_ORDER_ID').update({
     status: 'Selesai'
   });
   // Function `onOrderStatusUpdate` akan otomatis trigger
   ```

2. **Test New Message:**
   ```javascript
   // Create new message
   db.collection('orders')
     .doc('TEST_ORDER_ID')
     .collection('messages')
     .add({
       senderId: 'TEST_USER',
       receiverId: 'TEST_TUKANG',
       message: 'Test message',
       createdAt: Date.now()
     });
   // Function `onNewMessage` akan otomatis trigger
   ```

3. **Test New Rating:**
   ```javascript
   // Create new rating
   db.collection('ratings').add({
     orderId: 'TEST_ORDER_ID',
     userId: 'TEST_USER',
     tukangId: 'TEST_TUKANG',
     score: 4.5,
     comment: 'Test rating',
     createdAt: Date.now()
   });
   // Function `onNewRating` akan otomatis trigger
   ```

4. **Test Payment Success:**
   ```javascript
   // Update transaction status
   db.collection('transactions').doc('TEST_TX_ID').update({
     status: 'SUCCESS'
   });
   // Function `onPaymentSuccess` akan otomatis trigger
   ```

**Monitor Logs:**

```bash
# Watch function logs in real-time
firebase functions:log --project tukanginaja-staging --limit=100 --follow
```

### 6️⃣ Verify System Logs

Check `system_logs` collection di Firestore:

1. Masuk ke Firebase Console
2. Buka: **Firestore Database**
3. Buka collection: `system_logs`
4. Cek apakah events sudah tercatat:
   - `OrderStatusChange`
   - `NewMessage`
   - `RatingAdded`
   - `PaymentSuccess`

### 7️⃣ Validation Report

Setelah semua validasi:

```bash
# Script akan generate report
cat staging_validation_report.txt
```

**Report akan berisi:**
- ✅ Deployment status
- ✅ Function listing
- ✅ Recent logs
- ✅ Errors/warnings (jika ada)
- ✅ Recommendations

## 🔍 Troubleshooting

### Problem: Functions tidak ter-deploy

**Solution:**
1. Check GitHub Actions logs untuk error
2. Verify `FIREBASE_DEPLOY_TOKEN` secret sudah benar
3. Check Firebase project permissions
4. Try manual deploy: `firebase deploy --only functions --project tukanginaja-staging`

### Problem: Functions ter-deploy tapi tidak trigger

**Solution:**
1. Check Firestore rules - pastikan trigger collection readable
2. Verify function code - pastikan trigger path benar
3. Check function logs untuk errors
4. Test dengan manual Firestore write

### Problem: Notifications tidak terkirim

**Solution:**
1. Check FCM tokens di `users` collection
2. Verify function logs untuk notification errors
3. Test dengan valid user ID dan FCM token
4. Check Firebase Cloud Messaging setup

## 📊 Success Criteria

Deployment dianggap berhasil jika:

- ✅ All functions deployed tanpa error
- ✅ Functions muncul di Firebase Console
- ✅ Functions logs tidak ada critical errors
- ✅ Test triggers berhasil (create test documents)
- ✅ System logs collection terisi dengan events
- ✅ Notifications terkirim (jika ada valid FCM tokens)

## 🚀 Next Steps

Setelah validasi berhasil:

1. ✅ Update production deployment documentation
2. ✅ Setup monitoring & alerting
3. ✅ Schedule regular validation runs
4. ✅ Prepare for production deployment
5. ✅ Announce staging environment ready for beta testing

---

**Status:** ✅ Staging Environment Ready for Beta Testing

