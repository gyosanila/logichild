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
import androidx.compose.foundation.layout.safeDrawingPadding
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
import com.gyosanila.logichild.game.Dir
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
    /** Non-null = dibuka dari Petualangan: tanpa label level/peta, auto balik bawa rating. */
    onAdventureDone: ((rating: Int) -> Unit)? = null,
    vm: KartGameViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsState()
    val level = LevelGen.generate(state.levelIndex)
    val strings = LocalStrings.current
    val controllerType = rememberControllerType()

    // Level dari roadmap (kalau dipilih) — kalau 0, pakai level terakhir.
    LaunchedEffect(Unit) {
        if (startLevel > 0) vm.selectLevel(startLevel - 1, force = onAdventureDone != null)
    }

    Box(Modifier.fillMaxSize().background(SkyBlue)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
        Toolbar(
            emoji = "🚗",
            title = strings.playCar,
            soundOn = state.soundOn,
            onToggleSound = vm::toggleSound,
            onBack = if (onAdventureDone != null) onBack else null,
            onOpenMap = if (onAdventureDone == null) onBack else null,
        )
        if (onAdventureDone == null) Text(
            "${strings.level} ${state.levelIndex + 1}",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        )
        val showHint = level.index <= 2 || state.failureCount >= 3
        val boardMessage = when {
            state.crashed && state.crashCell in level.cones -> strings.carCrashCone
            state.crashed -> strings.carCrashEdge
            showHint -> strings.carHintForward
            else -> null
        }
        if (boardMessage != null) {
            Surface(
                color = if (state.crashed) Color(0xFFFFE0E4) else Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 2.dp),
            ) {
                Text(
                    boardMessage,
                    color = if (state.crashed) Color(0xFFB3263D) else TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                )
            }
        }
        GameBoard(
            level = level,
            kart = state.kart,
            crashed = state.crashed,
            crashCell = state.crashCell,
            instructions = state.instructions,
            // Ghost sesuai mode shadow (auto=1-5, on=semua, off=tidak).
            showGhost = !state.running && !state.won && rememberShadowMode().let { it == "on" || (it == "auto" && state.levelIndex < 5) },
            showHint = showHint && !state.running && !state.won,
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
            steps = state.instructions.mapIndexed { index, instruction ->
                StepSpec(color = instrColor(instruction), icon = instrIcon(instruction), failed = index == state.failedInstructionIndex)
            },
            onRemoveLast = vm::removeLast,
            onPlay = vm::play,
            onReset = vm::resetKart,
            canEdit = !state.running && !state.won && !state.crashed,
            playEnabled = !state.running && !state.won && !state.crashed && state.instructions.isNotEmpty(),
            resetEnabled = !state.running,
            resetSuggested = state.crashed,
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
            showNext = onAdventureDone == null,
            showReplay = onAdventureDone == null,
            nextLabel = strings.levelNext,
            replayLabel = strings.playAgain,
            onNext = vm::nextLevel,
            onReplay = vm::resetKart,
        )
    }
    if (onAdventureDone != null) {
        LaunchedEffect(state.won) {
            if (state.won) {
                delay(ADVENTURE_REWARD_MS)
                onAdventureDone(state.lastRating.coerceIn(1, 5))
            }
        }
}    }
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
    showHint: Boolean,
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
    val targetAngle = isoAngle(kart.dir)
    val animAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(200),
        label = "kartAngle",
    )

    // key(level.index): ganti level = Canvas baru, mobil tidak "meluncur"
    // dari posisi level sebelumnya.
    key(level.index) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val pad = 12f
            val tile = min(
                (size.width - pad * 2f) * 2f / (level.width + level.height),
                (size.height - pad * 2f) / ((level.width + level.height) * 0.25f + 0.36f),
            )
            val depth = tile * 0.32f
            val boardW = (level.width + level.height) * tile * 0.5f
            val boardH = (level.width + level.height) * tile * 0.25f + depth
            val left = (size.width - boardW) / 2f
            val top = (size.height - boardH) / 2f
            val originX = left + tile * 0.5f + (level.height - 1) * tile * 0.5f
            val originY = top + tile * 0.25f
            fun center(x: Float, y: Float) = Offset(
                originX + (x - y) * tile * 0.5f,
                originY + (x + y) * tile * 0.25f,
            )

            drawBoard(level, tile, originX, originY)

            if (showHint) {
                val route = findHintRoute(level, kart.pos).take(2)
                route.forEachIndexed { index, step ->
                    val p = center(step.first.x.toFloat(), step.first.y.toFloat())
                    drawHintTile(p, tile, step.second, 1f - index * 0.18f)
                }
            }

            // Bayangan (shadow) preview posisi akhir
            if (showGhost && ghost != null) {
                val ghostCenter = center(ghost.pos.x.toFloat(), ghost.pos.y.toFloat())
                val gx = ghostCenter.x
                val gy = ghostCenter.y
                drawCircle(
                    SunYellow.copy(alpha = 0.45f),
                    tile * 0.28f,
                    Offset(gx, gy),
                    style = Stroke(tile * 0.035f),
                )
                drawIntoCanvas { canvas ->
                    canvas.saveLayer(
                        Rect(gx - tile, gy - tile, gx + tile, gy + tile),
                        Paint().apply { alpha = 0.35f },
                    )
                    rotate(isoAngle(ghost.dir), pivot = Offset(gx, gy)) {
                        translate(gx, gy) {
                            drawKart(tile * 0.66f)
                        }
                    }
                    canvas.restore()
                }
            }

            crashCell?.let { c ->
                val p = center(c.x.toFloat(), c.y.toFloat())
                val cx = p.x
                val cy = p.y
                drawCircle(Color(0xFFFF5252), tile * 0.34f, Offset(cx, cy), style = Stroke(tile * 0.07f))
                // tanda silang putih
                val t = tile * 0.11f
                drawLine(Color.White, Offset(cx - t, cy - t), Offset(cx + t, cy + t), strokeWidth = tile * 0.045f)
                drawLine(Color.White, Offset(cx + t, cy - t), Offset(cx - t, cy + t), strokeWidth = tile * 0.045f)
            }

            val kartCenter = center(animCell.x - 0.5f, animCell.y - 0.5f)
            val cx = kartCenter.x
            val cy = kartCenter.y
            // PENTING: pivot rotasi = pusat mobil, BUKAN pusat canvas.
            // Default DrawScope.rotate = canvas center → mobil yang menghadap
            // E/S/W terlempar keluar sel (bahkan keluar layar).
            rotate(animAngle, pivot = Offset(cx, cy)) {
                translate(cx + shake, cy) {
                    drawKart(tile * 0.66f)
                }
            }
        }
    }
}

private fun findHintRoute(level: Level, from: Pos): List<Pair<Pos, Dir>> {
    val queue = ArrayDeque<Pos>()
    val previous = mutableMapOf<Pos, Pair<Pos, Dir>?>()
    queue.add(from)
    previous[from] = null
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (current == level.finish) break
        for (dir in Dir.entries) {
            val next = Pos(current.x + dir.dx, current.y + dir.dy)
            if (next.x !in 0 until level.width || next.y !in 0 until level.height || next in level.cones || next in previous) continue
            previous[next] = current to dir
            queue.addLast(next)
        }
    }
    if (level.finish !in previous) return emptyList()
    val reversed = mutableListOf<Pair<Pos, Dir>>()
    var cursor = level.finish
    while (cursor != from) {
        val link = previous[cursor] ?: break
        reversed += cursor to link.second
        cursor = link.first
    }
    return reversed.asReversed()
}

private fun DrawScope.drawHintTile(center: Offset, tile: Float, dir: Dir, alpha: Float) {
    drawIsoDiamond(center, tile * 0.58f, Color.White.copy(alpha = 0.94f * alpha), Color(0xFFE03B70).copy(alpha = alpha))
    val (dx, dy) = when (dir) {
        Dir.E -> 0.894f to 0.447f
        Dir.N -> 0.894f to -0.447f
        Dir.S -> -0.894f to 0.447f
        Dir.W -> -0.894f to -0.447f
    }
    val px = -dy
    val py = dx
    val len = tile * 0.18f
    val half = tile * 0.075f
    val tip = Offset(center.x + dx * len, center.y + dy * len)
    val base = Offset(center.x - dx * len * 0.65f, center.y - dy * len * 0.65f)
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(base.x + px * half, base.y + py * half)
        lineTo(base.x + px * half * 0.45f, base.y + py * half * 0.45f)
        lineTo(center.x - dx * len * 0.12f + px * half * 0.45f, center.y - dy * len * 0.12f + py * half * 0.45f)
        lineTo(center.x - dx * len * 0.12f - px * half * 0.45f, center.y - dy * len * 0.12f - py * half * 0.45f)
        lineTo(base.x - px * half, base.y - py * half)
        close()
    }
    drawPath(path, Color(0xFFD72660).copy(alpha = alpha))
}

private fun isoAngle(dir: Dir): Float = when (dir) {
    Dir.N -> 63.4f
    Dir.E -> 116.6f
    Dir.S -> 243.4f
    Dir.W -> 296.6f
}

private fun DrawScope.drawBoard(level: Level, tile: Float, originX: Float, originY: Float) {
    fun center(x: Float, y: Float) = Offset(
        originX + (x - y) * tile * 0.5f,
        originY + (x + y) * tile * 0.25f,
    )

    // Draw from back to front so the raised isometric faces layer naturally.
    for (sum in 0..(level.width + level.height - 2)) {
        for (y in 0 until level.height) {
            val x = sum - y
            if (x !in 0 until level.width) continue
            val p = center(x.toFloat(), y.toFloat())
            drawIsoBlock(p, tile, Color(0xFF9BE3A5), Color(0xFF6CC97C), Color(0xFF58B169))
        }
    }

    // Start marker: blue tile inset, finish: pink tile inset + checkered flag.
    val start = center(level.start.x.toFloat(), level.start.y.toFloat())
    drawIsoDiamond(start, tile * 0.68f, Color(0xFF75C8F4), Color(0xFF4CA6D8))
    val goal = center(level.finish.x.toFloat(), level.finish.y.toFloat())
    drawIsoDiamond(goal, tile * 0.68f, Color(0xFFFFA5C0), Color(0xFFE66F96))
    val poleX = goal.x + tile * 0.12f
    val poleY = goal.y - tile * 0.52f
    drawLine(Color(0xFF795548), Offset(poleX, poleY), Offset(poleX, goal.y - tile * 0.08f), tile * 0.035f)
    val flag = Path().apply {
        moveTo(poleX, poleY)
        lineTo(poleX + tile * 0.30f, poleY + tile * 0.07f)
        lineTo(poleX, poleY + tile * 0.20f)
        close()
    }
    drawPath(flag, Color.White)
    drawLine(Color(0xFF222222), Offset(poleX + tile * 0.10f, poleY + tile * 0.035f), Offset(poleX + tile * 0.20f, poleY + tile * 0.06f), tile * 0.045f)
    drawLine(Color(0xFF222222), Offset(poleX + tile * 0.10f, poleY + tile * 0.12f), Offset(poleX + tile * 0.20f, poleY + tile * 0.095f), tile * 0.045f)

    // Cones are the only obstacles; keep decorative plants off the playable tiles.
    for (cone in level.cones) {
        val p = center(cone.x.toFloat(), cone.y.toFloat())
        drawOval(ShadowColor.copy(alpha = 0.35f), topLeft = Offset(p.x - tile * 0.22f, p.y + tile * 0.10f), size = Size(tile * 0.44f, tile * 0.18f))
        val body = Path().apply {
            moveTo(p.x, p.y - tile * 0.33f)
            lineTo(p.x - tile * 0.23f, p.y + tile * 0.15f)
            quadraticTo(p.x, p.y + tile * 0.22f, p.x + tile * 0.23f, p.y + tile * 0.15f)
            close()
        }
        drawPath(body, Color(0xFFFF8A32))
        drawLine(Color.White, Offset(p.x - tile * 0.13f, p.y + tile * 0.01f), Offset(p.x + tile * 0.13f, p.y + tile * 0.01f), tile * 0.07f)
        drawLine(Color(0xFFB9521B), Offset(p.x - tile * 0.23f, p.y + tile * 0.15f), Offset(p.x + tile * 0.23f, p.y + tile * 0.15f), tile * 0.045f)
    }
}

private fun DrawScope.drawIsoBlock(center: Offset, tile: Float, top: Color, left: Color, right: Color) {
    val halfH = tile * 0.25f
    val depth = tile * 0.32f
    val north = Offset(center.x, center.y - halfH)
    val east = Offset(center.x + tile * 0.5f, center.y)
    val south = Offset(center.x, center.y + halfH)
    val west = Offset(center.x - tile * 0.5f, center.y)
    val leftFace = Path().apply {
        moveTo(west.x, west.y); lineTo(south.x, south.y)
        lineTo(south.x, south.y + depth); lineTo(west.x, west.y + depth); close()
    }
    val rightFace = Path().apply {
        moveTo(east.x, east.y); lineTo(south.x, south.y)
        lineTo(south.x, south.y + depth); lineTo(east.x, east.y + depth); close()
    }
    drawPath(leftFace, left)
    drawPath(rightFace, right)
    val topFace = Path().apply {
        moveTo(north.x, north.y); lineTo(east.x, east.y); lineTo(south.x, south.y); lineTo(west.x, west.y); close()
    }
    drawPath(topFace, top)
    drawPath(topFace, Color(0xFF4FA062), style = Stroke(width = tile * 0.012f))
}

private fun DrawScope.drawIsoDiamond(center: Offset, tile: Float, top: Color, side: Color) {
    val h = tile * 0.25f
    val path = Path().apply {
        moveTo(center.x, center.y - h); lineTo(center.x + tile * 0.5f, center.y)
        lineTo(center.x, center.y + h); lineTo(center.x - tile * 0.5f, center.y); close()
    }
    drawPath(path, side)
    drawPath(path, top, style = Stroke(tile * 0.035f))
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
