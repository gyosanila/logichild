package com.gyosanila.kartcilik

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gyosanila.kartcilik.game.Instruction
import com.gyosanila.kartcilik.game.KartState
import com.gyosanila.kartcilik.game.Level
import com.gyosanila.kartcilik.game.LevelGen
import com.gyosanila.kartcilik.game.Pos
import com.gyosanila.kartcilik.game.Reward
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
import kotlin.math.max
import kotlin.math.min

@Composable
fun KartGameScreen(
    onBack: (() -> Unit)? = null,
    vm: KartGameViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsState()
    val level = LevelGen.generate(state.levelIndex)

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
            onBack = onBack,
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
            hasNext = true,
            confettiTick = state.confettiTick,
            reward = state.reward,
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

// ─── Pemilih level ────────────────────────────────────────────────

@Composable
private fun LevelSelector(
    unlocked: Int,
    stars: Map<Int, Int>,
    current: Int,
    soundOn: Boolean,
    onSelect: (Int) -> Unit,
    onToggleSound: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "🚗 Main Mobil",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            if (onBack != null) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(40.dp),
                    onClick = onBack,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = "Menu",
                            tint = TextDark,
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
            }
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
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Level tak terbatas → tampilkan jendela 8 level di sekitar level aktif.
            val window = 8
            val total = unlocked + 1
            val start = max(0, min(current - window / 2, total - window))
            val end = min(total, start + window)

            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = if (start > 0) 0.9f else 0.35f),
                onClick = { if (start > 0) onSelect(start - 1) },
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.KeyboardArrowLeft, "Mundur", tint = TextDark)
                }
            }
            (start until end).forEach { i ->
                val starCount = stars[i] ?: 0
                val isCurrent = i == current
                val bg = when {
                    isCurrent -> SunYellow
                    starCount > 0 -> Color(0xFFFFF3B0)
                    else -> Color.White.copy(alpha = 0.85f)
                }
                Surface(
                    shape = CircleShape,
                    color = bg,
                    onClick = { onSelect(i) },
                    modifier = Modifier
                        .size(if (isCurrent) 40.dp else 32.dp)
                        .border(
                            width = if (isCurrent) 3.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape,
                        ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "${i + 1}",
                            fontSize = if (isCurrent) 16.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                        )
                    }
                }
            }
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = if (end < total) 0.9f else 0.35f),
                onClick = { if (end < total) onSelect(end) },
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.KeyboardArrowRight, "Maju", tint = TextDark)
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
            Icon(
                Icons.AutoMirrored.Filled.Backspace,
                "Hapus satu",
                tint = if (enabled && instructions.isNotEmpty()) TextDark else TextDark.copy(alpha = 0.35f),
            )
        }
        IconButton(enabled = enabled && instructions.isNotEmpty(), onClick = onClear) {
            Icon(
                Icons.Filled.Refresh,
                "Hapus semua",
                tint = if (enabled && instructions.isNotEmpty()) TextDark else TextDark.copy(alpha = 0.35f),
            )
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
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Panel 1: arah permainan
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White.copy(alpha = 0.18f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                BigRoundButton(
                    label = "Maju",
                    color = KartRed,
                    darker = Color(0xFFE53935),
                    icon = Icons.Filled.ArrowUpward,
                    enabled = enabled,
                    size = 76.dp,
                    onClick = { onAdd(Instruction.FORWARD) },
                )
                Spacer(Modifier.width(22.dp))
                BigRoundButton(
                    label = "Kiri",
                    color = OceanBlue,
                    darker = Color(0xFF1E88E5),
                    icon = Icons.Filled.RotateLeft,
                    enabled = enabled,
                    size = 76.dp,
                    onClick = { onAdd(Instruction.LEFT) },
                )
                Spacer(Modifier.width(22.dp))
                BigRoundButton(
                    label = "Kanan",
                    color = BerryPurple,
                    darker = Color(0xFF7B4FD8),
                    icon = Icons.Filled.RotateRight,
                    enabled = enabled,
                    size = 76.dp,
                    onClick = { onAdd(Instruction.RIGHT) },
                )
                Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(12.dp))
        // Panel 2: action (play & reset) — panel terpisah, beda warna
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White.copy(alpha = 0.26f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                BigRoundButton(
                    label = "Main!",
                    color = Color(0xFF66BB6A),
                    darker = Color(0xFF2E7D32),
                    icon = Icons.Filled.PlayArrow,
                    enabled = enabled && hasInstructions,
                    size = 96.dp,
                    iconSize = 58.dp,
                    shadow = 10.dp,
                    fontSize = 15.sp,
                    onClick = onPlay,
                )
                Spacer(Modifier.width(26.dp))
                BigRoundButton(
                    label = "Ulang",
                    color = Color(0xFF90A4AE),
                    darker = Color(0xFF607D8B),
                    icon = Icons.Filled.Refresh,
                    enabled = enabled,
                    size = 64.dp,
                    iconSize = 32.dp,
                    shadow = 5.dp,
                    onClick = onReset,
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun BigRoundButton(
    label: String,
    color: Color,
    darker: Color,
    icon: ImageVector? = null,
    emoji: String? = null,
    enabled: Boolean = true,
    size: Dp = 72.dp,
    iconSize: Dp = 36.dp,
    shadow: Dp = 6.dp,
    fontSize: TextUnit = 12.sp,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = tween(90),
        label = "btnScale",
    )
    val bg = Brush.verticalGradient(listOf(color, darker))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(shadow, CircleShape)
                .background(if (enabled) bg else Brush.verticalGradient(listOf(Color(0xFFB0BEC5), Color(0xFF90A4AE))), CircleShape)
                .border(
                    width = if (enabled) 0.dp else 2.dp,
                    color = Color.White.copy(alpha = 0.35f),
                    shape = CircleShape,
                )
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(color = Color.White.copy(alpha = 0.35f)),
                    enabled = enabled,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (emoji != null) {
                Text(emoji, fontSize = iconSize.value.sp)
            } else {
                Icon(
                    icon!!,
                    label,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

// ─── Overlay menang + konfeti ─────────────────────────────────────

@Composable
private fun WinOverlay(
    levelIndex: Int,
    hasNext: Boolean,
    confettiTick: Int,
    reward: Reward,
    onNext: () -> Unit,
    onReplay: () -> Unit,
) {
    val levelNumber = levelIndex + 1
    val title = when (reward) {
        Reward.BIG -> "🎉🎉 LEVEL $levelNumber!"
        Reward.SMALL -> "🎉 LEVEL $levelNumber!"
        Reward.NONE -> "Level $levelNumber Selesai!"
    }
    val sub = when (reward) {
        Reward.BIG -> "LUAR BIASA! 10 level beres! 👏👏👏"
        Reward.SMALL -> "Hebat! Semakin jago! 👏"
        Reward.NONE -> "Keren! Lanjut ya!"
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center,
    ) {
        if (reward == Reward.BIG) {
            Confetti(seed = confettiTick * 7)
            Confetti(seed = confettiTick * 7 + 3)
        } else {
            Confetti(seed = confettiTick * (if (reward == Reward.SMALL) 3 else 1))
        }
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.padding(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(if (reward == Reward.BIG) "🎉🎊🎉" else "🎉", fontSize = if (reward == Reward.BIG) 64.sp else 56.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    title,
                    fontSize = if (reward == Reward.BIG) 28.sp else 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    sub,
                    fontSize = 15.sp,
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
