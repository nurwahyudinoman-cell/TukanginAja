# ⚙️ TukanginAja – Technical Layer Breakdown (ATM Edition)

## 🎯 Purpose

Dokumen ini menjelaskan lapisan teknis utama TukanginAja,
berdasarkan pendekatan Amati – Tiru – Modifikasi (ATM),
untuk memastikan sistem bekerja cepat, aman, dan efisien.

---

## 🧩 Frontend (Flutter)

| Modul | Tujuan | Integrasi | Teknologi |
|--------|---------|------------|------------|
| **Auth & Role Redirect** | Login & dashboard otomatis sesuai peran | Firebase Auth, SharedPreferences | Flutter, Provider |
| **Order + Listener Realtime** | Pesanan realtime (User ↔ Tukang) | Firestore Listener, StreamBuilder | Flutter, Firebase SDK |
| **Chat In-App** | Komunikasi langsung | Firestore subcollection: `chats/{orderId}/messages` | Flutter, Firebase |
| **Saldo & Transaksi** | Top-up, riwayat, dan komisi | Firestore + Cloud Functions | Flutter, Firebase |
| **Rating & Trust Score** | Penilaian dan reputasi Tukang | Firestore + Function recalculation | Flutter, Firestore |

### 🔹 Arsitektur Frontend

- Menggunakan **Clean Architecture Pattern (MVVM)**.  
- Semua state management berbasis **Provider**.  
- Realtime data menggunakan **StreamBuilder (Firestore)**.  
- Navigasi otomatis berdasarkan role (`AuthViewModel`).

---

## ☁️ Backend (Firebase + Cloud Functions)

| Modul | Fungsi | Trigger | Output |
|--------|---------|---------|---------|
| **Order Function** | Update status order dan notifikasi | onWrite(`orders/{orderId}`) | Push Notification + Saldo Update |
| **Komisi Admin Function** | Potong 10% saat pekerjaan selesai | onUpdate(status="Selesai") | Update saldo Admin & Tukang |
| **Trust Score Function** | Hitung reputasi Tukang otomatis | Callable Function | Update field `trustScore` |
| **Push Notification Trigger** | Kirim notifikasi ke FCM token | onWrite() | FCM Message |
| **Payment Validator** | Validasi transaksi top-up | HTTPS Callable | Response JSON |

### 🔧 Struktur Database Firestore

```
users/{uid}
tukang/{uid}
orders/{orderId}
├── messages/{messageId}
transactions/{trxId}
ratings/{ratingId}
```

### 🔐 Security & Integritas Data

- UID-based write access (`request.auth.uid == resource.id`)
- Validasi saldo & transaksi hanya oleh fungsi backend
- Enkripsi data saldo di server (AES)
- Logging Cloud Functions untuk setiap transaksi

---

## 🛡️ Security Layer

| Aspek | Implementasi | Tujuan |
|--------|---------------|--------|
| **Auth Rules** | UID-based rule di Firestore | Mencegah manipulasi antar pengguna |
| **Role-based Access** | role=user/tukang/admin disimpan di Firestore | Batasi akses dashboard |
| **Session Token Validation** | Firebase persist auth token | Keamanan session |
| **Error Logging** | Cloud Logging + Admin Dashboard | Audit trail & trace bug |

---

## 🧠 Prinsip Teknis

1. Semua listener hanya aktif di context pengguna terkait.  
2. Setiap fungsi backend memiliki fallback handling & retry.  
3. Tidak ada data sensitif dikirim ke client tanpa otorisasi.  
4. Semua perubahan status diverifikasi 2 arah (client ↔ server).  
5. Admin memiliki log read-only terhadap semua transaksi.

---

## 🏁 Status

✅ Technical Layer Breakdown selesai dibuat.

Next Step → Implement **Flow Diagram Tiap Fitur Inti (Tahap 3)**

