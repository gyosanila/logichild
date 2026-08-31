package com.gyosanila.logichild

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    onBack: () -> Unit,
    vm: ColorMatchViewModel = viewModel(),
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val state by vm.uiState.collectAsState()
    val tts = remember { TtsSpeaker(context) }

    // Instruksi dibacakan tiap level baru.
    LaunchedEffect(state.level, state.won) {
        if (!state.won) vm.speakInstruction(strings)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue),
    ) {
        Toolbar(
            emoji = "🎨",
            title = strings.playColor,
            onBack = onBack,
            soundOn = vm.sounds.enabled,
            onToggleSound = {
                vm.sounds.enabled = !vm.sounds.enabled
                context.getSharedPreferences("kartcilik_prefs", android.content.Context.MODE_PRIVATE)
                    .edit().putBoolean("sound_on", vm.sounds.enabled).apply()
            },
        )

        LevelMapSelector(
            itemCount = state.unlocked,
            currentIndex = state.level - 1,
            isMarked = { i -> state.stars[i + 1] != null },
            onSelect = { vm.loadLevel(it + 1) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Instruksi
                Text(
                    String.format(strings.colorAsk, vm.colorName(state.target, strings)),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                )
                if (state.mistakes > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        strings.colorTryAgain,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(28.dp))

                // Pilihan warna: 2 sebaris / 3 sebaris / 4 kotak 2x2
                val blob = if (state.options.size >= 3) 120.dp else 130.dp
                when (state.options.size) {
                    4 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            state.options.chunked(2).forEach { rowColors ->
                                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                    rowColors.forEach { ci -> ColorBlob(ci, blob, vm) }
                                }
                            }
                        }
                    }
                    else -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            state.options.forEach { ci -> ColorBlob(ci, blob, vm) }
                        }
                    }
                }
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
