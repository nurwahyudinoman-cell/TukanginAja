# 🔌 TukanginAja – API Spec & Firestore Listener (ATM Edition)

## 🎯 Purpose

Dokumen ini mendefinisikan endpoint Cloud Functions, format request/response JSON, struktur dokumen Firestore yang digunakan, contoh listener di Flutter, security rules inti, deploy commands, dan checklist pengujian.

---

## 📁 Struktur Firestore (ringkas)

```
users/{uid}              // data umum (role, profile)
tukang/{uid}             // profil tukang, trustScore, rating, totalOrders, active
orders/{orderId}         // order master
├── messages/{messageId} // chat messages (subcollection)
transactions/{trxId}     // riwayat top-up dan payout
ratings/{ratingId}       // optional per-rating if separate
```

---

## 🔗 Endpoints / Cloud Functions (overview)

> Semua fungsi di-deploy sebagai Cloud Functions (Node.js/TS). Beberapa callable, beberapa onWrite/onUpdate triggers.

### 1. **onOrderWrite** (trigger)

- **Trigger:** `functions.firestore.document('orders/{orderId}').onWrite(...)`
- **Fungsi:** handle status transition, validasi perubahan, kirim push ke tukang/user, log event.
- **Input (via Firestore):** perubahan pada `orders/{orderId}`.
- **Output:** update `orders/{orderId}.history`, kirim FCM.

### 2. **onOrderComplete** (trigger)

- **Trigger:** onUpdate ketika `status` berubah menjadi `Selesai`
- **Fungsi:** hitung komisi, update `transactions/{trxId}`, update saldo `tukang/{uid}` & `users/admin` saldo, panggil `calculateTrustScore` (async), log transaksi.
- **Business rule:** Komisi default 10% (configurable).

### 3. **calculateTrustScore** (callable/HTTP)

- **Endpoint:** Callable `functions.https.onCall` atau `functions.https.onRequest`
- **Input:** `{ uid: string }` atau callable context
- **Output:** `{ uid, newTrustScore, rating, completionRate }`
- **Logika:** `trustScore = (avgRating * 0.7) + (completionRate * 0.3)`

### 4. **sendPushNotification** (callable)

- **Fungsi:** wrapper untuk FCM send (topic / token).
- **Input:** `{ token, title, body, data }`
- **Output:** FCM response.

### 5. **validateTopUp** (HTTP callable)

- **Fungsi:** server-side validation untuk top-up (cek payment gateway callback).
- **Input:** payment gateway payload
- **Output:** `{ success: boolean, trxId }` -> update `transactions/{trxId}` & user saldo.

---

## 🔍 Contoh Payload JSON

### Create Order (client writes to Firestore)

```json
{
  "userId": "uidUser",
  "tukangId": "uidTukang",
  "status": "Menunggu",
  "createdAt": "2025-11-02T09:30:00Z",
  "detail": {
    "serviceType": "Perbaikan Listrik",
    "address": "Jl. Merdeka No. 10",
    "priceEstimate": 150000
  },
  "paymentStatus": "pending"
}
```

### Order Status Update (contoh onWrite data)

```json
{
  "orderId": "abc123",
  "oldStatus": "Dikerjakan",
  "newStatus": "Selesai",
  "completedAt": "2025-11-02T10:30:00Z",
  "amount": 150000
}
```

### calculateTrustScore Callable Request / Response

**Request:**
```json
{ "uid": "uidTukang" }
```

**Response:**
```json
{
  "uid": "uidTukang",
  "newTrustScore": 87.4,
  "avgRating": 4.6,
  "completionRate": 0.92
}
```

---

## 🔧 Contoh Cloud Function (Node.js) — skeleton

```javascript
// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();
const fcm = admin.messaging();

exports.onOrderWrite = functions.firestore
  .document('orders/{orderId}')
  .onWrite(async (change, context) => {
    const orderId = context.params.orderId;
    const before = change.before.exists ? change.before.data() : null;
    const after = change.after.exists ? change.after.data() : null;

    // Basic validation: ignore meta writes
    if (!after) return null;

    const oldStatus = before ? before.status : null;
    const newStatus = after.status;

    // If status changed:
    if (oldStatus !== newStatus) {
      // push notification logic
      // write history subdoc
      // if newStatus == "Selesai" -> trigger payout flow
    }

    return null;
  });

exports.onOrderComplete = functions.firestore
  .document('orders/{orderId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    if (before.status !== 'Selesai' && after.status === 'Selesai') {
      const orderId = context.params.orderId;
      const { amount, tukangId, userId } = after;
      const commissionRate = 0.10; // configurable
      const commission = Math.round(amount * commissionRate);
      const payout = amount - commission;

      // Create transaction
      const trxRef = db.collection('transactions').doc();
      await trxRef.set({
        orderId,
        userId,
        tukangId,
        amount,
        commission,
        payout,
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      });

      // Update balances (use transaction to avoid race)
      const tukangRef = db.collection('tukang').doc(tukangId);
      const adminRef = db.collection('users').doc('admin-system'); // or admin wallet doc

      await db.runTransaction(async (tx) => {
        const tSnap = await tx.get(tukangRef);
        const aSnap = await tx.get(adminRef);
        const tBalance = (tSnap.exists && tSnap.data().saldo) ? tSnap.data().saldo : 0;
        const aBalance = (aSnap.exists && aSnap.data().saldo) ? aSnap.data().saldo : 0;

        tx.update(tukangRef, { saldo: tBalance + payout });
        tx.update(adminRef, { saldo: aBalance + commission });
      });

      // Trigger trustScore recalculation (async)
      await exports.calculateTrustScore({ data: { uid: tukangId } }, null);

      // Send push to user & tukang
      return null;
    }

    return null;
  });

exports.calculateTrustScore = functions.https.onCall(async (data, context) => {
  const uid = data.uid;

  // fetch rating and completion stats
  // compute trustScore
  // update tukang/{uid}.trustScore

  return { uid, newTrustScore: 0 };
});
```

**Catatan:** contoh di atas adalah skeleton — implementasi lengkap harus memasukkan error handling, retries, dan logging.

---

## 🔔 Contoh Listener di Flutter (Firestore + StreamBuilder)

### Listen orders for Tukang

```dart
StreamBuilder<QuerySnapshot>(
  stream: FirebaseFirestore.instance
    .collection('orders')
    .where('tukangId', isEqualTo: currentUid)
    .snapshots(),
  builder: (context, snapshot) { ... }
)
```

### Listen messages for an order

```dart
StreamBuilder<QuerySnapshot>(
  stream: FirebaseFirestore.instance
    .collection('orders').doc(orderId)
    .collection('messages')
    .orderBy('createdAt', descending: false)
    .snapshots(),
  builder: (context, snapshot) { ... }
)
```

---

## 🔐 Security Rules (core snippets)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    match /users/{userId} {
      allow create: if request.auth != null && request.auth.uid == userId;
      allow read, update, delete: if request.auth != null && request.auth.uid == userId;
    }
    
    match /tukang/{tukangId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.auth.uid == tukangId;
      allow update: if request.auth != null && (request.auth.uid == tukangId || isAdmin(request.auth.uid));
    }
    
    match /orders/{orderId} {
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow read: if request.auth != null && (
         resource.data.userId == request.auth.uid ||
         resource.data.tukangId == request.auth.uid ||
         isAdmin(request.auth.uid));
      allow update: if request.auth != null && (
         request.auth.uid == resource.data.userId ||
         request.auth.uid == resource.data.tukangId ||
         isAdmin(request.auth.uid));
    }
    
    function isAdmin(uid) {
      return get(/databases/$(database)/documents/users/$(uid)).data.role == "admin";
    }
  }
}
```

---

## 🚦 Deployment & Test Commands

### Install dependencies:
```bash
cd functions
npm install
```

### Deploy functions (staging or specific):
```bash
firebase deploy --only functions:onOrderWrite,functions:onOrderComplete,functions:calculateTrustScore --project staging
```

### Deploy rules:
```bash
firebase deploy --only firestore:rules --project staging
```

### Emulator tests:
```bash
FIRESTORE_EMULATOR_HOST=localhost:8080 firebase emulators:start --only firestore,functions,auth
# Run integration tests / scripts against emulator
```

---

## ✅ Acceptance Criteria & Verification Checklist

- [ ] File created: `docs/API_SPEC_AND_LISTENER.md` (readable)
- [ ] Commit created & pushed (include commit hash)
- [ ] Contains: list endpoints, example payloads, Cloud Function skeletons, Flutter listener snippets, security rules, deploy commands
- [ ] Lines >= 140 and chars >= 5000 (comprehensive)
- [ ] No syntax errors in markdown
- [ ] Provide summary: lines, chars, commit hash, top 10 lines preview

---

## 📝 Implementation Notes

### Error Handling
- Semua Cloud Functions harus memiliki try-catch dengan proper error logging
- Retry mechanism untuk transient failures
- Dead letter queue untuk failed operations

### Performance
- Batch operations untuk multiple updates
- Use transactions untuk consistency
- Cache user data untuk reduce Firestore reads

### Testing
- Unit tests untuk business logic
- Integration tests dengan emulator
- Load tests untuk concurrent operations

---

## 🏁 Status

✅ API Spec & Listener documentation selesai dibuat.

**Next Step:** Implement fitur sesuai roadmap (Tahap 1: Order System)

