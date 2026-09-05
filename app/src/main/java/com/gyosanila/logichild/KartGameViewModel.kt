package com.gyosanila.logichild

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gyosanila.logichild.game.GameEngine
import com.gyosanila.logichild.game.Instruction
import com.gyosanila.logichild.game.KartState
import com.gyosanila.logichild.game.Level
import com.gyosanila.logichild.game.LevelGen
import com.gyosanila.logichild.game.Reward
import com.gyosanila.logichild.ui.StringsEn
import com.gyosanila.logichild.ui.StringsId
import com.gyosanila.logichild.game.StepResult
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
    val kart: KartState = KartState(LevelGen.generate(0).start, LevelGen.generate(0).startDir),
    val running: Boolean = false,
    val won: Boolean = false,
    val crashed: Boolean = false,
    val crashCell: com.gyosanila.logichild.game.Pos? = null,
    val unlocked: Int = 0,
    val stars: Map<Int, Int> = emptyMap(),
    val soundOn: Boolean = true,
    val confettiTick: Int = 0,
    val reward: Reward = Reward.NONE,
)

/** Bunyi-bunyian: efek meriah (applause/fanfare/sparkle) + TTS apresiasi. */
class GameSounds(context: Context) {
    private val tts = TtsSpeaker(context)
    private val pool = android.media.SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val sApplause = pool.load(context, R.raw.applause, 1)
    private val sFanfare = pool.load(context, R.raw.fanfare, 1)
    private val sSparkle = pool.load(context, R.raw.sparkle, 1)
    private val sTap = pool.load(context, R.raw.tap, 1)
    private val sStep = pool.load(context, R.raw.step, 1)

    // Load SoundPool itu async — simpan status siap & pending play.
    private val ready = mutableSetOf<Int>()
    private var pending: Triple<Int, Float, Float>? = null

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                ready += sampleId
                pending?.let { (id, vol, rate) ->
                    if (id == sampleId) {
                        pool.play(id, vol, vol, 1, 0, rate)
                        pending = null
                    }
                }
            }
        }
    }

    private fun play(soundId: Int, vol: Float, rate: Float = 1f) {
        if (soundId in ready) {
            pool.play(soundId, vol, vol, 1, 0, rate)
        } else {
            // Belum ke-load (baru buka app) → tunggu, nanti diputar pas siap.
            pending = Triple(soundId, vol, rate)
        }
    }

    var enabled = true

    fun tap() {
        if (enabled) play(sTap, 0.24f)
    }

    fun move() {
        if (enabled) play(sStep, 0.28f)
    }

    fun turn() {
        if (enabled) play(sTap, 0.20f, 0.85f)
    }

    fun crash() {
        if (enabled) play(sFanfare, 0.35f, 0.7f)
    }

    /** Menang: apresiasi sesuai kalimat dialog + tepuk tangan + sparkle. */
    fun win(praise: String) {
        if (!enabled) return
        tts.speak(praise)
        play(sApplause, 0.9f)
        play(sSparkle, 0.6f, 1.3f)
    }

    /** Reward besar: fanfare + tepuk tangan + apresiasi. */
    fun bigWin(praise: String) {
        if (!enabled) return
        tts.speak(praise)
        play(sFanfare, 0.95f)
        play(sApplause, 0.85f)
    }

    /** Rating rendah: tetap apresiasi + tepuk tangan pelan. */
    fun clap(praise: String) {
        if (!enabled) return
        tts.speak(praise)
        play(sApplause, 0.5f)
    }

    fun stop() {
        pool.autoPause()
        tts.stop()
    }

    fun release() {
        pool.release()
        tts.shutdown()
    }
}

class KartGameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
    val sounds = GameSounds(application)

    private val _uiState = MutableStateFlow(
        KartGameUiState(
            unlocked = prefs.getInt("unlocked", 0),
            // Baca SEMUA star yang pernah disimpan (level bisa ribuan).
            stars = prefs.all.filterKeys { it.startsWith("star_") }.mapNotNull { (k, v) ->
                k.removePrefix("star_").toIntOrNull()?.let { it to ((v as? Int) ?: 0) }
            }.toMap(),
        )
    )
    val uiState: StateFlow<KartGameUiState> = _uiState.asStateFlow()

    private var runJob: Job? = null

    init {
        sounds.enabled = prefs.getBoolean("sound_on", true)
        val lastLevel = prefs.getInt("kart_last_level", 0)
        _uiState.update { it.copy(soundOn = sounds.enabled, levelIndex = lastLevel) }
        resetLevel(lastLevel)
    }

    val level: Level get() = LevelGen.generate(_uiState.value.levelIndex)

    fun selectLevel(index: Int) {
        if (index > _uiState.value.unlocked) return
        runJob?.cancel()
        prefs.edit().putInt("kart_last_level", index).apply()
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
        if (s.running || s.won || s.instructions.size >= 1000) return
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

    /** Kembalikan kart ke posisi start + bersihkan langkah (tombol Ulang / Main lagi). */
    fun resetKart() {
        val s = _uiState.value
        if (s.running) return
        val lv = level
        _uiState.update {
            it.copy(
                kart = KartState(lv.start, lv.startDir),
                instructions = emptyList(),
                crashed = false, crashCell = null, won = false
            )
        }
    }

    fun nextLevel() {
        val s = _uiState.value
        selectLevel(s.levelIndex + 1)
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
                        delay(600)
                        _uiState.update { it.copy(crashed = false, crashCell = null) }
                        return@launch
                    }
                    is StepResult.Won -> {
                        val lv = level
                        val prevStars = _uiState.value.stars[lv.index] ?: 0
                        // Rating 1-5: 5★ = program PALING HEMAT. BFS dihitung per
                        // perintah (termasuk belok), jadi rute tercepat = 5★.
                        // Ghost cuma preview posisi akhir — bukan solusi, jadi
                        // tidak mengurangi rating.
                        val best = LevelGen.bestInstructions(lv.start, lv.startDir, lv.finish, lv.width, lv.height, lv.cones)
                            ?: s.instructions.size
                        val steps = s.instructions.size
                        val rating = when {
                            steps <= best -> 5
                            steps <= best + 2 -> 4
                            steps <= best * 2 -> 3
                            steps <= best * 3 -> 2
                            else -> 1
                        }
                        val newStars = maxOf(prevStars, rating)
                        val newUnlocked = maxOf(_uiState.value.unlocked, lv.index + 1)
                        // Reward: level kelipatan 10 = besar, kelipatan 5 = kecil
                        val levelNumber = lv.index + 1
                        val reward = when {
                            levelNumber % 10 == 0 -> Reward.BIG
                            levelNumber % 10 == 5 -> Reward.SMALL
                            else -> Reward.NONE
                        }
                        // Apresiasi sesuai bahasa & rating (sama dengan teks di dialog).
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
                            .putInt("star_${lv.index}", newStars)
                            .putInt("unlocked", newUnlocked)
                            .apply()
                        _uiState.update {
                            it.copy(
                                running = false, won = true,
                                stars = it.stars + (lv.index to newStars),
                                unlocked = newUnlocked,
                                confettiTick = it.confettiTick + 1,
                                reward = reward,
                            )
                        }
                        // AdMob fullscreen setiap naik ke level kelipatan 5.
                        if (levelNumber % 5 == 0) {
                            AppActivityHolder.current?.let { act -> showInterstitialIfReady(act) }
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
        val lv = LevelGen.generate(index)
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
