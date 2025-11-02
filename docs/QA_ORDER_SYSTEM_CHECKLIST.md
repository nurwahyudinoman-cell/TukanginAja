# ✅ QA Checklist — Order System (Feature/order-system)

## 🎯 Tujuan

Memastikan sistem order real-time (User ↔ Tukang ↔ Admin) berfungsi 100% sesuai Blueprint ATM.

## 🧪 Skenario Pengujian Utama

| No | Skenario | Role | Langkah Uji | Hasil Diharapkan |
|----|-----------|------|--------------|------------------|
| 1 | User membuat order baru | User | Buka aplikasi → Pilih tukang → Isi detail → Submit | Order muncul di dashboard Tukang secara realtime |
| 2 | Tukang menerima order | Tukang | Tekan "Terima" → Status berubah jadi "Dikerjakan" | Status di user update otomatis |
| 3 | Tukang menyelesaikan order | Tukang | Tekan "Selesai" | Status User → "Selesai", muncul rating prompt |
| 4 | Admin memantau pesanan | Admin | Buka dashboard → Lihat log order terbaru | Data order muncul realtime |
| 5 | Order gagal atau ditolak | Tukang | Tekan "Tolak" | User menerima notifikasi gagal |

## 🔔 Notifikasi & Listener

- Pastikan notifikasi dikirim ke User dan Tukang pada setiap perubahan status.
- Listener real-time: `orders/{orderId}` dan subcollection `messages/{messageId}` berjalan tanpa delay (>100ms).

## 📊 Validasi Database

- Data order tersimpan di path `orders/{orderId}`
- Status flow: Menunggu → Diterima → Dikerjakan → Selesai
- Field wajib: `userId`, `tukangId`, `status`, `createdAt`, `serviceType`

## 🔐 Validasi Keamanan

- Role-based access sesuai Firestore rules
- User hanya bisa membaca dan update order miliknya
- Admin bisa membaca semua order

## 🧱 Kinerja & Stabilitas

- Target respon Firestore < 300ms
- Listener stabil (uptime 99.5%)
- Tidak ada error pada Cloud Function `onOrderWrite`

## 🧩 Hasil Akhir

- [ ] Semua status flow berhasil diuji
- [ ] Listener dan notifikasi real-time aktif
- [ ] Tidak ada error di emulator / log
- [ ] Build sukses dan commit diverifikasi

