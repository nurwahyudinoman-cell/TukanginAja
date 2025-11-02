# 🔄 TukanginAja – Flow Diagram Fitur Inti (ATM Edition)

## 🎯 Purpose

Menjelaskan alur data dan interaksi antar komponen utama (User – Tukang – Admin)
agar seluruh proses berjalan real-time, aman, dan sinkron dengan sistem backend Firebase.

---

## 1️⃣ Order Flow (User → Tukang → Admin)

```mermaid
sequenceDiagram
    participant U as User
    participant F as Firestore
    participant T as Tukang
    participant CF as Cloud Functions
    participant A as Admin
    
    U->>F: Create new order (status=Menunggu)
    F-->>T: Realtime listener detects new order
    T->>F: Accept order (status=Diterima)
    T->>F: Update progress (status=Dikerjakan)
    T->>F: Mark complete (status=Selesai)
    F-->>CF: Trigger commission & saldo update
    CF-->>A: Log transaction (Cloud Logging)
    CF-->>U: Push notification "Order Selesai"
```

**Ringkasan:**
Order real-time berbasis listener Firestore, dengan Cloud Function memproses komisi otomatis.

---

## 2️⃣ Chat Flow (Realtime Firestore Channel)

```mermaid
sequenceDiagram
    participant U as User
    participant F as Firestore
    participant T as Tukang
    participant FCM as Firebase Cloud Messaging
    
    U->>F: Send message (add to chats/{orderId}/messages)
    F-->>T: Realtime update (StreamBuilder)
    T->>F: Reply message
    F-->>U: Update chat view
    F-->>FCM: Send push notification for new message
```

**Catatan:**
Pesan hanya dapat diakses oleh pihak terkait (User & Tukang berdasarkan orderId).

---

## 3️⃣ Payment & Saldo Flow

```mermaid
flowchart TD
    A[User Top-up Saldo] --> B[Firestore: transactions/]
    B --> C[Cloud Function: validatePayment()]
    C --> D[Update Saldo User]
    D --> E[Order Complete -> Auto Transfer to Tukang]
    E --> F[Admin Commission 10%]
    F --> G[Log Transaction + Update Dashboard]
```

**Penjelasan:**
Sistem saldo internal memastikan keamanan dan transparansi transaksi.

---

## 4️⃣ Rating & Trust Score Flow

```mermaid
sequenceDiagram
    participant U as User
    participant F as Firestore
    participant CF as Cloud Function
    participant T as Tukang
    
    U->>F: Submit rating & comment
    F-->>CF: Trigger calculateTrustScore()
    CF->>T: Update field trustScore & averageRating
```

**Formula:**
```
trustScore = (avgRating * 0.7) + (completionRate * 0.3)
```

---

## 5️⃣ Admin Monitoring & Komisi Flow

```mermaid
flowchart TD
    A[Admin Dashboard] --> B[Fetch orders, ratings, transactions]
    B --> C[Display real-time stats]
    C --> D[Adjust commission / verify logs]
    D --> E[Export report to Cloud Storage]
```

**Fitur tambahan:**
- Monitoring real-time order & transaksi
- Error logs via Cloud Logging
- Export laporan otomatis ke CSV / Storage

---

## 🧠 Prinsip Implementasi Flow

1. Semua perubahan status diawasi via listener Firestore.
2. Cloud Functions memproses otomatis setiap perubahan kunci.
3. Chat & Notifikasi menggunakan FCM berbasis token.
4. Admin hanya memiliki akses monitoring (read-only).
5. Semua interaksi tercatat di Cloud Logging.

---

## 🏁 Status

✅ Flow Diagram Fitur Inti selesai dibuat.

Next Step → Implement **ATM Roadmap Teknis (Tahap 4)**.

