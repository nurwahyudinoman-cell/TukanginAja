# 🧭 Panduan Kolaborasi TukanginAja  

**Senior Developer x Cursor AI**

---

## 📌 1. Tujuan Dokumen

Panduan ini dibuat untuk memastikan kolaborasi antara **Senior Pengembang (Lead Engineer)** dan **Cursor AI** berjalan terarah, terukur, dan selaras dengan tujuan strategis proyek **TukanginAja**.  

Tujuan utama:  

- Mewujudkan pengembangan aplikasi **100% stabil, fungsional, dan siap produksi**.  

- Menciptakan sistem kerja otomatis namun terkontrol penuh oleh pengembang utama.

---

## 🧱 2. Struktur Peran

### 👨‍💻 Senior Developer (Lead Engineer)

Bertanggung jawab atas:

- Arsitektur sistem (frontend, backend, database, API).

- Standar coding, keamanan, dan performa.

- Membuat task, meninjau hasil, dan melakukan evaluasi akhir.

- Pengambilan keputusan teknis dan rilis versi.

### 🤖 Cursor AI

Bertanggung jawab atas:

- Menulis kode sesuai spesifikasi teknis yang diberikan.

- Melakukan unit testing, linting, dan refactor minor.

- Menghasilkan implementasi cepat berdasarkan guideline.

- Tidak melakukan merge atau deploy tanpa persetujuan Senior Dev.

---

## ⚙️ 3. Alur Kerja Kolaboratif

| Tahap | Deskripsi | Penanggung Jawab |

|-------|------------|------------------|

| **1. Task Planning** | Senior Dev menentukan task dan tujuan teknis. | Senior Dev |

| **2. Instruction Delivery** | Perintah spesifik dikirim ke Cursor AI. | Senior Dev |

| **3. Implementation** | Cursor AI menulis dan menjalankan kode. | Cursor AI |

| **4. Code Review** | Senior Dev memeriksa hasil dan validasi kualitas. | Senior Dev |

| **5. Merge & Deploy** | Setelah disetujui, kode di-merge ke main branch dan di-deploy ke staging. | Cursor AI (setelah approval) |

---

## 🧩 4. Standar Teknis Proyek

| Komponen | Teknologi | Catatan |

|-----------|-------------|----------|

| **Frontend** | Next.js (TypeScript) | Menggunakan Tailwind + shadcn/ui |

| **Backend** | Firebase (Firestore, Functions, Auth) | Menggunakan struktur modular |

| **Testing** | Jest + React Testing Library | Coverage minimal 80% |

| **Style Guide** | ESLint + Prettier | Mengikuti AirBnB Style Guide |

| **Version Control** | Git (Conventional Commit) | Branch utama: `main`, `dev`, `feature/*` |

| **Deployment** | Firebase Hosting / Vercel | CI/CD otomatis dengan manual approval |

---

## 🔐 5. Keamanan dan Kebijakan

1. Semua environment variable disimpan di `.env.local` dan tidak di-commit.  

2. Firestore rules diverifikasi sebelum setiap rilis.  

3. Tidak ada perubahan struktur database tanpa dokumentasi resmi.  

4. Cursor AI tidak dapat menulis ke branch `main` tanpa approval.  

5. Backup otomatis dijalankan setiap minggu di Cloud Storage.

---

## 📚 6. Dokumentasi & Log

- Semua perubahan disertai **changelog**.  

- Dokumentasi teknis disimpan di folder `/docs`.  

- Setiap rilis versi disertai catatan:  

```yaml
version: 1.0.x
date: YYYY-MM-DD
author: Senior Dev
summary: ringkasan perubahan
```

---

## 🧠 7. Evaluasi & Pembaruan

Setiap akhir fase roadmap:

- Dilakukan review performa sistem dan proses kerja Cursor AI.

- Jika perlu, revisi panduan ini agar tetap relevan dan efisien.

- Semua perubahan disetujui oleh Senior Developer sebelum diberlakukan.

---

## 🚀 8. Visi Akhir

Menghasilkan aplikasi TukanginAja yang:

- Siap rilis ke publik.

- Dapat berkembang otomatis dengan integrasi AI.

- Memiliki dokumentasi lengkap dan arsitektur bersih.

- Memberikan kemudahan bagi pengguna dan tim pengembang di masa depan.

---

📄 **Dokumen ini adalah repositori utama dan panduan permanen untuk semua pengembang TukanginAja.**  

Disusun oleh:  

**Nurwahyudin Oman (Senior Developer & Project Lead)**  

Tanggal: **1 November 2025**

