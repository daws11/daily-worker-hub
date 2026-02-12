# 🌴 Daily Worker Hub (DWhub)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-Managed-blue.svg)](https://kotlinlang.org/)
[![Next.js](https://img.shields.io/badge/Next.js-16-black.svg)](https://nextjs.org/)
[![Supabase](https://img.shields.io/badge/Powered_by-Supabase-green.svg)](https://supabase.com/)

**Daily Worker Hub** adalah ekosistem digital *Workforce-as-a-Service* (WaaS) yang dirancang khusus untuk merevolusi pasar tenaga kerja harian (*gig economy*) di sektor hospitality Bali. Platform ini menghubungkan bisnis (hotel, villa, restoran) dengan pekerja harian yang terverifikasi melalui sistem pencocokan instan (*On-Demand Dispatch*).

---

## 📌 Gambaran Umum Proyek

Proyek ini dikembangkan sebagai solusi atas tingginya biaya rekrutmen tradisional dan kurangnya transparansi bagi pekerja harian di Bali. Dengan mengadopsi model logistik ala Gojek/Uber, DWhub memungkinkan pengisian *shift* kerja dalam hitungan menit, bukan hari.

### 🌟 Fitur Unggulan
- **Komisi Kompetitif (6%)**: Struktur biaya transparan yang jauh lebih rendah dari agensi *outsourcing* tradisional (15-25%).
- **Matching Algorithm (Real-time)**: Menggunakan algoritma *Greedy* dan *Hungarian* untuk optimalisasi pencocokan pekerja berdasarkan lokasi, skill, dan rating.
- **Compliance Guard (Aturan 21 Hari)**: Sistem otomatis untuk mematuhi PP No. 35 Tahun 2021, mencegah risiko hukum pengangkatan karyawan tetap secara tidak sengaja.
- **Community Fund**: Alokasi 1% dari transaksi untuk jaminan sosial mikro (BPJS BPU) bagi para mitra pekerja.
- **Verified Workers (KYC)**: Integrasi verifikasi wajah (*liveness detection*), identitas (OCR KTP), dan riwayat performa.

---

## 🏗️ Struktur Proyek (Monorepo)

Repository ini terdiri dari beberapa komponen utama yang saling terintegrasi:

### 1. 📱 [DWhubfix](./DWhubfix) (Aplikasi Mobile Android)
Aplikasi utama untuk pengguna (Pekerja & Bisnis).
- **Tech Stack**: Kotlin, Jetpack Compose, Supabase SDK, CameraX (KYC).
- **Fitur Utama**:
  - Onboarding berkelanjutan berdasarkan peran (*Worker/Business*).
  - Verifikasi Identitas & Alamat yang ketat.
  - Dashboard Pekerja & Bisnis yang informatif.
  - Wallet internal untuk pengelolaan pendapatan harian.

### 2. 💻 [daily-worker-admin](./daily-worker-admin) (Dashboard Admin Web)
Platform pusat kendali untuk tim operasional DWhub.
- **Tech Stack**: Next.js 16 (App Router), React 19, Tailwind CSS 4, Supabase Auth & SSR.
- **Fitur Utama**:
  - Manajemen Verifikasi Bisnis & Pekerja.
  - Monitoring statistik transaksi dan *fill rate*.
  - Pengelolaan data master dan audit log.

### 3. ⚡ [Supabase](./supabase) (Backend & Database)
Tulang punggung infrastruktur serverless.
- **Layanan**: PostgreSQL, Edge Functions (Deno), Storage (KTP/Selfie), Real-time Database.

---

## 📄 Dokumentasi Strategis

Kami telah menyusun riset mendalam yang mendasari pengembangan platform ini:

- 📑 **[Whitepaper](./whitepaper.md)**: Visi, misi, dan analisis pasar hospitality Bali 2024-2025.
- 🧬 **[Matching Algorithm Research](./matching-algorithm.md)**: Analisis algoritma *On-Demand Dispatch* (Greedy, Hungarian, Min-Cost Max-Flow).
- 💰 **[Business Model Validation](./business-model.md)**: Studi kelayakan finansial (komisi 6%), unit ekonomi, dan kepatuhan hukum (PP 35/2021).

---

## 🚀 Memulai (Get Started)

### Prasyarat
- Android Studio Ladybug atau yang terbaru (untuk mobile).
- Node.js 20+ (untuk admin dashboard).
- Akun Supabase (untuk backend).

### Instalasi
1. Clone repository:
   ```bash
   git clone https://github.com/daws11/daily-worker-hub.git
   ```
2. Setup Admin Dashboard:
   ```bash
   cd daily-worker-admin
   npm install
   npm run dev
   ```
3. Setup Android:
   - Buka folder `DWhubfix` menggunakan Android Studio.
   - Konfigurasi `local.properties` atau `BuildConfig` dengan kunci API Supabase Anda.

---

## 🗺️ Roadmap Pengembangan

- [x] **Fase 1**: Inisialisasi Monorepo & Setup Supabase.
- [x] **Fase 2**: Implementasi KYC & Onboarding (Mobile).
- [x] **Fase 3**: Monitoring Dashboard Dasar (Web Admin).
- [ ] **Fase 4**: Integrasi Payment Gateway (Midtrans) & Wallet Cash-out.
- [ ] **Fase 5**: Implementasi Algoritma Matching Batching (Hungarian).
- [ ] **Fase 6**: Launching Pilot Program di Badung & Denpasar.

---

## 🤝 Kontribusi

DWhub adalah proyek yang berfokus pada komunitas. Jika Anda tertarik untuk berkontribusi atau memiliki pertanyaan mengenai riset algoritma kami, silakan hubungi tim pengembang kami atau ajukan *Pull Request*.

## ⚖️ Lisensi

Proyek ini dilisensikan di bawah [MIT License](./LICENSE).

---
*Dikembangkan dengan ❤️ untuk masyarakat pekerja hospitality Bali.*
