#!/usr/bin/env bash
set -euo pipefail

PKG=com.gyosanila.logichild
OUT_DIR="$GITHUB_WORKSPACE/ui-screenshots"
mkdir -p "$OUT_DIR"

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm clear "$PKG" >/dev/null
adb shell settings put system font_scale 1.0
adb shell am force-stop "$PKG"
adb shell am start -n "$PKG/.MainActivity"
# Allow the splash and first Compose frame to settle on the virtual device.
sleep 12
adb exec-out screencap -p > "$OUT_DIR/home.png"

# The primary Adventure card is the first large CTA below the header. Tap its
# center using the emulator's actual resolution (Pixel 2 profile, portrait).
read -r WIDTH HEIGHT < <(adb shell wm size | python3 -c 'import re,sys; s=sys.stdin.read(); m=re.findall(r"(\d+)x(\d+)",s); print(*map(int,m[-1]))')
X=$((WIDTH / 2))
Y=$((HEIGHT * 29 / 100))
adb shell input tap "$X" "$Y"
sleep 8
adb exec-out screencap -p > "$OUT_DIR/adventure.png"
adb shell dumpsys activity activities | grep -E 'ResumedActivity|topResumedActivity' > "$OUT_DIR/activity.txt" || true
file "$OUT_DIR/home.png" "$OUT_DIR/adventure.png"
