#!/usr/bin/env bash
set -euo pipefail

PKG=com.gyosanila.logichild
OUT_DIR="$GITHUB_WORKSPACE/ui-screenshots"
mkdir -p "$OUT_DIR"

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm clear "$PKG" >/dev/null
adb shell am force-stop "$PKG"
adb shell am start -n "$PKG/.MainActivity"

# Wait for splash to finish and the Indonesian home screen to appear.
for attempt in $(seq 1 30); do
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || true
  adb pull /sdcard/ui.xml "$OUT_DIR/window.xml" >/dev/null 2>&1 || true
  if [ -s "$OUT_DIR/window.xml" ] && grep -q 'Petualangan' "$OUT_DIR/window.xml"; then
    break
  fi
  sleep 1
done
if ! grep -q 'Petualangan' "$OUT_DIR/window.xml"; then
  echo 'Home screen did not expose the Petualangan button in the UI hierarchy.' >&2
  adb logcat -d -t 3000 > "$OUT_DIR/logcat.txt"
  exit 1
fi
adb exec-out screencap -p > "$OUT_DIR/home.png"

# Tap the accessible Petualangan control based on its live UIAutomator bounds.
python3 - "$OUT_DIR/window.xml" <<'PY' > "$OUT_DIR/tap.txt"
import re, sys, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
nodes = list(root.iter('node'))
matching = [n for n in nodes if 'Petualangan' in (n.attrib.get('text','') + ' ' + n.attrib.get('content-desc',''))]
clickable = [n for n in matching if n.attrib.get('clickable') == 'true']
node = clickable[0] if clickable else matching[0]
m = re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', node.attrib['bounds'])
if not m:
    raise SystemExit('Could not parse Petualangan bounds: ' + repr(node.attrib))
x1,y1,x2,y2 = map(int,m.groups())
print((x1+x2)//2, (y1+y2)//2)
PY
read -r X Y < "$OUT_DIR/tap.txt"
adb shell input tap "$X" "$Y"

# Confirm navigation reached the adventure board before capturing it.
for attempt in $(seq 1 20); do
  sleep 1
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || true
  adb pull /sdcard/ui.xml "$OUT_DIR/adventure-window.xml" >/dev/null 2>&1 || true
  if [ -s "$OUT_DIR/adventure-window.xml" ] && grep -q 'LEMPAR DADU\|LEMPAR Dadu\|ROLL THE DICE' "$OUT_DIR/adventure-window.xml"; then
    break
  fi
done
if ! grep -q 'LEMPAR DADU\|LEMPAR Dadu\|ROLL THE DICE' "$OUT_DIR/adventure-window.xml"; then
  echo 'Tap did not navigate to the adventure board.' >&2
  adb logcat -d -t 3000 > "$OUT_DIR/logcat.txt"
  exit 1
fi
adb exec-out screencap -p > "$OUT_DIR/adventure.png"
adb shell dumpsys activity activities | grep -E 'ResumedActivity|topResumedActivity' > "$OUT_DIR/activity.txt" || true
file "$OUT_DIR/home.png" "$OUT_DIR/adventure.png"
