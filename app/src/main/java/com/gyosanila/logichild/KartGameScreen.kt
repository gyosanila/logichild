package com.gyosanila.logichild

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gyosanila.logichild.game.Instruction
import com.gyosanila.logichild.game.KartState
import com.gyosanila.logichild.game.Level
import com.gyosanila.logichild.game.LevelGen
import com.gyosanila.logichild.game.Pos
import com.gyosanila.logichild.game.Reward
import com.gyosanila.logichild.game.GameEngine
import com.gyosanila.logichild.game.StepResult
import com.gyosanila.logichild.ui.AppStrings
import com.gyosanila.logichild.ui.BerryPurple
import com.gyosanila.logichild.ui.ConeOrange
import com.gyosanila.logichild.ui.FinishBlack
import com.gyosanila.logichild.ui.FinishWhite
import com.gyosanila.logichild.ui.GrassDark
import com.gyosanila.logichild.ui.GrassGreen
import com.gyosanila.logichild.ui.KartRed
import com.gyosanila.logichild.ui.LocalStrings
import com.gyosanila.logichild.ui.OceanBlue
import com.gyosanila.logichild.ui.ShadowColor
import com.gyosanila.logichild.ui.SkyBlue
import com.gyosanila.logichild.ui.SunYellow
import com.gyosanila.logichild.ui.TextDark
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

@Composable
fun KartGameScreen(
    startLevel: Int = 1,
    onBack: (() -> Unit)? = null,
    vm: KartGameViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsState()
    val level = LevelGen.generate(state.levelIndex)
    val strings = LocalStrings.current
    val controllerType = rememberControllerType()

    // Level dari roadmap (kalau dipilih) — kalau 0, pakai level terakhir.
    LaunchedEffect(Unit) {
        if (startLevel > 0) vm.selectLevel(startLevel - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue)
    ) {
        Toolbar(
            emoji = "🚗",
            title = strings.playCar,
            soundOn = state.soundOn,
            onToggleSound = vm::toggleSound,
            onBack = onBack,
        )
        Text(
            "${strings.level} ${state.levelIndex + 1}",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        )
        GameBoard(
            level = level,
            kart = state.kart,
            crashed = state.crashed,
            crashCell = state.crashCell,
            instructions = state.instructions,
            // Ghost sesuai mode shadow (auto=1-5, on=semua, off=tidak).
            showGhost = !state.running && !state.won && rememberShadowMode().let { it == "on" || (it == "auto" && state.levelIndex < 5) },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
        GameController(
            controllerType = controllerType,
            dirCmds = listOf(
                CmdSpec(strings.cmdLeft, OceanBlue, Color(0xFF1E88E5), icon = Icons.Filled.RotateLeft, onClick = { vm.addInstruction(Instruction.LEFT) }),
                CmdSpec(strings.cmdForward, KartRed, Color(0xFFE53935), icon = Icons.Filled.ArrowUpward, onClick = { vm.addInstruction(Instruction.FORWARD) }),
                CmdSpec(strings.cmdRight, BerryPurple, Color(0xFF7B4FD8), icon = Icons.Filled.RotateRight, onClick = { vm.addInstruction(Instruction.RIGHT) }),
            ),
            actionCmds = emptyList(),
            steps = state.instructions.map { StepSpec(color = instrColor(it), icon = instrIcon(it)) },
            onRemoveLast = vm::removeLast,
            onPlay = vm::play,
            onReset = vm::resetKart,
            canEdit = !state.running && !state.won,
            playEnabled = !state.running && !state.won && state.instructions.isNotEmpty(),
            resetEnabled = !state.running,
            hintText = strings.hintStripCar,
            deleteLabel = strings.deleteOne,
            strings = strings,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (state.won) {
        val rating = state.stars[state.levelIndex] ?: 0
        val levelNumber = state.levelIndex + 1
        GameWinOverlay(
            emoji = if (state.reward == Reward.BIG) "🎉🎊🎉" else "🎉",
            title = when (state.reward) {
                Reward.BIG -> String.format(strings.winBigTitle, levelNumber)
                Reward.SMALL -> String.format(strings.winSmallTitle, levelNumber)
                Reward.NONE -> String.format(strings.winNoneTitle, levelNumber)
            },
            starRow = "⭐".repeat(rating.coerceIn(0, 5)) + "☆".repeat((5 - rating).coerceIn(0, 5)),
            praise = when (rating) {
                5 -> strings.praise5
                4 -> strings.praise4
                3 -> strings.praise3
                2 -> strings.praise2
                else -> strings.praise1
            },
            showConfetti = true,
            confettiTick = state.confettiTick,
            showNext = true,
            showReplay = true,
            nextLabel = strings.levelNext,
            replayLabel = strings.playAgain,
            onNext = vm::nextLevel,
            onReplay = vm::resetKart,
        )
    }
}

// ─── Papan permainan ──────────────────────────────────────────────

@Composable
private fun GameBoard(
    level: Level,
    kart: KartState,
    crashed: Boolean,
    crashCell: Pos?,
    instructions: List<Instruction>,
    showGhost: Boolean,
    modifier: Modifier = Modifier,
) {
    var shake by remember(crashed) { mutableStateOf(0f) }
    LaunchedEffect(crashed) {
        if (crashed) {
            repeat(5) {
                shake = if (it % 2 == 0) -3f else 3f
                delay(60)
            }
            shake = 0f
        }
    }

    // Shadow/ghost: posisi akhir mobil kalau susunan instruksi dieksekusi.
    val ghost = remember(level, instructions) {
        if (instructions.isEmpty()) {
            null
        } else {
            var g = KartState(level.start, level.startDir)
            var ok = true
            for (i in instructions) {
                val (n, res) = GameEngine.apply(g, i, level)
                g = n
                if (res is StepResult.Crashed) {
                    ok = false
                    break
                }
            }
            if (ok) g else null
        }
    }

    // Posisi mobil dalam UNIT SEL (0..width, 0..height, +0.5 = tengah sel).
    // Scale-invariant: tidak pernah tercampur dp/px.
    val targetCell = Offset(kart.pos.x + 0.5f, kart.pos.y + 0.5f)
    val animCell by animateOffsetAsState(
        targetValue = targetCell,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "kartCell",
    )
    val targetAngle = kart.dir.angleDeg
    val animAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(200),
        label = "kartAngle",
    )

    // key(level.index): ganti level = Canvas baru, mobil tidak "meluncur"
    // dari posisi level sebelumnya.
    key(level.index) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val pad = 10f
            val cell = min(
                (size.width - pad * 2f) / level.width,
                (size.height - pad * 2f) / level.height,
            )
            val ox = (size.width - cell * level.width) / 2f
            val oy = (size.height - cell * level.height) / 2f

            drawBoard(level, cell, ox, oy)

            // Bayangan (shadow) preview posisi akhir
            if (showGhost && ghost != null) {
                val gx = ox + (ghost.pos.x + 0.5f) * cell
                val gy = oy + (ghost.pos.y + 0.5f) * cell
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
                    rotate(ghost.dir.angleDeg, pivot = Offset(gx, gy)) {
                        translate(gx, gy) {
                            drawKart(cell)
                        }
                    }
                    canvas.restore()
                }
            }

            crashCell?.let { c ->
                val cx = ox + (c.x + 0.5f) * cell
                val cy = oy + (c.y + 0.5f) * cell
                drawCircle(Color(0xFFFF5252), cell * 0.5f, Offset(cx, cy))
                // tanda silang putih
                val t = cell * 0.16f
                drawLine(Color.White, Offset(cx - t, cy - t), Offset(cx + t, cy + t), strokeWidth = cell * 0.09f)
                drawLine(Color.White, Offset(cx + t, cy - t), Offset(cx - t, cy + t), strokeWidth = cell * 0.09f)
            }

            val cx = ox + animCell.x * cell
            val cy = oy + animCell.y * cell
            // PENTING: pivot rotasi = pusat mobil, BUKAN pusat canvas.
            // Default DrawScope.rotate = canvas center → mobil yang menghadap
            // E/S/W terlempar keluar sel (bahkan keluar layar).
            rotate(animAngle, pivot = Offset(cx, cy)) {
                translate(cx + shake, cy) {
                    drawKart(cell)
                }
            }
        }
    }
}

private fun DrawScope.drawBoard(level: Level, cell: Float, ox: Float, oy: Float) {
    for (y in 0 until level.height) {
        for (x in 0 until level.width) {
            val color = if ((x + y) % 2 == 0) GrassGreen else GrassDark
            drawRoundRect(
                color = color,
                topLeft = Offset(ox + x * cell + 2f, oy + y * cell + 2f),
                size = Size(cell - 4f, cell - 4f),
                cornerRadius = CornerRadius(10f),
            )
        }
    }
    // Finish: pola bendera catur
    val fx = ox + level.finish.x * cell
    val fy = oy + level.finish.y * cell
    val seg = cell / 4f
    for (i in 0 until 4) {
        for (j in 0 until 4) {
            val color = if ((i + j) % 2 == 0) FinishWhite else FinishBlack
            drawRect(color, Offset(fx + i * seg + 2f, fy + j * seg + 2f), Size(seg, seg))
        }
    }
    // Cone
    for (c in level.cones) {
        val cx = ox + (c.x + 0.5f) * cell
        val cy = oy + (c.y + 0.5f) * cell
        val r = cell * 0.38f
        drawCircle(ShadowColor, r, Offset(cx, cy + r * 0.15f))
        val cone = Path().apply {
            moveTo(cx, cy - r * 1.15f)
            lineTo(cx - r, cy + r * 0.85f)
            lineTo(cx + r, cy + r * 0.85f)
            close()
        }
        drawPath(cone, ConeOrange)
        drawCircle(Color.White, r * 0.28f, Offset(cx, cy + r * 0.1f))
    }
    // Penanda start
    val sx = ox + (level.start.x + 0.5f) * cell
    val sy = oy + (level.start.y + 0.5f) * cell
    drawCircle(
        Color.White.copy(alpha = 0.55f),
        cell * 0.42f,
        Offset(sx, sy),
        style = Stroke(3f),
    )
}

private fun DrawScope.drawKart(cell: Float) {
    val w = cell * 0.66f   // lebar body
    val h = cell * 0.82f   // tinggi body (orientasi: atas = depan)
    val left = -w / 2f
    val top = -h / 2f
    val wheelColor = Color(0xFF37474F)

    // bayangan lembut
    drawRoundRect(
        ShadowColor,
        topLeft = Offset(left + 2f, top + 4f),
        size = Size(w, h + 2f),
        cornerRadius = CornerRadius(w * 0.3f),
    )

    // 4 roda (menonjol keluar body)
    val wheelW = w * 0.16f
    val wheelH = h * 0.24f
    val wx = w * 0.34f
    val wheelYFront = top - wheelH * 0.18f
    val wheelYBack = h * 0.60f
    drawRoundRect(wheelColor, Offset(-wx - wheelW / 2f, wheelYFront), Size(wheelW, wheelH), CornerRadius(wheelW * 0.4f))
    drawRoundRect(wheelColor, Offset(wx - wheelW / 2f, wheelYFront), Size(wheelW, wheelH), CornerRadius(wheelW * 0.4f))
    drawRoundRect(wheelColor, Offset(-wx - wheelW / 2f, wheelYBack), Size(wheelW, wheelH), CornerRadius(wheelW * 0.4f))
    drawRoundRect(wheelColor, Offset(wx - wheelW / 2f, wheelYBack), Size(wheelW, wheelH), CornerRadius(wheelW * 0.4f))

    // body gradien merah
    val bodyBrush = Brush.verticalGradient(listOf(Color(0xFFFF6B70), KartRed, Color(0xFFE53935)))
    drawRoundRect(bodyBrush, Offset(left, top), Size(w, h), CornerRadius(w * 0.32f))

    // garis balap putih di tengah
    drawRoundRect(
        Color.White.copy(alpha = 0.9f),
        topLeft = Offset(left, top + h * 0.42f),
        size = Size(w, h * 0.09f),
        cornerRadius = CornerRadius(w * 0.05f),
    )

    // kaca kokpit
    drawRoundRect(
        Color(0xFFB3E5FC),
        topLeft = Offset(left + w * 0.24f, top + h * 0.10f),
        size = Size(w * 0.52f, h * 0.26f),
        cornerRadius = CornerRadius(w * 0.14f),
    )

    // spoiler belakang
    drawRoundRect(
        Color(0xFFC62828),
        topLeft = Offset(left + w * 0.06f, h * 0.58f),
        size = Size(w * 0.88f, h * 0.18f),
        cornerRadius = CornerRadius(w * 0.10f),
    )

    // panah arah kuning — penunjuk arah utama
    val arrow = Path().apply {
        moveTo(0f, -h * 0.50f)
        lineTo(w * 0.28f, -h * 0.08f)
        lineTo(w * 0.09f, -h * 0.08f)
        lineTo(w * 0.09f, h * 0.50f)
        lineTo(-w * 0.09f, h * 0.50f)
        lineTo(-w * 0.09f, -h * 0.08f)
        lineTo(-w * 0.28f, -h * 0.08f)
        close()
    }
    drawPath(arrow, SunYellow)
}

private fun instrIcon(instr: Instruction) = when (instr) {
    Instruction.FORWARD -> Icons.Filled.ArrowUpward
    Instruction.LEFT -> Icons.Filled.RotateLeft
    Instruction.RIGHT -> Icons.Filled.RotateRight
}

private fun instrColor(instr: Instruction) = when (instr) {
    Instruction.FORWARD -> KartRed
    Instruction.LEFT -> OceanBlue
    Instruction.RIGHT -> BerryPurple
}
