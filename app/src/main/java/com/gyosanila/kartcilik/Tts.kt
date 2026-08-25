package com.gyosanila.kartcilik

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Text-to-speech Bahasa Indonesia — dipakai buat ucapan & pengumuman. */
class TtsSpeaker(context: Context) {
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("id", "ID")
            }
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
