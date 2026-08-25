#!/usr/bin/env python3
"""Generate semua aset ikon Logichild: preview, legacy mipmaps, adaptive foreground."""
from PIL import Image, ImageDraw
import os

S = 1024
ROOT = "/home/ilga/KartCilik"

def lerp(a, b, t): return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

SKIN = (255, 217, 184)
SKIN_D = (232, 168, 122)
HAIR = (82, 58, 48)
EYE = (62, 45, 40)
BLUSH = (255, 148, 160, 175)
MOUTH = (178, 84, 84)
TONGUE = (255, 138, 128)

def sparkle(d, x, y, r, fill=(255, 255, 255, 200)):
    d.rounded_rectangle([x - r, y - r // 3, x + r, y + r // 3], radius=r // 3, fill=fill)
    d.rounded_rectangle([x - r // 3, y - r, x + r // 3, y + r], radius=r // 3, fill=fill)

def draw_scene(img, d, with_bg=True, with_mask=True):
    if with_bg:
        bg = Image.new("RGBA", (S, S), (0, 0, 0, 0))
        db = ImageDraw.Draw(bg)
        top, bottom = (93, 203, 245), (139, 214, 140)
        for y in range(S):
            db.line([(0, y), (S, y)], fill=lerp(top, bottom, y / S))
        if with_mask:
            mask = Image.new("L", (S, S), 0)
            dm = ImageDraw.Draw(mask)
            dm.rounded_rectangle([16, 16, S - 16, S - 16], radius=210, fill=255)
            img.paste(bg, (0, 0), mask)
        else:
            img.paste(bg, (0, 0))
        d.ellipse([-250, -320, S + 250, 260], fill=(255, 255, 255, 45))

    cx, cy = 512, 350
    R = 235
    d.ellipse([cx - R, cy - R, cx + R, cy + R], fill=SKIN, outline=SKIN_D, width=14)
    d.ellipse([cx - R - 28, cy - 60, cx - R + 42, cy + 45], fill=SKIN, outline=SKIN_D, width=10)
    d.ellipse([cx + R - 42, cy - 60, cx + R + 28, cy + 45], fill=SKIN, outline=SKIN_D, width=10)
    d.pieslice([cx - R, cy - R - 6, cx + R, cy + R], 180, 360, fill=HAIR)
    d.ellipse([cx - 26, cy - R - 30, cx + 26, cy - R + 30], fill=HAIR)
    d.arc([cx - R + 20, cy - R + 10, cx + R - 20, cy + R * 0.55], 190, 350, fill=HAIR, width=34)

    for ex in (cx - 85, cx + 85):
        d.ellipse([ex - 42, cy + 18, ex + 42, cy + 118], fill=EYE)
        d.ellipse([ex - 24, cy + 34, ex - 2, cy + 56], fill=(255, 255, 255, 240))
        d.ellipse([ex + 8, cy + 62, ex + 20, cy + 74], fill=(255, 255, 255, 170))
    d.ellipse([cx - 200, cy + 100, cx - 96, cy + 200], fill=BLUSH)
    d.ellipse([cx + 96, cy + 100, cx + 200, cy + 200], fill=BLUSH)
    d.ellipse([cx - 10, cy + 130, cx + 10, cy + 148], fill=(232, 168, 122, 160))
    d.ellipse([cx - 46, cy + 150, cx + 46, cy + 224], fill=MOUTH)
    d.ellipse([cx - 26, cy + 192, cx + 26, cy + 226], fill=TONGUE)
    d.arc([cx - 46, cy + 150, cx + 46, cy + 224], 200, 340, fill=(120, 50, 50), width=8)

    d.rounded_rectangle([396, 580, 628, 700], radius=50, fill=SKIN, outline=SKIN_D, width=10)
    d.rounded_rectangle([372, 620, 652, 800], radius=75, fill=(255, 255, 255), outline=(150, 205, 240), width=10)
    d.rounded_rectangle([392, 660, 632, 795], radius=60, fill=(190, 228, 250))
    d.rounded_rectangle([412, 685, 612, 775], radius=45, fill=(255, 255, 255))
    for px in range(445, 595, 42):
        for py in (700, 742):
            d.ellipse([px - 8, py - 8, px + 8, py + 8], fill=(120, 190, 230, 180))
    d.ellipse([418, 770, 508, 872], fill=SKIN, outline=SKIN_D, width=10)
    d.ellipse([516, 770, 606, 872], fill=SKIN, outline=SKIN_D, width=10)
    for tx, ty in ((442, 852), (462, 856), (540, 856), (560, 852)):
        d.ellipse([tx - 6, ty - 6, tx + 6, ty + 6], fill=SKIN_D)

    d.line([420, 640, 322, 706], fill=SKIN, width=52)
    d.line([604, 640, 702, 706], fill=SKIN, width=52)
    d.ellipse([290, 678, 356, 744], fill=SKIN, outline=SKIN_D, width=8)
    d.ellipse([668, 678, 734, 744], fill=SKIN, outline=SKIN_D, width=8)

    d.rounded_rectangle([318, 700, 706, 848], radius=72, fill=(58, 71, 82), outline=(30, 40, 46), width=12)
    d.rounded_rectangle([306, 734, 372, 872], radius=42, fill=(58, 71, 82))
    d.rounded_rectangle([652, 734, 718, 872], radius=42, fill=(58, 71, 82))
    d.rounded_rectangle([380, 750, 430, 820], radius=14, fill=(145, 158, 170))
    d.rounded_rectangle([356, 774, 454, 796], radius=14, fill=(145, 158, 170))
    d.ellipse([500, 754, 552, 806], fill=(130, 142, 152))
    d.ellipse([488, 778, 564, 820], fill=(214, 96, 96))
    bx, byy = 604, 782
    d.ellipse([bx - 28, byy - 28, bx + 28, byy + 28], fill=(244, 67, 54))
    d.ellipse([bx - 64, byy - 6, bx - 8, byy + 50], fill=(255, 213, 79))
    d.ellipse([bx + 8, byy - 6, bx + 64, byy + 50], fill=(33, 150, 243))
    d.ellipse([bx - 28, byy + 26, bx + 28, byy + 82], fill=(76, 175, 80))
    d.ellipse([bx - 20, byy - 20, bx - 4, byy - 4], fill=(255, 255, 255, 100))
    d.ellipse([bx + 34, byy + 52, bx + 50, byy + 68], fill=(255, 255, 255, 80))

    if with_bg:
        sparkle(d, 190, 240, 26)
        sparkle(d, 840, 200, 34)
        sparkle(d, 130, 560, 20)
        sparkle(d, 880, 520, 22)
        sparkle(d, 512, 90, 18)

# ── 1. Preview + legacy mipmaps (full, rounded) ──
img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
draw_scene(img, ImageDraw.Draw(img), with_bg=True, with_mask=True)
img.save(f"{ROOT}/icon_preview.png")
img.resize((512, 512), Image.LANCZOS).save(f"{ROOT}/icon_preview_512.png")

legacy = {48: "mdpi", 72: "hdpi", 96: "xhdpi", 144: "xxhdpi", 192: "xxxhdpi"}
for px, dens in legacy.items():
    p = f"{ROOT}/app/src/main/res/mipmap-{dens}"
    os.makedirs(p, exist_ok=True)
    img.resize((px, px), Image.LANCZOS).save(f"{p}/ic_launcher.png")
    img.resize((px, px), Image.LANCZOS).save(f"{p}/ic_launcher_round.png")
    print("legacy", dens, px)

# ── 2. Adaptive foreground (transparan, dalam safe zone) ──
fg = Image.new("RGBA", (S, S), (0, 0, 0, 0))
draw_scene(fg, ImageDraw.Draw(fg), with_bg=False, with_mask=False)
# zoom out biar muat di safe zone 66%
small = fg.resize((int(S * 0.72), int(S * 0.72)), Image.LANCZOS)
fg2 = Image.new("RGBA", (S, S), (0, 0, 0, 0))
fg2.paste(small, ((S - small.width) // 2, (S - small.height) // 2 + 30), small)
fg2.resize((432, 432), Image.LANCZOS).save(f"{ROOT}/app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png")
print("foreground saved (432px)")

# ── 3. Adaptive background (gradien biru→hijau) ──
bg_xml = f"{ROOT}/app/src/main/res/drawable/ic_launcher_background.xml"
os.makedirs(os.path.dirname(bg_xml), exist_ok=True)
with open(bg_xml, "w") as f:
    f.write("""<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient
        android:startColor="#5DCBF5"
        android:endColor="#8BD68C"
        android:angle="270" />
</shape>
""")

# ── 4. Adaptive icon XML ──
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
print("adaptive icons written")
