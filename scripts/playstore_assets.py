#!/usr/bin/env python3
"""Generate aset Play Store: feature graphic 1024x500 + 5 screenshots 1080x1920."""
from PIL import Image, ImageDraw, ImageFont
import math, os

ROOT = "/home/ilga/KartCilik"
OUT = f"{ROOT}/playstore"
os.makedirs(OUT, exist_ok=True)

FONT_B = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
FONT_R = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"

def F(size, bold=True): return ImageFont.truetype(FONT_B if bold else FONT_R, size)

SKY = (142, 214, 255)
RED = (229, 57, 53); RED_D = (198, 40, 40)
BLUE = (30, 136, 229); BLUE_D = (21, 101, 192)
PURPLE = (123, 79, 216)
GREEN = (102, 187, 106); GREEN_D = (46, 125, 50)
YELLOW = (255, 213, 79)
ORANGE = (255, 152, 0)
TEXT = (45, 52, 66)
WHITE = (255, 255, 255)
GRAY = (120, 130, 145)
LGREEN = (214, 240, 200)  # grass light
DGREEN = (155, 200, 140)  # grass dark

def rr(d, box, r, fill=None, outline=None, w=0):
    d.rounded_rectangle(box, radius=r, fill=fill, outline=outline, width=w)

def txt(d, xy, s, size, fill=TEXT, center=True, bold=True, font=None):
    f = font or F(size, bold)
    d.text(xy, s, font=f, fill=fill, anchor="mm")

def star(d, cx, cy, r, fill=YELLOW, outline=None, w=0):
    pts = []
    for i in range(10):
        ang = -math.pi / 2 + i * math.pi / 5
        rad = r if i % 2 == 0 else r * 0.45
        pts.append((cx + rad * math.cos(ang), cy + rad * math.sin(ang)))
    d.polygon(pts, fill=fill, outline=outline, width=w) if outline else d.polygon(pts, fill=fill)

def status_bar(d, W):
    txt(d, (60, 45), "09:41", 40, WHITE)
    # battery
    rr(d, [W - 150, 30, W - 30, 60], 10, outline=WHITE, w=4)
    d.rectangle([W - 22, 40, W - 14, 50], fill=WHITE)
    d.rectangle([W - 142, 38, W - 110, 52], fill=WHITE)
    # wifi dots
    for i, (dx, dy) in enumerate([(30, 0), (18, 10), (6, 20)]):
        d.ellipse([W - 230 + dx - 8, 20 + dy - 8, W - 230 + dx + 8, 20 + dy + 8], fill=WHITE)

def ad_banner(d, W, y, H=170):
    rr(d, [60, y, W - 60, y + H], 34, fill=(230, 235, 240))
    rr(d, [W - 190, y + 30, W - 90, y + 80], 16, fill=GRAY)
    txt(d, ((W - 140), y + 55), "Ad", 30, WHITE)
    txt(d, ((W - 60) / 2 + 60, y + 100), "Banner iklan", 34, GRAY)

def logo(img, size, xy):
    """Tempel icon asli."""
    lg = Image.open(f"{ROOT}/icon_source.png").convert("RGBA").resize((size, size), Image.LANCZOS)
    mask = Image.new("L", (size, size), 0)
    dm = ImageDraw.Draw(mask)
    dm.rounded_rectangle([0, 0, size, size], radius=int(size * 0.22), fill=255)
    img.paste(lg, xy, mask)

def card_game(d, W, y, H, color, icon_fn, title, sub, icon_size=150):
    rr(d, [80, y, W - 80, y + H], 60, fill=color)
    icon_fn(160 + icon_size // 2, y + H // 2, icon_size)
    txt(d, (W // 2 + 60, y + H // 2 - 55), title, 62, TEXT)
    txt(d, (W // 2 + 60, y + H // 2 + 35), sub, 38, TEXT, bold=False)

def draw_car(cx, cy, s):
    img = Image.new("RGBA", (s * 2, s * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rr(d, [s * 0.3, s * 0.55, s * 1.7, s * 1.35], 24, fill=RED, outline=TEXT, w=6)
    d.polygon([(s * 0.55, s * 0.55), (s * 0.8, s * 0.15), (s * 1.3, s * 0.15), (s * 1.5, s * 0.55)],
              fill=(255, 210, 120), outline=TEXT, width=6)
    rr(d, [s * 0.7, s * 0.22, s * 1.25, s * 0.55], 10, fill=(180, 230, 255))
    d.ellipse([s * 0.45, s * 1.15, s * 0.85, s * 1.55], fill=TEXT)
    d.ellipse([s * 1.15, s * 1.15, s * 1.55, s * 1.55], fill=TEXT)
    return img

def draw_apple(cx, cy, s):
    img = Image.new("RGBA", (s * 2, s * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([s * 0.3, s * 0.35, s * 1.0, s * 1.55], fill=RED, outline=TEXT, width=6)
    d.ellipse([s * 0.65, s * 0.35, s * 1.35, s * 1.55], fill=(244, 67, 54), outline=TEXT, width=6)
    d.line([s * 0.82, s * 0.3, s * 0.82, s * 0.05], fill=(101, 67, 52), width=10)
    d.ellipse([s * 0.5, s * 0.05, s * 0.95, s * 0.42], fill=GREEN, outline=TEXT, width=5)
    d.ellipse([s * 0.55, s * 0.9, s * 0.85, s * 1.2], fill=(255, 255, 255, 90))
    return img

def draw_robot(cx, cy, s):
    img = Image.new("RGBA", (s * 2, s * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rr(d, [s * 0.35, s * 0.4, s * 1.65, s * 1.6], 40, fill=YELLOW, outline=TEXT, w=6)
    d.ellipse([s * 0.55, s * 0.55, s * 0.85, s * 0.85], fill=TEXT)
    d.ellipse([s * 1.15, s * 0.55, s * 1.45, s * 0.85], fill=TEXT)
    d.arc([s * 0.6, s * 1.0, s * 1.4, s * 1.35], 20, 160, fill=TEXT, width=8)
    d.line([s * 1.0, s * 0.4, s * 1.0, s * 0.1], fill=TEXT, width=10)
    d.ellipse([s * 0.85, s * 0.0, s * 1.15, s * 0.3], fill=RED, outline=TEXT, width=5)
    return img

def draw_kart_icon(cx, cy, s):
    img = draw_car(cx, cy, s)
    return img

def paste_icon(base, icon_img, cx, cy, s):
    icon_img = icon_img.resize((s, s), Image.LANCZOS)
    base.paste(icon_img, (cx - s // 2, cy - s // 2), icon_img)

# ══════════════════ 1. FEATURE GRAPHIC 1024x500 ══════════════════
W, H = 1024, 500
fg = Image.new("RGBA", (W, H), (0, 0, 0, 0))
d = ImageDraw.Draw(fg)
for y in range(H):
    t = y / H
    d.line([(0, y), (W, y)], fill=(int(142 - 40 * t), int(214 - 8 * t), int(255 - 90 * t)))
# ornamen: lingkaran & bintang
d.ellipse([-60, -80, 260, 240], fill=(255, 255, 255, 40))
d.ellipse([830, 330, 1120, 620], fill=(255, 255, 255, 35))
star(d, 950, 90, 30, (255, 255, 255, 200))
star(d, 90, 430, 24, (255, 255, 255, 180))
star(d, 560, 40, 20, (255, 255, 255, 150))
# logo
logo(fg, 210, (60, 145))
txt(d, (330, 190), "Logichild", 92, WHITE)
txt(d, (330, 290), "Belajar Logika Sambil Main!", 44, WHITE, bold=False)
# chips manfaat
chips = ["Aman untuk anak", "Tanpa internet", "Kontrol waktu main"]
cx0 = 340
for c in chips:
    wch = 60 + len(c) * 30
    rr(d, [cx0 - wch // 2, 350, cx0 + wch // 2, 420], 35, fill=(255, 255, 255, 70))
    txt(d, (cx0, 385), c, 30, WHITE)
    cx0 += wch + 30
# ikon game kecil
paste_icon(fg, draw_car(0, 0, 200), 860, 400, 200)
fg.convert("RGB").save(f"{OUT}/feature_graphic.png")
print("feature_graphic.png")

# ══════════════════ 2. SCREEN MENU ══════════════════
W, H = 1080, 1920
def new_screen():
    im = Image.new("RGB", (W, H), SKY)
    d = ImageDraw.Draw(im)
    status_bar(d, W)
    return im, d

im, d = new_screen()
logo(im, 300, (390, 260))
txt(d, (540, 640), "Logichild", 90, WHITE)
txt(d, (540, 730), "Pilih game-nya!", 46, WHITE, bold=False)
card_game(d, W, 830, 300, YELLOW, lambda cx, cy, s: paste_icon(im, draw_car(0, 0, s), cx, cy, s), "Main Mobil", "Susun langkah, mobil sampai finish!", 170)
card_game(d, W, 1170, 300, (102, 187, 106), lambda cx, cy, s: paste_icon(im, draw_apple(0, 0, s), cx, cy, s), "Petik Buah", "Susun perintah, robot panen buah!", 170)
rr(d, [80, 1520, W - 80, 1640], 40, fill=(255, 255, 255, 60))
txt(d, (540, 1580), "Pengaturan", 44, WHITE)
txt(d, (540, 1730), "Tanpa internet  •  Untuk balita 2+", 34, WHITE, bold=False)
ad_banner(d, W, 1750)
im.save(f"{OUT}/screen_1_menu.png")
print("screen_1_menu.png")

# ══════════════════ 3. SCREEN KART ══════════════════
im, d = new_screen()
# toolbar
paste_icon(im, draw_car(0, 0, 90), 110, 130, 90)
txt(d, (300, 130), "Main Mobil", 56, WHITE)
d.ellipse([850, 90, 950, 190], fill=(255, 255, 255, 230))
d.ellipse([975, 90, 1075, 190], fill=(255, 255, 255, 230))
txt(d, (900, 140), "OK", 34, TEXT)   # speaker
txt(d, (1025, 140), "H", 40, TEXT)   # home
# level dots
for i in range(8):
    r = 40 if i == 3 else 32
    d.ellipse([90 + i * 115, 230 - r, 90 + i * 115 + r * 2, 230 + r], fill=YELLOW if i == 3 else (255, 255, 255, 220))
    txt(d, (90 + i * 115 + r, 230), str(i + 1), 30, TEXT)
# board
bx, by, bw, bh = 90, 330, 900, 900
rr(d, [bx, by, bx + bw, by + bh], 40, fill=WHITE)
cell = 900 // 6
for x in range(6):
    for y in range(4):
        col = LGREEN if (x + y) % 2 == 0 else DGREEN
        rr(d, [bx + x * cell + 6, by + y * cell + 6, bx + (x + 1) * cell - 6, by + (y + 1) * cell - 6], 12, fill=col)
# cone
for cx, cy in [(2, 0), (4, 1), (1, 3), (5, 2)]:
    px = bx + cx * cell + cell // 2
    py = by + cy * cell + cell // 2
    d.polygon([(px, py - 55), (px - 40, py + 40), (px + 40, py + 40)], fill=ORANGE, outline=TEXT, width=5)
# finish
fx = bx + 5 * cell + cell // 2
fy = by + 3 * cell + cell // 2
for i in range(2):
    for j in range(2):
        d.rectangle([fx - 50 + i * 50, fy - 50 + j * 50, fx - 50 + (i + 1) * 50, fy - 50 + (j + 1) * 50], fill=TEXT if (i + j) % 2 == 0 else WHITE)
# kart
paste_icon(im, draw_car(0, 0, 150), bx + cell // 2, by + cell // 2 + 130, 150)
# strip instruksi
rr(d, [90, 1280, 940, 1360], 30, fill=(255, 255, 255, 235))
for i, col in enumerate([BLUE, RED, PURPLE, BLUE]):
    rr(d, [120 + i * 110, 1305, 210 + i * 110, 1365], 16, fill=col)
rr(d, [955, 1300, 1015, 1360], 14, fill=(255, 255, 255, 235))
# controller
rr(d, [90, 1400, 990, 1580], 40, fill=(255, 255, 255, 50))
btns = [(BLUE, "Kiri"), (RED, "Maju"), (PURPLE, "Kanan")]
for i, (col, lb) in enumerate(btns):
    d.ellipse([160 + i * 240, 1430, 260 + i * 240, 1530], fill=col, outline=TEXT, width=5)
    txt(d, (210 + i * 240, 1550), lb, 28, WHITE)
rr(d, [90, 1610, 990, 1740], 40, fill=(255, 255, 255, 70))
d.ellipse([260, 1635, 380, 1715], fill=GREEN, outline=TEXT, width=5)
d.ellipse([500, 1635, 660, 1715], fill=GREEN_D, outline=TEXT, width=5)
d.ellipse([760, 1635, 840, 1715], fill=GRAY, outline=TEXT, width=5)
txt(d, (320, 1700), "Kiri", 26, WHITE)
txt(d, (580, 1695), "Main!", 30, WHITE)
txt(d, (800, 1700), "Ulang", 26, WHITE)
ad_banner(d, W, 1750)
im.save(f"{OUT}/screen_2_kart.png")
print("screen_2_kart.png")

# ══════════════════ 4. SCREEN FRUIT ══════════════════
im, d = new_screen()
paste_icon(im, draw_apple(0, 0, 90), 110, 130, 90)
txt(d, (300, 130), "Petik Buah", 56, WHITE)
d.ellipse([850, 90, 950, 190], fill=(255, 255, 255, 230))
d.ellipse([975, 90, 1075, 190], fill=(255, 255, 255, 230))
txt(d, (900, 140), "OK", 34, TEXT)
txt(d, (1025, 140), "H", 40, TEXT)
for i in range(6):
    r = 40 if i == 2 else 32
    d.ellipse([90 + i * 140, 230 - r, 90 + i * 140 + r * 2, 230 + r], fill=YELLOW if i == 2 else (255, 255, 255, 220))
    txt(d, (90 + i * 140 + r, 230), str(i + 1), 30, TEXT)
bx, by, bw, bh = 90, 330, 900, 900
rr(d, [bx, by, bx + bw, by + bh], 40, fill=WHITE)
cell = 900 // 6
for x in range(6):
    for y in range(4):
        col = LGREEN if (x + y) % 2 == 0 else DGREEN
        rr(d, [bx + x * cell + 6, by + y * cell + 6, bx + (x + 1) * cell - 6, by + (y + 1) * cell - 6], 12, fill=col)
for ax, ay in [(1, 0), (3, 1), (5, 0), (2, 3), (4, 2)]:
    paste_icon(im, draw_apple(0, 0, 90), bx + ax * cell + cell // 2, by + ay * cell + cell // 2, 90)
for rx, ry in [(4, 3), (0, 2), (2, 2)]:
    rr(d, [bx + rx * cell + 30, by + ry * cell + 30, bx + rx * cell + cell - 30, by + ry * cell + cell - 30], 30, fill=GRAY, outline=TEXT, w=5)
paste_icon(im, draw_robot(0, 0, 130), bx + cell // 2, by + cell // 2 + 120, 130)
rr(d, [90, 1280, 940, 1360], 30, fill=(255, 255, 255, 235))
for i, col in enumerate([BLUE, RED, PURPLE, RED]):
    rr(d, [120 + i * 110, 1305, 210 + i * 110, 1365], 16, fill=col)
rr(d, [955, 1300, 1015, 1360], 14, fill=(255, 255, 255, 235))
rr(d, [90, 1400, 990, 1580], 40, fill=(255, 255, 255, 50))
btns = [(BLUE, "Kiri"), (RED, "Maju"), (PURPLE, "Kanan"), (ORANGE, "Petik")]
for i, (col, lb) in enumerate(btns):
    d.ellipse([110 + i * 200, 1430, 210 + i * 200, 1530], fill=col, outline=TEXT, width=5)
    txt(d, (160 + i * 200, 1550), lb, 26, WHITE)
rr(d, [90, 1610, 990, 1740], 40, fill=(255, 255, 255, 70))
d.ellipse([260, 1635, 380, 1715], fill=GREEN, outline=TEXT, width=5)
d.ellipse([500, 1635, 660, 1715], fill=GREEN_D, outline=TEXT, width=5)
d.ellipse([760, 1635, 840, 1715], fill=GRAY, outline=TEXT, width=5)
txt(d, (320, 1700), "Main!", 30, WHITE)
txt(d, (800, 1700), "Ulang", 26, WHITE)
ad_banner(d, W, 1750)
im.save(f"{OUT}/screen_3_fruit.png")
print("screen_3_fruit.png")

# ══════════════════ 5. SCREEN WIN (rating) ══════════════════
im, d = new_screen()
over = Image.new("RGBA", (W, H), (0, 0, 0, 110))
im.paste(over, (0, 0), over)
d = ImageDraw.Draw(im)
# confetti
for i in range(30):
    x = (i * 137) % W
    y = (i * 251) % 500
    cols = [RED, YELLOW, BLUE, GREEN, PURPLE, ORANGE]
    d.rectangle([x, y, x + 18, y + 10], fill=cols[i % 6])
rr(d, [190, 620, 890, 1380], 60, fill=WHITE)
txt(d, (540, 760), "🎉" if False else "LEVEL 5 SELESAI!", 72, TEXT)
for i in range(5):
    star(d, 360 + i * 90, 900, 34, YELLOW, TEXT, 4)
txt(d, (540, 1010), "Hebat! Hampir sempurna!", 44, TEXT, bold=False)
rr(d, [290, 1100, 790, 1230], 36, fill=GREEN)
txt(d, (540, 1165), "Level berikutnya  ▶", 40, WHITE)
rr(d, [290, 1260, 790, 1390], 36, fill=BLUE)
txt(d, (540, 1325), "Main lagi", 40, WHITE)
im.save(f"{OUT}/screen_4_win.png")
print("screen_4_win.png")

# ══════════════════ 6. SCREEN LOCK (parent gate / timer) ══════════════════
im = Image.new("RGB", (W, H), (11, 31, 15))
d = ImageDraw.Draw(im)
# timer bar atas
d.rectangle([0, 0, W, 14], fill=YELLOW)
txt(d, (540, 90), "Waktu bermain sudah habis, teman.", 56, WHITE)
txt(d, (540, 170), "Waktunya istirahat!", 64, WHITE, bold=True)
# lock icon
lx, ly = 540, 480
rr(d, [lx - 150, ly - 60, lx + 150, ly + 160], 50, fill=YELLOW, outline=TEXT, w=8)
d.arc([lx - 95, ly - 220, lx + 95, ly - 30], 180, 360, fill=YELLOW, width=24)
d.ellipse([lx - 25, ly + 30, lx + 25, ly + 80], fill=TEXT)
txt(d, (540, 780), "Minta tolong orang tua ya", 46, (255, 255, 255, 200), bold=False)
rr(d, [340, 880, 740, 1010], 40, fill=GREEN)
txt(d, (540, 945), "Main Lagi  ▶", 44, WHITE)
# chip keamanan bawah
secs = ["Kunci orang tua", "Kontrol waktu", "Tanpa iklan mengganggu"]
cx0 = 540
yy = 1250
for s in secs:
    wch = 80 + len(s) * 26
    rr(d, [cx0 - wch // 2, yy, cx0 + wch // 2, yy + 80], 40, fill=(255, 255, 255, 25))
    txt(d, (cx0, yy + 40), s, 30, WHITE, bold=False)
    cx0 += wch + 40
im.save(f"{OUT}/screen_5_lock.png")
print("screen_5_lock.png")
print("ALL DONE ->", OUT)
