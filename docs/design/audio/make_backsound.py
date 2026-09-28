#!/usr/bin/env python3
"""Logichild - backsound loops, synthesised from scratch (no samples, no royalties).

Two loops, both exactly N beats long at an even tempo so they loop seamlessly:
  taman  : 96 BPM, 16 beats (10.0 s) - marimba melody over C-G-Am-F, soft shaker
  tenang : 80 BPM, 16 beats (12.0 s) - kalimba arpeggio + pad, no percussion

Usage: python3 make_backsound.py            -> writes .wav then encodes .mp3/.ogg
"""
import numpy as np, subprocess, os, wave

SR = 44100
OUT = os.path.dirname(os.path.abspath(__file__))

def env(n, attack=0.004, decay=4.5, sr=SR):
    t = np.arange(n) / sr
    a = np.clip(t / attack, 0, 1)
    return a * np.exp(-decay * t)

def tone(freq, dur, amp=1.0, harmonics=((1, 1.0), (2, 0.32), (3, 0.14), (4, 0.06)), decay=4.5):
    n = int(dur * SR)
    e = env(n, decay=decay)
    out = np.zeros(n)
    for mult, a in harmonics:
        out += a * np.sin(2 * np.pi * freq * mult * np.arange(n) / SR)
    out *= e
    return out * amp

def bell(freq, dur, amp=0.5):
    n = int(dur * SR)
    t = np.arange(n) / SR
    e = np.exp(-7.0 * t) * np.clip(t / 0.002, 0, 1)
    return (np.sin(2*np.pi*freq*t) + 0.5*np.sin(2*np.pi*freq*2.76*t) + 0.25*np.sin(2*np.pi*freq*5.4*t)) * e * amp

def bass(freq, dur, amp=0.5):
    n = int(dur * SR)
    t = np.arange(n) / SR
    e = np.clip(t / 0.012, 0, 1) * np.exp(-2.2 * t)
    return (np.sin(2*np.pi*freq*t) + 0.25*np.sin(2*np.pi*freq*2*t)) * e * amp

def shaker(dur, amp=0.18):
    n = int(dur * SR)
    x = np.random.RandomState(7).randn(n)
    x = np.diff(np.concatenate([[0], x]))          # crude high-pass
    e = np.exp(-38 * np.arange(n) / SR) * np.clip(np.arange(n) / (0.001*SR), 0, 1)
    return x * e * amp

def pad(freq, dur, amp=0.18):
    n = int(dur * SR)
    t = np.arange(n) / SR
    e = np.clip(t / 0.35, 0, 1) * np.clip((dur - t) / 0.5, 0, 1)
    vib = 1 + 0.0016 * np.sin(2*np.pi*4.6*t)
    return (np.sin(2*np.pi*freq*t*vib) + 0.4*np.sin(2*np.pi*freq*2*t) + 0.18*np.sin(2*np.pi*freq*3*t)) * e * amp

N = {'C3':130.81,'D3':146.83,'E3':164.81,'F3':174.61,'G3':196.00,'A3':220.00,'B3':246.94,
     'C4':261.63,'D4':293.66,'E4':329.63,'F4':349.23,'G4':392.00,'A4':440.00,'B4':493.88,
     'C5':523.25,'D5':587.33,'E5':659.25,'G5':783.99}
N.update({'C2':65.41,'F2':87.31,'G2':98.00,'A2':110.00})

def render(beat, beats, melody, bassline, chords, perc=True, mel_amp=0.5, bas_amp=0.34, pad_amp=0.0):
    total = int(beats * beat * SR) + SR
    L = np.zeros(total); R = np.zeros(total)
    def put(sig, at, pan=0.0, gain=1.0):
        i = int(at * SR); j = min(total, i + len(sig))
        seg = sig[:j-i]
        L[i:j] += seg * (1 - max(0, pan)) * gain
        R[i:j] += seg * (1 + min(0, pan)) * gain
    for name, at, dur in melody:
        note = tone(N[name], dur, amp=mel_amp, decay=5.2)
        put(note, at*beat, pan=0.12)
    for name, at, dur in bassline:
        put(bass(N[name], dur, amp=bas_amp), at*beat)
    if pad_amp:
        for name, at, dur in chords:
            put(pad(N[name], dur, amp=pad_amp), at*beat, pan=-0.1)
    if perc:
        b = 0
        while b < beats:
            put(shaker(0.09, amp=0.14 if b % 4 in (1, 3) else 0.09), (b+0.5)*beat)
            b += 1
        for b in (0, 2, 4, 6, 8, 10, 12, 14):
            put(bell(N['C5'] if b % 8 == 0 else N['G4'], 0.30, amp=0.10), b*beat, pan=0.25)
    mix = np.stack([L[:int(beats*beat*SR)], R[:int(beats*beat*SR)]], axis=1)
    mix = np.tanh(mix * 1.15) / 1.15                    # soft limiter
    peak = np.abs(mix).max() or 1.0
    mix = mix / peak * 0.70                              # ~ -3.1 dBFS, aman untuk codec lossy
    fade = int(0.03 * SR)                                # tiny fade so the loop point is click-free
    mix[:fade] *= np.linspace(0, 1, fade)[:, None]
    mix[-fade:] *= np.linspace(1, 0, fade)[:, None]
    return mix

# ---- loop 1: "Taman" - cheerful, C - G - Am - F ----
beat = 60 / 96
mel = [('E4',0,.55),('G4',.5,.5),('A4',1,.5),('G4',1.5,.45),('E4',2,.6),('G4',2.5,.5),('A4',3,.5),('C5',3.5,.6),
       ('D4',4,.55),('G4',4.5,.5),('B4',5,.5),('G4',5.5,.45),('D4',6,.7),('G4',7,.6),
       ('C5',8,.6),('A4',8.5,.5),('E4',9,.5),('A4',9.5,.5),('C5',10,.7),('E5',11,.6),
       ('A4',12,.55),('C5',12.5,.5),('F4',13,.5),('A4',13.5,.5),('C5',14,.6),('A4',15,.5)]
bas = [('C3',0,1.6),('C3',2,1.6),('G2',4,1.6),('G2',6,1.6),
       ('A2',8,1.6),('A2',10,1.6),('F2',12,1.6),('F2',14,1.6)]
mix1 = render(beat, 16, mel, bas, [], perc=True)

# ---- loop 2: "Tenang" - kalimba arpeggio + pad, C - Am - F - G ----
beat2 = 60 / 80
mel2 = [('C4',0,.6),('E4',1,.6),('G4',2,.7),('E4',3,.6),
        ('A3',4,.6),('C4',5,.6),('E4',6,.7),('C4',7,.6),
        ('F3',8,.6),('A3',9,.6),('C4',10,.7),('A3',11,.6),
        ('G3',12,.6),('B3',13,.6),('D4',14,.7),('B3',15,.6)]
bas2 = [('C2',0,3.4),('A2',4,3.4),('F2',8,3.4),('G2',12,3.4)]
chords = [('E4',0,3.6),('C4',4,3.6),('A3',8,3.6),('B3',12,3.6)]
mix2 = render(beat2, 16, mel2, bas2, chords, perc=False, mel_amp=0.42, bas_amp=0.26, pad_amp=0.20)

def write_wav(path, data):
    d = (np.clip(data, -1, 1) * 32767).astype('<i2')
    with wave.open(path, 'wb') as w:
        w.setnchannels(2); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes(d.tobytes())

for name, mix in (('backsound-taman', mix1), ('backsound-tenang', mix2)):
    wav = f'{OUT}/{name}.wav'
    write_wav(wav, mix)
    for ext, args in (('mp3', ['-c:a','libmp3lame','-b:a','160k']),
                      ('ogg', ['-c:a','libvorbis','-q:a','4'])):
        subprocess.run(['ffmpeg','-y','-loglevel','error','-i',wav]+args+[f'{OUT}/{name}.{ext}'], check=True)
    os.remove(wav)
    dur = len(mix)/SR
    print(f'{name}: {dur:.2f}s  peak {np.abs(mix).max():.3f}  rms {np.sqrt((mix**2).mean()):.3f}  ' +
          ' '.join(f'{ext}={os.path.getsize(f"{OUT}/{name}.{ext}")//1024}KB' for ext in ('mp3','ogg')))
