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
import kotlin.random.Random

data class PatternUiState(
    val level: Int = 1,
    val unlocked: Int = 1,
    val sequence: List<String> = emptyList(),
    val choices: List<String> = emptyList(),
    val answer: String = "",
    val mistakes: Int = 0,
    val stars: Map<Int, Int> = emptyMap(),
    val won: Boolean = false,
    val reward: Reward = Reward.NONE,
    val confettiTick: Int = 0,
    val soundOn: Boolean = true,
)

data class PatternPuzzle(
    val sequence: List<String>,
    val answer: String,
    val choices: List<String>,
)

class PatternMatchViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
    val sounds = GameSounds(application)
    private val voice = GameVoiceClips(application)
    private val _uiState = MutableStateFlow(PatternUiState())
    val uiState: StateFlow<PatternUiState> = _uiState.asStateFlow()

    init {
        sounds.enabled = prefs.getBoolean("sound_on", true)
        voice.enabled = sounds.enabled
        val level = prefs.getInt("pattern_level", 1)
        _uiState.update { it.copy(soundOn = sounds.enabled, unlocked = maxOf(1, level)) }
        loadLevel(level)
    }

    fun toggleSound() {
        sounds.enabled = !sounds.enabled
        voice.enabled = sounds.enabled
        prefs.edit().putBoolean("sound_on", sounds.enabled).apply()
        _uiState.update { it.copy(soundOn = sounds.enabled) }
        if (sounds.enabled) sounds.tap()
    }

    fun loadLevel(level: Int) {
        val safe = level.coerceAtLeast(1)
        val rng = Random(safe * 7919)
        val puzzle = if (safe == 9) {
            PatternPuzzle(
                sequence = listOf("⭐", "🌙", "⭐", "🌙", "?"),
                answer = "⭐",
                choices = listOf("🌙", "⭐", "🌈"),
            )
        } else {
            val pool = when {
                safe <= 3 -> listOf("🍎", "🍌", "🍊", "🍇")
                safe <= 6 -> listOf("🔴", "🔵", "🟡", "🟢")
                else -> listOf("⭐", "🌙", "🌈", "☀️")
            }
            val a = pool[rng.nextInt(pool.size)]
            var b = pool[rng.nextInt(pool.size)]
            while (b == a) b = pool[rng.nextInt(pool.size)]
            val sequence = if (safe % 3 == 0) listOf(a, b, a, b, "?") else listOf(a, b, a, "?")
            val wrong = pool.filter { it != b }.shuffled(rng).take(if (safe <= 4) 1 else 2)
            PatternPuzzle(sequence, b, (wrong + b).shuffled(rng))
        }
        check(puzzle.sequence.count { it == "?" } == 1)
        check(puzzle.sequence.size >= 4)
        check(puzzle.answer in puzzle.choices)
        check(puzzle.choices.distinct().size == puzzle.choices.size)
        _uiState.update { it.copy(level = safe, sequence = puzzle.sequence, choices = puzzle.choices, answer = puzzle.answer, mistakes = 0, won = false, reward = Reward.NONE) }
    }

    fun answer(choice: String) {
        val s = _uiState.value
        if (s.won || choice !in s.choices) return
        if (choice == s.answer) {
            val rating = when (s.mistakes) { 0 -> 5; 1 -> 4; 2 -> 3; 3 -> 2; else -> 1 }
            val previous = prefs.getInt("pstar_${s.level}", 0)
            val best = maxOf(previous, rating)
            val next = maxOf(s.unlocked, s.level + 1)
            val reward = when { s.level % 10 == 0 -> Reward.BIG; s.level % 5 == 0 -> Reward.SMALL; else -> Reward.NONE }
            val st = if (prefs.getString("lang", "id") == "en") StringsEn else StringsId
            val praise = when (rating) { 5 -> st.praise5; 4 -> st.praise4; 3 -> st.praise3; 2 -> st.praise2; else -> st.praise1 }
            voice.feedback(rating, prefs.getString("lang", "id") == "en")
            sounds.reward(rating)
            prefs.edit().putInt("pstar_${s.level}", best).putInt("pattern_level", next).putInt("punlocked", next).apply()
            _uiState.update { it.copy(won = true, stars = it.stars + (s.level to best), unlocked = next, reward = reward, confettiTick = it.confettiTick + 1) }
        } else {
            _uiState.update { it.copy(mistakes = it.mistakes + 1) }
            voice.tryAgain(prefs.getString("lang", "id") == "en")
            sounds.tap()
        }
    }

    fun nextLevel() = loadLevel(_uiState.value.level + 1)
    fun replay() = loadLevel(_uiState.value.level)
}
