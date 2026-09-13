package com.gyosanila.logichild

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gyosanila.logichild.ui.LocalStrings
import com.gyosanila.logichild.ui.SkyBlue
import com.gyosanila.logichild.ui.TextDark

/** Palette warna game (index cocok sama ColorMatchViewModel). */
private val Palette = listOf(
    Color(0xFFE53935), // merah
    Color(0xFF1E88E5), // biru
    Color(0xFFFFD54F), // kuning
    Color(0xFF66BB6A), // hijau
    Color(0xFF7B4FD8), // ungu
    Color(0xFFFF9800), // oranye
    Color(0xFFF06292), // pink
)

@Composable
fun ColorMatchScreen(
    startLevel: Int = 1,
    onBack: () -> Unit,
    vm: ColorMatchViewModel = viewModel(),
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val state by vm.uiState.collectAsState()
    val tts = remember { TtsSpeaker(context) }

    // Level dari roadmap — kalau 0, mulai dari level 1.
    LaunchedEffect(Unit) {
        if (startLevel > 0) vm.loadLevel(startLevel)
    }

    // Instruksi dibacakan tiap level baru.
    LaunchedEffect(state.level, state.won) {
        if (!state.won) vm.speakInstruction(strings)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(SkyBlue),
    ) {
        ColorGardenDecor()
        Column(Modifier.fillMaxSize()) {
            Toolbar(
                emoji = "🎨",
                title = strings.playColor,
                soundOn = state.soundOn,
                onToggleSound = vm::toggleSound,
                onOpenMap = onBack,
            )
            Text(
                "${strings.level} ${state.level}",
                color = TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    shadowElevation = 5.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 18.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                    ) {
                        Text(
                            String.format(strings.colorAsk, vm.colorName(state.target, strings)),
                            color = TextDark,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8E4FF),
                            onClick = vm::repeatInstruction,
                            modifier = Modifier.size(52.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = "Repeat", tint = Color(0xFF5746A8), modifier = Modifier.size(29.dp))
                            }
                        }
                    }
                }
                if (state.mistakes > 0) {
                    Text(strings.colorTryAgain, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Text("Pilih warna yang sama", color = TextDark, fontSize = 21.sp, fontWeight = FontWeight.Black)
                val blob = when {
                    state.options.size <= 2 -> 118.dp
                    state.options.size <= 4 -> 100.dp
                    else -> 84.dp
                }
                val rows = if (state.options.size == 4) state.options.chunked(2) else state.options.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    rows.forEach { rowColors ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowColors.forEach { ci -> ColorBlob(ci, blob, vm) }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    if (state.won) {
        val starCount = (state.stars[state.level] ?: 5).coerceIn(0, 5)
        GameWinOverlay(
            emoji = if (state.reward != com.gyosanila.logichild.game.Reward.NONE) "🏆" else "🎨",
            title = String.format(strings.winNoneTitle, state.level),
            starRow = "⭐".repeat(starCount) + "☆".repeat(5 - starCount),
            praise = when (starCount) {
                5 -> strings.praise5
                4 -> strings.praise4
                3 -> strings.praise3
                2 -> strings.praise2
                else -> strings.praise1
            },
            showConfetti = starCount >= 4,
            confettiTick = state.confettiTick,
            showNext = state.level < state.unlocked,
            showReplay = true,
            nextLabel = strings.levelNext,
            replayLabel = strings.playAgain,
            onNext = vm::nextLevel,
            onReplay = vm::replay,
        )
    }
}


@Composable
private fun ColorGardenDecor() {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(Color(0xFFFFD54F), radius = 42f, center = androidx.compose.ui.geometry.Offset(w - 70f, 82f))
            drawArc(Color(0xFFF28B82), 190f, 160f, false, style = Stroke(11f), topLeft = androidx.compose.ui.geometry.Offset(w * .18f, 70f), size = androidx.compose.ui.geometry.Size(w * .64f, 230f))
            drawArc(Color(0xFFFFC857), 190f, 160f, false, style = Stroke(11f), topLeft = androidx.compose.ui.geometry.Offset(w * .22f, 70f), size = androidx.compose.ui.geometry.Size(w * .56f, 200f))
            drawArc(Color(0xFF75C878), 190f, 160f, false, style = Stroke(11f), topLeft = androidx.compose.ui.geometry.Offset(w * .26f, 70f), size = androidx.compose.ui.geometry.Size(w * .48f, 170f))
            drawRect(Color(0xFF9BD77F), topLeft = androidx.compose.ui.geometry.Offset(0f, h - 145f), size = androidx.compose.ui.geometry.Size(w, 145f))
            drawLine(Color(0xFF6CB56A), androidx.compose.ui.geometry.Offset(0f, h - 145f), androidx.compose.ui.geometry.Offset(w, h - 145f), strokeWidth = 5f)
        }
        Text("☁️", fontSize = 34.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 22.dp, top = 82.dp))
        Text("☁️", fontSize = 28.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 78.dp, top = 172.dp))
        Text("🌼", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 30.dp))
        Text("🌷", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 28.dp))
    }
}

@Composable
private fun ColorBlob(colorIndex: Int, size: androidx.compose.ui.unit.Dp, vm: ColorMatchViewModel) {
    val strings = LocalStrings.current
    Surface(
        shape = CircleShape,
        color = Palette[colorIndex % Palette.size],
        onClick = { vm.answer(colorIndex) },
        modifier = Modifier.size(size),
        shadowElevation = 6.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                vm.colorName(colorIndex, strings),
                color = TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}
