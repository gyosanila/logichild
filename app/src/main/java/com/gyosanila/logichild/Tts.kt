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

    // Status buat diagnosa (dibaca lewat statusInfo()).
    private var engineReady = false
    private var voiceLabel = "belum init"
    private var voicesCount = 0

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech
                engineReady = true
                engine.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                voicesCount = engine.voices?.size ?: 0
                val res = engine.setLanguage(Locale("id", "ID"))
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val idVoice = engine.voices?.firstOrNull { it.locale.language == "id" }
                    if (idVoice != null) {
                        engine.voice = idVoice
                        voiceLabel = idVoice.name
                        Log.i("LogichildTTS", "voice id dipilih: ${idVoice.name}")
                    } else {
                        engine.language = Locale.getDefault()
                        voiceLabel = "fallback ${Locale.getDefault()}"
                        Log.w("LogichildTTS", "voice id gak ada (res=$res), fallback ${Locale.getDefault()}")
                    }
                } else {
                    voiceLabel = "id-ID (res=$res)"
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

    /** Status ringkas buat diagnosa: "engine OK, voice=...", atau "init GAGAL". */
    fun statusInfo(): String =
        if (engineReady) "engine OK, voice=$voiceLabel, total voice=$voicesCount"
        else "engine BELUM ready / GAGAL init (sudah retry sekali)"

    /** true = speak diterima engine. */
    fun speak(text: String): Boolean {
        val engine = tts ?: return false
        val soundOn = appContext.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
            .getBoolean("sound_on", true)
        if (!soundOn) return false
        val r = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts${System.currentTimeMillis()}")
        Log.i("LogichildTTS", "speak()=$r (0=SUCCESS) status=${statusInfo()}")
        return r == TextToSpeech.SUCCESS
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
