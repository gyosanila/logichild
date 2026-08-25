#!/usr/bin/env python3
"""Fix icon: safe zone adaptive (gak kepotong), logo untuk splash/home, compress semua."""
from PIL import Image
import os

SRC = "/home/ilga/.hermes/cache/images/img_556666852a57.jpg"
ROOT = "/home/ilga/KartCilik"

im = Image.open(SRC).convert("RGB")

def save_compressed(img, path, size=None):
    """Resize + kompres PNG (quantize 256 + optimize) — gak bengkak."""
    if size:
        img = img.resize((size, size), Image.LANCZOS)
    img = img.quantize(colors=256, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.FLOYDSTEINBERG)
    img.save(path, optimize=True)
    print(f"{path}: {os.path.getsize(path)} bytes")

# ── Legacy mipmaps (full gambar, gak kena mask) ──
legacy = {48: "mdpi", 72: "hdpi", 96: "xhdpi", 144: "xxhdpi", 192: "xxxhdpi"}
for px, dens in legacy.items():
    p = f"{ROOT}/app/src/main/res/mipmap-{dens}"
    os.makedirs(p, exist_ok=True)
    save_compressed(im, f"{p}/ic_launcher.png", px)
    save_compressed(im, f"{p}/ic_launcher_round.png", px)

# ── Adaptive foreground: zoom-out ke safe zone 66% biar mask gak motong ──
CANVAS = 432
fg = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
content = int(CANVAS * 0.68)  # konten dalam safe zone
small = im.resize((content, content), Image.LANCZOS)
fg.paste(small, ((CANVAS - content) // 2, (CANVAS - content) // 2))
fg_rgb = fg.convert("RGB").quantize(colors=256, dither=Image.Dither.FLOYDSTEINBERG)
fg_rgb.save(f"{ROOT}/app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png", optimize=True)
print(f"foreground: {os.path.getsize(ROOT + '/app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png')} bytes")

# ── Logo untuk splash + home (drawable, kompres) ──
logo = f"{ROOT}/app/src/main/res/drawable-nodpi"
os.makedirs(logo, exist_ok=True)
save_compressed(im, f"{logo}/ic_app_logo.png", 512)

# preview
im.resize((512, 512), Image.LANCZOS).save(f"{ROOT}/icon_preview_512.png")
print("done")
