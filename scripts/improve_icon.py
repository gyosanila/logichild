#!/usr/bin/env python3
"""Improve icon ChatGPT → gaya 3-5 tahun: warna lebih hidup, frame kartun, decal lucu."""
from PIL import Image, ImageDraw, ImageEnhance
import colorsys

SRC = "/home/ilga/.hermes/cache/images/img_bc82c0c467ae.jpg"
OUT = "/home/ilga/KartCilik/icon_preview_512.png"
OUT_FULL = "/home/ilga/KartCilik/icon_preview.png"

im = Image.open(SRC).convert("RGB")
S = 1024
im = im.resize((S, S), Image.LANCZOS)

# ── 1. Warna lebih hidup (saturasi) + cerah + kontras + tajam ──
pix = im.load()
for y in range(S):
    for x in range(S):
        r, g, b = pix[x, y][:3]
        h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        s = min(1.0, s * 1.28)
        v = min(1.0, v * 1.10)
        r, g, b = colorsys.hsv_to_rgb(h, s, v)
        pix[x, y] = (int(r * 255), int(g * 255), int(b * 255))

im = ImageEnhance.Contrast(im).enhance(1.12)
im = ImageEnhance.Sharpness(im).enhance(1.35)

# ── 2. Frame kartun: stroke putih tebal + border warna cerah ──
canvas = Image.new("RGBA", (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(canvas)
# stroke putih tebal (efek sticker)
mask = Image.new("L", (S, S), 0)
dm = ImageDraw.Draw(mask)
dm.rounded_rectangle([30, 30, S - 30, S - 30], radius=170, fill=255)
canvas.paste(im, (0, 0), mask)
# border luar warna cerah (kuning-oranye gradient sederhana)
outline = Image.new("RGBA", (S, S), (0, 0, 0, 0))
do = ImageDraw.Draw(outline)
for i, col in enumerate([(255, 190, 60), (255, 150, 70), (90, 200, 120)]):
    w = 34 - i * 8
    do.rounded_rectangle([16 + i * 10, 16 + i * 10, S - 16 - i * 10, S - 16 - i * 10],
                         radius=190 - i * 10, outline=col, width=w)
canvas = Image.alpha_composite(outline, canvas)

# ── 3. Decal lucu: matahari, sparkle, awan di pojok ──
def sparkle(d, x, y, r, fill=(255, 255, 255, 235)):
    d.rounded_rectangle([x - r, y - r // 3, x + r, y + r // 3], radius=r // 3, fill=fill)
    d.rounded_rectangle([x - r // 3, y - r, x + r // 3, y + r], radius=r // 3, fill=fill)

def cloud(d, x, y, s, a):
    d.ellipse([x, y, x + 90 * s, y + 55 * s], fill=(255, 255, 255, a))
    d.ellipse([x - 40 * s, y + 18 * s, x + 50 * s, y + 70 * s], fill=(255, 255, 255, a))
    d.ellipse([x + 55 * s, y + 16 * s, x + 145 * s, y + 68 * s], fill=(255, 255, 255, a))

# matahari kecil di pojok kiri atas
d.ellipse([120, 120, 220, 220], fill=(255, 214, 64))
for ang in range(0, 360, 45):
    import math
    a = math.radians(ang)
    x0, y0 = 170 + 58 * math.cos(a), 170 + 58 * math.sin(a)
    x1, y1 = 170 + 82 * math.cos(a), 170 + 82 * math.sin(a)
    d.line([x0, y0, x1, y1], fill=(255, 214, 64), width=18)
d.ellipse([120, 120, 220, 220], fill=(255, 214, 64))
d.ellipse([146, 146, 168, 168], fill=(255, 255, 255, 140))
# awan putih di pojok kanan atas & kiri bawah
cloud(d, 760, 110, 0.9, 80)
cloud(d, 60, 720, 0.8, 75)
# sparkle
sparkle(d, 780, 660, 30)
sparkle(d, 250, 660, 24)
sparkle(d, 880, 420, 20)
sparkle(d, 150, 430, 18)

img_out = canvas.convert("RGB")
img_out.save(OUT_FULL)
img_out.resize((512, 512), Image.LANCZOS).save(OUT)
print("saved", OUT)
