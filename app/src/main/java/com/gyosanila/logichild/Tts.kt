package com.gyosanila.logichild

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/** Text-to-speech Bahasa Indonesia — dipakai buat ucapan & pengumuman. */
class TtsSpeaker(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var initRetried = false

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = tts?.setLanguage(Locale("id", "ID")) ?: TextToSpeech.LANG_MISSING_DATA
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Voice id-ID gak ada → fallback ke bahasa default device, terus English.
                    Log.w("LogichildTTS", "id-ID gak didukung (res=$res), fallback ke default")
                    tts?.language = Locale.getDefault()
                    if (tts?.language == null) tts?.language = Locale.ENGLISH
                }
            } else {
                // Init gagal (engine TTS gak aktif) → coba sekali lagi.
                Log.w("LogichildTTS", "init status=$status")
                if (!initRetried) {
                    initRetried = true
                    tts?.shutdown()
                    initTts()
                }
            }
        }
    }

    fun speak(text: String) {
        val engine = tts ?: return
        // Ikut toggle suara di Pengaturan (sound_on, default nyala).
        val soundOn = appContext.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
            .getBoolean("sound_on", true)
        if (!soundOn) return
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
