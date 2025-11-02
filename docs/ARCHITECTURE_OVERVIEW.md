# 📘 TukanginAja – Architecture Overview (ATM Edition)

## 🎯 Purpose

Blueprint arsitektur sistem TukanginAja dirancang dengan pendekatan Amati – Tiru – Modifikasi (ATM)
untuk menciptakan ekosistem digital yang cepat, aman, dan profesional antara User – Tukang – Admin.

---

## 🧱 System Overview

```
┌───────────────────────────────────────────────────────────┐
│                       TukanginAja                         │
│                  (ATM Architecture Blueprint)             │
├───────────────────────────────────────────────────────────┤
│  🧍 User App (Flutter)                                    │
│   • Login/Auth (Firebase Auth)                            │
│   • Dashboard & Order                                     │
│   • Chat Realtime (Firestore Listener)                    │
│   • Rating & Payment Interface                            │
│   ↓                                                       │
│  🔗 Communicates via Firebase SDK                         │
├───────────────────────────────────────────────────────────┤
│  👷 Tukang App (Flutter)                                  │
│   • Auth & Status (Online/Offline)                        │
│   • Receive & Accept Orders                               │
│   • Chat Realtime + Job Status Update                     │
│   • Earnings & Trust Score                                │
│   ↓                                                       │
│  🔗 Communicates via Firebase SDK                         │
├───────────────────────────────────────────────────────────┤
│  🧑‍💼 Admin Dashboard (Web Flutter)                        │
│   • Monitor Orders, Payments, Ratings                     │
│   • Manage Users/Tukang                                   │
│   • Adjust Commission Rates                               │
│   • Logs & Analytics (Cloud Logging + Firestore)          │
├───────────────────────────────────────────────────────────┤
│  ☁️ Backend (Firebase Cloud)                              │
│   • Firestore (Realtime DB)                               │
│   • Cloud Functions (Order, Payment, Notification)       │
│   • Firebase Auth (Role-based Access)                     │
│   • Firebase Cloud Messaging (Push Notifications)        │
│   • Cloud Storage (Foto & Bukti Transaksi)                │
├───────────────────────────────────────────────────────────┤
│  🔒 Security & Monitoring                                 │
│   • Firestore Rules (UID-based)                           │
│   • Function Error Logging                                │
│   • Admin Monitoring Dashboard                            │
└───────────────────────────────────────────────────────────┘
```

---

## ⚙️ Component Summary

| Layer | Function | Key Tech |
|--------|-----------|-----------|
| **Frontend (Flutter)** | Interface untuk User, Tukang, Admin | Flutter SDK, Firebase Auth, Firestore |
| **Backend (Firebase)** | Realtime database dan fungsi otomatis | Cloud Functions, Firestore, FCM |
| **Security** | Role-based access dan UID validation | Firestore Rules, JWT Token |
| **Monitoring** | Logging & analitik admin | Cloud Logging, Firestore Stats |

---

## 🧠 Design Principles

1. **Single Source of Truth:** Semua data tersimpan di Firestore dan disinkronisasi real-time.  
2. **Lightweight Interaction:** Realtime listener menggantikan pooling.  
3. **Role Isolation:** Setiap dashboard dipisahkan sesuai role.  
4. **Security First:** UID-based access & Firestore rule enforcement.  
5. **Transparency:** Semua transaksi & rating tercatat otomatis.

---

## 🔍 Integration Overview

| Service | Purpose | Linked Modules |
|----------|----------|----------------|
| Firebase Auth | Login & role validation | Auth, Role Redirect |
| Firestore | Order, Chat, Rating, Saldo | Semua role |
| Cloud Functions | Komisi & Trust Score otomatis | Backend |
| FCM | Notifikasi pekerjaan & chat | User, Tukang |
| Cloud Storage | Upload bukti & sertifikat | Tukang, Admin |

---

## 🧾 Monitoring Points

| Modul | Metric | Source |
|--------|---------|--------|
| Order | Jumlah order aktif & selesai | Firestore |
| Chat | Latency (ms) | Firestore Snapshot |
| Saldo | Transaksi sukses vs gagal | Cloud Functions |
| Admin | Audit logs & error trace | Cloud Logging |

---

## 🏁 Status

✅ Blueprint Architecture established  

Next Step → Implement **Technical Layer Breakdown (Tahap 2)**

