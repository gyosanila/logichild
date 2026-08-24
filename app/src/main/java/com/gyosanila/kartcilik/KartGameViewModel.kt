package com.gyosanila.kartcilik

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gyosanila.kartcilik.game.GameEngine
import com.gyosanila.kartcilik.game.Instruction
import com.gyosanila.kartcilik.game.KartState
import com.gyosanila.kartcilik.game.Level
import com.gyosanila.kartcilik.game.Levels
import com.gyosanila.kartcilik.game.StepResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KartGameUiState(
    val levelIndex: Int = 0,
    val instructions: List<Instruction> = emptyList(),
    val kart: KartState = KartState(Levels.all[0].start, Levels.all[0].startDir),
    val running: Boolean = false,
    val won: Boolean = false,
    val crashed: Boolean = false,
    val crashCell: com.gyosanila.kartcilik.game.Pos? = null,
    val unlocked: Int = 0,
    val stars: Map<Int, Int> = emptyMap(),
    val soundOn: Boolean = true,
    val confettiTick: Int = 0,
)

/** Bunyi-bunyian pakai ToneGenerator — tanpa file asset sama sekali. */
class GameSounds(context: Context) {
    private val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
    var enabled = true

    fun tap() {
        if (enabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 40)
    }

    fun move() {
        if (enabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
    }

    fun turn() {
        if (enabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 60)
    }

    fun crash() {
        if (enabled) tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250)
    }

    fun win() {
        if (!enabled) return
        // Nada naik sederhana: 3 beep dengan pitch berbeda
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        Thread {
            try {
                Thread.sleep(120); tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 80)
                Thread.sleep(120); tone.startTone(ToneGenerator.TONE_PROP_ACK, 160)
            } catch (_: InterruptedException) {
            }
        }.start()
    }

    fun release() = tone.release()
}

class KartGameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
    val sounds = GameSounds(application)

    private val _uiState = MutableStateFlow(
        KartGameUiState(
            unlocked = prefs.getInt("unlocked", 0),
            stars = (0..7).mapNotNull { i ->
                prefs.getInt("star_$i", -1).takeIf { it >= 0 }?.let { i to it }
            }.toMap(),
        )
    )
    val uiState: StateFlow<KartGameUiState> = _uiState.asStateFlow()

    private var runJob: Job? = null

    init {
        sounds.enabled = prefs.getBoolean("sound_on", true)
        _uiState.update { it.copy(soundOn = sounds.enabled) }
        resetLevel(0)
    }

    val level: Level get() = Levels.all[_uiState.value.levelIndex]

    fun selectLevel(index: Int) {
        if (index > _uiState.value.unlocked) return
        runJob?.cancel()
        _uiState.update { it.copy(levelIndex = index) }
        resetLevel(index)
    }

    fun toggleSound() {
        sounds.enabled = !sounds.enabled
        prefs.edit().putBoolean("sound_on", sounds.enabled).apply()
        _uiState.update { it.copy(soundOn = sounds.enabled) }
        if (sounds.enabled) sounds.tap()
    }

    fun addInstruction(i: Instruction) {
        val s = _uiState.value
        if (s.running || s.won || s.instructions.size >= 12) return
        sounds.tap()
        _uiState.update { it.copy(instructions = it.instructions + i) }
    }

    fun removeLast() {
        val s = _uiState.value
        if (s.running || s.won) return
        sounds.tap()
        _uiState.update { it.copy(instructions = it.instructions.dropLast(1)) }
    }

    fun clearInstructions() {
        val s = _uiState.value
        if (s.running || s.won) return
        sounds.tap()
        _uiState.update { it.copy(instructions = emptyList()) }
    }

    /** Kembalikan kart ke posisi start, instruksi tetap (biar anak bisa perbaiki). */
    fun resetKart() {
        val s = _uiState.value
        if (s.running) return
        val lv = level
        _uiState.update {
            it.copy(
                kart = KartState(lv.start, lv.startDir),
                crashed = false, crashCell = null, won = false
            )
        }
    }

    fun nextLevel() {
        val s = _uiState.value
        val next = s.levelIndex + 1
        if (next < Levels.all.size) selectLevel(next)
    }

    fun play() {
        val s = _uiState.value
        if (s.running || s.won || s.instructions.isEmpty()) return
        runJob = viewModelScope.launch {
            _uiState.update { it.copy(running = true, crashed = false, crashCell = null) }
            var kart = KartState(level.start, level.startDir)
            _uiState.update { it.copy(kart = kart) }
            delay(250)
            for (instr in s.instructions) {
                val (next, result) = GameEngine.apply(kart, instr, level)
                kart = next
                _uiState.update { it.copy(kart = kart) }
                when (result) {
                    is StepResult.Moved -> sounds.move()
                    is StepResult.Turned -> sounds.turn()
                    is StepResult.Crashed -> {
                        sounds.crash()
                        _uiState.update { it.copy(running = false, crashed = true, crashCell = result.at) }
                        delay(500)
                        _uiState.update { it.copy(crashed = false) }
                        return@launch
                    }
                    is StepResult.Won -> {
                        sounds.win()
                        val lv = level
                        val prevStars = _uiState.value.stars[lv.index] ?: 0
                        // Star 1 = menang. (v1: 1 bintang; nanti bisa 3 kalau pakai
                        // instruksi lebih sedikit dari minimum.)
                        val newStars = maxOf(prevStars, 1)
                        val newUnlocked = maxOf(_uiState.value.unlocked, lv.index + 1)
                        prefs.edit()
                            .putInt("star_${lv.index}", newStars)
                            .putInt("unlocked", newUnlocked)
                            .apply()
                        _uiState.update {
                            it.copy(
                                running = false, won = true,
                                stars = it.stars + (lv.index to newStars),
                                unlocked = newUnlocked,
                                confettiTick = it.confettiTick + 1,
                            )
                        }
                        return@launch
                    }
                }
                delay(420)
            }
            _uiState.update { it.copy(running = false) }
        }
    }

    private fun resetLevel(index: Int) {
        val lv = Levels.all[index]
        _uiState.update {
            it.copy(
                kart = KartState(lv.start, lv.startDir),
                instructions = emptyList(),
                running = false, won = false, crashed = false, crashCell = null,
            )
        }
    }

    override fun onCleared() {
        sounds.release()
        super.onCleared()
    }
}
