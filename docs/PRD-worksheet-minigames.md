# PRD — Logichild Mini Game Worksheet

Status: DRAFT — menunggu approval
Versi dokumen: 1.4
Tanggal: 2026-09-23
Pemilik produk: Yovi
Target rilis: Logichild v1.2.0 (tahap 1) → v1.3.0 (tahap 2)

---

## 0. Catatan Sumber

PRD ini disusun dari analisa worksheet anak yang dikirimkan.

Worksheet yang terverifikasi:

1. **"Hubungkan Benda yang Berpasangan"** — kolom kiri dan kolom kanan berisi
   objek, anak menarik garis untuk menghubungkan pasangan. Gaya flat vector,
   pastel, outline tebal, kotak dashed border, titik navy sebagai titik sambung.
   Border halaman teal muted.

2. **"Kelompokkan Hewan dan Buah!"** — grid 4x4 berisi 16 objek (8 hewan,
   8 buah), anak melingkari kelompok hewan dan menyilang kelompok buah. Gaya
   flat cartoon, pastel, label teks di bawah tiap ilustrasi, border halaman
   coral/salmon, dekorasi titik dan sparkle di dekat judul.

3. **"Bandingkan Ukuran Hewan!"** — grid 4x4 berisi 3 jenis hewan yang
   berulang dengan pose identik (tikus, kucing, gajah), anak memberi centang
   pada yang paling besar dan silang pada yang paling kecil. Ada checkbox kecil
   di kanan setiap kartu. Gaya flat cartoon, border halaman coral/salmon.

4. **"Cari Gambar yang Berbeda!"** — 4 baris, masing-masing 4 gambar hitam
   putih line art untuk diwarnai. Anak mencari satu gambar yang paling berbeda
   di tiap baris. Nomor baris berupa lingkaran berwarna (pink, kuning, teal).
   Border halaman pink, ada ikon kaca pembesar di dekat judul.

Worksheet lain yang dikirim (6 gambar) terdeteksi sebagai poster produk
Sweetories Scoopable Cookies, bukan worksheet, sehingga belum dimasukkan.

PRD ini menggeneralisasi pola tersebut menjadi satu keluarga mini-game
worksheet yang dimainkan di dalam app.

---

## 1. Latar Belakang

Logichild saat ini punya 4 mini-game: Cocok Warna, Susun Pola, Main Mobil, dan
Petik Buah. Keempatnya mengasah warna, pola, dan motorik. Yang belum ada adalah
mini-game yang mengasah **asosiasi dan logika relasi** — kemampuan anak
menghubungkan dua benda yang berpasangan atau berhubungan.

Worksheet anak klasik seperti "hubungkan benda yang berpasangan" melatih:

- Pengenalan hubungan sebab-akibat (hujan → payung)
- Pengenalan fungsi benda (sikat gigi → pasta gigi)
- Kategorisasi (hewan → makanan, kaki → sepatu)
- Memori visual dan kosakata

Worksheet kertas punya kelemahan: tidak ada feedback, tidak ada suara, sekali
salah tidak bisa diulang tanpa dihapus, dan mudah hilang. Mengubahnya menjadi
mini-game digital menghilangkan semua kelemahan itu.

---

## 2. Tujuan

1. Menambah mini-game baru yang mengasah logika relasi, bukan hanya warna/pola.
2. Mengadopsi gaya visual worksheet anak (flat vector, outline tebal, pastel)
   agar terasa familiar bagi anak dan orang tua.
3. Tetap memenuhi standar anak 2 tahun: objek besar, instruksi suara, tanpa
   teks wajib, tanpa hukuman.
4. Memberi orang tua rasa nilai edukatif yang jelas dari app.

### Non-tujuan

- Menggantikan mini-game lama.
- Mencetak ulang worksheet sebagai PDF (di luar scope app).
- Menambah teks bacaan panjang di layar.

---

## 3. Prinsip Desain Hasil Analisa Worksheet

| Temuan dari worksheet | Penerapan di app |
|---|---|
| Flat vector, outline tebal, sudut membulat | Semua ilustrasi digambar dengan gaya sama |
| Pastel + warna primer, tanpa gradien | Palet terbatas, latar putih/terang |
| Kotak dashed border sebagai wadah objek | Kartu objek dengan border putus-putus |
| Titik navy di sisi dalam kotak | Titik sambung untuk garis penghubung |
| Warna **tidak** dipakai sebagai penanda pasangan | Anak harus berpikir logis, bukan cocokkan warna |
| Objek diberi wajah/personalitas | Objek dekoratif boleh punya mata/senyum |
| Judul + instruksi singkat di atas | Judul di atas, instruksi dibacakan suara |
| Credit pembuat di kanan atas | Tidak dipakai di app |

---

## 4. Mini Game 1: "Hubungkan Pasangan"

### 4.1 Inti Permainan

- Layar menampilkan dua kolom: kiri dan kanan.
- Setiap kolom berisi kartu objek dengan border dashed.
- Anak memilih satu kartu di kiri, lalu satu kartu di kanan.
- Jika pasangan benar: garis menghubungkan kedua kartu, suara pujian, kartu
  menyala.
- Jika salah: kartu bergetar lembut, suara "coba lagi", tidak ada penalti,
  bisa langsung coba lagi.

### 4.2 Interaksi

Worksheet asli memakai tarik garis dengan jari. Untuk anak 2 tahun itu terlalu
sulit secara motorik. Keputusan:

- **Tap dua kartu berurutan**, bukan drag garis.
- Setelah dua kartu terpilih, garis muncul otomatis sebagai umpan balik visual.
- Drag tetap bisa ditambahkan di rilis lanjutan kalau anak sudah lebih mahir.

### 4.3 Progres Jumlah Pasangan per Level

| Level | Jumlah pasangan | Jumlah kartu per kolom |
|---|---|---|
| 1–2 | 1 pasang | 1 |
| 3–5 | 2 pasang | 2 |
| 6–9 | 3 pasang | 3 |
| 10–14 | 3 pasang, kategori lebih sulit | 3 |
| 15+ | 4 pasang | 4 |

Catatan: jumlah kartu per kolom = jumlah pasangan (tidak ada kartu pengecoh di
level awal). Kartu pengecoh ditambahkan mulai level 15.

### 4.4 Kurasi Soal

Semua pasangan harus:

- Berhubungan jelas dan konkret
- Bisa dikenali anak 2 tahun dari bentuknya
- Tidak ambigu (satu objek hanya punya satu pasangan valid)

Contoh kurasi per kategori:

**Level 1–2 (sangat konkret, hubungan langsung):**
| Kiri | Kanan | Hubungan |
|---|---|---|
| 🐻 Beruang | 🍯 Madu | hewan → makanan |
| 🐰 Kelinci | 🥕 Wortel | hewan → makanan |
| 👶 Bayi | 🍼 Susu | orang → benda |

**Level 3–5 (fungsi benda):**
| Kiri | Kanan | Hubungan |
|---|---|---|
| 🦶 Kaki | 👟 Sepatu | anggota badan → benda |
| 🖐️ Tangan | 🧤 Sarung tangan | anggota badan → benda |
| 🌧️ Hujan | ☂️ Payung | cuaca → benda |
| 🦷 Gigi | 🪥 Sikat gigi | bagian tubuh → alat |

**Level 6–9 (rutinitas & tempat):**
| Kiri | Kanan | Hubungan |
|---|---|---|
| 🐟 Ikan | 🌊 Air | hewan → habitat |
| 🐦 Burung | 🪹 Sarang | hewan → tempat tinggal |
| ✏️ Krayon | 📄 Kertas | alat → media |
| 🛏️ Kasur | 😴 Tidur | tempat → aktivitas |

**Level 10–14 (relasi lebih abstrak):**
| Kiri | Kanan | Hubungan |
|---|---|---|
| 🧦 Kaus kaki | 👟 Sepatu | pelengkap |
| 🔑 Kunci | 🚪 Pintu | alat → objek |
| 🕯️ Lilin | 🔥 Api | objek → pemicu |
| 🧊 Es | ☀️ Matahari | objek → penyebab perubahan |

**Level 15+ (4 pasang + 1 pengecoh per kolom):**
Gabungan kategori di atas, ditambah kartu pengecoh yang jelas berbeda kategori.

### 4.5 Validasi Soal (Wajib)

Mengikuti pola `PatternPuzzleFactory`, semua logic dipisah ke kelas pure
`PairMatchFactory`:

- Setiap level wajib punya minimal satu pasangan valid.
- Tidak boleh ada objek yang muncul dua kali dalam satu level.
- Setiap objek di kolom kiri wajib punya tepat satu pasangan di kolom kanan.
- Jumlah kartu kiri harus sama dengan jumlah kartu kanan.
- Pengecoh (level 15+) tidak boleh menjadi pasangan valid objek mana pun.
- Urutan kolom kanan diacak, tapi tidak boleh sama persis dengan kolom kiri.

### 4.6 Gaya Visual

- Latar halaman: putih bersih dengan frame teal muted (mengikuti worksheet)
- Kartu objek: putih, border dashed abu terang, sudut membulat
- Titik sambung: navy gelap di sisi dalam kartu
- Garis penghubung: tebal membulat, warna kontras (contoh: oranye)
- Objek: flat vector, outline tebal 3–4px, tanpa gradien
- Judul: font rounded tebal, aksen warna multi-warna pada satu kata kunci
- Instruksi: satu baris, dibacakan juga lewat suara

### 4.7 Audio

- Instruksi level: voice natural MP3 yang sudah ada
- Benar: voice pujian sesuai rating + efek lembut
- Salah: voice "Coba lagi" + getar halus
- Selesai level: applause volume rendah + voice pujian
- Mute global (`sound_on`) mematikan semua

---

## 5. Mini Game 2: "Kelompokkan Hewan dan Buah"

Konsep ini diambil langsung dari worksheet kedua.

### 5.1 Inti Permainan

- Layar menampilkan objek satu per satu (atau grid kecil).
- Di bawahnya ada **dua keranjang besar**: 🐾 HEWAN dan 🍎 BUAH.
- Anak memilih keranjang yang benar untuk objek tersebut.
- Benar: objek "masuk" ke keranjang dengan animasi + suara pujian.
- Salah: objek bergetar lembut, suara "coba lagi", anak bisa langsung ulangi.

### 5.2 Kenapa Ini Cocok untuk Anak 2 Tahun

Worksheet aslinya meminta anak **melingkari** dan **menyilang**. Dua instruksi
itu terlalu abstrak untuk usia 2 tahun — anak belum paham konsep "lingkari
sebagian dan silang sebagian". Karena itu diganti menjadi:

| Worksheet | Versi app |
|---|---|
| Lingkari hewan, silang buah | Pilih keranjang hewan atau buah |
| 16 objek sekaligus di grid 4x4 | Mulai 4 objek, naik bertahap |
| Instruksi teks panjang | Instruksi suara + 2 tombol besar |
| Tidak ada feedback | Suara + animasi tiap jawaban |

Keunggulan konsep ini: **hanya 2 pilihan**, jadi tidak ada beban memori.

### 5.3 Progres Jumlah Objek per Level

| Level | Jumlah objek | Komposisi |
|---|---|---|
| 1–3 | 4 | 2 hewan, 2 buah |
| 4–6 | 6 | 3 hewan, 3 buah |
| 7–9 | 8 | 4 hewan, 4 buah |
| 10–14 | 10 | 5 hewan, 5 buah |
| 15+ | 12 | 6 hewan, 6 buah |

Aturan: jumlah objek per kategori selalu **seimbang** agar anak tidak bisa
menebak dengan pola hitungan.

### 5.4 Kategori Bertingkat

Level 1–9 memakai kategori hewan vs buah (paling konkret). Setelah itu kategori
diganti atau ditambah:

| Level | Kategori | Contoh |
|---|---|---|
| 1–9 | Hewan vs Buah | 🐱 vs 🍎 |
| 10–14 | Hewan vs Kendaraan | 🐘 vs 🚗 |
| 15–19 | Buah vs Sayur | 🍌 vs 🥕 |
| 20+ | Campuran acak dari semua kategori | — |

### 5.5 Bank Aset Objek

Minimal 8 objek per kategori (mengikuti worksheet):

**Hewan:** 🐱 Kucing, 🐘 Gajah, 🐦 Burung, 🐰 Kelinci, 🦁 Singa, 🦒 Jerapah,
🐵 Monyet, 🐢 Kura-kura

**Buah:** 🍎 Apel, 🍌 Pisang, 🍊 Jeruk, 🍉 Semangka, 🍓 Stroberi, 🍇 Anggur,
🥭 Mangga, 🍍 Nanas

**Kendaraan:** 🚗 Mobil, 🚌 Bus, 🚲 Sepeda, ✈️ Pesawat, 🚂 Kereta, 🚢 Kapal,
🏍️ Motor, 🚁 Helikopter

**Sayur:** 🥕 Wortel, 🥦 Brokoli, 🍅 Tomat, 🌽 Jagung, 🥒 Timun, 🍆 Terong,
🫑 Paprika, 🥔 Kentang

### 5.6 Validasi Soal (Wajib)

Kelas pure `CategorySortFactory`:

- Setiap level wajib punya minimal satu objek per kategori.
- Komposisi hewan/buah (atau kategori aktif) harus seimbang.
- Tidak ada objek duplikat dalam satu level.
- Jumlah objek sesuai tabel level.
- Objek tidak boleh muncul di dua kategori berbeda.
- Urutan objek diacak, tapi objek pertama tidak selalu dari kategori yang sama.

### 5.7 Interaksi

- Objek muncul di tengah layar, besar (minimum 120dp).
- Dua keranjang di bawah: masing-masing minimum 140dp lebar.
- Kartu objek bisa di-tap, keranjang juga bisa di-tap.
- Setelah objek terjawab, objek berikutnya muncul dengan animasi ringan.
- Progres ditampilkan sebagai deretan titik di atas (● sudah, ○ belum).

### 5.8 Gaya Visual

- Ikut tema worksheet: latar putih bersih, border halaman coral/salmon
- Kartu objek: putih, border abu terang membulat, label teks di bawah
- Keranjang: kotak besar warna berbeda (hewan hijau muda, buah oranye muda)
- Objek: flat cartoon, pastel, outline rapi
- Dekorasi: titik dan sparkle kecil di dekat judul

### 5.9 Audio

- Instruksi level: voice natural MP3
- Nama objek dibacakan saat objek muncul (memanfaatkan voice clip yang ada)
- Benar: voice pujian + efek lembut
- Salah: voice "Coba lagi"
- Selesai level: applause volume rendah + voice pujian
- Mute global mematikan semua

---

## 6. Mini Game 3: "Bandingkan Ukuran"

Konsep ini diambil dari worksheet ketiga, dengan penyesuaian besar karena
konsep aslinya terlalu abstrak untuk anak 2 tahun.

### 6.1 Masalah Worksheet Asli

| Masalah | Penjelasan |
|---|---|
| Instruksi ganda | Centang untuk besar, silang untuk kecil — dua konsep terbalik sekaligus |
| Konsep relatif | "Paling besar" hanya bermakna jika ada pembanding lebih dari satu |
| 16 kartu sekaligus | Beban visual terlalu tinggi untuk anak 2 tahun |
| Hewan sama berulang | Membingungkan: tikus mana yang harus disilang? |
| Tanpa feedback | Anak tidak tahu benar atau salah |

Kesimpulan: konsep "paling besar" dan "paling kecil" secara peringkat baru
dipahami anak pada usia 3–4 tahun. Untuk usia 2 tahun, yang bisa dipahami
adalah **perbandingan dua objek** ("mana yang lebih besar?"), bukan peringkat
tiga objek.

### 6.2 Adaptasi

Worksheet meminta: centang yang paling besar, silang yang paling kecil.
Versi app meminta: **pilih salah satu dari dua objek**.

```
   ┌─────────┐    ┌─────────┐
   │   🐭    │    │   🐘    │
   │  Tikus  │    │  Gajah  │
   └─────────┘    └─────────┘

      Mana yang lebih besar?
```

Yang dihilangkan:

- Instruksi silang (X) — anak 2 tahun belum paham konsep "yang salah"
- Grid 16 kartu — diganti satu pertanyaan per layar
- Konsep "paling kecil" — diperkenalkan di level lanjut

### 6.3 Inti Permainan

- Layar menampilkan **dua objek** berdampingan, ukuran visual berbeda.
- Pertanyaan dibacakan lewat suara: "Mana yang lebih besar?"
- Anak tap objek yang menurutnya benar.
- Benar: objek membesar + suara pujian + lanjut.
- Salah: objek bergetar lembut + "coba lagi", bisa langsung ulangi.

Catatan penting: ukuran ilustrasi **harus berbeda secara visual di layar**,
bukan hanya berbeda di dunia nyata. Anak 2 tahun menilai dari apa yang dilihat.

### 6.4 Progres per Level

| Level | Aktivitas | Jumlah objek |
|---|---|---|
| 1–4 | Pilih yang lebih besar | 2 |
| 5–8 | Pilih yang lebih kecil | 2 |
| 9–12 | Pilih yang paling besar | 3 |
| 13–16 | Pilih yang paling kecil | 3 |
| 17+ | Urutkan dari kecil ke besar | 3–4 |

Pergantian dari "lebih besar" ke "lebih kecil" di level 5 sengaja diberi jeda
4 level agar anak sempat menguasai satu konsep sebelum pindah.

### 6.5 Bank Aset Objek Bertingkat Ukuran

Objek harus punya perbedaan ukuran alami yang jelas dan dikenal anak:

| Kelompok | Kecil | Sedang | Besar |
|---|---|---|---|
| Hewan | 🐭 Tikus | 🐱 Kucing | 🐘 Gajah |
| Hewan | 🐛 Ulat | 🐰 Kelinci | 🐻 Beruang |
| Hewan | 🐟 Ikan kecil | 🐢 Kura-kura | 🐋 Paus |
| Buah | 🍒 Ceri | 🍎 Apel | 🍉 Semangka |
| Kendaraan | 🚲 Sepeda | 🚗 Mobil | 🚌 Bus |
| Benda | ⚽ Bola | 🪑 Kursi | 🚪 Pintu |

Setiap kelompok minimal 3 objek agar bisa dipakai untuk level 9+ (tiga objek).

### 6.6 Validasi Soal (Wajib)

Kelas pure `SizeCompareFactory`:

- Setiap level wajib punya minimal 2 objek.
- Objek dalam satu soal harus berasal dari kelompok yang sama (agar
  perbandingan masuk akal — jangan bandingkan gajah dengan ceri).
- Ukuran render di layar harus proporsional dengan ukuran dunia nyata objek
  (gajah selalu lebih besar dari kucing pada soal yang sama).
- Tidak ada dua objek dengan ukuran sama dalam satu soal.
- Untuk level 9+, objek terbesar dan terkecil harus unik (tidak ada seri).
- Urutan posisi objek diacak, tapi objek terbesar tidak selalu di kanan.

### 6.7 Gaya Visual

- Latar putih bersih, border halaman coral/salmon (ikut worksheet)
- Objek tanpa kartu border untuk mode 2 objek (biar terasa seperti perbandingan)
- Objek dengan kartu border untuk level 9+ (3–4 objek)
- Label nama objek di bawah setiap ilustrasi
- Objek: flat cartoon, pastel, outline rapi
- Skala ukuran render: objek terbesar minimal 2x tinggi objek terkecil

### 6.8 Audio

- Instruksi level: voice natural MP3 ("Mana yang lebih besar?")
- Nama objek dibacakan saat objek muncul
- Benar: voice pujian + efek lembut
- Salah: voice "Coba lagi"
- Selesai level: applause volume rendah + voice pujian
- Mute global mematikan semua

---

## 7. Mini Game 4: "Cari yang Berbeda"

Konsep ini diambil dari worksheet keempat. Ini worksheet paling siap diadaptasi
karena hanya punya **satu jawaban per baris** dan tidak ada instruksi ganda.

### 7.1 Inti Permainan

- Layar menampilkan beberapa gambar dalam satu baris (atau grid).
- Satu gambar adalah "yang berbeda" — tidak sekelompok dengan yang lain.
- Anak tap gambar tersebut.
- Benar: gambar membesar + suara pujian + lanjut ke soal berikutnya.
- Salah: gambar bergetar lembut + "coba lagi", bisa langsung ulangi.

### 7.2 Adaptasi dari Worksheet

| Worksheet | Versi app | Alasan |
|---|---|---|
| Mewarnai gambar yang berbeda | Tap gambar yang berbeda | Mewarnai butuh gesek berulang, motorik 2 tahun belum siap dan tidak bisa dinilai benar/salah |
| Hitam putih line art | Berwarna | Anak 2 tahun memakai warna sebagai alat bantu mengenali objek |
| 4 gambar per baris | Mulai 3 gambar | Mengurangi beban visual di level awal |
| 4 baris sekaligus | Satu soal per layar | Fokus satu tugas pada satu waktu |
| Nomor baris lingkaran warna | Progres titik di atas | Menunjukkan kemajuan tanpa perlu membaca angka |

### 7.3 Progres per Level

| Level | Jumlah gambar | Jenis kategori |
|---|---|---|
| 1–4 | 3 | Jarak kategori jauh (hewan vs kendaraan) |
| 5–9 | 4 | Jarak kategori sedang (serangga vs alat tulis) |
| 10–14 | 4 | Jarak kategori dekat (pakaian vs makanan) |
| 15+ | 5 | Kategori campuran dengan kemiripan visual |

Yang dimaksud "jarak kategori":

- **Jauh** — anak langsung lihat bedanya tanpa berpikir (ikan vs mobil)
- **Sedang** — satu kategori hidup vs satu kategori benda (lebah vs pensil)
- **Dekat** — dua kategori benda yang sering dipakai bersama (es krim vs baju)

### 7.4 Contoh Soal per Level

**Level 1–4 (3 gambar, kategori jauh):**
| Soal | Isi | Jawaban |
|---|---|---|
| 1 | 🚗 Mobil, 🚌 Bus, 🐟 Ikan | Ikan |
| 2 | 🍎 Apel, 🍌 Pisang, 🚲 Sepeda | Sepeda |
| 3 | 🐱 Kucing, 🐶 Anjing, 🪑 Kursi | Kursi |
| 4 | 👕 Baju, 🧢 Topi, 🍉 Semangka | Semangka |

**Level 5–9 (4 gambar, kategori sedang):**
| Soal | Isi | Jawaban |
|---|---|---|
| 5 | 🚗 Mobil, 🚌 Bus, 🚂 Kereta, 🐟 Ikan | Ikan |
| 6 | 🦋 Kupu-kupu, 🐝 Lebah, 🐜 Semut, ✏️ Pensil | Pensil |
| 7 | 🍎 Apel, 🍌 Pisang, 🍇 Anggur, 🐘 Gajah | Gajah |
| 8 | 🥄 Sendok, 🍴 Garpu, 🥛 Gelas, 🚗 Mobil | Mobil |

**Level 10–14 (4 gambar, kategori dekat):**
| Soal | Isi | Jawaban |
|---|---|---|
| 9 | 👕 Baju, 🩳 Celana, 🧢 Topi, 🍦 Es krim | Es krim |
| 10 | 🪑 Kursi, 🎸 Gitar, 🎹 Piano, 🥁 Drum | Kursi |
| 11 | 👟 Sepatu, 🧦 Kaus kaki, 🧤 Sarung tangan, 🍕 Pizza | Pizza |
| 12 | 🥕 Wortel, 🥦 Brokoli, 🌽 Jagung, 🍎 Apel | Apel |

**Level 15+ (5 gambar, kemiripan visual):**
| Soal | Isi | Jawaban |
|---|---|---|
| 13 | 🍎 Apel, 🍅 Tomat, 🍒 Ceri, 🍓 Stroberi, 🚗 Mobil | Mobil |
| 14 | 🐭 Tikus, 🐹 Hamster, 🐰 Kelinci, 🐿️ Tupai, 🌳 Pohon | Pohon |

### 7.5 Aturan Validasi Soal (Wajib)

Kelas pure `OddOneOutFactory`:

- Setiap soal wajib punya **tepat satu** gambar yang berbeda.
- Gambar lain dalam satu soal harus sekelompok secara jelas (minimal 2 gambar
  yang benar-benar sekelompok).
- Tidak boleh ada gambar duplikat dalam satu soal.
- Gambar "yang berbeda" tidak boleh muncul dua kali dalam satu soal.
- Posisi gambar berbeda diacak, tapi tidak boleh selalu di kanan.
- Tiap level minimal punya 4 soal berbeda sebelum bisa naik level.

### 7.6 Gaya Visual

- Latar putih bersih, border halaman pink (ikut worksheet)
- Gambar ditampilkan **berwarna**, bukan line art
- Kartu gambar: border abu terang membulat
- Progres soal: deretan titik di atas (● sudah, ○ belum)
- Objek: flat cartoon, pastel, outline rapi
- Ikon kaca pembesar kecil sebagai penanda judul

### 7.7 Audio

- Instruksi level: voice natural MP3 ("Mana yang berbeda?")
- Nama objek dibacakan saat gambar muncul
- Benar: voice pujian + efek lembut
- Salah: voice "Coba lagi"
- Selesai level: applause volume rendah + voice pujian
- Mute global mematikan semua

---

## 8. Kandidat Mini Game Worksheet Lain

Bagian ini menunggu konfirmasi worksheet yang belum terverifikasi. Kandidat
berdasarkan jenis worksheet anak yang umum dan cocok untuk usia 2–3 tahun:

| # | Mini Game | Aktivitas | Kesulitan untuk 2 th |
|---|---|---|---|
| 1 | Hubungkan Pasangan | Tap dua kartu yang berhubungan | Sedang |
| 2 | Kelompokkan | Pindahkan objek ke keranjang kategori yang benar | Mudah–sedang |
| 3 | Bandingkan Ukuran | Pilih objek yang lebih besar/kecil | Sedang |
| 4 | Cari yang Berbeda | Tap gambar yang tidak sekelompok | Sedang–sulit |
| 5 | Hitung Benda | Tap jumlah objek yang sesuai angka | Sedang |
| 6 | Bayangan | Cocokkan objek dengan bayangannya | Sedang |
| 7 | Jejak Garis | Ikuti jalur putus-putus dengan jari | Sedang (motorik) |
| 8 | Urutkan Ukuran | Susun objek dari kecil ke besar | Sulit |

Rekomendasi urutan implementasi: **2 → 1 → 3 → 4 → 5 → 6 → 7 → 8**.

Alasan:

- Kelompokkan paling mudah untuk usia 2 tahun karena hanya dua pilihan, dan
  sudah punya referensi worksheet jelas.
- Hubungkan Pasangan menyusul; butuh dua kolom dan garis penghubung.
- Bandingkan Ukuran hanya butuh dua objek per layar, komponennya sederhana.
- Cari yang Berbeda sudah punya kurasi soal siap pakai dan validasinya
  sederhana (satu jawaban per soal), tapi butuh aset gambar lebih banyak.
- Hitung Benda mudah dibuat dengan objek yang sudah ada.
- Bayangan butuh aset siluet tambahan.
- Jejak Garis terakhir karena butuh handling gesture khusus.
- Urutkan Ukuran paling abstrak untuk usia 2 tahun.

---

## 9. Struktur Navigasi

Mini-game baru masuk ke dua jalur yang sudah ada:

- **Menu utama** → kartu mini-game baru di grid
- **Papan Petualangan** → bisa muncul di kotak 🎮 bersamaan mini-game lain

Tidak ada layar baru yang perlu ditambahkan ke navigasi utama.

---

## 10. Persyaratan Teknis

- Kotlin + Jetpack Compose, tanpa library baru.
- Ilustrasi objek memakai vector drawable atau emoji yang sudah aman
  (hindari emoji yang tidak ter-render di device — masalah yang sudah terjadi
  di mockup papan).
- Logic soal di `PairMatchFactory` (pure, tanpa ViewModel) + unit test.
- State di `PairMatchViewModel` mengikuti pola ViewModel mini-game yang ada.
- Skor bintang, level terbuka, dan progres memakai mekanisme yang sudah ada.
- Bilingual: semua teks via `strings.xml` (EN) + `values-in/` (ID).

### Unit test wajib

- Setiap level punya minimal satu pasangan valid.
- Tidak ada objek duplikat dalam satu level.
- Setiap objek kiri punya tepat satu pasangan kanan.
- Jumlah kartu kiri = jumlah kartu kanan.
- Pengecoh tidak menjadi pasangan valid objek mana pun.
- Level 1–500 tidak pernah gagal generate.

---

## 11. Rencana Rilis

| Tahap | Isi | Versi |
|---|---|---|
| 1 | PRD disetujui | — |
| 2 | Kelompokkan (hewan vs buah, level 1–9) | 1.2.0 |
| 3 | Kelompokkan kategori tambahan (level 10+) | 1.2.1 |
| 4 | Hubungkan Pasangan (level 1–9) | 1.3.0 |
| 5 | Hubungkan Pasangan (level 10+) + pengecoh | 1.3.1 |
| 6 | Bandingkan Ukuran (2 objek) | 1.4.0 |
| 7 | Bandingkan Ukuran (3 objek + urutkan) | 1.4.1 |
| 8 | Cari yang Berbeda | 1.5.0 |
| 9 | Hitung Benda | 1.6.0 |
| 10 | Improvement Susun Pola — pola berulang berpola blok (lihat bagian 15) | 1.7.0 |
| 11 | Mini game worksheet lain sesuai prioritas | 1.8.0+ |

Aturan versi: fitur baru → naik `y`, bugfix → naik `z`.

---

## 12. Ringkasan Leveling Semua Mini Game

Tabel ini adalah acuan tunggal untuk semua kurva kesulitan. Kalau ada perubahan
leveling di game mana pun, tabel ini yang diupdate lebih dulu.

### 12.1 Susun Pola (sudah ada + improvement di bagian 13)

| Level | Keluarga pola | Contoh | Panjang |
|---|---|---|---|
| 1–3 | `A B A ?` | ⭐ 🌙 ⭐ ? | 4 |
| 4–6 | `A B A B ?` | ⭐ 🌙 ⭐ 🌙 ? | 5 |
| 7–9 | `A B C A B ?` | ⭐ 🌙 ☀️ ⭐ 🌙 ? | 6 |
| 10–12 | `A B C A B C ?` | ⭐ 🌙 ☀️ ⭐ 🌙 ☀️ ? | 7 |
| 13–15 | `A A B B ?` | ⭐ ⭐ 🌙 🌙 ? | 5 |
| 16–18 | `A B C A B C A ?` | ⭐ 🌙 ☀️ ⭐ 🌙 ☀️ ⭐ ? | 8 |
| 19–22 | `A B C D A B C ?` | 4 simbol berputar | 8 |
| 23+ | Blok berulang `[A B] [A B] ?` | ⭐ 🌙 ⭐ 🌙 ⭐ ? | 6 |
| 25+ | Blok berulang `[A A B] [A A B] ?` | ⭐ ⭐ 🌙 ⭐ ⭐ 🌙 ? | 7 |
| 28+ | Blok berulang `[A B C] [A B C] ?` | ⭐ 🌙 ☀️ ⭐ 🌙 ☀️ ? | 7 |
| 30+ | Blok bercampur `[A B] [C C] ?` | ⭐ 🌙 ☀️ ☀️ ⭐ ? | 6 |

### 12.2 Cocok Warna (sudah ada)

| Level | Pilihan | Warna dalam pool |
|---|---|---|
| 1–3 | 2 | 2 |
| 4–6 | 3 | 3 |
| 7–10 | 4 | 4 |
| 11–15 | 5 | 5 |
| 16–20 | 6 | 6 |
| 21+ | 7 | 7 (termasuk pasangan mirip: merah–pink, biru–ungu) |

### 12.3 Kelompokkan (baru)

| Level | Objek | Kategori |
|---|---|---|
| 1–3 | 4 (2+2) | Hewan vs Buah |
| 4–6 | 6 (3+3) | Hewan vs Buah |
| 7–9 | 8 (4+4) | Hewan vs Buah |
| 10–14 | 10 (5+5) | Hewan vs Kendaraan |
| 15–19 | 12 (6+6) | Buah vs Sayur |
| 20+ | 12 | Campuran acak dari semua kategori |

### 12.4 Hubungkan Pasangan (baru)

| Level | Pasangan | Kartu per kolom | Kategori |
|---|---|---|---|
| 1–2 | 1 | 1 | Hewan → makanan |
| 3–5 | 2 | 2 | Fungsi benda (kaki→sepatu) |
| 6–9 | 3 | 3 | Habitat & rutinitas (ikan→air) |
| 10–14 | 3 | 3 | Relasi lebih abstrak (kunci→pintu) |
| 15+ | 4 | 5 (ada pengecoh) | Campuran kategori |

### 12.5 Bandingkan Ukuran (baru)

| Level | Aktivitas | Objek |
|---|---|---|
| 1–4 | Pilih yang lebih besar | 2 |
| 5–8 | Pilih yang lebih kecil | 2 |
| 9–12 | Pilih yang paling besar | 3 |
| 13–16 | Pilih yang paling kecil | 3 |
| 17+ | Urutkan dari kecil ke besar | 3–4 |

### 12.6 Cari yang Berbeda (baru)

| Level | Gambar | Jarak kategori |
|---|---|---|
| 1–4 | 3 | Jauh (ikan vs mobil) |
| 5–9 | 4 | Sedang (lebah vs pensil) |
| 10–14 | 4 | Dekat (es krim vs baju) |
| 15+ | 5 | Kemiripan visual (apel vs tomat vs ceri) |

### 12.7 Aturan Umum Leveling

Berlaku untuk semua mini game:

1. **Satu variabel naik per tahap.** Jangan naikkan jumlah objek DAN kesulitan
   kategori sekaligus — anak butuh satu variabel baru saja per tahap.
2. **Setiap tahap minimal 3 level.** Terlalu cepat naik bikin anak frustrasi.
3. **Konsep baru diperkenalkan di level pertama tahap**, lalu diulang 2–3 kali
   sebelum variasi ditambah.
4. **Tidak ada level yang lebih mudah dari level sebelumnya.** Kesulitan harus
   monoton naik.
5. **Level 1–3 harus bisa diselesaikan anak 2 tahun tanpa bantuan.** Kalau
   tidak, kurva awalnya terlalu curam.
6. **Jawaban benar tidak boleh bisa ditebak dari posisi.** Objek terbesar atau
   gambar berbeda tidak selalu di kanan.
7. **Bintang maksimum 5 per level**, mengikuti sistem rating yang sudah ada.
8. **Satu game minimal 12 level** sebelum masuk siklus variasi ulang.

---

## 13. Metrik Keberhasilan

- Anak bisa menyelesaikan minimal 1 pasangan tanpa bantuan orang tua.
- Waktu rata-rata per pasangan turun setelah 3 kali bermain.
- Tidak ada crash di rilis.
- Orang tua melaporkan mini-game ini terasa "mendidik" (bukan sekadar hiburan).

---

## 14. Risiko & Mitigasi

| Risiko | Mitigasi |
|---|---|
| Pasangan terlalu abstrak untuk 2 tahun | Level 1–5 hanya hubungan konkret (hewan–makanan, badan–benda) |
| Emoji tidak ter-render di sebagian device | Uji render emoji di beberapa device, siapkan fallback vector |
| Kartu terlalu kecil untuk jari anak | Minimum 90dp per kartu, maksimum 4 kartu per kolom |
| Garis penghubung menutupi objek | Garis digambar di bawah kartu, tidak menimpa ilustrasi |
| Anak bingung tap dua kartu | Kartu terpilih diberi border tebal + suara klik |
| Jumlah aset ilustrasi besar | Mulai dengan 20 objek inti, tambah bertahap per rilis |

---

## 15. Improvement Susun Pola

Game Susun Pola sudah ada, tapi baru memakai **pola satuan** (satu simbol
berulang dalam satu siklus sederhana). Tiga point improvement berikut menaikkan
variasi dan tantangan tanpa keluar dari batas usia 2 tahun.

### 15.1 Point 1 — Pola Berulang Berpola Blok

**Masalah sekarang:** semua pola adalah siklus satu simbol (`A B A B`). Anak
yang hafal pola `A B` akan lolos semua level tanpa benar-benar berpikir.

**Improvement:** tambahkan pola yang siklusnya **blok berisi beberapa simbol**.

| Pola blok | Contoh | Jawaban | Level |
|---|---|---|---|
| `[A B] [A B] ?` | ⭐ 🌙 ⭐ 🌙 ⭐ ? | 🌙 | 23+ |
| `[A A B] [A A B] ?` | ⭐ ⭐ 🌙 ⭐ ⭐ 🌙 ? | ⭐ | 25+ |
| `[A B C] [A B C] ?` | ⭐ 🌙 ☀️ ⭐ 🌙 ☀️ ? | ⭐ | 28+ |
| `[A B] [C C] ?` | ⭐ 🌙 ☀️ ☀️ ⭐ ? | 🌙 | 30+ |

Keunggulan: anak tidak bisa menebak dengan "gantian A B", harus melihat blok
sebagai satu unit.

### 15.2 Point 2 — Variasi Sumber Pola (Bukan Hanya Simbol)

**Masalah sekarang:** semua pola memakai simbol bentuk (bintang, bulan,
matahari). Kalau anak bosan bentuknya, tidak ada variasi lain.

**Improvement:** tambahkan sumber pola baru yang tetap bisa dikenali anak 2
tahun:

| Sumber pola | Contoh | Level |
|---|---|---|
| Bentuk geometris | 🔴 🔺 🔴 🔺 ? | sepanjang game |
| Hewan | 🐱 🐶 🐱 🐶 ? | 10+ |
| Buah | 🍎 🍌 🍎 🍌 ? | 10+ |
| Ukuran (besar-kecil) | 🔵besar 🔵kecil 🔵besar ? | 20+ |
| Warna (objek sama, warna beda) | 🔴 🟡 🔴 🟡 ? | 20+ |
| Arah (menghadap kiri-kanan) | 🐟→ 🐟← 🐟→ ? | 25+ |

Pola ukuran dan arah paling menantang karena anak harus memperhatikan atribut,
bukan objeknya. Keduanya baru cocok di level 20+.

### 15.3 Point 3 — Variasi Mode Jawaban

**Masalah sekarang:** semua level memakai mode yang sama — ada satu `?` dan
anak memilih satu simbol dari beberapa pilihan. Anak yang sudah hafal mekanik
bisa main tanpa berpikir.

**Improvement:** tambahkan tiga mode jawaban berbeda yang bergantian:

| Mode | Cara main | Level |
|---|---|---|
| Mode 1: Lanjutkan | Ada `?`, pilih simbol yang tepat | 1+ (sudah ada) |
| Mode 2: Cari yang salah | Pola sudah terisi, tapi ada satu simbol salah. Tap yang salah | 12+ |
| Mode 3: Salin pola | Tampilkan pola di atas, anak menyalin ke kotak kosong dengan tap | 18+ |
| Mode 4: Lanjutkan 2 langkah | Ada `? ?` di akhir, pilih dua simbol berurutan | 30+ |

Mode 2 dan 4 paling efektif melatih pemahaman, karena anak tidak bisa main
tebak-tebakan — harus benar-benar tahu polanya.

#### Detail Mode 3 — Salin Pola (tap, bukan drag)

Keputusan: **memakai tap, bukan drag.** Alasan:

| Aspek | Tap | Drag |
|---|---|---|
| Motorik | Tekan 1x | Tekan + tahan + geser + lepas |
| Kalau gagal | Tinggal tap lagi | Simbol terlepas, harus ulang dari awal |
| Cocok umur | 2 tahun ke atas | Umumnya 3 tahun ke atas |
| Ukuran target | Besar, tidak perlu presisi | Butuh target drop presisi |

Jari anak 2 tahun belum stabil untuk drag; kalau jari terlepas di tengah,
simbol jatuh dan anak bisa frustrasi. Karena itu mode 3 memakai tap:

1. Anak melihat pola contoh di bagian atas (tidak bisa diubah).
2. Di bawahnya ada kotak kosong sejumlah panjang pola.
3. Di bagian paling bawah ada deretan simbol sebagai sumber.
4. Anak tap simbol di deretan, lalu tap kotak kosong tujuan.
5. Simbol masuk ke kotak; kalau salah, bisa di-tap ulang untuk diganti.

Aturan tambahan:

- Kotak kosong yang sedang aktif diberi border tebal berwarna.
- Simbol sumber tidak pernah hilang dari deretan (bisa dipakai berkali-kali).
- Tidak ada penalti kalau salah; anak bebas mengganti sampai benar.
- Level dianggap selesai kalau semua kotak terisi dengan benar.

### 15.4 Aturan Validasi Setelah Improvement

Kelas `PatternPuzzleFactory` harus diperluas:

- Setiap pola blok wajib punya minimal 2 pengulangan blok sebelum `?`.
- Panjang sequence maksimum 8 elemen (batas layar).
- Satu level hanya memakai satu sumber pola (jangan campur hewan dan bentuk
  dalam satu soal).
- Mode 2 wajib punya tepat satu simbol salah.
- Mode 4 wajib punya jawaban yang tidak ambigu (hanya satu kombinasi benar).
- Jawaban wajib selalu tersedia di pilihan.
- Tidak ada pilihan duplikat.

### 15.5 Dampak Leveling

Level 1–22 tidak berubah. Perubahan mulai level 23:

| Level | Perubahan |
|---|---|
| 23–24 | Pola blok `[A B] [A B] ?` |
| 25–27 | Pola blok `[A A B] [A A B] ?` |
| 28–29 | Pola blok `[A B C] [A B C] ?` |
| 30+ | Pola blok bercampur + mode jawaban bergantian |

### 15.6 Urutan Implementasi Improvement

| Tahap | Isi | Versi |
|---|---|---|
| 1 | Point 1: pola blok | 1.7.0 |
| 2 | Point 2: variasi sumber pola | 1.7.1 |
| 3 | Point 3: mode jawaban bergantian | 1.8.0 |

---

## 16. Pertanyaan Terbuka

1. Tema visual ikut worksheet (putih + border coral/teal) atau ikut tema taman
   Logichild yang sudah ada?
2. Perlu mode "cetak worksheet" dari app, atau cukup digital saja?
3. Worksheet mana saja dari 7 gambar yang dikirim — perlu konfirmasi ulang
   karena 6 gambar terdeteksi poster produk.
4. Mini game worksheet diprioritaskan sebelum papan petualangan, atau sesudahnya?
5. Apakah nama objek perlu dibacakan dalam dua bahasa (ID + EN) setiap kali
   objek muncul, atau hanya saat pertama kali?
6. ~~Untuk Susun Pola mode 3 (salin pola), anak menyusun dengan tap pilihan atau
   drag dari deretan simbol?~~ → **Sudah diputuskan: tap** (lihat 15.3).
