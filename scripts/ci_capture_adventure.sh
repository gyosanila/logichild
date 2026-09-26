#!/usr/bin/env bash
set -euo pipefail

PKG=com.gyosanila.logichild
OUT_DIR="$GITHUB_WORKSPACE/ui-screenshots"
mkdir -p "$OUT_DIR"

# The emulator can report boot-complete before PackageManager is ready; wait for
# the service rather than failing immediately on an early adb install.
for attempt in $(seq 1 18); do
  if adb shell service check package 2>&1 | grep -q 'found'; then
    break
  fi
  echo "Waiting for PackageManager ($attempt/18)..."
  sleep 5
done
adb shell service check package 2>&1 | grep -q 'found' || {
  echo 'PackageManager did not become available after emulator boot.' >&2
  adb logcat -d -t 300 || true
  exit 1
}

adb install -r artifacts/app-debug.apk
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
