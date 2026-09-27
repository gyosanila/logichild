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
# Emulator lambat sering munculin dialog "isn't responding" — tutup sebelum tiap capture.
close_dialogs() { adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true; }
shot() { close_dialogs; sleep 1; adb exec-out screencap -p > "$OUT_DIR/$1.png"; }
read -r WIDTH HEIGHT < <(adb shell wm size | python3 -c 'import re,sys; s=sys.stdin.read(); m=re.findall(r"(\d+)x(\d+)",s); print(*map(int,m[-1]))')
# Tombol JALAN: tengah bawah, pusat ±70 unit dari bawah (lebar layar = 540 unit).
WALK_X=$((WIDTH / 2)); WALK_Y=$((HEIGHT - WIDTH * 70 / 540))

# open <nama> [extras...] — buka app langsung ke layar tertentu (hook intent di MainActivity).
open() { adb shell am force-stop "$PKG"; adb shell am start -n "$PKG/.MainActivity" "$@" >/dev/null; sleep 12; }

open;                                                          shot home
open --es screen adventure --ei adv_pos 1;                     shot board-1
adb shell input tap "$WALK_X" "$WALK_Y"; sleep 0.3;            shot board-hop
sleep 3;                                                       shot board-2
open --es screen adventure --ei adv_pos 9 --ei adv_stars 12;   shot zone-summer
open --es screen adventure --ei adv_pos 15 --ei adv_stars 30;  shot zone-autumn
open --es screen adventure --ei adv_pos 19 --ei adv_fruits 4;  shot zone-winter
adb shell input tap "$WALK_X" "$WALK_Y"; sleep 1.2;            shot apple
sleep 1;                                                       shot hadiah
open --es screen adventure --ei adv_pos 23 --ei adv_stars 58;  adb shell input tap "$WALK_X" "$WALK_Y"; sleep 3; shot finish
adb shell dumpsys activity activities | grep -E 'ResumedActivity|topResumedActivity' > "$OUT_DIR/activity.txt" || true
adb logcat -d -t 400 '*:E' > "$OUT_DIR/logcat-errors.txt" || true
ls -la "$OUT_DIR"
