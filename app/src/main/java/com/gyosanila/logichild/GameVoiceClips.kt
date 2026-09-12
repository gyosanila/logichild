package com.gyosanila.logichild

import android.content.Context
import android.media.MediaPlayer

/** Rekaman suara manusia/natural khusus Cocok Warna, offline. */
class GameVoiceClips(private val context: Context) {
    var enabled = true
    private var player: MediaPlayer? = null

    private val idFind = intArrayOf(
        R.raw.id_find_red, R.raw.id_find_blue, R.raw.id_find_yellow,
        R.raw.id_find_green, R.raw.id_find_purple, R.raw.id_find_orange,
        R.raw.id_find_pink,
    )
    private val enFind = intArrayOf(
        R.raw.en_find_red, R.raw.en_find_blue, R.raw.en_find_yellow,
        R.raw.en_find_green, R.raw.en_find_purple, R.raw.en_find_orange,
        R.raw.en_find_pink,
    )

    private fun play(resourceId: Int) {
        if (!enabled) return
        player?.runCatching { stop() }
        player?.release()
        player = MediaPlayer.create(context, resourceId)?.also { p ->
            p.setVolume(1.0f, 1.0f)
            p.setOnCompletionListener { it.release(); if (player === it) player = null }
            p.start()
        }
    }

    fun instruction(colorIndex: Int, english: Boolean) {
        val resources = if (english) enFind else idFind
        resources.getOrNull(colorIndex)?.let(::play)
    }

    fun feedback(stars: Int, english: Boolean) {
        val resources = if (english) {
            intArrayOf(R.raw.en_keep_going, R.raw.en_good_try, R.raw.en_great_job_keep_practicing, R.raw.en_awesome_almost_perfect, R.raw.en_praise_perfect)
        } else {
            intArrayOf(R.raw.id_keep_going, R.raw.id_good_try, R.raw.id_great_job_keep_practicing, R.raw.id_awesome_almost_perfect, R.raw.id_praise_perfect)
        }
        resources[(stars - 1).coerceIn(0, 4)].let(::play)
    }

    fun tryAgain(english: Boolean) {
        play(if (english) R.raw.en_try_again else R.raw.id_try_again)
    }

    fun stop() {
        player?.runCatching { stop() }
        player?.release()
        player = null
    }
}
