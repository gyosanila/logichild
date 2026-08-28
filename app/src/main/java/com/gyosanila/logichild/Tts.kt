package com.gyosanila.logichild

import android.content.Context
import android.media.AudioAttributes
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
                val engine = tts ?: return@TextToSpeech
                // Eksplisit STREAM_MUSIC biar volume konsisten sama nada game.
                engine.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                // 1) Coba voice Indonesia.
                val res = engine.setLanguage(Locale("id", "ID"))
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // 2) Cari voice Indonesia dari daftar yang tersedia.
                    val idVoice = engine.voices?.firstOrNull { it.locale.language == "id" }
                    if (idVoice != null) {
                        engine.voice = idVoice
                        Log.i("LogichildTTS", "voice id dipilih: ${idVoice.name}")
                    } else {
                        // 3) Fallback terakhir: bahasa default device.
                        engine.language = Locale.getDefault()
                        Log.w("LogichildTTS", "voice id gak ada (res=$res), fallback ${Locale.getDefault()}")
                    }
                } else {
                    Log.i("LogichildTTS", "id-ID OK (res=$res)")
                }
            } else {
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
        val engine = tts ?: run {
            Log.w("LogichildTTS", "speak dipanggil tapi TTS null (engine gak ready)")
            return
        }
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
