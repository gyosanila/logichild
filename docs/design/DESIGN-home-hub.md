# DESIGN — Home (Game Hub) Logichild

Status: DRAFT desain — menunggu approval
Mockup: `docs/design/mockups/home.html` → `home-1.png` (`?v=1`), `home-2.png` (`?v=2`)
Alur: `docs/PRD-adventure-roadmap.md` §4

## Kenapa desainnya dirombak (2026-09)

Versi pertama dinilai user *"kurang bagus, monoton"*. Penyebabnya: semua elemen berbentuk kartu
putih berukuran sama yang ditumpuk vertikal (hero polos + baris peta + 2×2 tile identik) di atas
latar hijau datar. Perbaikan yang dilakukan:

1. **Hero jadi panggung peta, bukan kartu teks.** Kartu "Lanjutkan Petualangan" sekarang berisi
   **pratinjau peta asli** (board.html di-iframe, di-crop ke area sekitar pemain) — bukan gambar
   hiasan. Jadi anak/orang tua langsung melihat posisi beruang di kotaknya.
2. **Latar jadi taman hidup**: pohon sakura kiri-kanan, semak pagar di bawah, bunga di sela kartu,
   kupu-kupu, awan tersenyum, matahari, burung.
3. **Progres musim kelihatan**: 4 segmen batang + 4 bulatan musim (tulip/matahari/daun/salju),
   musim yang sedang aktif menyala putih + cincin emas.
4. **Mini game jadi rak mainan**: tiap tile miring sedikit (rotasi −2,4°..+2,4°), tinggi berbeda
   (160/142), punya kilau putih, warna gradien sendiri per game, badge bintang.
5. **Baris bawah + pintu orang tua**: kartu "Papan Petualangan", kartu "Hadiah" (badge "2 baru"),
   dan pil "Pengaturan · khusus orang tua · terkunci".

## Struktur (koordinat 540×960)

| Zona | Isi |
|---|---|
| Header (12–64) | logo bulat + "Logichild" + versi; tombol gembok Pengaturan di kanan (badge kunci) |
| Panggung peta (72–380) | bar judul + counter bintang; jendela pratinjau peta (190); batang musim; 4 bulatan musim |
| Tombol LANJUT MAIN (368–428) | pil hijau besar, menumpuk di tepi bawah panggung (efek melayang) |
| Judul seksi (436–472) | chip "MINI GAME" + chip total bintang |
| Rak mini game (478–790) | 4 tile mainan, 2 kolom, tinggi & kemiringan berbeda |
| Baris bawah (844–918) | "Papan Petualangan" · "Hadiah" (badge "2 baru") |

**Pil "Pengaturan" di bawah DIHAPUS** (permintaan user). Pengaturan tetap hanya lewat ikon gear
bergembok di header. Jendela peta ditinggikan 190 → 230 px (crop y −463) supaya ruang bekas pil itu
terpakai dan makin banyak kotak peta yang kelihatan.

## Dua varian hero

| | Varian 1 (`?v=1`) | Varian 2 (`?v=2`) |
|---|---|---|
| Isi jendela | peta **zoom** di sekitar pemain (skala 0,926, crop y −470) | **seluruh peta** 24 kotak 4 musim (skala 0,347) |
| Kesan | "kamu di sini" — beruang besar, jelas untuk anak 2 tahun | "perjalananmu sejauh ini" — kelihatan 4 musim + sisa perjalanan |
| Kelemahan | tidak kelihatan seberapa jauh lagi | detail kecil, beruang jadi mini |

Rekomendasi: **Varian 1** untuk layar utama (target usia 2 tahun butuh elemen besar & jelas).
Varian 2 bisa dipakai di kartu "Papan Petualangan" sebagai pratinjau.

## Aturan teknis

- Pratinjau = `<iframe src="board.html?...">` di-crop dengan `transform: scale()` + `overflow:hidden`,
  sehingga pratinjau **selalu** sama dengan papan asli. Butuh `--allow-file-access-from-files` saat
  render headless (kalau tidak, iframe file:// tidak dimuat).
- **UI harus di atas ambience** (`#ui { z-index: 5 }`) — pernah kejadian bunga menabrak label tile.
- Dekorasi hanya ditaruh di **celah antar kartu** (x 0–16, 262–278, 524–540 atau pita y 436–472),
  jangan di bawah kartu.
- Tombol utama tidak boleh menutupi jendela pratinjau atau bulatan musim (tinggi panggung 308 px,
  tombol di top 368).
- Warna: latar langit → rumput (dunia yang sama dengan papan), kartu putih, teks navy `#1E3A5F`,
  aksen emas `#FFC94D` untuk bintang, hijau `#4fae2e` untuk aksi utama.
- Semua wording bilingual via `strings.xml` (EN) + `values-in/` (ID).

## Isi layar turunan (dijelaskan ke user)

### Papan Petualangan
Layar pemilih peta / pratinjau seluruh perjalanan. Isinya:
- **Peta 1 · Taman 4 Musim** — seluruh 24 kotak (skala kecil, 4 musim kelihatan), kotak yang sudah
  selesai hijau, posisi beruang ditandai, sisa perjalanan kelihatan. Bisa di-tap kotaknya untuk
  masuk langsung ke kotak itu.
- **Peta 2** — terkunci (badge gembok), muncul sebagai janji konten berikutnya.
- Statistik ringkas: kotak terbuka, bintang, musim yang sedang aktif.

Bedanya dengan tombol LANJUT MAIN: LANJUT = lompat langsung ke posisi terakhir (1 tap).
Papan Petualangan = buka petanya dulu, lihat progres, baru pilih kotak.

### Hadiah
"Buku stiker" anak — kumpulan hadiah dari kotak 🎁 di peta:
- Baris **hadiah yang sudah didapat** (stiker/badge), bisa di-tap → animasi + suara.
- Baris **belum didapat** (siluet + "2 kejutan menunggu") — jadi target berikutnya.
- Untuk usia 2 tahun ini bukan sistem ekonomi, hanya koleksi yang bisa dilihat & dibanggakan.

## Backsound

Dua loop **disintesis sendiri** (`docs/design/audio/make_backsound.py`) → bebas royalti, hasil
`.ogg` untuk app (`res/raw/`) + `.mp3` untuk preview:

| File | Tempo | Panjang | Karakter | Ukuran ogg |
|---|---|---|---|---|
| `backsound-taman.ogg` | 96 BPM | 10,0 s (16 ketuk) | marimba + shaker, progresi C–G–Am–F, ceria | 89 KB |
| `backsound-tenang.ogg` | 80 BPM | 12,0 s | kalimba arpeggio + pad, tanpa perkusi, tenang | 64 KB |

Panjang loop = bilangan bulat ketuk, ujungnya diredam + fade 30 ms → loop mulus (syarat: jangan
potong di tengah nada). Master −3,1 dBFS (peak 0,70 stereo, RMS 0,19) supaya tidak menutupi efek
suara mini game.

Implementasi di app:
- Putar dengan **Media3 ExoPlayer** (`setRepeatMode(REPEAT_MODE_ALL)`) atau `MediaPlayer`
  (`isLooping = true`); satu instance di level Application supaya meneruskan lintas layar.
- `AudioAttributes(USAGE_GAME, CONTENT_TYPE_MUSIC)` + `handleAudioFocus = true` → otomatis
  mengecil/berhenti saat TTS atau voice prompt mini game bicara.
- Volume 0,30–0,35 (musik latar), fade-in/out 400 ms saat masuk/keluar layar.
- **Pause di `onStop`** (wajib: keluar app → musik berhenti), resume di `onStart` kalau toggle aktif.
- Toggle "Musik" di Pengaturan (orang tua) + simpan di prefs.
- Pertimbangan: beberapa orang tua tidak suka backsound → default ON tapi mudah dimatikan, dan
  durasi loop pendek supaya tidak melelahkan.

## Layar turunan (mockup: `docs/design/mockups/screens.html?s=peta|hadiah|games`)

### 1. Papan Petualangan → `peta.png`
- Header: tombol kembali + plakat judul + counter bintang.
- **Pratinjau peta penuh** (iframe `board.html?v=4`, skala 0,353) + **penanda "kotak 8"** dengan
  panah ke kotaknya → anak/orang tua langsung tahu posisi sekarang. Di bawah pratinjau ada baris
  detail kotak itu: game-nya apa, musim apa, berapa bintang + tombol MULAI.
- Baris statistik: kotak terbuka 8/24 · bintang 13 · musim ini.
- Kartu "Perjalanan 4 Musim": 4 baris (Semi/Panas/Gugur/Dingin) dengan progress bar + status
  (selesai / aktif / terkunci).
- Kartu Peta 2 terkunci ("terbuka di kotak 24") + tombol utama "LANJUT DARI KOTAK 8".
- Interaksi kunci: **tap kotak mana saja di peta = mulai dari kotak itu** (teks hint di kartu).

### 2. Hadiah → `hadiah.png`
- Featured: stiker terbaru + asal hadiahnya ("dari kotak hadiah ke-6") + chip "tap buat lihat".
- Grid koleksi 4 kolom × 3 baris = **12 slot**: yang sudah didapat berwarna (tap → animasi+suara),
  yang belum = gembok. 3 slot terbawah diberi label **PETA 2** supaya jelas kenapa masih terkunci
  padahal di peta cuma sisa 2 kotak hadiah.
- Kartu bawah: "2 kejutan lagi menunggu · buka kotak hadiah ke-9 dan ke-12 di peta" + tombol KE PETA.

### 3. Mini Game (saat game sudah banyak) → `games.png`

Masalah: Home tidak boleh tumbuh tanpa batas, dan anak 2 tahun tidak bisa membaca/menavigasi daftar
panjang. Solusi berlapis:

| Lapis | Aturan |
|---|---|
| Home | tetap **4 permainan** (yang paling sering dimainkan) + 1 pintu **"SEMUA 12 PERMAINAN ›"** di header seksi. Home tidak pernah bertambah baris. |
| Daftar penuh | dikelompokkan **per kategori** (Semua · Warna · Bentuk · Angka · Kata · Motorik) sebagai chip yang bisa digeser. Tiap kategori dijaga ≤ 8 game. |
| Urutan | otomatis: belum-terkunci dulu, di dalamnya urut bintang/paling sering dimainkan; yang baru terbuka dapat badge **BARU**. **Tidak ada tombol sort** untuk anak. |
| Game terkunci | **tidak disembunyikan** — tampil redup + gembok + teks "terbuka di kotak 14". Ini yang mengikat daftar mini game ke roadmap. |
| Pencarian | **tidak ada** (anak belum bisa baca). Panjang daftar dibatasi kategori, bukan scroll tak berujung. |
| Kotak 🎮 di peta | tetap **otomatis**: ambil acak di antara game yang sudah punya progres — anak tidak pernah ditanya "mau main yang mana?". |

Kalau nanti game > 30: tambah kategori baru, dan boleh tambah baris "Favorit" (otomatis dari
paling sering dimainkan) di atas grid. Teknis: `LazyVerticalGrid` 2 kolom + ikon vector
(`ic_*.xml` yang sudah ada) + progres per game di store yang sama dengan papan.
