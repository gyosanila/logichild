package com.gyosanila.kartcilik

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gyosanila.kartcilik.game.Instruction
import com.gyosanila.kartcilik.game.KartState
import com.gyosanila.kartcilik.game.Level
import com.gyosanila.kartcilik.game.Levels
import com.gyosanila.kartcilik.game.Pos
import com.gyosanila.kartcilik.ui.BerryPurple
import com.gyosanila.kartcilik.ui.ConeOrange
import com.gyosanila.kartcilik.ui.FinishBlack
import com.gyosanila.kartcilik.ui.FinishWhite
import com.gyosanila.kartcilik.ui.GrassDark
import com.gyosanila.kartcilik.ui.GrassGreen
import com.gyosanila.kartcilik.ui.KartRed
import com.gyosanila.kartcilik.ui.OceanBlue
import com.gyosanila.kartcilik.ui.ShadowColor
import com.gyosanila.kartcilik.ui.SkyBlue
import com.gyosanila.kartcilik.ui.SunYellow
import com.gyosanila.kartcilik.ui.TextDark
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun KartGameScreen(vm: KartGameViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val level = Levels.all[state.levelIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue)
    ) {
        LevelSelector(
            unlocked = state.unlocked,
            stars = state.stars,
            current = state.levelIndex,
            soundOn = state.soundOn,
            onSelect = vm::selectLevel,
            onToggleSound = vm::toggleSound,
        )
        GameBoard(
            level = level,
            kart = state.kart,
            crashed = state.crashed,
            crashCell = state.crashCell,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
        InstructionStrip(
            instructions = state.instructions,
            enabled = !state.running && !state.won,
            onRemoveLast = vm::removeLast,
            onClear = vm::clearInstructions,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        ControlTray(
            enabled = !state.running && !state.won,
            hasInstructions = state.instructions.isNotEmpty(),
            onAdd = vm::addInstruction,
            onPlay = vm::play,
            onReset = vm::resetKart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }

    if (state.won) {
        WinOverlay(
            levelIndex = state.levelIndex,
            hasNext = state.levelIndex < Levels.all.size - 1,
            confettiTick = state.confettiTick,
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
            translate(cx + shake, cy) {
                rotate(animAngle) {
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
    val w = cell * 0.72f
    val h = cell * 0.72f
    val left = -w / 2f
    val top = -h / 2f
    // bayangan
    drawRoundRect(
        ShadowColor,
        topLeft = Offset(left + 2f, top + 3f),
        size = Size(w, h),
        cornerRadius = CornerRadius(w * 0.25f),
    )
    // badan kart
    drawRoundRect(
        KartRed,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(w * 0.25f),
    )
    // kaca depan
    drawRoundRect(
        Color.White.copy(alpha = 0.85f),
        topLeft = Offset(left + w * 0.25f, top + h * 0.12f),
        size = Size(w * 0.5f, h * 0.28f),
        cornerRadius = CornerRadius(w * 0.1f),
    )
    // panah arah
    val arrow = Path().apply {
        moveTo(0f, -h * 0.58f)
        lineTo(w * 0.28f, -h * 0.12f)
        lineTo(w * 0.1f, -h * 0.12f)
        lineTo(w * 0.1f, h * 0.55f)
        lineTo(-w * 0.1f, h * 0.55f)
        lineTo(-w * 0.1f, -h * 0.12f)
        lineTo(-w * 0.28f, -h * 0.12f)
        close()
    }
    drawPath(arrow, SunYellow)
}

// ─── Pemilih level ────────────────────────────────────────────────

@Composable
private fun LevelSelector(
    unlocked: Int,
    stars: Map<Int, Int>,
    current: Int,
    soundOn: Boolean,
    onSelect: (Int) -> Unit,
    onToggleSound: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Kart Cilik",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(40.dp),
                onClick = onToggleSound,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (soundOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                        contentDescription = "Suara",
                        tint = TextDark,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Levels.all.forEachIndexed { i, lv ->
                val isLocked = i > unlocked
                val starCount = stars[i] ?: 0
                val isCurrent = i == current
                val bg = when {
                    isCurrent -> SunYellow
                    isLocked -> Color(0xFFB9C4CE)
                    starCount > 0 -> Color(0xFFFFF3B0)
                    else -> Color.White.copy(alpha = 0.85f)
                }
                Surface(
                    shape = CircleShape,
                    color = bg,
                    onClick = { if (!isLocked) onSelect(i) },
                    modifier = Modifier
                        .size(if (isCurrent) 44.dp else 36.dp)
                        .border(
                            width = if (isCurrent) 3.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape,
                        ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isLocked) {
                            Icon(
                                Icons.Filled.Lock,
                                null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp),
                            )
                        } else {
                            Text(
                                "${lv.index + 1}",
                                fontSize = if (isCurrent) 18.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Strip instruksi ──────────────────────────────────────────────

@Composable
private fun InstructionStrip(
    instructions: List<Instruction>,
    enabled: Boolean,
    onRemoveLast: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f).height(52.dp),
        ) {
            if (instructions.isEmpty()) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "susun langkah di sini 👇",
                        color = TextDark.copy(alpha = 0.45f),
                        fontSize = 14.sp,
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    instructions.forEach { instr ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = instrColor(instr),
                            modifier = Modifier.size(38.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    instrIcon(instr),
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        IconButton(enabled = enabled && instructions.isNotEmpty(), onClick = onRemoveLast) {
            Icon(Icons.AutoMirrored.Filled.Backspace, "Hapus satu", tint = Color.White)
        }
        IconButton(enabled = enabled && instructions.isNotEmpty(), onClick = onClear) {
            Icon(Icons.Filled.Refresh, "Hapus semua", tint = Color.White)
        }
    }
}

@Composable
private fun IconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = if (enabled) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.4f),
        modifier = Modifier.size(44.dp),
        onClick = { if (enabled) onClick() },
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

// ─── Tray kontrol ─────────────────────────────────────────────────

@Composable
private fun ControlTray(
    enabled: Boolean,
    hasInstructions: Boolean,
    onAdd: (Instruction) -> Unit,
    onPlay: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InstrButton(
            label = "Maju",
            color = KartRed,
            icon = Icons.Filled.ArrowUpward,
            enabled = enabled,
            onClick = { onAdd(Instruction.FORWARD) },
        )
        InstrButton(
            label = "Kiri",
            color = OceanBlue,
            icon = Icons.Filled.RotateLeft,
            enabled = enabled,
            onClick = { onAdd(Instruction.LEFT) },
        )
        Surface(
            shape = CircleShape,
            color = Color(0xFF4CAF50),
            modifier = Modifier.size(84.dp),
            onClick = { if (enabled && hasInstructions) onPlay() },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.PlayArrow,
                    "Main",
                    tint = Color.White,
                    modifier = Modifier.size(52.dp),
                )
            }
        }
        InstrButton(
            label = "Kanan",
            color = BerryPurple,
            icon = Icons.Filled.RotateRight,
            enabled = enabled,
            onClick = { onAdd(Instruction.RIGHT) },
        )
        InstrButton(
            label = "Ulang",
            color = Color(0xFF90A4AE),
            icon = Icons.Filled.Refresh,
            enabled = enabled,
            onClick = onReset,
        )
    }
}

@Composable
private fun InstrButton(
    label: String,
    color: Color,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (enabled) color else color.copy(alpha = 0.45f),
            modifier = Modifier.size(64.dp),
            onClick = { if (enabled) onClick() },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ─── Overlay menang + konfeti ─────────────────────────────────────

@Composable
private fun WinOverlay(
    levelIndex: Int,
    hasNext: Boolean,
    confettiTick: Int,
    onNext: () -> Unit,
    onReplay: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center,
    ) {
        Confetti(seed = confettiTick)
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.padding(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🎉", fontSize = 56.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Level ${levelIndex + 1} Selesai!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                )
                Spacer(Modifier.height(6.dp))
                Icon(Icons.Filled.Star, null, tint = SunYellow, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(18.dp))
                if (hasNext) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF4CAF50),
                        onClick = onNext,
                    ) {
                        Text(
                            "Level berikutnya ▶",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = OceanBlue,
                    onClick = onReplay,
                ) {
                    Text(
                        "Main lagi",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private data class Particle(
    val x: Float,   // 0..1 dari lebar
    val y: Float,   // 0..1 dari tinggi (posisi awal)
    val color: Color,
    val size: Float,
    val delayMs: Long,
    val spin: Float,
)

@Composable
private fun Confetti(seed: Int) {
    val particles = remember(seed) {
        val colors = listOf(KartRed, SunYellow, OceanBlue, GrassGreen, BerryPurple, ConeOrange)
        List(46) { i ->
            Particle(
                x = (i * 37 % 100) / 100f,
                y = (i * 53 % 40) / 100f,
                color = colors[i % colors.size],
                size = 8f + (i % 5) * 3f,
                delayMs = (i % 8) * 90L,
                spin = (i % 7) * 50f,
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(seed) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(2600))
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            val t = (progress.value * 1.25f - p.delayMs / 2600f).coerceIn(0f, 1f)
            if (t > 0f) {
                val x = p.x * w + t * 70f * (if ((p.spin / 50).toInt() % 2 == 0) 1 else -1)
                val y = p.y * h + t * t * h * 1.15f
                rotate(p.spin + t * 420f, pivot = Offset(x + p.size / 2f, y + p.size / 2f)) {
                    drawRect(p.color, Offset(x, y), Size(p.size, p.size * 0.6f))
                }
            }
        }
    }
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
