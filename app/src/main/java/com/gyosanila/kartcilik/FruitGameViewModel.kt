package com.gyosanila.kartcilik

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gyosanila.kartcilik.game.Dir
import com.gyosanila.kartcilik.game.FruitCommand
import com.gyosanila.kartcilik.game.FruitLevel
import com.gyosanila.kartcilik.game.FruitLevelGen
import com.gyosanila.kartcilik.game.Pos
import com.gyosanila.kartcilik.game.Reward
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FruitUiState(
    val level: Int = 1,
    val commands: List<FruitCommand> = emptyList(),
    val robot: Pos = Pos(0, 0),
    val dir: Dir = Dir.S,
    val fruitsLeft: List<Pos> = emptyList(),
    val rocks: List<Pos> = emptyList(),
    val size: Int = 5,
    val maxCommands: Int = 7,
    val running: Boolean = false,
    val won: Boolean = false,
    val crashed: Boolean = false,
    val exhausted: Boolean = false,
    val reward: Reward = Reward.NONE,
)

class FruitGameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
    private val sounds = GameSounds(application)

    private val _uiState = MutableStateFlow(FruitUiState())
    val uiState: StateFlow<FruitUiState> = _uiState.asStateFlow()

    private var runJob: Job? = null

    init {
        sounds.enabled = prefs.getBoolean("sound_on", true)
        val savedLevel = prefs.getInt("fruit_level", 1)
        _uiState.update { it.copy(level = savedLevel) }
        newLevel(savedLevel)
    }

    private fun newLevel(level: Int) {
        // Deterministik per level: level yang sama selalu layout yang sama.
        val lv: FruitLevel = FruitLevelGen.generate(level, Random(level * 104729L))
        _uiState.update {
            it.copy(
                level = level,
                commands = emptyList(),
                robot = lv.start,
                dir = Dir.S,
                fruitsLeft = lv.fruits,
                rocks = lv.rocks,
                size = lv.size,
                maxCommands = lv.maxCommands,
                running = false, won = false, crashed = false, exhausted = false,
                reward = Reward.NONE,
            )
        }
    }

    fun addCommand(c: FruitCommand) {
        val s = _uiState.value
        if (s.running || s.won || s.commands.size >= s.maxCommands) return
        sounds.tap()
        _uiState.update { it.copy(commands = it.commands + c) }
    }

    fun removeLast() {
        val s = _uiState.value
        if (s.running || s.won) return
        sounds.tap()
        _uiState.update { it.copy(commands = it.commands.dropLast(1)) }
    }

    fun clearCommands() {
        val s = _uiState.value
        if (s.running || s.won) return
        sounds.tap()
        _uiState.update { it.copy(commands = emptyList()) }
    }

    /** Robot balik ke start, perintah tetap (anak bisa perbaiki). */
    fun resetRobot() {
        val s = _uiState.value
        if (s.running) return
        _uiState.update {
            it.copy(
                robot = Pos(0, 0),
                dir = Dir.S,
                crashed = false, exhausted = false, won = false,
            )
        }
    }

    /** Setelah nabrak: bersihkan perintah & balik ke start. */
    fun retryAfterCrash() {
        _uiState.update {
            it.copy(
                commands = emptyList(),
                robot = Pos(0, 0),
                dir = Dir.S,
                crashed = false, exhausted = false,
            )
        }
    }

    fun nextLevel() {
        val next = _uiState.value.level + 1
        prefs.edit().putInt("fruit_level", next).apply()
        newLevel(next)
    }

    fun play() {
        val s = _uiState.value
        if (s.running || s.won || s.commands.isEmpty()) return
        runJob = viewModelScope.launch {
            _uiState.update {
                it.copy(running = true, crashed = false, exhausted = false, robot = Pos(0, 0), dir = Dir.S)
            }
            delay(250)
            val cmds = _uiState.value.commands
            for (cmd in cmds) {
                val cur = _uiState.value
                if (cur.crashed) break
                when (cmd) {
                    FruitCommand.FORWARD -> {
                        val nx = cur.robot.x + cur.dir.dx
                        val ny = cur.robot.y + cur.dir.dy
                        if (nx < 0 || ny < 0 || nx >= cur.size || ny >= cur.size || cur.rocks.contains(Pos(nx, ny))) {
                            sounds.crash()
                            _uiState.update { it.copy(running = false, crashed = true) }
                            return@launch
                        }
                        _uiState.update { it.copy(robot = Pos(nx, ny)) }
                        sounds.move()
                    }
                    FruitCommand.LEFT -> {
                        _uiState.update { it.copy(dir = cur.dir.left()) }
                        sounds.turn()
                    }
                    FruitCommand.RIGHT -> {
                        _uiState.update { it.copy(dir = cur.dir.right()) }
                        sounds.turn()
                    }
                    FruitCommand.PICK -> {
                        val picked = cur.fruitsLeft.filter { it == cur.robot }
                        if (picked.isNotEmpty()) {
                            _uiState.update { it.copy(fruitsLeft = it.fruitsLeft - picked.first()) }
                            sounds.move()
                        }
                    }
                }
                delay(420)
            }
            val after = _uiState.value
            if (!after.crashed) {
                if (after.fruitsLeft.isEmpty()) {
                    // Reward: level kelipatan 10 = besar, kelipatan 5 = kecil
                    val reward = when {
                        after.level % 10 == 0 -> Reward.BIG
                        after.level % 10 == 5 -> Reward.SMALL
                        else -> Reward.NONE
                    }
                    when (reward) {
                        Reward.BIG -> sounds.bigWin()
                        Reward.SMALL -> sounds.clap()
                        Reward.NONE -> sounds.win()
                    }
                    prefs.edit().putInt("fruit_level", after.level + 1).apply()
                    _uiState.update { it.copy(running = false, won = true, reward = reward) }
                } else {
                    _uiState.update { it.copy(running = false, exhausted = true) }
                }
            }
        }
    }

    override fun onCleared() {
        sounds.release()
        super.onCleared()
    }
}
