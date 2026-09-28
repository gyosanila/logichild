#!/usr/bin/env python3
"""Render the animated Logichild roadmap mockup into two short MP4s.

Frame content is a pure function of the ?t= parameter, so the clips are deterministic.
Everything in the mockup oscillates on a 4 s loop => the snow clip (4 s) tiles seamlessly.
Frames are shot in parallel (4 Chrome instances) to keep the wall time down.
"""
import os
import shutil
import subprocess
from concurrent.futures import ThreadPoolExecutor

CH = '/home/ilga/.cache/ms-playwright/chromium-1243/chrome-linux64/chrome'
URL = 'file:///home/ilga/KartCilik/docs/design/mockups/board.html?v='
WD = '/home/ilga/KartCilik/docs/design/mockups'
TMP = '/tmp/board_frames'


def shoot(args):
    query, out = args
    subprocess.run(
        [CH, '--headless=new', '--no-sandbox', '--disable-gpu', '--hide-scrollbars',
         '--force-device-scale-factor=2', '--window-size=540,960',
         '--virtual-time-budget=4000', '--screenshot=' + out, URL + query],
        capture_output=True, cwd=WD)


def clip(name, frames, fps, query_of):
    d = os.path.join(TMP, name)
    shutil.rmtree(d, ignore_errors=True)
    os.makedirs(d, exist_ok=True)
    jobs = [(query_of(i), os.path.join(d, 'f_%03d.png' % i)) for i in range(frames)]
    with ThreadPoolExecutor(max_workers=4) as pool:
        list(pool.map(shoot, jobs))
    out = os.path.join(WD, name + '.mp4')
    subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-framerate', str(fps),
                    '-i', os.path.join(d, 'f_%03d.png'), '-c:v', 'libx264',
                    '-pix_fmt', 'yuv420p', '-crf', '21', '-movflags', '+faststart', out],
                   check=True)
    print(out, os.path.getsize(out), 'bytes', flush=True)
    return d


import sys
only = sys.argv[1] if len(sys.argv) > 1 else 'all'
d1 = d2 = None
# 1. winter: falling snow + wind, 4 s at 12 fps (exactly one wind loop, tiles seamlessly)
if only in ('all', 'snow'):
    d1 = clip('roadmap-salju', 48, 12, lambda i: '1&z=4&t=%.4f' % (i / 12.0))
# 2. trailer: slow pan across all four seasons with parallax layers, 8 s at 8 fps
if only in ('all', 'pan'):
    d2 = clip('roadmap-4musim-pan', 64, 8, lambda i: '1&z=1&cam=pan&dur=8&t=%.4f' % (i / 8.0))

# 3. the bear actually walks the road: 7 hops, camera follows, tiles unlock as it lands
if only in ('all', 'walk'):
    walk = clip('roadmap-beruang-jalan', 88, 12, lambda i: '1&z=1&walk=1&t=%.4f' % (i / 12.0))

# sanity check: the frames really differ (something is animating)
try:
    import numpy as np
    from PIL import Image
    dd = d1 or d2
    a = np.asarray(Image.open(os.path.join(dd, 'f_000.png')).convert('L'), dtype=float)
    b = np.asarray(Image.open(os.path.join(dd, 'f_006.png')).convert('L'), dtype=float)
    print('mean abs diff 0.5 s apart: %.3f' % abs(a - b).mean(), flush=True)
except Exception as exc:  # noqa
    print('diff check skipped:', exc)
print('DONE', flush=True)
