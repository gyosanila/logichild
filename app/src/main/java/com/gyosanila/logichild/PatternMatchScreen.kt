package com.gyosanila.logichild

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gyosanila.logichild.ui.LocalStrings
import com.gyosanila.logichild.ui.SkyBlue
import com.gyosanila.logichild.ui.TextDark

@Composable
fun PatternMatchScreen(
    startLevel: Int = 1,
    onBack: () -> Unit,
    vm: PatternMatchViewModel = viewModel(),
) {
    val strings = LocalStrings.current
    val state by vm.uiState.collectAsState()
    LaunchedEffect(Unit) { if (startLevel > 0) vm.loadLevel(startLevel) }

    Box(Modifier.fillMaxSize().background(SkyBlue)) {
        PatternGardenDecor()
        Column(Modifier.fillMaxSize()) {
            Toolbar(
                emoji = "🧩",
                title = strings.playPattern,
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
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(strings.patternAsk, color = TextDark, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.size(18.dp))
                val compact = state.sequence.size > 4
                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp),
                ) {
                    state.sequence.forEach { token ->
                        val tileSize = when {
                            state.sequence.size >= 8 -> 44.dp
                            compact -> 52.dp
                            else -> 64.dp
                        }
                        Surface(
                            shape = RoundedCornerShape(if (compact) 16.dp else 20.dp),
                            color = if (token == "?") Color.White else Color(0xFFFFF1B2),
                            modifier = Modifier.size(tileSize),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(token, fontSize = if (token == "?") 30.sp else if (compact) 28.sp else 36.sp, fontWeight = FontWeight.Black, color = TextDark)
                            }
                        }
                    }
                }
                if (state.mistakes > 0) {
                    Spacer(Modifier.size(12.dp))
                    Text(strings.patternTryAgain, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.size(30.dp))
                if (state.choices.size >= 4) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        state.choices.chunked(2).forEach { rowChoices ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                rowChoices.forEach { choice -> PatternChoice(choice, vm) }
                            }
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        state.choices.forEach { choice -> PatternChoice(choice, vm) }
                    }
                }
            }
        }
        if (state.won) {
            val count = (state.stars[state.level] ?: 5).coerceIn(0, 5)
            GameWinOverlay(
                emoji = if (state.reward != com.gyosanila.logichild.game.Reward.NONE) "🏆" else "🧩",
                title = String.format(strings.winNoneTitle, state.level),
                starRow = "⭐".repeat(count) + "☆".repeat(5 - count),
                praise = when (count) { 5 -> strings.praise5; 4 -> strings.praise4; 3 -> strings.praise3; 2 -> strings.praise2; else -> strings.praise1 },
                showConfetti = count >= 4,
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
}


@Composable
private fun PatternGardenDecor() {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(Color(0xFFFFD54F), radius = 42f, center = androidx.compose.ui.geometry.Offset(w - 70f, 82f))
            drawArc(Color(0xFFF28B82), 190f, 160f, false, style = Stroke(11f), topLeft = androidx.compose.ui.geometry.Offset(w * .16f, 65f), size = androidx.compose.ui.geometry.Size(w * .68f, 235f))
            drawArc(Color(0xFFFFC857), 190f, 160f, false, style = Stroke(11f), topLeft = androidx.compose.ui.geometry.Offset(w * .20f, 65f), size = androidx.compose.ui.geometry.Size(w * .60f, 205f))
            drawArc(Color(0xFF75C878), 190f, 160f, false, style = Stroke(11f), topLeft = androidx.compose.ui.geometry.Offset(w * .24f, 65f), size = androidx.compose.ui.geometry.Size(w * .52f, 175f))
            drawRect(Color(0xFF9BD77F), topLeft = androidx.compose.ui.geometry.Offset(0f, h - 145f), size = androidx.compose.ui.geometry.Size(w, 145f))
            drawLine(Color(0xFF6CB56A), androidx.compose.ui.geometry.Offset(0f, h - 145f), androidx.compose.ui.geometry.Offset(w, h - 145f), strokeWidth = 5f)
        }
        Text("☁️", fontSize = 34.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 18.dp, top = 82.dp))
        Text("☁️", fontSize = 28.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 70.dp, top = 172.dp))
        Text("🧩", fontSize = 36.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp))
        Text("⭐", fontSize = 30.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp, top = 40.dp))
        Text("🔷", fontSize = 28.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 72.dp, bottom = 28.dp))
        Text("🔶", fontSize = 28.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 72.dp, bottom = 30.dp))
        Text("🌼", fontSize = 28.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 18.dp, bottom = 84.dp))
        Text("🌷", fontSize = 28.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 84.dp))
    }
}


@Composable
private fun PatternChoice(choice: String, vm: PatternMatchViewModel) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        onClick = { vm.answer(choice) },
        modifier = Modifier.size(104.dp),
        shadowElevation = 8.dp,
    ) {
        Box(contentAlignment = Alignment.Center) { Text(choice, fontSize = 52.sp) }
    }
}
