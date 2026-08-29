#!/usr/bin/env python3
"""Generate efek suara meriah buat Logichild (WAV 44.1kHz 16-bit mono)."""
import numpy as np, wave, os

SR = 44100
OUT = "/home/ilga/KartCilik/app/src/main/res/raw"
os.makedirs(OUT, exist_ok=True)

def save(name, data):
    pcm = np.clip(data, -1, 1)
    pcm16 = (pcm * 32767).astype(np.int16)
    with wave.open(f"{OUT}/{name}.wav", "w") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes(pcm16.tobytes())
    print(name, f"{os.path.getsize(f'{OUT}/{name}.wav')//1024}KB")

def noise(n): return np.random.uniform(-1, 1, n)

# ── Applause: banyak "clap" acak (noise burst 12ms + decay) di atas ruang riuh ──
dur = 2.4
t = np.arange(int(SR * dur)) / SR
app = np.zeros_like(t)
rng = np.random.default_rng(42)
room = noise(len(t)) * 0.05
app += room
for _ in range(140):
    start = int(rng.uniform(0, dur) * SR)
    clap = noise(int(SR * 0.012)) * rng.uniform(0.4, 1.0)
    env = np.exp(-np.linspace(0, 8, len(clap)))          # decay cepat
    end = min(start + len(clap), len(app))
    app[start:end] += clap[: end - start] * env[: end - start]
# lowpass sederhana biar gak tajam
k = np.ones(24) / 24
app = np.convolve(app, k, mode="same")
# fade in/out
fi = int(0.05 * SR); fo = int(0.4 * SR)
app[:fi] *= np.linspace(0, 1, fi)
app[-fo:] *= np.linspace(1, 0, fo)
app = np.clip(app, -0.95, 0.95) * 0.9
save("applause", app)

# ── Fanfare: arpeggio C5-E5-G5-C6 naik + chord akhir ──
def tone(freq, d, amp=0.5, vib=6):
    tt = np.arange(int(SR * d)) / SR
    sig = (np.sin(2 * np.pi * freq * tt)
           + 0.35 * np.sin(2 * np.pi * freq * 2 * tt)
           + 0.12 * np.sin(2 * np.pi * freq * 3 * tt))
    vib_sig = np.sin(2 * np.pi * vib * tt) * 0.004
    sig = np.sin(2 * np.pi * (freq * tt + np.cumsum(vib_sig)))
    env = np.minimum(tt / 0.02, 1) * np.exp(-tt * 2.2)
    return sig * env * amp

notes = [523.25, 659.25, 783.99, 1046.5]
fan = np.concatenate([tone(f, 0.32) for f in notes[:3]])
fan = np.concatenate([fan, tone(1046.5, 0.9, 0.55) + tone(783.99, 0.9, 0.3) + tone(659.25, 0.9, 0.22)])
fan = np.clip(fan, -0.95, 0.95)
save("fanfare", fan)

# ── Sparkle: nada tinggi naik cepat (C6 D6 E6 G6) ──
def spark(freq, d=0.14):
    tt = np.arange(int(SR * d)) / SR
    sig = np.sin(2 * np.pi * freq * tt) + 0.3 * np.sin(2 * np.pi * freq * 2 * tt)
    env = np.minimum(tt / 0.01, 1) * np.exp(-tt * 18)
    return sig * env * 0.5

sp = np.concatenate([spark(f) for f in [1046.5, 1174.7, 1318.5, 1568.0]])
save("sparkle", sp)
print("DONE")
