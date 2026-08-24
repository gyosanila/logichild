package com.gyosanila.kartcilik

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gyosanila.kartcilik.game.Dir
import com.gyosanila.kartcilik.game.FruitCommand
import com.gyosanila.kartcilik.game.Pos
import com.gyosanila.kartcilik.ui.BerryPurple
import com.gyosanila.kartcilik.ui.ConeOrange
import com.gyosanila.kartcilik.ui.GrassDark
import com.gyosanila.kartcilik.ui.GrassGreen
import com.gyosanila.kartcilik.ui.KartRed
import com.gyosanila.kartcilik.ui.OceanBlue
import com.gyosanila.kartcilik.ui.RoadGray
import com.gyosanila.kartcilik.ui.ShadowColor
import com.gyosanila.kartcilik.ui.SkyBlue
import com.gyosanila.kartcilik.ui.SunYellow
import com.gyosanila.kartcilik.ui.TextDark
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "🍎 Petik Buah",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.9f),
            ) {
                Text(
                    "Level ${state.level}",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(40.dp),
                onClick = onBack,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Home, "Menu", tint = TextDark)
                }
            }
        }

        FruitBoard(
            state = state,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )

        FruitInstructionStrip(
            state = state,
            onRemoveLast = vm::removeLast,
            onClear = vm::clearCommands,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        FruitControlTray(
            state = state,
            onAdd = vm::addCommand,
            onPlay = vm::play,
            onReset = vm::resetRobot,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }

    if (state.won) {
        FruitWinOverlay(
            level = state.level,
            onNext = vm::nextLevel,
        )
    }
    if (state.crashed) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("💥 Ups!") },
            text = { Text("Robot nabrak batu! Ayo susun ulang perintahnya.") },
            confirmButton = {
                TextButton(onClick = vm::retryAfterCrash) { Text("Coba Lagi 🔄") }
            },
        )
    }
    if (state.exhausted) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("🤔 Belum Selesai") },
            text = { Text("Masih ada buah yang belum dipetik. Tambah blok perintah, robot balik ke awal lagi.") },
            confirmButton = {
                TextButton(onClick = vm::resetRobot) { Text("Lanjut Susun ➕") }
            },
        )
    }
}

// ─── Papan permainan ──────────────────────────────────────────────

@Composable
private fun FruitBoard(state: FruitUiState, modifier: Modifier = Modifier) {
    val targetCell = Offset(state.robot.x + 0.5f, state.robot.y + 0.5f)
    val animCell by animateOffsetAsState(
        targetValue = targetCell,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "robotCell",
    )

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
    // Batu
    for (r in state.rocks) {
        val cx = ox + (r.x + 0.5f) * cell
        val cy = oy + (r.y + 0.5f) * cell
        drawRoundRect(
            ShadowColor,
            Offset(cx - cell * 0.3f + 2f, cy - cell * 0.3f + 4f),
            Size(cell * 0.6f, cell * 0.6f),
            CornerRadius(cell * 0.12f),
        )
        drawRoundRect(
            RoadGray,
            Offset(cx - cell * 0.3f, cy - cell * 0.3f),
            Size(cell * 0.6f, cell * 0.6f),
            CornerRadius(cell * 0.12f),
        )
        // kilau kecil
        drawCircle(Color.White.copy(alpha = 0.55f), cell * 0.08f, Offset(cx - cell * 0.12f, cy - cell * 0.12f))
    }
    // Buah (warna per urutan: merah, kuning, ungu, oranye)
    val fruitColors = listOf(KartRed, SunYellow, BerryPurple, ConeOrange)
    state.fruitsLeft.forEachIndexed { i, f ->
        val cx = ox + (f.x + 0.5f) * cell
        val cy = oy + (f.y + 0.5f) * cell
        drawCircle(ShadowColor, cell * 0.24f, Offset(cx, cy + cell * 0.06f))
        drawCircle(fruitColors[i % fruitColors.size], cell * 0.22f, Offset(cx, cy))
        drawCircle(Color.White.copy(alpha = 0.7f), cell * 0.07f, Offset(cx - cell * 0.08f, cy - cell * 0.09f))
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

private fun DrawScope.drawFruitRobot(cell: Float, dir: Dir) {
    val s = cell * 0.62f
    // Bayangan
    drawRoundRect(
        ShadowColor,
        Offset(-s / 2f + 2f, -s / 2f + 4f),
        Size(s, s),
        CornerRadius(s * 0.3f),
    )
    // Body kuning
    drawRoundRect(
        SunYellow,
        Offset(-s / 2f, -s / 2f),
        Size(s, s),
        CornerRadius(s * 0.3f),
    )
    // Mata hitam — posisi sesuai arah hadap
    val d = cell * 0.16f
    val e = cell * 0.11f
    drawCircle(TextDark, cell * 0.07f, Offset(dir.dx * d - dir.dy * e, dir.dy * d - dir.dx * e))
    drawCircle(TextDark, cell * 0.07f, Offset(dir.dx * d + dir.dy * e, dir.dy * d + dir.dx * e))
}

// ─── Strip perintah ───────────────────────────────────────────────

@Composable
private fun FruitInstructionStrip(
    state: FruitUiState,
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
            if (state.commands.isEmpty()) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "susun perintah di bawah 👇",
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
                    state.commands.forEach { c ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = c.color(),
                            modifier = Modifier.size(38.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(c.emoji(), fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
        IconButton(enabled = !state.running && !state.won && state.commands.isNotEmpty(), onClick = onRemoveLast) {
            Icon(
                Icons.AutoMirrored.Filled.Backspace,
                "Hapus satu",
                tint = if (state.commands.isNotEmpty()) TextDark else TextDark.copy(alpha = 0.35f),
            )
        }
        IconButton(enabled = !state.running && !state.won && state.commands.isNotEmpty(), onClick = onClear) {
            Icon(
                Icons.Filled.Refresh,
                "Hapus semua",
                tint = if (state.commands.isNotEmpty()) TextDark else TextDark.copy(alpha = 0.35f),
            )
        }
    }
}

// ─── Tombol perintah & kontrol ────────────────────────────────────

@Composable
private fun FruitControlTray(
    state: FruitUiState,
    onAdd: (FruitCommand) -> Unit,
    onPlay: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FruitCommand.entries.forEach { c ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = c.color(),
                    onClick = { if (!state.running && !state.won && state.commands.size < state.maxCommands) onAdd(c) },
                    modifier = Modifier.weight(1f),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 10.dp),
                    ) {
                        Text(c.emoji(), fontSize = 24.sp)
                        Text(
                            c.label(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF43A047),
                onClick = onPlay,
                enabled = !state.running && !state.won && state.commands.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 14.dp),
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                    Text(
                        "JALAN!",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                onClick = onReset,
                enabled = !state.running,
                modifier = Modifier.size(52.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Refresh,
                        "Ulang",
                        tint = if (state.running) TextDark.copy(alpha = 0.35f) else TextDark,
                    )
                }
            }
        }
        Text(
            "Blok: ${state.commands.size}/${state.maxCommands}",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

// ─── Overlay menang ───────────────────────────────────────────────

@Composable
private fun FruitWinOverlay(level: Int, onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
            ) {
                Text("🎉", fontSize = 48.sp)
                Text(
                    "Hore!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                )
                Text(
                    "Level $level selesai! Semua buah kepetik.",
                    color = TextDark,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF43A047),
                    onClick = onNext,
                ) {
                    Text(
                        "Level Berikutnya ▶",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}
