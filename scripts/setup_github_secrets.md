# 🔐 Setup GitHub Secrets untuk CI/CD Deployment

## Required Secrets

Untuk menjalankan GitHub Actions workflow, Anda perlu menambahkan secrets berikut di GitHub Repository.

### 1. FIREBASE_DEPLOY_TOKEN

**Deskripsi:** Token untuk otentikasi Firebase CLI di CI/CD pipeline.

**Cara Mendapatkan:**

```bash
# 1. Install Firebase CLI (jika belum)
npm install -g firebase-tools

# 2. Login ke Firebase
firebase login

# 3. Generate CI token
firebase login:ci
```

Token akan muncul di terminal. **Copy token ini.**

**Setup di GitHub:**

1. Masuk ke repository: `https://github.com/nurwahyudinoman-cell/TukanginAja`
2. Buka: **Settings** → **Secrets and variables** → **Actions**
3. Klik **New repository secret**
4. Name: `FIREBASE_DEPLOY_TOKEN`
5. Value: Paste token yang di-copy dari `firebase login:ci`
6. Klik **Add secret**

### 2. FIREBASE_SERVICE_ACCOUNT (Optional)

**Deskripsi:** Service account JSON untuk advanced Firebase deployment.

**Cara Mendapatkan:**

1. Masuk ke [Firebase Console](https://console.firebase.google.com)
2. Pilih project: `tukanginaja-staging`
3. Buka: **Project Settings** → **Service Accounts**
4. Klik **Generate New Private Key**
5. Download JSON file

**Setup di GitHub:**

1. Masuk ke repository: **Settings** → **Secrets and variables** → **Actions**
2. Klik **New repository secret**
3. Name: `FIREBASE_SERVICE_ACCOUNT`
4. Value: Copy seluruh isi JSON file yang di-download
5. Klik **Add secret**

## Verifikasi Setup

Setelah secrets ditambahkan:

1. Masuk ke tab **Actions** di GitHub
2. Pilih workflow: **🚀 Deploy TukanginAja Staging**
3. Klik **Run workflow** (manual trigger)
4. Workflow akan berjalan dan menggunakan secrets yang sudah ditambahkan

## Troubleshooting

### Error: "FIREBASE_DEPLOY_TOKEN not set"

**Solusi:** Pastikan secret `FIREBASE_DEPLOY_TOKEN` sudah ditambahkan dengan nama yang benar.

### Error: "Firebase authentication failed"

**Solusi:** 
1. Generate token baru dengan `firebase login:ci`
2. Update secret di GitHub dengan token baru
3. Token kadang kadaluarsa, generate ulang jika perlu

### Error: "Project tukanginaja-staging not found"

**Solusi:**
1. Pastikan project `tukanginaja-staging` sudah dibuat di Firebase Console
2. Atau update `.firebaserc` dengan project ID yang benar
3. Pastikan user yang login memiliki akses ke project tersebut

## Testing Secrets

Untuk test apakah secrets bekerja:

1. Trigger workflow manual dari GitHub Actions
2. Check logs di GitHub Actions
3. Jika deployment berhasil, secrets sudah benar
4. Jika masih error, periksa error message di logs

