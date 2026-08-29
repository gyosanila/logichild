package com.gyosanila.logichild

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.gyosanila.logichild.game.Reward
import com.gyosanila.logichild.ui.StringsEn
import com.gyosanila.logichild.ui.StringsId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Warna yang bisa dipilih (index = urutan di palette). */
const val COLOR_COUNT = 7 // merah, biru, kuning, hijau, ungu, oranye, pink

data class ColorUiState(
    val level: Int = 1,
    val unlocked: Int = 1,
    val options: List<Int> = emptyList(), // index warna yang tampil
    val target: Int = 0,                  // index warna yang harus dipilih
    val mistakes: Int = 0,
    val stars: Map<Int, Int> = emptyMap(),
    val won: Boolean = false,
    val reward: Reward = Reward.NONE,
    val confettiTick: Int = 0,
)

class ColorMatchViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
    val sounds = GameSounds(application)
    private val tts = TtsSpeaker(application)

    private val _uiState = MutableStateFlow(ColorUiState())
    val uiState: StateFlow<ColorUiState> = _uiState.asStateFlow()

    /** Baca semua bintang (per level, tanpa batas). */
    private fun readStars(): Map<Int, Int> {
        val m = mutableMapOf<Int, Int>()
        val all = prefs.all
        for ((k, v) in all) {
            if (k.startsWith("cstar_") && v is Int) {
                k.removePrefix("cstar_").toIntOrNull()?.let { m[it] = v }
            }
        }
        return m
    }

    /** Banyak pilihan & jumlah warna per level (naik bertahap). */
    private fun optionsCountFor(level: Int) = when {
        level <= 3 -> 2
        level <= 6 -> 3
        else -> 4
    }

    private fun poolSizeFor(level: Int) = minOf(COLOR_COUNT, 2 + (level - 1) / 2)

    fun loadLevel(level: Int) {
        val pool = (0 until poolSizeFor(level)).toList().shuffled()
        val options = pool.take(optionsCountFor(level)).shuffled()
        val target = options.random()
        _uiState.update {
            it.copy(
                level = level,
                options = options,
                target = target,
                mistakes = 0,
                won = false,
                reward = Reward.NONE,
            )
        }
        // Instruksi dibacakan oleh screen (butuh strings sesuai bahasa).
    }

    fun answer(colorIndex: Int) {
        val s = _uiState.value
        if (s.won || colorIndex !in s.options) return
        if (colorIndex == s.target) {
            val rating = when (s.mistakes) {
                0 -> 5
                1 -> 4
                2 -> 3
                3 -> 2
                else -> 1
            }
            val prevStars = prefs.getInt("cstar_${s.level}", 0)
            val newStars = maxOf(prevStars, rating)
            val newUnlocked = maxOf(_uiState.value.unlocked, s.level + 1)
            val levelNumber = s.level
            val reward = when {
                levelNumber % 10 == 0 -> Reward.BIG
                levelNumber % 10 == 5 -> Reward.SMALL
                else -> Reward.NONE
            }
            val st = if (prefs.getString("lang", "id") == "en") StringsEn else StringsId
            val praise = when (rating) {
                5 -> st.praise5
                4 -> st.praise4
                3 -> st.praise3
                2 -> st.praise2
                else -> st.praise1
            }
            when {
                reward == Reward.BIG -> sounds.bigWin(st.praise5)
                rating >= 4 -> sounds.win(praise)
                rating == 3 -> sounds.win(praise)
                else -> sounds.clap(praise)
            }
            prefs.edit()
                .putInt("cstar_${s.level}", newStars)
                .putInt("cunlocked", newUnlocked)
                .putInt("color_level", s.level + 1)
                .apply()
            _uiState.update {
                it.copy(
                    won = true,
                    stars = it.stars + (s.level to newStars),
                    unlocked = newUnlocked,
                    reward = reward,
                    confettiTick = it.confettiTick + 1,
                )
            }
        } else {
            _uiState.update { it.copy(mistakes = it.mistakes + 1) }
            sounds.crash()
        }
    }

    fun nextLevel() = loadLevel(_uiState.value.level + 1)
    fun replay() = loadLevel(_uiState.value.level)

    /** Nama warna sesuai index (dipakai screen buat teks & TTS). */
    fun colorName(colorIndex: Int, strings: com.gyosanila.logichild.ui.AppStrings): String =
        when (colorIndex) {
            0 -> strings.colorRed
            1 -> strings.colorBlue
            2 -> strings.colorYellow
            3 -> strings.colorGreen
            4 -> strings.colorPurple
            5 -> strings.colorOrange
            else -> strings.colorPink
        }

    /** Instruksi yang dibacakan: "Cari yang merah!" */
    fun speakInstruction(strings: com.gyosanila.logichild.ui.AppStrings) {
        val s = _uiState.value
        tts.speak(String.format(strings.colorAsk, colorName(s.target, strings)))
    }
}
