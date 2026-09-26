# PRD — Logichild "Petualangan" Roadmap (Mario Party Style)

Status: DISETUJUI (revisi 1.2)
Versi dokumen: 1.2
Tanggal: 2026-09-26
Pemilik produk: Yovi
Target rilis: Logichild v1.2.0

---

## 1. Latar Belakang

Saat ini Logichild memakai roadmap horizontal linier per game: anak hanya bisa
maju satu level demi satu level di dalam satu game tertentu. Pola ini bagus
untuk latihan bertahap, tapi:

- Tidak ada rasa "perjalanan" atau progres besar yang terlihat.
- Anak tidak bisa melompat ke mini-game favoritnya tanpa menekan banyak level.
- Belum ada elemen kejutan/reward yang membuat anak ingin kembali bermain.
- Orang tua tidak punya jalur cepat untuk melatih skill tertentu (warna, pola,
  motorik).

Mario Party memberi inspirasi struktur papan: pemain melempar dadu, berjalan di
peta, mendarat di kotak yang memberi efek berbeda, dan sesekali masuk mini-game.
PRD ini mengadaptasi struktur itu untuk anak usia ~2 tahun, dengan dua mode:
**Mode Petualangan (papan)** dan **Mode Bebas (pilih mini-game langsung)**.

---

## 2. Tujuan

1. Menyediakan roadmap bergaya papan petualangan yang terasa seperti perjalanan.
2. Tetap memungkinkan anak langsung memilih mini-game tanpa melewati papan.
3. Menjaga semua mekanik tetap bisa dimainkan anak 2 tahun: tanpa teks wajib,
   tanpa kalah keras, tanpa timer yang menekan.
4. Meningkatkan retensi orang tua lewat laporan progres ringkas.

### Non-tujuan (out of scope rilis ini)

- Multiplayer online / real-time.
- Sistem ekonomi koin kompleks atau toko item berbayar.
- Leaderboard global.
- IAP baru.

---

## 3. Persona & Konteks Pakai

| Persona | Deskripsi | Kebutuhan utama |
|---|---|---|
| Anak (2 th) | Pemain utama, belum bisa baca | Objek besar, warna cerah, instruksi suara, tap/geser sederhana |
| Orang tua | Yang memegang device & memilihkan aktivitas | Jalur cepat ke mini-game tertentu, laporan progres, kontrol suara |
| Kakak (4–6 th) | Kadang ikut bermain | Level lebih sulit tanpa mengubah UI |

Konteks: dimainkan sambil didampingi orang tua, sesi pendek 3–10 menit,
sering tanpa audio eksternal (speaker HP).

---

## 4. Struktur Navigasi

```
Home (Game Hub)
├── ▶ Lanjutkan Petualangan     → Papan Petualangan di posisi terakhir
├── 🗺️ Papan Petualangan        → pilih peta
├── 🎮 Mini Game (mode bebas)   → daftar mini-game → pilih level
└── ⚙️ Settings
```

Aturan:

- **Mode Petualangan** dan **Mode Bebas** berbagi satu sumber progres bintang
  yang sama per mini-game, supaya tidak ada progres terpisah yang
  membingungkan.
- Menekan kartu mini-game di mode bebas membuka pemilih level (grid), bukan
  langsung level 1.

---

## 5. Papan Petualangan (Mode Utama)

### 5.1 Struktur Papan

Papan meniru Mario Party, disederhanakan untuk anak 2 tahun:

- Peta bergulir otomatis mengikuti karakter.
- **1 peta = 24 kotak** yang tersusun sebagai jalur berkelok, tidak lurus.
- Jalur dibuat dari daftar koordinat tetap (bukan acak) supaya anak belajar
  mengenali peta yang sama di setiap permainan.
- Bentuk jalur: berkelok seperti huruf S memanjang, dengan 3–4 belokan besar.
- Kamera scroll horizontal mengikuti kotak tempat karakter berada.
- Peta berikutnya dibuka setelah peta sebelumnya tamat.
- Rilis ini hanya menyertakan **1 peta** (Peta 1: Taman Bermain).

Tema visual: selaras dengan background Cocok Warna dan Susun Pola yang sudah
ada (langit, awan, pelangi, rumput, bunga) supaya seluruh app terasa satu dunia.

### 5.2 Kotak & Efek

| Kotak | Nama | Efek |
|---|---|---|
| 🟢 | Start | Titik awal, tidak ada efek |
| ⬜ | Biasa | Tidak ada efek, hanya langkah |
| ⭐ | Bintang | Menambah 1 bintang petualangan, suara reward |
| 🎮 | Mini Game | Membuka mini-game; bintang hasil game ditambahkan, lalu otomatis kembali ke papan |
| 🌈 | Bonus | Melangkah maju 2 kotak lagi (maksimum sekali per giliran) |
| 🍎 | Kumpul | Menambah 1 apel ke counter header |
| 🎁 | Hadiah | Membuka bintang tambahan + confetti |
| 🏁 | Finish | Peta selesai → layar perayaan + badge peta |

Aturan penting: **tidak ada kotak hukuman** — tidak ada mundur, tidak ada
kehilangan bintang, tidak ada jebakan. Untuk usia 2 tahun, semua efek harus
netral atau positif.

Distribusi kotak pada Peta 1 (24 kotak):

```
1  Start
2  Biasa
3  Bintang
4  Mini Game
5  Biasa
6  Bonus
7  Kumpul
8  Mini Game
9  Bintang
10 Biasa
11 Hadiah
12 Mini Game
13 Biasa
14 Kumpul
15 Bintang
16 Biasa
17 Mini Game
18 Bonus
19 Bintang
20 Kumpul
21 Mini Game
22 Hadiah
23 Bintang
24 Finish
```

### 5.3 Tombol JALAN (tanpa dadu)

Dadu dihapus: anak 2 tahun main sendiri, belum bisa menghitung titik dadu, dan
tidak ada lawan. Langkah acak tidak memberi pilihan apa pun ke anak.

- Satu tombol besar di tengah bawah: ikon kaki beruang + label "JALAN".
- Tap sekali → beruang **melompat 1 kotak** (animasi lompat ± 600 ms), kamera
  mengikuti.
- Setelah mendarat, efek kotak dijalankan otomatis.
- Tombol dinonaktifkan selama animasi/efek berjalan (anti tap beruntun).
- Suara: `step.wav` saat lompat.

### 5.4 Karakter

- 1 karakter default (maskot beruang, selaras dengan mockup Cocok Warna).
- Karakter berdiri di kotak aktif dengan animasi kecil (mengambang/mengangguk).
- Pemilihan karakter ditunda ke rilis berikutnya.

### 5.5 Mini-game di Papan

- Mendarat di kotak 🎮 → mini-game langsung terbuka (acak dari mini-game yang
  sudah punya progres minimal level 1).
- **Bintang = rating hasil game itu (1–5).** Contoh: kotak 5, main mobil, dapat
  4 bintang → bintang petualangan +4. Minimal 1, tidak ada gagal.
- Alur selesai: layar reward (bintang + confetti) sebentar → **otomatis kembali
  ke papan**, tanpa tap tambahan dan tanpa lewat pemilih level.
- Saat dibuka dari petualangan, di dalam game **disembunyikan**: label level,
  pemilih level, tombol peta/roadmap. Anak hanya melihat permainannya.
- Rating terbaik per level (mode bebas) tetap tersimpan terpisah dan tidak
  berubah aturannya; main ulang tetap menambah bintang petualangan.

### 5.5a Bintang & Apel

| Item | Didapat dari | Dipakai untuk |
|---|---|---|
| ⭐ Bintang | Rating mini-game (1–5) + kotak Bintang (+1) + kotak Hadiah | Membuka musim berikutnya (semi → panas → gugur → salju) setelah kotak 24; ambang selalu tercapai, jadi hanya penanda progres |
| 🍎 Apel | Mendarat di kotak Kumpul (+1) | Memberi makan beruang di layar Hadiah: tiap 5 apel → 1 stiker/baju (topi, syal, …) yang langsung dipakai beruang di papan |

- Tanpa toko, harga, atau keputusan membeli.
- Tidak ada yang bisa berkurang atau hilang.
- Angka hanya untuk orang tua; anak melihat ikon dan animasi.

### 5.6 Giliran & Akhir Papan

- Satu giliran = 1 tap JALAN (maju 1 kotak).
- Sesi papan tidak punya batas giliran; anak berhenti kapan saja.
- Mencapai kotak 24 → layar perayaan: badge peta, total bintang, total buah,
  tombol "Main Lagi" dan "Ke Menu".
- Peta 1 tetap bisa dimainkan ulang setelah tamat.

### 5.7 Progres Petualangan

- Disimpan di SharedPreferences: `adv_map`, `adv_position`, `adv_stars`,
  `adv_fruits`, `adv_maps_done`.
- Posisi dipulihkan saat app dibuka ulang (kartu "Lanjutkan Petualangan").
- Nama file SharedPreferences tidak diubah agar install lama tidak kehilangan
  progres.

---

## 6. Mode Bebas (Mini Game Langsung)

- Daftar kartu mini-game: Cocok Warna, Susun Pola, Main Mobil, Petik Buah.
- Tiap kartu menampilkan level tertinggi dan total bintang.
- Tap kartu → pemilih level (grid angka besar, level terkunci ditampilkan
  dengan tanda 🔒).
- Tap level → langsung masuk permainan, tanpa lewat papan.
- Aturan buka level tetap sama dengan sekarang: naik level setelah menyelesaikan
  level sebelumnya di mini-game tersebut.

---

## 7. Mini-game Terkait

| Mini-game | Status | Perubahan di rilis ini |
|---|---|---|
| Cocok Warna | Ada | Jumlah pilihan naik per level (2→7) — sudah dikerjakan |
| Susun Pola | Ada | Kesulitan naik per keluarga pola — sudah dikerjakan |
| Main Mobil | Ada | Tidak diubah selain integrasi suara |
| Petik Buah | Ada | Tidak diubah selain integrasi suara |

Mode bebas dan kotak 🎮 memakai mini-game yang sama persis agar tidak ada dua
versi logika.

---

## 8. Audio

- Suara langkah: `step.wav` volume rendah, maksimum 3 langkah per detik.
- Suara efek kotak: bintang/hadiah memakai voice natural MP3 yang sudah ada.
- Tepuk tangan volume rendah (mengikuti setelan yang sudah diturunkan).
- Mute global (`sound_on`) mematikan efek dan voice.
- Tidak memakai `sparkle.wav` berulang untuk langkah.

---

## 9. UI & Visual

Design language: **Google Stitch** untuk mockup, lalu diterapkan ke Compose.

Prinsip:

- Objek besar, kontras tinggi, sudut membulat.
- Panel putih dengan shadow untuk area informasi; teks navy gelap.
- Background taman dengan objek dekoratif di pinggir, tidak menutupi jalur
  papan maupun area permainan.
- Tombol jawaban di area bawah layar (nyaman untuk jempol anak).
- Toolbar atas: kembali, judul, posisi/level, toggle suara.
- Bilingual wajib: semua teks lewat `strings.xml` (EN) + `values-in/` (ID).

### Layar yang perlu dibuat

1. Papan Petualangan (peta berkelok + tombol JALAN + karakter)
2. Pemilih Peta
3. Daftar Mini Game (mode bebas)
4. Pemilih Level
5. Layar Perayaan Selesai Peta
6. Kartu "Lanjutkan Petualangan" di Home

---

## 10. Persyaratan Teknis

- Kotlin + Jetpack Compose, tanpa library baru untuk papan (Canvas + emoji
  cukup, menghindari penambahan dependensi).
- State papan disimpan di ViewModel + SharedPreferences.
- Logika papan (efek kotak, langkah, posisi, jalur) dipisahkan ke kelas pure
  (`AdventureBoard`) agar bisa diuji unit test, mengikuti pola
  `PatternPuzzleFactory`.
- Jalur peta dikodekan sebagai `List<Offset>` tetap, bukan hasil perhitungan
  acak, sehingga hasilnya identik setiap kali.
- Tidak ada `LocalContext` di layar non-Compose.
- R8 + resource shrink tetap aktif di debug dan release.

### Unit test wajib

- Satu langkah JALAN selalu maju tepat 1 kotak.
- Bintang mini-game menambah sesuai rating (1–5).
- Bergerak dari posisi X sebanyak N tidak pernah melewati kotak terakhir.
- Kotak Bonus memindahkan +2 dan **tidak memicu efek berantai** (bonus dari
  bonus).
- Kotak Mini Game tidak mengubah posisi setelah mini-game selesai.
- Mendarat tepat di kotak terakhir memicu finish, dan kelebihan langkah tidak
  membuat posisi melewati batas.
- Progres tersimpan dan dipulihkan secara konsisten.
- Jalur peta punya 24 titik dan tidak ada dua kotak dengan koordinat sama.

---

## 11. Metrik Keberhasilan

- Anak bisa mulai bermain dalam ≤ 2 tap dari Home.
- Sesi bermain rata-rata ≥ 5 menit.
- Tidak ada crash di rilis (Crashlytics crash-free ≥ 99%).
- Orang tua dapat masuk ke mini-game pilihan dalam ≤ 2 tap.

---

## 12. Proses Desain (Stitch)

1. Generate mockup dengan Google Stitch untuk tiap layar di bagian 9.
2. Simpan mockup sebagai referensi visual di repo (`docs/design/`).
3. Iterasi mockup sampai disetujui.
4. Terapkan ke Compose.
5. Build APK lewat CI, ambil screenshot device, bandingkan dengan mockup,
   revisi bila perlu.

Catatan: mockup bukan jaminan hasil identik — ukuran layar, font emoji, dan
safe area/iklan membuat posisi bergeser. Mockup = arah visual.

---

## 13. Adaptasi Mario Party — Apa yang Ditiru dan Tidak

| Elemen Mario Party | Keputusan | Alasan |
|---|---|---|
| Papan jalur berkelok | ✅ Ditiru | Inti rasa "perjalanan" |
| Dadu dengan animasi gulir | ❌ Tidak | Diganti tombol JALAN 1 kotak — pemain tunggal, anak belum bisa menghitung |
| Kotak dengan efek berbeda | ✅ Ditiru | Variasi tanpa menambah kesulitan |
| Mini-game di tengah perjalanan | ✅ Ditiru | Ini puncak keseruan Mario Party |
| Layar perayaan akhir papan | ✅ Ditiru | Reward visual untuk anak |
| Koin untuk membeli bintang | ❌ Tidak | Terlalu abstrak untuk usia 2 tahun |
| Item sabotase pemain lain | ❌ Tidak | Konsep "mengganggu" belum dipahami |
| Pemenang & yang kalah | ❌ Tidak | Anak 2 tahun belum siap kalah |
| 4 pemain bergantian | ❌ Tidak (rilis ini) | Bisa ditambahkan sebagai hot-seat di rilis lanjutan |
| Papan berubah tiap ronde | ❌ Tidak | Prioritas rendah, peta tetap lebih mudah dikenali |

Prinsip: **tiru struktur dan rasa serunya, bukan seluruh sistemnya.**

---

## 14. Rencana Rilis

| Tahap | Isi | Versi |
|---|---|---|
| 1 | PRD + mockup Stitch disetujui | — |
| 2 | Papan petualangan + tombol JALAN + efek kotak | 1.2.0 |
| 3 | Mode bebas + pemilih level | 1.2.0 |
| 4 | Polish audio, layar perayaan, badge peta | 1.2.1 |
| 5 | Peta kedua | 1.3.0 |

Aturan versi: fitur baru → naik `y`, bugfix → naik `z`.
Version code = `X*100000 + Y*1000 + Z` (otomatis dari tag).

---

## 15. Risiko & Mitigasi

| Risiko | Mitigasi |
|---|---|
| Anak bingung dengan dua mode | Mode petualangan jadi kartu paling besar di Home; mode bebas diberi label "Mini Game" |
| Papan terlalu ramai untuk usia 2 th | Maksimum 3 tipe kotak terlihat sekaligus, objek besar, tanpa teks wajib |
| Jalur berkelok membingungkan | Jalur tetap sama tiap permainan, karakter selalu di tengah layar |
| Progres papan hilang saat reinstall | Simpan di SharedPreferences dengan nama file lama (jangan diubah) |
| AdMob banner menutupi tombol JALAN | Papan pakai safe area; banner hanya di Home |
| Mockup Stitch tidak sama dengan hasil Compose | Screenshot device sebagai acuan akhir, iterasi posisi |

---

## 16. Pertanyaan Terbuka

1. Perlu pilihan karakter (beruang, kelinci, kucing) di rilis pertama atau nanti?
2. Mini-game di kotak 🎮: acak dari yang sudah punya progres, atau acak total?
3. Mode bebas perlu menampilkan level terkunci, atau semua bebas dibuka?
4. ~~Buah dipakai untuk apa?~~ → terjawab di 5.5a (apel → stiker/baju beruang).
