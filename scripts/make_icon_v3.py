#!/usr/bin/env python3
"""Ikon Logichild v3 — bayi gamer chibi: popok + headset cat-ear + stik PS glossy."""
from PIL import Image, ImageDraw

S = 1024
img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

def lerp(a, b, t): return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

def glow_ellipse(x0, y0, x1, y1, color, alpha):
    """Ellips dengan highlight glossy (cahaya dari kiri-atas)."""
    d.ellipse([x0, y0, x1, y1], fill=color)
    w, h = x1 - x0, y1 - y0
    d.ellipse([x0 + w * 0.14, y0 + h * 0.10, x1 - w * 0.42, y1 - h * 0.45],
              fill=(255, 255, 255, int(alpha * 0.35)))

def sparkle(x, y, r, fill=(255, 255, 255, 220)):
    d.rounded_rectangle([x - r, y - r // 3, x + r, y + r // 3], radius=r // 3, fill=fill)
    d.rounded_rectangle([x - r // 3, y - r, x + r // 3, y + r], radius=r // 3, fill=fill)

# ── Background: gradien + glow + awan imut ──
bg = Image.new("RGBA", (S, S), (0, 0, 0, 0))
db = ImageDraw.Draw(bg)
top, mid, bottom = (86, 205, 250), (122, 214, 190), (150, 222, 140)
for y in range(S):
    t = y / S
    col = lerp(top, mid, t * 2) if t < 0.5 else lerp(mid, bottom, t * 2 - 1)
    db.line([(0, y), (S, y)], fill=col)
mask = Image.new("L", (S, S), 0)
dm = ImageDraw.Draw(mask)
dm.rounded_rectangle([16, 16, S - 16, S - 16], radius=210, fill=255)
img.paste(bg, (0, 0), mask)

# glow lembut di belakang bayi
d.ellipse([112, 130, 912, 850], fill=(255, 255, 255, 55))
d.ellipse([262, 280, 762, 700], fill=(255, 255, 255, 45))
# awan imut di pojok
def cloud(x, y, s, a):
    d.ellipse([x, y, x + 90 * s, y + 55 * s], fill=(255, 255, 255, a))
    d.ellipse([x - 40 * s, y + 18 * s, x + 50 * s, y + 70 * s], fill=(255, 255, 255, a))
    d.ellipse([x + 55 * s, y + 16 * s, x + 145 * s, y + 68 * s], fill=(255, 255, 255, a))
cloud(60, 120, 1.1, 42)
cloud(720, 80, 0.9, 38)
cloud(780, 640, 0.8, 34)
cloud(90, 700, 0.9, 36)

SKIN = (255, 219, 186)
SKIN_D = (233, 170, 125)
HAIR = (90, 62, 52)
EYE = (58, 42, 38)
BLUSH = (255, 145, 158, 190)
MOUTH = (180, 88, 88)
TONGUE = (255, 140, 130)
HP = (72, 108, 178)      # headset biru
HP_D = (52, 82, 144)
HP_IN = (255, 214, 224)  # dalam ear cup pink
PAD = (52, 66, 80)
PAD_D = (32, 42, 52)

# ── Bayangan lembut di bawah ──
d.ellipse([300, 858, 724, 950], fill=(20, 40, 60, 60))

cx, cy = 512, 350
R = 232

# ── Kepala ──
d.ellipse([cx - R, cy - R, cx + R, cy + R], fill=SKIN, outline=SKIN_D, width=14)

# ── Headset cat-ear ──
band_y = cy - R + 30
# band melengkung di atas kepala
d.arc([cx - R - 40, cy - R - 70, cx + R + 40, cy + R - 40], 185, 355, fill=HP, width=52)
d.arc([cx - R - 40, cy - R - 70, cx + R + 40, cy + R - 40], 185, 355, fill=(255, 255, 255, 40), width=16)
# ear cup kiri & kanan
for ex in (cx - R - 12, cx + R - 12):
    d.rounded_rectangle([ex - 34, cy - 58, ex + 34, cy + 62], radius=34, fill=HP, outline=HP_D, width=8)
    d.rounded_rectangle([ex - 18, cy - 42, ex + 18, cy + 46], radius=18, fill=HP_IN)
# telinga kucing di atas band
def cat_ear(ex, y0):
    d.polygon([(ex - 52, y0 + 26), (ex - 8, y0 - 58), (ex + 44, y0 + 26)],
              fill=HP, outline=HP_D, width=8)
    d.polygon([(ex - 26, y0 + 8), (ex - 4, y0 - 28), (ex + 18, y0 + 8)], fill=(255, 170, 185))
cat_ear(cx - 118, cy - R - 8)
cat_ear(cx + 118, cy - R - 8)

# ── Rambut depan (poni) + jambul ──
d.arc([cx - R + 30, cy - R + 40, cx + R - 30, cy + R - 10], 200, 340, fill=HAIR, width=40)
d.ellipse([cx - 24, cy - R - 38, cx + 24, cy - R + 26], fill=HAIR)

# ── Mata gede glossy ──
for ex in (cx - 88, cx + 88):
    d.ellipse([ex - 44, cy + 16, ex + 44, cy + 120], fill=EYE)
    d.ellipse([ex - 26, cy + 32, ex - 2, cy + 56], fill=(255, 255, 255, 255))
    d.ellipse([ex + 10, cy + 60, ex + 22, cy + 72], fill=(255, 255, 255, 200))
    d.ellipse([ex + 18, cy + 78, ex + 26, cy + 86], fill=(120, 200, 255, 190))
# alis imut
d.arc([cx - 138, cy + 6, cx - 40, cy + 40], 200, 340, fill=HAIR, width=12)
d.arc([cx + 40, cy + 6, cx + 138, cy + 40], 200, 340, fill=HAIR, width=12)
# pipi merona + highlight
d.ellipse([cx - 208, cy + 98, cx - 92, cy + 204], fill=BLUSH)
d.ellipse([cx - 196, cy + 108, cx - 166, cy + 138], fill=(255, 255, 255, 70))
d.ellipse([cx + 92, cy + 98, cx + 208, cy + 204], fill=BLUSH)
d.ellipse([cx + 166, cy + 108, cx + 196, cy + 138], fill=(255, 255, 255, 70))
# hidung
d.ellipse([cx - 10, cy + 132, cx + 10, cy + 150], fill=(233, 170, 125, 170))
# mulut senyum mangap + lidah
d.ellipse([cx - 48, cy + 152, cx + 48, cy + 228], fill=MOUTH)
d.ellipse([cx - 28, cy + 194, cx + 28, cy + 230], fill=TONGUE)
d.arc([cx - 48, cy + 152, cx + 48, cy + 228], 200, 340, fill=(120, 52, 52), width=8)

# ── Badan, popok, kaki kaos kaki ──
d.rounded_rectangle([398, 578, 626, 700], radius=50, fill=SKIN, outline=SKIN_D, width=10)
d.rounded_rectangle([374, 618, 650, 798], radius=75, fill=(255, 255, 255), outline=(150, 205, 240), width=10)
d.rounded_rectangle([394, 658, 630, 793], radius=60, fill=(190, 228, 250))
d.rounded_rectangle([414, 683, 610, 773], radius=45, fill=(255, 255, 255))
for px in range(447, 597, 42):
    for py in (698, 740):
        d.ellipse([px - 8, py - 8, px + 8, py + 8], fill=(120, 190, 230, 190))
# kaki + kaos kaki belang
d.ellipse([416, 766, 510, 874], fill=SKIN, outline=SKIN_D, width=10)
d.ellipse([514, 766, 608, 874], fill=SKIN, outline=SKIN_D, width=10)
for sx, exx in ((424, 502), (522, 600)):
    d.rounded_rectangle([sx, 780, exx, 860], radius=26, fill=(255, 255, 255), outline=(200, 210, 220), width=6)
    d.rounded_rectangle([sx, 804, exx, 824], radius=10, fill=(120, 210, 240))
for tx, ty in ((440, 850), (460, 854), (538, 854), (558, 850)):
    d.ellipse([tx - 6, ty - 6, tx + 6, ty + 6], fill=SKIN_D)

# ── Lengan pegang stik ──
d.line([418, 640, 320, 708], fill=SKIN, width=52)
d.line([606, 640, 704, 708], fill=SKIN, width=52)
d.ellipse([288, 680, 354, 746], fill=SKIN, outline=SKIN_D, width=8)
d.ellipse([670, 680, 736, 746], fill=SKIN, outline=SKIN_D, width=8)

# ── Stik PS glossy ──
d.rounded_rectangle([316, 698, 708, 850], radius=74, fill=PAD, outline=PAD_D, width=12)
d.rounded_rectangle([316, 698, 708, 764], radius=74, fill=PAD)
# light bar biru di atas (vibe PS4)
d.rounded_rectangle([360, 706, 664, 722], radius=8, fill=(70, 190, 255))
d.ellipse([380, 700, 430, 724], fill=(160, 230, 255, 120))
# grip
d.rounded_rectangle([304, 732, 372, 874], radius=44, fill=PAD, outline=PAD_D, width=8)
d.rounded_rectangle([652, 732, 720, 874], radius=44, fill=PAD, outline=PAD_D, width=8)
# d-pad mengkilap
d.rounded_rectangle([382, 752, 432, 822], radius=14, fill=(150, 164, 176))
d.rounded_rectangle([358, 776, 456, 798], radius=14, fill=(150, 164, 176))
d.rounded_rectangle([386, 756, 428, 818], radius=10, fill=(180, 192, 202))
# tombol tengah
d.ellipse([502, 756, 552, 806], fill=(138, 150, 160))
d.ellipse([490, 780, 564, 820], fill=(220, 100, 100))
# 4 tombol warna + kilau
bx, byy = 606, 784
d.ellipse([bx - 28, byy - 28, bx + 28, byy + 28], fill=(244, 67, 54))
d.ellipse([bx - 64, byy - 6, bx - 8, byy + 50], fill=(255, 213, 79))
d.ellipse([bx + 8, byy - 6, bx + 64, byy + 50], fill=(33, 150, 243))
d.ellipse([bx - 28, byy + 26, bx + 28, byy + 82], fill=(76, 175, 80))
for off in ((-20, -20, -4, -4), (-52, -2, -36, 14), (20, -2, 36, 14), (-20, 34, -4, 50)):
    d.ellipse([bx + off[0], byy + off[1], bx + off[2], byy + off[3]], fill=(255, 255, 255, 110))
# highlight besar di badan stik
d.rounded_rectangle([344, 720, 470, 752], radius=14, fill=(255, 255, 255, 60))

# ── Sparkle ──
sparkle(200, 250, 28)
sparkle(830, 210, 36)
sparkle(140, 560, 22)
sparkle(880, 520, 24)
sparkle(512, 84, 20)
d.ellipse([240, 700, 268, 728], fill=(255, 255, 255, 170))
d.ellipse([790, 350, 812, 372], fill=(255, 255, 255, 150))

out = "/home/ilga/KartCilik/icon_preview.png"
img.save(out)
img.resize((512, 512), Image.LANCZOS).save("/home/ilga/KartCilik/icon_preview_512.png")
print("saved", out)
