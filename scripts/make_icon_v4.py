#!/usr/bin/env python3
"""Ikon Logichild v4 — gaya kartun balita (3-5 th): outline tebal, warna cerah, mata gede."""
from PIL import Image, ImageDraw

S = 1024
img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

def lerp(a, b, t): return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

SKIN = (255, 224, 179)
SKIN_D = (240, 190, 130)
LINE = (60, 60, 70)          # outline tebal ala kartun
HAIR = (101, 67, 52)
EYE = (50, 40, 45)
WHITE = (255, 255, 255)

def sparkle(x, y, r, fill=(255, 255, 255, 230)):
    d.rounded_rectangle([x - r, y - r // 3, x + r, y + r // 3], radius=r // 3, fill=fill)
    d.rounded_rectangle([x - r // 3, y - r, x + r // 3, y + r], radius=r // 3, fill=fill)

# ── Background: gradien cerah + matahari + awan ──
bg = Image.new("RGBA", (S, S), (0, 0, 0, 0))
db = ImageDraw.Draw(bg)
top, mid, bottom = (64, 214, 255), (96, 222, 200), (168, 232, 130)
for y in range(S):
    t = y / S
    col = lerp(top, mid, t * 2) if t < 0.5 else lerp(mid, bottom, t * 2 - 1)
    db.line([(0, y), (S, y)], fill=col)
mask = Image.new("L", (S, S), 0)
dm = ImageDraw.Draw(mask)
dm.rounded_rectangle([16, 16, S - 16, S - 16], radius=210, fill=255)
img.paste(bg, (0, 0), mask)

# matahari (pojok kiri atas)
d.ellipse([70, 70, 260, 260], fill=(255, 219, 77))
for ang in range(0, 360, 45):
    import math
    a = math.radians(ang)
    x0, y0 = 165 + 105 * math.cos(a), 165 + 105 * math.sin(a)
    x1, y1 = 165 + 140 * math.cos(a), 165 + 140 * math.sin(a)
    d.line([x0, y0, x1, y1], fill=(255, 219, 77), width=26)
d.ellipse([70, 70, 260, 260], fill=(255, 219, 77))
d.ellipse([110, 105, 150, 145], fill=(255, 255, 255, 120))

def cloud(x, y, s, a):
    d.ellipse([x, y, x + 90 * s, y + 55 * s], fill=(255, 255, 255, a))
    d.ellipse([x - 40 * s, y + 18 * s, x + 50 * s, y + 70 * s], fill=(255, 255, 255, a))
    d.ellipse([x + 55 * s, y + 16 * s, x + 145 * s, y + 68 * s], fill=(255, 255, 255, a))
cloud(770, 110, 1.0, 60)
cloud(90, 680, 0.9, 55)
cloud(700, 640, 0.8, 50)

# glow di belakang karakter
d.ellipse([180, 190, 844, 800], fill=(255, 255, 255, 40))

cx, cy = 512, 380
R = 218

def outline_ellipse(box, fill, width=16):
    d.ellipse(box, fill=fill, outline=LINE, width=width)

# ── Kepala (bulat banget, outline tebal) ──
outline_ellipse([cx - R, cy - R, cx + R, cy + R], SKIN, 18)
# telinga
outline_ellipse([cx - R - 26, cy - 66, cx - R + 46, cy + 40], SKIN, 14)
outline_ellipse([cx + R - 46, cy - 66, cx + R + 26, cy + 40], SKIN, 14)
# rambut (topi rambut + jambul)
d.pieslice([cx - R, cy - R - 4, cx + R, cy + R], 180, 360, fill=HAIR, outline=LINE, width=16)
d.ellipse([cx - 30, cy - R - 34, cx + 30, cy - R + 28], fill=HAIR, outline=LINE, width=12)

# ── Mata gede ala balita: putih + pupil gede + kilau ──
for ex in (cx - 88, cx + 88):
    outline_ellipse([ex - 58, cy + 8, ex + 58, cy + 128], WHITE, 12)
    d.ellipse([ex - 34, cy + 36, ex + 34, cy + 104], fill=EYE)
    d.ellipse([ex - 20, cy + 50, ex + 4, cy + 74], fill=WHITE)
    d.ellipse([ex + 16, cy + 78, ex + 26, cy + 88], fill=(255, 255, 255, 200))
# alis
d.arc([cx - 168, cy - 14, cx - 30, cy + 30], 200, 340, fill=LINE, width=16)
d.arc([cx + 30, cy - 14, cx + 168, cy + 30], 200, 340, fill=LINE, width=16)
# pipi
d.ellipse([cx - 216, cy + 108, cx - 104, cy + 216], fill=(255, 138, 150, 200))
d.ellipse([cx + 104, cy + 108, cx + 216, cy + 216], fill=(255, 138, 150, 200))
# hidung kecil
d.ellipse([cx - 12, cy + 132, cx + 12, cy + 152], fill=SKIN_D)
# mulut lebar + gigi + lidah
d.pieslice([cx - 70, cy + 130, cx + 70, cy + 250], 20, 160, fill=(150, 60, 60), outline=LINE, width=12)
d.rectangle([cx - 52, cy + 138, cx + 52, cy + 168], fill=WHITE)
d.rectangle([cx - 52, cy + 138, cx - 4, cy + 168], outline=LINE, width=6)
d.rectangle([cx + 4, cy + 138, cx + 52, cy + 168], outline=LINE, width=6)
d.ellipse([cx - 34, cy + 196, cx + 34, cy + 246], fill=(255, 130, 120))

# ── Badan + popok ──
d.rounded_rectangle([392, 574, 632, 706], radius=52, fill=SKIN, outline=LINE, width=14)
d.rounded_rectangle([366, 616, 658, 802], radius=78, fill=WHITE, outline=LINE, width=14)
d.rounded_rectangle([390, 658, 634, 796], radius=60, fill=(120, 205, 245))
d.rounded_rectangle([412, 686, 612, 776], radius=46, fill=WHITE)
for px in range(448, 596, 40):
    for py in (702, 742):
        d.ellipse([px - 9, py - 9, px + 9, py + 9], fill=(90, 180, 230))
# kaki chubby + sepatu kaus
outline_ellipse([412, 766, 512, 878], SKIN, 12)
outline_ellipse([512, 766, 612, 878], SKIN, 12)
d.rounded_rectangle([418, 792, 506, 872], radius=30, fill=(255, 214, 79), outline=LINE, width=10)
d.rounded_rectangle([518, 792, 606, 872], radius=30, fill=(255, 214, 79), outline=LINE, width=10)
d.rounded_rectangle([418, 828, 506, 848], radius=10, fill=WHITE)
d.rounded_rectangle([518, 828, 606, 848], radius=10, fill=WHITE)

# ── Lengan pegang gamepad ──
d.line([424, 636, 322, 712], fill=SKIN, width=56)
d.line([600, 636, 702, 712], fill=SKIN, width=56)
outline_ellipse([286, 678, 358, 750], SKIN, 10)
outline_ellipse([666, 678, 738, 750], SKIN, 10)

# ── Gamepad chunky warna-warni ──
d.rounded_rectangle([306, 700, 718, 858], radius=80, fill=(244, 96, 84), outline=LINE, width=16)
d.rounded_rectangle([306, 700, 718, 790], radius=80, fill=(252, 122, 100))
d.rounded_rectangle([292, 740, 366, 884], radius=48, fill=(244, 96, 84), outline=LINE, width=12)
d.rounded_rectangle([658, 740, 732, 884], radius=48, fill=(244, 96, 84), outline=LINE, width=12)
# d-pad abu
d.rounded_rectangle([376, 756, 430, 826], radius=16, fill=(120, 130, 145), outline=LINE, width=10)
d.rounded_rectangle([352, 780, 454, 802], radius=16, fill=(120, 130, 145), outline=LINE, width=10)
# tombol tengah
d.ellipse([498, 760, 552, 814], fill=(255, 255, 255), outline=LINE, width=10)
d.ellipse([486, 786, 564, 826], fill=(120, 200, 255), outline=LINE, width=8)
# 4 tombol warna + kilau
bx, byy = 606, 790
d.ellipse([bx - 32, byy - 32, bx + 32, byy + 32], fill=(255, 235, 59), outline=LINE, width=10)
d.ellipse([bx - 70, byy - 8, bx - 8, byy + 56], fill=(255, 92, 92), outline=LINE, width=10)
d.ellipse([bx + 8, byy - 8, bx + 70, byy + 56], fill=(64, 190, 120), outline=LINE, width=10)
d.ellipse([bx - 32, byy + 30, bx + 32, byy + 94], fill=(80, 160, 255), outline=LINE, width=10)
for off in ((-22, -22, -6, -6), (-56, -2, -40, 14), (22, -2, 38, 14), (-22, 40, -6, 56)):
    d.ellipse([bx + off[0], byy + off[1], bx + off[2], byy + off[3]], fill=(255, 255, 255, 130))
# highlight badan gamepad
d.rounded_rectangle([336, 716, 480, 756], radius=18, fill=(255, 255, 255, 90))

# ── Sparkle ──
sparkle(230, 300, 26)
sparkle(810, 240, 30)
sparkle(150, 520, 20)
sparkle(870, 470, 22)
sparkle(512, 70, 18)

out = "/home/ilga/KartCilik/icon_preview.png"
img.save(out)
img.resize((512, 512), Image.LANCZOS).save("/home/ilga/KartCilik/icon_preview_512.png")
print("saved", out)
