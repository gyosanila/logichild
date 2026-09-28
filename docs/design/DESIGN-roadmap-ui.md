# DESIGN — UI Roadmap "Petualangan" Logichild (Papan Mario-Party)

Status: DRAFT desain v0.1 — menunggu approval sebelum implementasi Compose
Referensi: `docs/PRD-adventure-roadmap.md` (PRD v1.1)
Mockup: `docs/design/mockups/board.html` (buka `?v=1|2|3`) → `board-1.png` (eksplorasi), `board-2.png` (lempar dadu + langkah), `board-3.png` (peta tamat)

Prinsip: anak 2 tahun. Satu jari, tanpa baca, tanpa kalah, tanpa timer.
Target: 1 sesi = 3–10 menit didampingi orang tua, speaker HP.

---

## 1. Peta layar & grid

Viewport referensi 540×960 (render 1080×1920 @2x, layar 360dp = skala 1.5).

| Zona | Tinggi | Isi |
|---|---|---|
| HUD | 88 | kembali, plakat nama peta, ⭐ total, 🍎 total, toggle suara |
| Ribbon progres | 12 | 24 titik, terisi = sudah dilewati, bendera = posisi kini |
| Dunia (peta) | 730 | langit, awan, pelangi, bukit, rumput, jalur, kotak, karakter |
| Bar dadu | 130 | tombol dadu 96 + panah petunjuk + chip "Kotak 4/24" |

Toolbar tetap komponen `Toolbar` yang sama dengan halaman game (satu komponen, bukan toolbar baru).

---

## 2. Susunan lapisan & parallax

Digambar dari belakang ke depan. Faktor = pengali offset scroll horizontal.

| # | Lapisan | Faktor | Catatan render |
|---|---|---|---|
| 1 | Langit gradien | 0.00 | `Brush.verticalGradient` fixed, sibling di luar container scroll |
| 2 | Matahari + 3 awan | 0.10 | `graphicsLayer { translationX = -scroll*0.10f }` |
| 3 | Bukit jauh | 0.25 | dua siluet hijau-teal, tanpa detail |
| 4 | Pelangi + pohon jauh | 0.40 | pelangi 5 pita, opacity 0.35 |
| 5 | Rumput + jalur + kotak | 1.00 | dunia utama (Canvas + node) |
| 6 | Semak, bunga, kristal, jamur, batu (zona belakang) | 1.00 | asymmetris, minimal 90px dari pusat jalur |
| 7 | Kotak + nomor + meter bintang | 1.00 | digambar setelah jalur |
| 8 | **Karakter + bayangan** | 1.00 | digambar setelah kotak, tapi **sebelum** vegetasi depan |
| 9 | — | — | **Tidak ada semak tambahan di dekat beruang** (user 2026-09: "bulat-bulat hijau dekat beruang, gk perlu, hapus"). Beruang berdiri polos di atas kotaknya; jangan menambah properti baru khusus untuk kotak aktif |
| 10 | Rumput depan (blade besar) | 1.25 | bergoyang idle, memberi kedalaman |
| 11 | Vignette + HUD | — | fixed, paling depan |

Aturan: karakter berdiri polos di atas kotaknya. Dekorasi yang sudah ada di peta (semak/bunga milik
semua kotak) tetap dipakai apa adanya — **jangan** membuat properti baru khusus untuk kotak aktif.

Aturan keras: lapisan 1–4 & 9 **tidak** ikut scroll (pernah salah, langit ikut hilang). Semua lapisan statis di-cache di `graphicsLayer`; hanya karakter, dadu, dan partikel yang di-animate tiap frame.

---

## 3. Palet & token

| Token | Hex | Pakai |
|---|---|---|
| skyTop / skyBottom | `#8FD4F7` / `#CFEEFF` | langit |
| grassLight / grassDark | `#9AD86A` / `#61B04A` | rumput |
| pathEdge | `#C8955B` | tepi jalur (shadow) |
| pathFill | `#F2D7A3` | badan jalur |
| pathLight | `#FDF0D5` | highlight tengah jalur |
| woodDark / woodLight | `#8B5E3C` / `#A9744F` | plakat, tombol |
| stoneDone | `#7ED957` | kotak selesai |
| stoneCurrent | `#FFD24D` | kotak aktif (frontier) |
| stoneOpen | `#FFF3DC` | kotak terbuka |
| stoneLocked | `#C9C9C9` (opacity .65) | kotak terkunci |
| starGold | `#FFC94D` | bintang, pita hadiah |
| inkNavy | `#1E3A5F` | teks di panel putih |
| accentBerry | `#FF6B6B` | buah, badge |
| accentPurple | `#A78BFA` | aksen bonus/rainbow |

Kontras: angka kotak = `#4A2E14` + outline putih 3px (wajib, angka di atas hijau/kuning tetap terbaca).
Panel informasi selalu putih opaque + shadow, teks navy. Tidak ada biru-di-atas-biru, tidak ada teks putih di atas putih transparan.

---

## 3b. Footer (bar dadu) — peta-nya yang turun sampai bawah

Update user 2026-09: *"background pake yg sama aja dngn roadmapny, cuma dikasih overlay aja, trus
tambahin rumput2nya"*. Jadi implementasinya bukan band/warna terpisah sama sekali:

- **`.world` sekarang `bottom: 0`** (dulu berhenti 134 px di atas footer) dan tinggi dunia 864
  (dulu 730). Tanah, rumput, bunga, kerikil di zona footer **milik dunia** — ikut ter-scroll dan
  ikut parallax, bukan background statis.
- Footer hanya **overlay**: gradien `#0E2A12` 0 → 7% → 16% (makin gelap ke bawah) + garis tipis
  14 px di tepi atas (7%). Tidak ada warna dasar sendiri.
- **Rumput ditambah**: 78 rumpun rumput (y 736–848) + 20 bunga + 16 kerikil, semua bergoyang angin.
- Konsekuensi: kotak terjauh ada di y 615 (masih di atas footer) sehingga dadu tidak menutupi kotak
  mana pun. Kalau nanti jalur peta makin ke bawah, ingat batas ini.

## 3b-old. Catatan lama footer

Prinsip (user 2026-09): footer **bukan panel/dunia terpisah**. Field-nya menyambung langsung dari
peta: gradien rumput footer mulai dari warna persis di tepi bawah peta (`#57A63F` → `#478F32`),
tanpa band gelap pemisah dan **tanpa dek kayu**. Dadu duduk langsung di rumput, hanya pakai
bayangan sendiri (ellipse hitam 22%).

Struktur: plakat kayu kiri (tapak + "KOTAK 4/24"), dadu 84px dengan 2 cincin denyut di tengah,
plakat kayu kanan (ikon dadu + "LEMPAR DADU"). Kedua plakat memakai **gradien + inset yang sama
persis dengan plakat judul di HUD** (`linear-gradient(#a9744f,#8B5E3C)`, inset putih atas / gelap
bawah) supaya terbaca sebagai satu tempat; jangan dimiringkan lagi.

Tinggi 134; dadu WAJIB muat di dalam strip (jangan overflow — pernah terpotong). Saat lempar dadu,
kanan jadi "JALAN 3 KOTAK" dan kiri jadi "DADU 3".

## 3c. Yang terjadi saat peta di-geser sampai ujung

Peta = **satu dunia sepanjang 2880 px** (±5,3 layar) untuk 24 kotak. **Tidak ada pengulangan /
tiling background.**

- Posisi kotak = daftar `List<Offset>` tetap; lebar dunia = `x kotak terakhir + padding`
  (mockup: 2880 px). `horizontalScroll` otomatis berhenti di ujung isi.
- **Langit digambar FIX di layar** (sibling di luar container scroll) — tidak ikut bergeser.
- Awan, bukit, pohon, pelangi bergerak dengan faktor **0,10 / 0,25 / 0,40 / 0,10** dari jarak
  scroll (`graphicsLayer { translationX = -scroll * f }`). Objek-objek ini disebar sepanjang
  seluruh lebar peta, jadi tidak perlu diulang: untuk faktor 0,4 hanya ±1150 px yang perlu tertutup.
- Kotak 24 = FINISH. Kamera berhenti di situ (clamp), anak tidak bisa scroll melewatinya.
  Setelah peta tamat → layar perayaan → Peta 2 (konten baru, bukan background berulang).

Lihat `docs/design/mockups/board-4.png`: gambaran seluruh peta (MULAI → FINISH) untuk memeriksa
bahwa latar nyambung penuh dan tidak ada pengulangan.

## 3d. Suasana 4 musim (permintaan user 2026-09)

Peta 1 = satu perjalanan melewati empat musim, **6 kotak per musim**:

| Kotak | Musim | Ground | Pohon | Properti | Partikel & langit |
|---|---|---|---|---|---|
| 1–6 | Semi | hijau segar `#A6DE78` | pohon berbunga pink | tulip, jamur, semak | kelopak pink jatuh, kupu-kupu, balon |
| 7–12 | Panas | hijau pekat `#63B537` | pohon rindang | bunga matahari, lebah, rumput tinggi | kilau kecil, lebah terbang |
| 13–18 | Gugur | kuning-kering `#B7B94F` | pohon oranye | labu, tumpukan daun, jamur | daun oranye jatuh |
| 19–24 | Dingin | salju `#EFF7FC` | pinus bersalju | boneka salju, pinus kecil, gundukan salju | titik salju + kepingan, langit lebih pucat |

Aturan penting: **ground digambar sebagai SATU gradien horizontal** dengan stop di batas musim, bukan
4 blok terpisah. Blok terpisah menghasilkan garis vertikal yang kelihatan (pernah kejadian, terlihat
seperti pita). Yang berubah per musim: gradien ground, jenis pohon, properti, partikel jatuh, dan
lapisan salju di atas jalan (clip rect di zona dingin). Semak/bunga yang sudah ada di peta tetap
dipakai; di zona dingin otomatis dapat tutup salju putih dan bunganya jadi pucat — bukan objek baru.

Di app, musim ditentukan oleh posisi kotak (`zoneAt(x)`), jadi tidak perlu state tambahan: cukup
pilih palet + set properti berdasarkan indeks kotak.

## 3e. Animasi: angin + partikel jatuh (permintaan user 2026-09)

Semua gerak dibuat sebagai **fungsi murni dari waktu** dengan periode loop 4 detik — bukan animasi
acak — supaya hasilnya bisa direproduksi dan loop-nya mulus (frame ke-48 = frame ke-0).

| Gerak | Rumus / nilai |
|---|---|
| Angin | `sin(t/4 · 2π + phase)`, `phase` diambil dari posisi x objek → gelombang angin menjalar dari kiri ke kanan, bukan semua bergoyang serempak |
| Pohon | rotasi ±2,6° di titik pangkal (bukan translasi) |
| Rumput | rotasi ±7–8°, termasuk rumput di footer |
| Awan | geser horizontal ±11 px + napas vertikal ±3 px |
| Partikel jatuh | turun 100 px/s, di-wrap tiap 400 px (1 siklus = 4 s), melayang ±30 px ikut angin, rotasi ±46°/s bergantian arah. **Satu partikel tiap ±48 px lebar dunia → 60 partikel** |
| Jenis partikel | kelopak pink (semi) · kilau + lebah (panas) · daun oranye (gugur) · butiran & kepingan salju (dingin) |
| Karakter | napas bob ±3,2 px periode 2 s + squash 1,4 %; bayangannya mengecil saat naik |
| UI | cincin kotak aktif & cincin dadu berdenyut; kotak tujuan tetap disorot |

Implementasi Compose: **satu** `rememberInfiniteTransition` (atau satu nilai waktu dari
`withInfiniteAnimationFrameNanos`) jadi sumber waktu tunggal; posisi tiap partikel dihitung dari
waktu itu, jadi tidak ada state per-partikel yang di-mutasi. Layer partikel digambar di `Canvas`
di atas dunia dan di bawah HUD. Batas partikel ±60; jangan lewat ~80 di app, dan sediakan flag
kualitas kalau perlu turun untuk HP low-end (partikel = satu-satunya kerja per-frame).
Layer parallax (awan/bukit) tidak menambah biaya karena cuma `graphicsLayer` per layer.

Video demo hasil mockup: `mockups/roadmap-salju.mp4` (loop salju 4 s) dan
`mockups/roadmap-4musim-pan.mp4` (geser seluruh peta 8 s + parallax).

## 4. Kotak (24 kotak, koordinat tetap)

Bentuk: blob batu 6 sudut tidak beraturan (bukan lingkaran/persegi), rx ±40. Tiga lapis:
1. bayangan lembut di bawah (`rgba(0,0,0,.18)`, blur 8)
2. badan bawah = warna state di-gelapkan 12% (rim)
3. badan atas = warna state + highlight 20% di kiri-atas (bevel)

Isi kotak: nomor level 30px bold. Badge ikon khusus di kanan-atas kotak (⭐ 🎮 🌈 🍎 🎁), ukuran 26.
Meter bintang: 5 glif ★/☆ ukuran 12 di bawah kotak (bukan di samping nomor) — kuning vs putih 60%.

| State | Visual | Idle animation |
|---|---|---|
| Selesai | hijau `stoneDone` | bob halus ±2px, 2.4s |
| Aktif / frontier | kuning `stoneCurrent` + cincin putih 4px + ring glow berdenyut | glow 1.2s, 3 sparkle mengorbit, panah ▼ melayang di atas |
| Terbuka | cream `stoneOpen` | tidak ada |
| Terkunci | abu + glyph 🔒, desaturasi | tidak ada |
| Start (kotak 1) | papan kayu + bendera merah 🚩 | bendera berkibar 3s |
| Finish (kotak 24) | podium + piala emas | shine menyapu 2.5s |

Distribusi kotak mengikuti PRD §5.2 (Start, Biasa×7, Bintang×4, Mini Game×4, Bonus×2, Kumpul×3, Hadiah×2, Finish).

Jalur: 3 stroke bertumpuk dari satu `Path` Bézier yang sama:
- tepi `pathEdge` 52px (di-offset +4px y → kesan terangkat)
- badan `pathFill` 46px
- highlight `pathLight` 30px
- plus kerikil kecil `#E0B877` tiap ±70px dan dash `#E8C48D` (2 22) di tengah

---

## 5. Karakter (maskot)

**Maskot = gambar yang diberikan user (2026-09), dipakai apa adanya sebagai SATU aset.**
File: `docs/design/mockups/mascot.png` (448×448, 25 KB, PNG RGBA) →
`app/src/main/res/drawable-nodpi/ic_mascot.png`. Anchor: telapak kaki di **0,968 × tinggi gambar**.

Cara ambil dari gambar sumber (JPG putih): flood-fill background dari tepi (PIL
`ImageDraw.floodfill`, thresh 70) — **jangan threshold global**, karena area putih di dalam
karakter (baju, bantalan telapak, moncong) juga putih; flood-fill dari tepi hanya membuang
background yang tersambung ke border. Lalu feather 1 px, crop ke bbox alpha, pad ke persegi,
resize 448, quantize FASTOCTREE 256 (224 KB → 25 KB, alpha tetap). Verifikasi dengan render
di atas background terang pada 220 / 120 / 88 px sebelum dipakai.

### Gerakan tangan: DIBATALKAN (jangan diulang tanpa aset baru)

Percobaan 2026-09: maskot dipotong 2 lapis (badan + lengan) supaya lengan bisa diputar di bahu.
Hasilnya **ditolak user**: *"tangan geraknya jelek, kayak kepotong"* — memotong lengan dari satu
gambar tidak bisa bersih, karena (a) potongan di bahu meninggalkan takik transparan saat lengan
diputar, dan (b) memotong di pergelangan butuh inpainting area yang tertutup telapak, yang
hasilnya tetap meninggalkan jejak. User lalu bilang *"gk usah dipotong dulu lah klo jadi jelek"*.

**Aturan: jangan pecah aset maskot.** Animasi yang dipakai hanya transform utuh badan
(lihat tabel). Kalau nanti mau lambaian tangan yang benar, minta ke user **satu gambar tambahan**
(pose tangan turun) atau versi vektor/Lottie — jangan akali dengan memotong PNG.

| Animasi | Trigger | Spec |
|---|---|---|
| Idle napas | selalu | translateY ±3,2 px periode 2 s + squash 1,4 % |
| Langkah (hop) | per kotak | busur 74 px, `squash` 1,07, rotate −7° di puncak; bayangan mengecil saat di udara; debu 3 bulatan di kotak yang ditinggalkan |
| Hadap | arah gerak | `scaleX = −1` (mirror), transisi 120 ms |
| Menang | efek kotak positif | rotate −6° + bounce 1,0→1,08→1,0 |
| Kedip / lambai | — | butuh aset tambahan dari user; **jangan** dipalsukan dengan memotong PNG |

## 6. Dadu & alur interaksi (inti "interactive")

Penanda kotak aktif: kotak kuning + glow + **cincin putus-putus emas**, dan karakter berdiri di atasnya.
Jangan tambah panah/segitiga melayang di dekat karakter — user menilai posisinya aneh dan artinya tidak jelas.

Rute yang akan dilewati: **jejak tapak kaki** (putih, 2 per celah kotak, selang-seling kiri/kanan,
ikut arah jalan) + cincin emas di tiap kotak tujuan. Jangan pakai angka 1-2-3 di kotak —
user tidak tahu itu apa.

1. **Antisipasi** — tombol dadu ditekan → kamera bergeser +35% viewport ke arah depan dulu (500ms) supaya anak lihat jalur yang akan dilewati.
2. **Gulir dadu** — dadu rotate + shake 1s, bunti `tap.wav`. Hasil hanya **1–3**.
3. **Penanda langkah** — kotak tujuan diberi badge urutan 1, 2, 3 yang menyala berurutan (250ms antar kotak).
4. **Jalan kotak demi kotak** (sesuai PRD §5.3, bukan teleport): hop 260ms + jeda 120ms, `step.wav` volume rendah, kamera follow dengan ease 500ms dan mendahului 35%.
5. **Efek kotak otomatis** setelah mendarat:
   - ⭐ Bintang: bintang terbang busur ke counter HUD (500ms) + counter pop 1→1.3→1 + glitter
   - 🌈 Bonus +2: kilat pelangi menyapu jalur + 2 hop tambahan (tidak berantai ke efek lain)
   - 🍎 Kumpul: buah terbang ke counter 🍎
   - 🎁 Hadiah: kotak bergetar 400ms → terbuka → confetti + tepuk tangan volume rendah
   - 🎮 Mini Game: cincin portal mengecil → fade in ke game; balik → karakter pop-bounce keluar, posisi kotak **tidak berubah**
   - 🏁 Finish: kamera zoom-out lihat seluruh peta → piala naik → confetti → layar perayaan
6. **Giliran selesai** — tombol dadu kembali aktif dengan pulse 1.0→1.05 (menandakan "sekarang kamu").

Setiap langkah = 1 sinyal audio + 1 sinyal haptic. Tidak ada `sparkle.wav` berulang di langkah (pernah dikeluhkan memusingkan).

---

## 7. HUD

- Kiri: `CircleToolbarButton` 40 — ArrowBack (AutoMirrored), vektor bukan glif teks.
- Tengah: plakat kayu rounded 20, isi "Peta 1 · Taman Bermain", putih bold 15 dengan bayangan teks tipis.
- Kanan: pill ⭐ total dan 🍎 total (putih, navy, angka roll-up 300ms saat berubah) + toggle suara (ikon vektor, state dari ViewModel).
- Ribbon progres: track `rgba(0,0,0,.22)` rounded, fill gradien `#FFB347→#FF8C42`; bendera kecil di titik posisi kini; milestone kotak 1/6/12/18/24 punya dot lebih besar.
- Bar bawah: chip kecil "Kotak 4/24" (baru dibaca orang tua, bukan anak).

Bilingual: semua wording lewat `strings.xml` (EN) + `values-in/` (ID). ID: "Petualangan", "Lempar Dadu", "Main Lagi", "Ke Menu", "Peta Selesai". EN: "Adventure", "Roll Dice", "Play Again", "Menu", "Map Complete".

---

## 8. Performa

- Lapisan statis (langit, awan, bukit, pelangi, jalur) dirender ke layer ter-cache; hanya karakter/dadu/partikel berubah tiap frame.
- Maksimum 3 animasi infinite bersamaan (glow frontier, bob karakter, goyang rumput depan).
- Partikel debu/confetti dibatasi ±40 objek, di-recycle.
- Target 60fps. **Belum diukur di HP low-RAM** — perlu tes device sebelum rilis.

---

## 9. Mapping ke Compose

| Elemen | Implementasi |
|---|---|
| Dunia + jalur + kerikil | `Canvas` + `Path` (cubicTo) + `drawPath` 3 lapis `Stroke(join=Round)` |
| Kotak / karakter | `Box` + `Modifier.offset(dp)` (klik & posisi pakai Dp, konversi px hanya di dalam Canvas) |
| Parallax | `Modifier.graphicsLayer { translationX = -scroll.value * f }` |
| Glow frontier | `rememberInfiniteTransition` + `animateFloat` (alpha/scale) |
| Hop | `Animatable.animateTo(target, keyframes { ... })` |
| Dadu | `Rotatable`/`animateFloatAsState` rotate + `shake` via keyframes |
| Debud/confetti | reusable `Particle` list + `Confetti` yang sudah ada |
| Kamera | `LaunchedEffect(posisi)` → `horizontalScroll.animateScrollTo` (mendahului 35% viewport) |
| Audio | `SoundPool` (`tap.wav`, `step.wav`, applause rendah), voice MP3 natural untuk efek kotak |
| Haptic | `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)` per langkah |

---

## 10. Aset yang perlu dibuat

1. Blob batu 4 state — digambar di Canvas (tanpa aset file) untuk versi 1; kalau perlu tekstur batu, ekspor dari Blender nanti.
2. Maskot beruang: 1 PNG per pose (idle, hop, senang, bingung) **atau** vektor Compose. Decide sebelum implementasi — PNG lebih cepat, vektor lebih ringan/ANR-safe.
3. WAV: `tap.wav`, `step.wav` (sudah ada dari `scripts/gen_sounds.py`).
4. Voice: klip natural ⭐/🎁/selamat sudah ada (`GameVoiceClips`).

---

## 11. Pertanyaan terbuka (blokir implementasi)

1. Peta 1: jalur berkelok horizontal (mockup ini) atau ring loop?
2. Maskot beruang: PNG pose atau digambar vektor di Compose?
3. Kotak 🎮: acak dari game yang sudah punya progres (PRD §5.5) — setuju?
4. Mode bebas: level terkunci tetap ditampilkan dengan 🔒 (PRD §6) — setuju?
