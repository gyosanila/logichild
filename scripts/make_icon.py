#!/usr/bin/env python3
"""Ikon Logichild v2 — chibi: bayi popok pegang stik PS, lebih lucu."""
from PIL import Image, ImageDraw
import math

S = 1024
img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

def lerp(a, b, t): return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

# ── Background rounded square, gradien biru muda → hijau ──
bg = Image.new("RGBA", (S, S), (0, 0, 0, 0))
db = ImageDraw.Draw(bg)
top, bottom = (93, 203, 245), (139, 214, 140)
for y in range(S):
    db.line([(0, y), (S, y)], fill=lerp(top, bottom, y / S))
mask = Image.new("L", (S, S), 0)
dm = ImageDraw.Draw(mask)
dm.rounded_rectangle([16, 16, S - 16, S - 16], radius=210, fill=255)
img.paste(bg, (0, 0), mask)
d.ellipse([-250, -320, S + 250, 260], fill=(255, 255, 255, 45))

SKIN = (255, 217, 184)
SKIN_D = (232, 168, 122)
HAIR = (82, 58, 48)
EYE = (62, 45, 40)
BLUSH = (255, 148, 160, 175)
MOUTH = (178, 84, 84)
TONGUE = (255, 138, 128)

def sparkle(x, y, r, fill=(255, 255, 255, 200)):
    d.rounded_rectangle([x - r, y - r // 3, x + r, y + r // 3], radius=r // 3, fill=fill)
    d.rounded_rectangle([x - r // 3, y - r, x + r // 3, y + r], radius=r // 3, fill=fill)

# ── Kepala GEDE (chibi) ──
cx, cy = 512, 350
R = 235
d.ellipse([cx - R, cy - R, cx + R, cy + R], fill=SKIN, outline=SKIN_D, width=14)
# telinga
d.ellipse([cx - R - 28, cy - 60, cx - R + 42, cy + 45], fill=SKIN, outline=SKIN_D, width=10)
d.ellipse([cx + R - 42, cy - 60, cx + R + 28, cy + 45], fill=SKIN, outline=SKIN_D, width=10)
# rambut: tutup kepala + jambul ala bayi
d.pieslice([cx - R, cy - R - 6, cx + R, cy + R], 180, 360, fill=HAIR)
d.ellipse([cx - 26, cy - R - 30, cx + 26, cy - R + 30], fill=HAIR)   # jambul
d.arc([cx - R + 20, cy - R + 10, cx + R - 20, cy + R * 0.55], 190, 350, fill=HAIR, width=34)

# ── Mata GEDE + kilau ──
for ex in (cx - 85, cx + 85):
    d.ellipse([ex - 42, cy + 18, ex + 42, cy + 118], fill=EYE)
    d.ellipse([ex - 24, cy + 34, ex - 2, cy + 56], fill=(255, 255, 255, 240))   # kilau besar
    d.ellipse([ex + 8, cy + 62, ex + 20, cy + 74], fill=(255, 255, 255, 170))   # kilau kecil
# pipi merona gede
d.ellipse([cx - 200, cy + 100, cx - 96, cy + 200], fill=BLUSH)
d.ellipse([cx + 96, cy + 100, cx + 200, cy + 200], fill=BLUSH)
# hidung imut
d.ellipse([cx - 10, cy + 130, cx + 10, cy + 148], fill=(232, 168, 122, 160))
# mulut senyum mangap (lucu!)
d.ellipse([cx - 46, cy + 150, cx + 46, cy + 224], fill=MOUTH)
d.ellipse([cx - 26, cy + 192, cx + 26, cy + 226], fill=TONGUE)
d.arc([cx - 46, cy + 150, cx + 46, cy + 224], 200, 340, fill=(120, 50, 50), width=8)

# ── Badan mungil & popok ──
d.rounded_rectangle([396, 580, 628, 700], radius=50, fill=SKIN, outline=SKIN_D, width=10)
# popok putih + garis biru + pola titik
d.rounded_rectangle([372, 620, 652, 800], radius=75, fill=(255, 255, 255), outline=(150, 205, 240), width=10)
d.rounded_rectangle([392, 660, 632, 795], radius=60, fill=(190, 228, 250))
d.rounded_rectangle([412, 685, 612, 775], radius=45, fill=(255, 255, 255))
for px in range(445, 595, 42):
    for py in (700, 742):
        d.ellipse([px - 8, py - 8, px + 8, py + 8], fill=(120, 190, 230, 180))
# kaki chubby menjulur
d.ellipse([418, 770, 508, 872], fill=SKIN, outline=SKIN_D, width=10)
d.ellipse([516, 770, 606, 872], fill=SKIN, outline=SKIN_D, width=10)
for tx, ty in ((442, 852), (462, 856), (540, 856), (560, 852)):
    d.ellipse([tx - 6, ty - 6, tx + 6, ty + 6], fill=SKIN_D)

# ── Lengan pegang stik ──
d.line([420, 640, 322, 706], fill=SKIN, width=52)
d.line([604, 640, 702, 706], fill=SKIN, width=52)
d.ellipse([290, 678, 356, 744], fill=SKIN, outline=SKIN_D, width=8)
d.ellipse([668, 678, 734, 744], fill=SKIN, outline=SKIN_D, width=8)

# ── Stik PS ──
d.rounded_rectangle([318, 700, 706, 848], radius=72, fill=(58, 71, 82), outline=(30, 40, 46), width=12)
d.rounded_rectangle([306, 734, 372, 872], radius=42, fill=(58, 71, 82))
d.rounded_rectangle([652, 734, 718, 872], radius=42, fill=(58, 71, 82))
# d-pad
d.rounded_rectangle([380, 750, 430, 820], radius=14, fill=(145, 158, 170))
d.rounded_rectangle([356, 774, 454, 796], radius=14, fill=(145, 158, 170))
# tombol tengah
d.ellipse([500, 754, 552, 806], fill=(130, 142, 152))
d.ellipse([488, 778, 564, 820], fill=(214, 96, 96))
# 4 tombol warna
bx, byy = 604, 782
d.ellipse([bx - 28, byy - 28, bx + 28, byy + 28], fill=(244, 67, 54))
d.ellipse([bx - 64, byy - 6, bx - 8, byy + 50], fill=(255, 213, 79))
d.ellipse([bx + 8, byy - 6, bx + 64, byy + 50], fill=(33, 150, 243))
d.ellipse([bx - 28, byy + 26, bx + 28, byy + 82], fill=(76, 175, 80))
d.ellipse([bx - 20, byy - 20, bx - 4, byy - 4], fill=(255, 255, 255, 100))
d.ellipse([bx + 34, byy + 52, bx + 50, byy + 68], fill=(255, 255, 255, 80))

# ── Sparkle biar makin imut ──
sparkle(190, 240, 26)
sparkle(840, 200, 34)
sparkle(130, 560, 20)
sparkle(880, 520, 22)
sparkle(512, 90, 18)

out = "/home/ilga/KartCilik/icon_preview.png"
img.save(out)
img.resize((512, 512), Image.LANCZOS).save("/home/ilga/KartCilik/icon_preview_512.png")
print("saved", out)
