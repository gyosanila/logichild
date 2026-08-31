package com.gyosanila.logichild

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.BerryPurple
import com.gyosanila.logichild.ui.ConeOrange
import com.gyosanila.logichild.ui.GrassGreen
import com.gyosanila.logichild.ui.KartRed
import com.gyosanila.logichild.ui.OceanBlue
import com.gyosanila.logichild.ui.SunYellow
import com.gyosanila.logichild.ui.TextDark

// ─── Toolbar (sama di semua game): kiri icon+judul, kanan audio+home ─

@Composable
fun Toolbar(
    emoji: String,
    title: String,
    onBack: (() -> Unit)?,
    soundOn: Boolean? = null,
    onToggleSound: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Kiri: icon + judul
        Text(emoji, fontSize = 32.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f),
        )
        // Kanan: audio + home
        if (soundOn != null && onToggleSound != null) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(40.dp),
                onClick = onToggleSound,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (soundOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                        contentDescription = null,
                        tint = TextDark,
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
        }
        if (onBack != null) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(40.dp),
                onClick = onBack,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Home, contentDescription = "Menu", tint = TextDark)
                }
            }
        }
    }
}

// ─── Pemilih level (LazyRow smooth, dipakai semua game) ───────────

@Composable
fun LevelSelector(
    itemCount: Int,
    currentIndex: Int,
    isMarked: (Int) -> Boolean = { false },
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(itemCount) { i ->
            val isCurrent = i == currentIndex
            val bg = when {
                isCurrent -> SunYellow
                isMarked(i) -> Color(0xFFFFF3B0)
                else -> Color.White.copy(alpha = 0.85f)
            }
            Surface(
                shape = CircleShape,
                color = bg,
                onClick = { onSelect(i) },
                modifier = Modifier
                    .size(if (isCurrent) 30.dp else 24.dp)
                    .border(
                        width = if (isCurrent) 2.dp else 0.dp,
                        color = Color.White,
                        shape = CircleShape,
                    ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${i + 1}",
                        fontSize = if (isCurrent) 12.sp else 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                    )
                }
            }
        }
    }
    // Scroll halus ke level aktif (gak loncat-loncat).
    LaunchedEffect(currentIndex) {
        listState.animateScrollToItem((currentIndex - 3).coerceAtLeast(0))
    }
}

// ─── Overlay menang (rating + pujian, dipakai semua game) ─────────

@Composable
fun GameWinOverlay(
    emoji: String,
    title: String,
    starRow: String,
    praise: String,
    showConfetti: Boolean,
    confettiTick: Int,
    showNext: Boolean,
    showReplay: Boolean,
    nextLabel: String,
    replayLabel: String,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center,
    ) {
        if (showConfetti && confettiTick > 0) {
            Confetti(seed = confettiTick * 7)
            Confetti(seed = confettiTick * 7 + 3)
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
                Text(emoji, fontSize = if (showConfetti) 64.sp else 56.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    title,
                    fontSize = if (showConfetti) 28.sp else 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    starRow,
                    fontSize = 34.sp,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    praise,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(18.dp))
                if (showNext) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF4CAF50),
                        onClick = onNext,
                    ) {
                        Text(
                            nextLabel,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
                if (showReplay) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = OceanBlue,
                        onClick = onReplay,
                    ) {
                        Text(
                            replayLabel,
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
}

// ─── Konfeti (dipakai overlay menang) ─────────────────────────────

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
