# SKILLS.md

---

### Skill: Anti-Context-Bleed & Production-Ready UI Copywriting

#### 1. Deskripsi & Pemicu (When to Trigger)
Aktifkan skill ini setiap kali:
- Menulis, memodifikasi, atau merefactor kode antarmuka (UI components, widgets, screens, pages).
- Memberi nama/label pada tombol (button), link navigasi, header, tab, modal/dialog, placeholder form, atau empty-state message.
- Menerima instruksi pembuatan fitur antarmuka yang mengandung penjelasan intensi pengguna (contoh: "buat tombol start untuk mulai").

#### 2. Aturan Mutlak (Strict Directives)
1. **Dilarang Keras Menaruh Penjelasan dalam Tanda Kurung/Kurawal:**
   - JANGAN PERNAH menambahkan keterangan fungsi di samping label teks visual.
   - Contoh DILARANG: `"Start (Mulai)"`, `"Submit (Kirim Data)"`, `"Settings [Pengaturan]"`.
2. **Prinsip Konkret & Ringkas (1–3 Kata):**
   - Label tombol dan elemen aksi harus padat, jelas, dan siap produksi (production-ready).
   - Gunakan kata kerja aksi langsung tanpa kalimat berbunga atau edukatif.
3. **Pemisahan Teks Visual dan Aksesibilitas:**
   - Teks yang dirender ke layar HANYA label inti.
   - Jika membutuhkan deskripsi fungsional untuk pembaca layar (screen reader) atau panduan pengguna, masukkan HANYA ke atribut semantik (`tooltip`, `aria-label`, atau `Semantics(label: ...)`), bukan pada string visual.
4. **Isolasi Logika vs Copywriting:**
   - Jangan pernah mengubah variabel state, API route, ID elemen, atau handler fungsi saat merapikan teks antarmuka.

#### 3. Standar Perbandingan (Good vs Bad Examples)
- ❌ **SALAH:** `<button>Mulai (Mulai Ujian)</button>`
- ✅ **BENAR:** `<button>Mulai</button>`
- ❌ **SALAH:** `ElevatedButton(child: Text("Simpan (Menyimpan data)"))`
- ✅ **BENAR:** `ElevatedButton(child: Text("Simpan"))`
- ❌ **SALAH:** `"Keluar [Klik untuk logout dari akun]"`
- ✅ **BENAR:** `"Keluar"` (dengan tooltip terpisah jika relevan)
