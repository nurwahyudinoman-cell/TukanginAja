# 🚀 TukanginAja - Pull Request Template

## 🧩 Jenis Perubahan

Tandai semua yang relevan:

- [ ] ✨ Fitur baru
- [ ] 🐛 Perbaikan bug
- [ ] 🧱 Pembaruan arsitektur
- [ ] 🧪 Testing / QA
- [ ] 📚 Dokumentasi

## 🎯 Ringkasan Perubahan

> Jelaskan perubahan utama dan alasan implementasinya (maks 5 baris).

## 🔗 Issue / Blueprint Terkait

> Cantumkan referensi Blueprint ATM TukanginAja dan nomor issue jika ada.

## ✅ Checklist QA Sebelum Merge

- [ ] Build sukses tanpa error
- [ ] Login → Dashboard flow tetap stabil
- [ ] Order creation, update, dan status listener berjalan realtime
- [ ] Tidak ada error di log Firebase
- [ ] Semua perubahan sudah diuji di emulator multi-role
- [ ] Dokumentasi (docs/...) diperbarui

## 🧠 Catatan Developer

> Informasi tambahan jika ada hal penting dari implementasi.

## 🔍 Reviewer Checklist

- [ ] Kode sesuai guideline (naming, struktur, komentar)
- [ ] Tidak ada perubahan sensitif pada konfigurasi
- [ ] Realtime listener bekerja di semua role
- [ ] Performa dan respon < 300ms untuk event Firestore

