package com.gyosanila.logichild

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.SunYellow
import com.gyosanila.logichild.ui.TextDark

/**
 * Roadmap level — jalur berliku ala peta game casual.
 *
 * Level = node bulat di sepanjang jalan S-curve (atas-bawah bergantian).
 * - Level selesai (isMarked): node kuning pucat + badge ⭐
 * - Level aktif: node kuning + ring putih, lebih besar
 * - Level terkunci: abu-abu + 🔒
 * Scroll horizontal halus, otomatis ke-tengahin level aktif.
 */
@Composable
fun LevelMapSelector(
    itemCount: Int,
    currentIndex: Int,
    isMarked: (Int) -> Boolean = { false },
    onSelect: (Int) -> Unit,
    lockedCount: Int = 2,
    modifier: Modifier = Modifier,
) {
    val spacing = 78.dp
    val nodeSize = 44.dp
    val yTop = 36.dp
    val yBot = 100.dp
    val mapHeight = 132.dp
    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val scroll = rememberScrollState()
    val totalNodes = itemCount + lockedCount
    val totalWidth = (totalNodes * spacing.value + 60f).dp

    var first by remember { mutableStateOf(true) }
    LaunchedEffect(currentIndex) {
        val targetPx = (currentIndex * spacingPx - with(density) { 160.dp.toPx() }).coerceAtLeast(0f)
        if (first) {
            scroll.scrollTo(targetPx.toInt())
            first = false
        } else {
            scroll.animateScrollTo(targetPx.toInt())
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(mapHeight),
    ) {
        // Panel rumput
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFA8E063).copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
        )
        Row(
            Modifier
                .horizontalScroll(scroll)
                .fillMaxHeight(),
        ) {
            Box(
                Modifier
                    .width(totalWidth)
                    .fillMaxHeight(),
            ) {
                // Jalan tanah berliku (bezier antar node)
                Canvas(Modifier.fillMaxSize()) {
                    val yT = yTop.toPx()
                    val yB = yBot.toPx()
                    val road = Path()
                    val pts = List(totalNodes) { i ->
                        Offset(i * spacingPx, if (i % 2 == 0) yT else yB)
                    }
                    if (pts.isNotEmpty()) {
                        road.moveTo(pts[0].x, pts[0].y)
                        for (i in 0 until pts.size - 1) {
                            val a = pts[i]
                            val b = pts[i + 1]
                            val midX = (a.x + b.x) / 2f
                            road.cubicTo(midX, a.y, midX, b.y, b.x, b.y)
                        }
                        drawPath(
                            road,
                            Color(0xFFE8C88A),
                            style = Stroke(18.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                        drawPath(
                            road,
                            Color(0xFFD9A85E).copy(alpha = 0.55f),
                            style = Stroke(5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                    }
                }
                // Node level
                for (i in 0 until totalNodes) {
                    val locked = i >= itemCount
                    val isCurrent = i == currentIndex
                    val marked = !locked && isMarked(i)
                    val x = (i * spacing.value).dp - nodeSize / 2
                    val y = (if (i % 2 == 0) yTop else yBot) - nodeSize / 2
                    val bg = when {
                        locked -> Color(0xFFB9C4CE)
                        isCurrent -> SunYellow
                        marked -> Color(0xFFFFF3B0)
                        else -> Color.White
                    }
                    Box(
                        Modifier
                            .offset(x = x, y = y)
                            .size(nodeSize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = bg,
                            onClick = { if (!locked) onSelect(i) },
                            modifier = Modifier
                                .size(if (isCurrent) nodeSize + 4.dp else nodeSize)
                                .border(
                                    width = if (isCurrent) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape,
                                )
                                .shadow(4.dp, CircleShape),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (locked) {
                                    Icon(
                                        Icons.Filled.Lock,
                                        null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp),
                                    )
                                } else {
                                    Text(
                                        "${i + 1}",
                                        fontSize = if (isCurrent) 17.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                    )
                                }
                            }
                        }
                        // Badge bintang level selesai
                        if (marked && !isCurrent) {
                            Text(
                                "⭐",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 2.dp, end = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
