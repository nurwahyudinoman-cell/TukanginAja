# 🧱 TukanginAja – ATM Roadmap Teknis

## 🎯 Tujuan

Menjabarkan tahapan implementasi teknis sistem TukanginAja berdasarkan strategi Amati – Tiru – Modifikasi (ATM).

Dokumen ini menjadi panduan utama bagi tim pengembang untuk memastikan pengembangan berjalan 100% sinkron, efisien, dan terukur.

---

## 📅 Fase Implementasi Teknis

| Tahap | Fokus | Durasi | Deliverable | Branch |
|--------|--------|---------|--------------|----------|
| **1. Integrasi Realtime Order** | Sinkronisasi pesanan realtime (User ↔ Tukang) | 2 minggu | `OrderSystem v1` | feature/order-system |
| **2. Chat & Notifikasi** | Realtime chat dan FCM push notification | 3 minggu | `ChatSystem v1` | feature/chat-system |
| **3. Trust System + Rating** | Implementasi Trust Score otomatis dan Rating UI | 2 minggu | `RatingSystem v1` | feature/trust-rating |
| **4. Payment & Komisi** | Sistem saldo internal + komisi otomatis admin | 3 minggu | `PaymentSystem v1` | feature/payment-system |
| **5. QA Automation + Beta Launch** | Testing otomatis dan peluncuran versi beta publik | 2 minggu | `Beta Release v1` | release/beta |

---

## ⚙️ Detail Setiap Tahap

### 🔹 Tahap 1: Integrasi Realtime Order

- Setup struktur Firestore `orders/{orderId}`
- Implementasi listener realtime (User ↔ Tukang)
- Status flow: *Menunggu → Diterima → Dikerjakan → Selesai*
- Tambahkan notifikasi push via FCM
- Unit Test coverage: 85%

### 🔹 Tahap 2: Chat & Notifikasi

- Gunakan `chats/{orderId}/messages`
- Listener aktif untuk kedua pihak
- Integrasi push notification FCM
- Test delay < 100 ms antar pesan
- QA target: 99% uptime selama testing

### 🔹 Tahap 3: Trust System + Rating

- Fungsi Cloud Function: `calculateTrustScore()`
- Rating → update ke `tukang/{uid}`
- Trust Score formula: `(rating * 0.7) + (completionRate * 0.3)`
- Validasi UI input & integritas data

### 🔹 Tahap 4: Payment & Komisi

- Koleksi `transactions/{trxId}`
- Trigger onUpdate(order.status == "Selesai")
- Update saldo User, Tukang, Admin (10% komisi)
- Audit logging di Cloud Logging
- QA checklist: race condition test, saldo consistency test

### 🔹 Tahap 5: QA Automation + Beta Launch

- Setup automated test (unit + integration)
- Multi-role emulator testing (User, Tukang, Admin)
- Load test 500 concurrent sessions
- Deploy Beta ke Play Console internal track

---

## 🔐 Quality & Monitoring Standard

| Aspek | Target | Tools |
|--------|---------|--------|
| **Uptime** | ≥ 99.5% | Firebase Performance |
| **Crash-free Sessions** | ≥ 98% | Firebase Crashlytics |
| **Error Response Time** | < 300ms | Cloud Logging |
| **Testing Coverage** | ≥ 85% | GitHub Actions + Test Lab |

---

## 🧠 Prinsip Roadmap

1. Setiap tahap wajib melewati code review dan testing.  
2. Hanya branch stabil yang boleh di-merge ke `main`.  
3. Semua fungsi yang menyentuh transaksi wajib melalui QA manual.  
4. Cloud Function di-deploy bertahap (staging → production).  
5. Dokumentasi wajib diperbarui setiap tahapan selesai.

---

## 🏁 Status

✅ Roadmap Teknis ATM selesai dibuat.

Next Step → Implement **Tahap 5: Spesifikasi API & Listener Backend**

