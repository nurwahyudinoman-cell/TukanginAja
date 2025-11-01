# 📋 TAHAP 16 – Background Tracking & Push Notification (FCM)

## ✅ Implementasi Selesai

### 1. Background Location Service ✅
- **File**: `app/src/main/java/com/tukanginAja/solusi/service/BackgroundLocationService.kt`
- **Perubahan**:
  - ✅ Interval update diubah dari **5 detik** ke **12 detik** (10-15 detik sesuai spesifikasi)
  - ✅ Menambahkan monitoring order status untuk stop otomatis saat `completed`
  - ✅ Menggunakan `GeoPoint` untuk menyimpan posisi di Firestore (sesuai spesifikasi)
  - ✅ Menambahkan parameter `orderId` untuk tracking order yang sedang aktif

### 2. Notification Service ✅
- **File**: `app/src/main/java/com/tukanginAja/solusi/service/NotificationService.kt` (BARU)
- **Fitur**:
  - ✅ `sendNotificationToUser()` - Kirim notifikasi ke user
  - ✅ `sendNotificationToTukang()` - Kirim notifikasi ke tukang
  - ✅ `sendNewOrderNotificationToTukang()` - Notifikasi order baru ke tukang
  - ✅ `sendOrderCompletedNotificationToUser()` - Notifikasi order selesai ke user
  - ✅ Fallback ke Firestore notification queue jika Cloud Functions tidak tersedia

### 3. Integrasi FCM Notifikasi ✅
- **RequestViewModel**: Kirim notifikasi ke tukang saat order baru dibuat
- **TukangDashboardViewModel**: Kirim notifikasi ke user saat order selesai
- **RequestRepository**: Return `orderId` untuk notifikasi

### 4. Dependencies ✅
- ✅ Firebase Functions (`firebase-functions-ktx`) ditambahkan ke `build.gradle.kts`

### 5. Update Screen & Service ✅
- ✅ `TukangDashboardScreen`: Pass `orderId` saat start tracking
- ✅ `BackgroundLocationService`: Monitor order status dan stop otomatis saat `completed`

## 📝 Catatan Penting

1. **Firebase Cloud Functions**: 
   - Jika Cloud Functions `sendNotification` belum tersedia, notifikasi akan di-queue ke Firestore collection `notification_queue`
   - Untuk production, buat Cloud Function `sendNotification` di Firebase Console

2. **FCM Token**:
   - Token disimpan di Firestore collection `users` dengan field `fcmToken`
   - Token otomatis di-update oleh `MyFirebaseMessagingService.onNewToken()`

3. **Background Tracking**:
   - Service berjalan di foreground dengan notification channel
   - Interval update: 12 detik (sesuai spesifikasi 10-15 detik)
   - Stop otomatis saat order status = `completed`

## 🔄 Alur Notifikasi

### Order Baru (User → Tukang)
1. User membuat order via `RequestScreen`
2. `RequestViewModel.createRequest()` memanggil `RequestRepository.createRequest()`
3. Setelah order dibuat, `NotificationService.sendNewOrderNotificationToTukang()` dipanggil
4. Notifikasi dikirim ke tukang via FCM

### Order Selesai (Tukang → User)
1. Tukang menyelesaikan order via `TukangDashboardScreen`
2. `TukangDashboardViewModel.completeOrder()` memanggil `RequestRepository.updateRequestStatus()`
3. Setelah status updated, `NotificationService.sendOrderCompletedNotificationToUser()` dipanggil
4. Notifikasi dikirim ke user via FCM
5. BackgroundLocationService otomatis stop karena order status = `completed`

## 🚀 Build & Test

1. Pastikan Firebase Cloud Messaging sudah di-setup di Firebase Console
2. Tambahkan Cloud Function `sendNotification` (optional, bisa menggunakan Firestore queue)
3. Test dengan membuat order baru dan menyelesaikan order
4. Pastikan notifikasi diterima di kedua arah (user ↔ tukang)

## 📌 Files Modified/Created

### Modified:
- `app/src/main/java/com/tukanginAja/solusi/service/BackgroundLocationService.kt`
- `app/src/main/java/com/tukanginAja/solusi/data/repository/RequestRepository.kt`
- `app/src/main/java/com/tukanginAja/solusi/ui/screens/request/RequestViewModel.kt`
- `app/src/main/java/com/tukanginAja/solusi/ui/screens/tukang/TukangDashboardViewModel.kt`
- `app/src/main/java/com/tukanginAja/solusi/ui/screens/tukang/TukangDashboardScreen.kt`
- `app/build.gradle.kts`

### Created:
- `app/src/main/java/com/tukanginAja/solusi/service/NotificationService.kt`

---

**Status**: ✅ **TAHAP 16 COMPLETE**
**Build Status**: ✅ **No Linter Errors**
