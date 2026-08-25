#!/usr/bin/env python3
"""Pasang ikon ChatGPT: legacy mipmaps + adaptive icon (bg warna sudut gambar)."""
from PIL import Image
import os

SRC = "/home/ilga/.hermes/cache/images/img_556666852a57.jpg"
ROOT = "/home/ilga/KartCilik"
os.makedirs(f"{ROOT}/app/src/main/res", exist_ok=True)

im = Image.open(SRC).convert("RGB")

# salin sumber ke project
im.save(f"{ROOT}/icon_source.png")

# ── Legacy mipmaps ──
legacy = {48: "mdpi", 72: "hdpi", 96: "xhdpi", 144: "xxhdpi", 192: "xxxhdpi"}
for px, dens in legacy.items():
    p = f"{ROOT}/app/src/main/res/mipmap-{dens}"
    os.makedirs(p, exist_ok=True)
    im.resize((px, px), Image.LANCZOS).save(f"{p}/ic_launcher.png")
    im.resize((px, px), Image.LANCZOS).save(f"{p}/ic_launcher_round.png")
    print("legacy", dens, px)

# ── Adaptive: foreground 432px + background warna sudut ──
im.resize((432, 432), Image.LANCZOS).save(
    f"{ROOT}/app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png")

def avg_color(box):
    px = im.crop(box).resize((1, 1), Image.LANCZOS).getpixel((0, 0))
    return px

tl = avg_color((0, 0, 100, 100))
br = avg_color((im.width - 100, im.height - 100, im.width, im.height))
print("tl", tl, "br", br)

with open(f"{ROOT}/app/src/main/res/drawable/ic_launcher_background.xml", "w") as f:
    f.write(f"""<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient
        android:startColor="#{tl[0]:02X}{tl[1]:02X}{tl[2]:02X}"
        android:endColor="#{br[0]:02X}{br[1]:02X}{br[2]:02X}"
        android:angle="270" />
</shape>
""")

anydpi = f"{ROOT}/app/src/main/res/mipmap-anydpi-v26"
os.makedirs(anydpi, exist_ok=True)
for name in ("ic_launcher", "ic_launcher_round"):
    with open(f"{anydpi}/{name}.xml", "w") as f:
        f.write(f"""<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
""")
print("adaptive written")

# preview kecil buat konfirmasi
im.resize((512, 512), Image.LANCZOS).save(f"{ROOT}/icon_preview_512.png")
print("preview saved")
