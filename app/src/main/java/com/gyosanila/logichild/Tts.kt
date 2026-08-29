package com.gyosanila.logichild

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/** Text-to-speech Bahasa Indonesia — coba default engine, terus semua engine terinstall. */
class TtsSpeaker(context: Context) {
    private val appContext = context.applicationContext

    // Daftar engine yang tersedia (nama package), dari sistem.
    private val engines: List<String> =
        appContext.packageManager.queryIntentServices(
            android.content.Intent("android.intent.action.TTS_SERVICE"), 0
        ).mapNotNull { it.serviceInfo?.packageName }
    private var tts: TextToSpeech? = null
    private var attempt = 0

    private var engineReady = false
    private var engineUsed = "?"
    private var voiceLabel = "belum init"
    private var voicesCount = 0
    private var round = 0

    init {
        Log.i("LogichildTTS", "engine terinstall: $engines")
        initTts()
    }

    private fun initTts() {
        // attempt 0 = default engine; berikutnya = engine dari daftar.
        val enginePkg: String? = if (attempt == 0) null else engines.getOrNull(attempt - 1)
        engineUsed = enginePkg ?: "default"
        tts = TextToSpeech(appContext, { status ->
            if (status == TextToSpeech.SUCCESS) {
                engineReady = true
                Log.i("LogichildTTS", "init SUCCESS engine=$engineUsed")
                val engine = tts ?: return@TextToSpeech
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
                // Init gagal → coba engine berikutnya.
                attempt++
                if (attempt <= engines.size) {
                    Log.w("LogichildTTS", "engine $engineUsed gagal (status=$status), coba berikutnya")
                    tts?.shutdown()
                    initTts()
                } else if (round < 2) {
                    // Semua engine gagal — ColorOS kadang baru ngizinin service
                    // beberapa detik setelah app buka. Retry delay.
                    round++
                    attempt = 0
                    Log.w("LogichildTTS", "SEMUA engine gagal, retry round $round dalam 4 dtk")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        initTts()
                    }, 4000)
                } else {
                    Log.e("LogichildTTS", "SEMUA engine gagal setelah retry. engines=$engines")
                }
            }
        }, enginePkg)
    }

    /** Status ringkas buat diagnosa. */
    fun statusInfo(): String =
        if (engineReady) "engine OK ($engineUsed), voice=$voiceLabel, total voice=$voicesCount"
        else "init GAGAL semua (${engines.size} engine: $engines)"

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
