#!/usr/bin/env python3
"""Regenerate applause realistis: 800+ clap padat + noise kerumunan + bandpass."""
import numpy as np, wave, os

SR = 44100
OUT = "/home/ilga/KartCilik/app/src/main/res/raw"

def bandpass(x, lo, hi):
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / SR)
    X[(f < lo) | (f > hi)] = 0
    return np.fft.irfft(X, len(x))

def save(name, data):
    pcm = np.clip(data, -1, 1)
    pcm16 = (pcm * 32767).astype(np.int16)
    with wave.open(f"{OUT}/{name}.wav", "w") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes(pcm16.tobytes())
    print(name, f"{os.path.getsize(f'{OUT}/{name}.wav')//1024}KB")

dur = 3.0
n = int(SR * dur)
rng = np.random.default_rng(7)
app = np.zeros(n)

# ── Kerumunan (crowd wash): noise band 400-6000Hz, modulasi pelan ──
wash = bandpass(rng.uniform(-1, 1, n), 400, 6000)
slow = 0.6 + 0.4 * np.sin(2 * np.pi * 0.7 * np.arange(n) / SR + 1.3) \
       + 0.25 * np.sin(2 * np.pi * 2.1 * np.arange(n) / SR)
app += wash * slow * 0.30

# ── Clap padat: 900 tepukan, attack 1ms, decay eksponensial, bandpass 800-9000Hz ──
clap_len = int(SR * 0.05)
clap_t = np.arange(clap_len) / SR
for _ in range(900):
    t0 = int(rng.uniform(0, dur - 0.1) * SR)
    burst = rng.uniform(-1, 1, clap_len)
    env = np.minimum(clap_t / 0.0012, 1) * np.exp(-clap_t / 0.007)
    clap = bandpass(burst * env, 800, 9000)
    amp = (0.25 + 1.8 * rng.random() ** 2.5)   # mayoritas pelan, sesekali keras
    end = min(t0 + clap_len, n)
    app[t0:end] += clap[: end - t0] * amp

# ── Reverb tipis: gema cepat teredam ──
echo = np.zeros(n)
for delay, gain in [(700, 0.35), (1400, 0.18), (2100, 0.10)]:
    echo[delay:] += app[:-delay] * gain
app += echo

# fade in/out + normalisasi
fi, fo = int(0.08 * SR), int(0.9 * SR)
app[:fi] *= np.linspace(0, 1, fi)
app[-fo:] *= np.linspace(1, 0, fo)
app = app / (np.max(np.abs(app)) + 1e-9) * 0.92
save("applause", app)
print("DONE")
