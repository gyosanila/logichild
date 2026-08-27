package com.gyosanila.logichild

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gyosanila.logichild.game.Dir
import com.gyosanila.logichild.game.FruitCommand
import com.gyosanila.logichild.game.Pos
import com.gyosanila.logichild.game.Reward
import com.gyosanila.logichild.ui.AppStrings
import com.gyosanila.logichild.ui.BerryPurple
import com.gyosanila.logichild.ui.ConeOrange
import com.gyosanila.logichild.ui.GrassDark
import com.gyosanila.logichild.ui.GrassGreen
import com.gyosanila.logichild.ui.KartRed
import com.gyosanila.logichild.ui.LocalStrings
import com.gyosanila.logichild.ui.OceanBlue
import com.gyosanila.logichild.ui.RoadGray
import com.gyosanila.logichild.ui.ShadowColor
import com.gyosanila.logichild.ui.SkyBlue
import com.gyosanila.logichild.ui.SunYellow
import com.gyosanila.logichild.ui.TextDark
import kotlin.math.max
import kotlin.math.min

private fun FruitCommand.emoji(): String = when (this) {
    FruitCommand.FORWARD -> "⬆️"
    FruitCommand.LEFT -> "↩️"
    FruitCommand.RIGHT -> "↪️"
    FruitCommand.PICK -> "🍎"
}

private fun FruitCommand.label(): String = when (this) {
    FruitCommand.FORWARD -> "Maju"
    FruitCommand.LEFT -> "Kiri"
    FruitCommand.RIGHT -> "Kanan"
    FruitCommand.PICK -> "Petik"
}

private fun FruitCommand.color(): Color = when (this) {
    FruitCommand.FORWARD -> OceanBlue
    FruitCommand.LEFT -> ConeOrange
    FruitCommand.RIGHT -> BerryPurple
    FruitCommand.PICK -> KartRed
}

@Composable
fun FruitGameScreen(
    onBack: () -> Unit,
    vm: FruitGameViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsState()
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue)
    ) {
        Toolbar(
            emoji = "🍎",
            title = strings.playFruit,
            onBack = onBack,
        )

        LevelSelector(
            itemCount = maxOf(state.unlocked, state.level),
            currentIndex = state.level - 1,
            onSelect = { vm.selectLevel(it + 1) },
        )

        FruitBoard(
            state = state,
            // Ghost sesuai mode shadow (auto=1-5, on=semua, off=tidak).
            showGhost = !state.running && !state.won && rememberShadowMode().let { it == "on" || (it == "auto" && state.level <= 5) },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )

        GameController(
            controllerType = rememberControllerType(),
            dirCmds = listOf(
                CmdSpec(strings.cmdLeft, OceanBlue, Color(0xFF1E88E5), icon = Icons.Filled.RotateLeft, onClick = { vm.addCommand(FruitCommand.LEFT) }),
                CmdSpec(strings.cmdForward, KartRed, Color(0xFFE53935), icon = Icons.Filled.ArrowUpward, onClick = { vm.addCommand(FruitCommand.FORWARD) }),
                CmdSpec(strings.cmdRight, BerryPurple, Color(0xFF7B4FD8), icon = Icons.Filled.RotateRight, onClick = { vm.addCommand(FruitCommand.RIGHT) }),
            ),
            actionCmds = listOf(
                CmdSpec(strings.cmdPick, ConeOrange, Color(0xFFF57C00), emoji = "🍎", onClick = { vm.addCommand(FruitCommand.PICK) }),
            ),
            steps = state.commands.map { StepSpec(color = it.color(), emoji = it.emoji()) },
            onRemoveLast = vm::removeLast,
            onPlay = vm::play,
            onReset = vm::resetAll,
            canEdit = !state.running && !state.won && state.commands.size < state.maxCommands,
            playEnabled = !state.running && !state.won && state.commands.isNotEmpty(),
            resetEnabled = !state.running,
            hintText = strings.hintStripFruit,
            deleteLabel = strings.deleteOne,
            strings = strings,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (state.won) {
        val rating = state.stars[state.level] ?: 0
        GameWinOverlay(
            emoji = if (state.reward == Reward.BIG) "🎉🎊🎉" else "🎉",
            title = when (state.reward) {
                Reward.BIG -> String.format(strings.winBigTitle, state.level)
                Reward.SMALL -> String.format(strings.winSmallTitle, state.level)
                Reward.NONE -> String.format(strings.winNoneTitle, state.level)
            },
            starRow = "⭐".repeat(rating.coerceIn(0, 5)) + "☆".repeat((5 - rating).coerceIn(0, 5)),
            praise = when (rating) {
                5 -> strings.praise5
                4 -> strings.praise4
                3 -> strings.praise3
                2 -> strings.praise2
                else -> strings.praise1
            },
            showConfetti = false,
            confettiTick = 0,
            showNext = true,
            showReplay = false,
            nextLabel = strings.levelNext,
            replayLabel = strings.playAgain,
            onNext = vm::nextLevel,
            onReplay = {},
        )
    }
    if (state.crashed) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(strings.crashTitle) },
            text = { Text(strings.crashMsg) },
            confirmButton = {
                TextButton(onClick = vm::retryAfterCrash) { Text(strings.retry) }
            },
        )
    }
    if (state.exhausted) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(strings.exhaustedTitle) },
            text = { Text(strings.exhaustedMsg) },
            confirmButton = {
                TextButton(onClick = vm::resetRobot) { Text(strings.keepBuilding) }
            },
        )
    }
}

// ─── Papan permainan ──────────────────────────────────────────────

@Composable
private fun FruitBoard(state: FruitUiState, showGhost: Boolean, modifier: Modifier = Modifier) {
    val targetCell = Offset(state.robot.x + 0.5f, state.robot.y + 0.5f)
    val animCell by animateOffsetAsState(
        targetValue = targetCell,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "robotCell",
    )

    // Shadow/ghost: posisi akhir robot kalau perintah dieksekusi (PICK diabaikan).
    val ghost = remember(state.level, state.commands) {
        if (state.commands.isEmpty()) {
            null
        } else {
            var robot = Pos(0, 0)
            var dir = Dir.S
            var ok = true
            for (cmd in state.commands) {
                when (cmd) {
                    FruitCommand.FORWARD -> {
                        val nx = robot.x + dir.dx
                        val ny = robot.y + dir.dy
                        if (nx < 0 || ny < 0 || nx >= state.size || ny >= state.size || state.rocks.contains(Pos(nx, ny))) {
                            ok = false
                            break
                        }
                        robot = Pos(nx, ny)
                    }
                    FruitCommand.LEFT -> dir = dir.left()
                    FruitCommand.RIGHT -> dir = dir.right()
                    FruitCommand.PICK -> {}
                }
            }
            if (ok) robot to dir else null
        }
    }

    // key(level): ganti level = Canvas baru, robot tidak "meluncur" dari posisi lama
    key(state.level) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val pad = 10f
            val cell = min(
                (size.width - pad * 2f) / state.size,
                (size.height - pad * 2f) / state.size,
            )
            val ox = (size.width - cell * state.size) / 2f
            val oy = (size.height - cell * state.size) / 2f

            drawFruitBoard(state, cell, ox, oy)

            // Bayangan (shadow) preview posisi akhir
            if (showGhost && ghost != null) {
                val gx = ox + (ghost.first.x + 0.5f) * cell
                val gy = oy + (ghost.first.y + 0.5f) * cell
                drawCircle(
                    SunYellow.copy(alpha = 0.45f),
                    cell * 0.46f,
                    Offset(gx, gy),
                    style = Stroke(cell * 0.06f),
                )
                drawIntoCanvas { canvas ->
                    canvas.saveLayer(
                        Rect(gx - cell, gy - cell, gx + cell, gy + cell),
                        Paint().apply { alpha = 0.35f },
                    )
                    translate(gx, gy) {
                        drawFruitRobot(cell, ghost.second)
                    }
                    canvas.restore()
                }
            }

            val cx = ox + animCell.x * cell
            val cy = oy + animCell.y * cell
            translate(cx, cy) {
                drawFruitRobot(cell, state.dir)
            }
        }
    }
}

private fun DrawScope.drawFruitBoard(state: FruitUiState, cell: Float, ox: Float, oy: Float) {
    // Grid
    for (y in 0 until state.size) {
        for (x in 0 until state.size) {
            val color = if ((x + y) % 2 == 0) GrassGreen else GrassDark
            drawRoundRect(
                color = color,
                topLeft = Offset(ox + x * cell + 2f, oy + y * cell + 2f),
                size = Size(cell - 4f, cell - 4f),
                cornerRadius = CornerRadius(10f),
            )
        }
    }
    // Batu — bongkahan batu dengan sisi-sisi (facets)
    for (r in state.rocks) {
        val cx = ox + (r.x + 0.5f) * cell
        val cy = oy + (r.y + 0.5f) * cell
        drawRock(cx, cy, cell)
    }
    // Buah — bentuk buah beneran (badan + tangkai + daun)
    val fruitColors = listOf(KartRed, SunYellow, BerryPurple, ConeOrange)
    state.fruitsLeft.forEachIndexed { i, f ->
        val cx = ox + (f.x + 0.5f) * cell
        val cy = oy + (f.y + 0.5f) * cell
        drawFruit(cx, cy, cell, fruitColors[i % fruitColors.size])
    }
    // Penanda start
    val sx = ox + 0.5f * cell
    val sy = oy + 0.5f * cell
    drawCircle(
        Color.White.copy(alpha = 0.55f),
        cell * 0.42f,
        Offset(sx, sy),
        style = Stroke(3f),
    )
}

/** Batu: poligon tidak beraturan + sisi terang, keliatan kayak bongkahan. */
private fun DrawScope.drawRock(cx: Float, cy: Float, cell: Float) {
    val r = cell * 0.30f
    // bayangan di tanah
    drawOval(
        ShadowColor,
        topLeft = Offset(cx - r * 0.85f, cy + r * 0.55f),
        size = Size(r * 1.7f, r * 0.5f),
    )
    val rock = Path().apply {
        moveTo(cx - r, cy - r * 0.15f)
        lineTo(cx - r * 0.55f, cy - r * 0.75f)
        lineTo(cx + r * 0.25f, cy - r * 0.85f)
        lineTo(cx + r * 0.85f, cy - r * 0.25f)
        lineTo(cx + r * 0.75f, cy + r * 0.55f)
        lineTo(cx + r * 0.1f, cy + r * 0.8f)
        lineTo(cx - r * 0.7f, cy + r * 0.6f)
        close()
    }
    drawPath(rock, RoadGray)
    // sisi atas yang kena cahaya
    val facet = Path().apply {
        moveTo(cx - r * 0.55f, cy - r * 0.75f)
        lineTo(cx - r * 0.05f, cy - r * 0.35f)
        lineTo(cx + r * 0.15f, cy + r * 0.2f)
        lineTo(cx - r * 0.7f, cy + r * 0.05f)
        close()
    }
    drawPath(facet, Color(0xFFB0BEC5))
}

/** Buah: badan bulat + tangkai + daun — jelas kebaca buah, bukan dot. */
private fun DrawScope.drawFruit(cx: Float, cy: Float, cell: Float, color: Color) {
    val r = cell * 0.21f
    // bayangan
    drawOval(
        ShadowColor,
        topLeft = Offset(cx - r, cy + r * 0.55f),
        size = Size(r * 2f, r * 0.6f),
    )
    // badan buah
    drawCircle(color, r, Offset(cx, cy))
    // tangkai
    drawLine(
        Color(0xFF795548),
        Offset(cx, cy - r * 0.75f),
        Offset(cx + r * 0.22f, cy - r * 1.15f),
        strokeWidth = cell * 0.045f,
    )
    // daun
    drawOval(
        Color(0xFF66BB6A),
        topLeft = Offset(cx + r * 0.35f, cy - r * 1.25f),
        size = Size(r * 0.75f, r * 0.42f),
    )
    // kilau
    drawCircle(Color.White.copy(alpha = 0.75f), r * 0.28f, Offset(cx - r * 0.38f, cy - r * 0.42f))
}

/** Robot pemetik: antena, badan metalik, wajah putih, mata besar, senyum, panah arah. */
private fun DrawScope.drawFruitRobot(cell: Float, dir: Dir) {
    val s = cell * 0.58f          // setengah lebar body
    val w = s * 2f
    val bodyH = s * 1.75f
    val top = -bodyH * 0.5f
    val metal = Color(0xFF64B5F6)
    val metalDark = Color(0xFF42A5F5)

    // antena
    drawLine(
        metalDark,
        Offset(0f, top),
        Offset(0f, top - s * 0.5f),
        strokeWidth = cell * 0.05f,
    )
    drawCircle(Color(0xFFEF5350), cell * 0.07f, Offset(0f, top - s * 0.55f))

    // bayangan
    drawRoundRect(
        ShadowColor,
        Offset(-s + 2f, top + 4f),
        Size(w, bodyH),
        CornerRadius(s * 0.35f),
    )

    // body metalik
    drawRoundRect(
        Brush.verticalGradient(listOf(metal, metalDark)),
        Offset(-s, top),
        Size(w, bodyH),
        CornerRadius(s * 0.35f),
    )

    // panel perut putih + tombol
    drawRoundRect(
        Color.White.copy(alpha = 0.85f),
        Offset(-s * 0.55f, top + bodyH * 0.60f),
        Size(w * 0.55f, bodyH * 0.24f),
        CornerRadius(s * 0.12f),
    )
    drawCircle(Color(0xFFEF5350), s * 0.09f, Offset(-s * 0.42f, top + bodyH * 0.76f))
    drawCircle(Color(0xFF66BB6A), s * 0.09f, Offset(s * 0.42f, top + bodyH * 0.76f))

    // wajah putih
    val faceY = top + s * 0.22f
    drawRoundRect(
        Color.White,
        Offset(-s * 0.72f, faceY),
        Size(w * 0.72f, s * 0.95f),
        CornerRadius(s * 0.3f),
    )

    // mata besar — pupil mengikuti arah hadap
    val eyeY = faceY + s * 0.30f
    val ex = s * 0.24f
    drawCircle(Color.White, s * 0.20f, Offset(-ex, eyeY))
    drawCircle(Color.White, s * 0.20f, Offset(ex, eyeY))
    val pr = s * 0.10f
    val look = s * 0.10f
    drawCircle(TextDark, pr, Offset(-ex + dir.dx * look, eyeY + dir.dy * look))
    drawCircle(TextDark, pr, Offset(ex + dir.dx * look, eyeY + dir.dy * look))

    // senyum
    drawArc(
        Color(0xFFEF5350),
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(-s * 0.22f, eyeY + s * 0.16f),
        size = Size(s * 0.44f, s * 0.30f),
        style = Stroke(cell * 0.04f),
    )

    // panah arah kuning di atas — jelas menunjuk ke mana robot menghadap
    val a = cell * 0.22f
    val arrow = Path().apply {
        moveTo(0f, top - s * 1.35f)
        lineTo(a, top - s * 0.95f)
        lineTo(a * 0.45f, top - s * 0.95f)
        lineTo(a * 0.45f, top - s * 0.62f)
        lineTo(-a * 0.45f, top - s * 0.62f)
        lineTo(-a * 0.45f, top - s * 0.95f)
        lineTo(-a, top - s * 0.95f)
        close()
    }
    rotate(dir.angleDeg, pivot = Offset(0f, 0f)) {
        drawPath(arrow, SunYellow)
    }
}

