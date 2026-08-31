package com.gyosanila.logichild

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.LocalStrings
import com.gyosanila.logichild.ui.SkyBlue
import com.gyosanila.logichild.ui.SunYellow
import com.gyosanila.logichild.ui.TextDark
import kotlin.math.roundToInt

/**
 * Halaman ROADMAP (Varian A) — pilih level di peta jalur berliku:
 * langit + awan + rumput, jalan aspal dengan garis putus-putus,
 * semak/bunga, bendera start 🚩, piala 🏆 di ujung.
 *
 * Node: selesai = hijau + ⭐, aktif/frontier = kuning + ring putih + ✨,
 * kebuka = putih, terkunci = abu + 🔒 (2 preview di depan).
 * Auto-scroll ke level frontier (level terakhir yang kebuka).
 */
@Composable
fun RoadmapScreen(
    emoji: String,
    title: String,
    unlockedCount: Int,          // level 1-based yang bisa dimainkan
    stars: Map<Int, Int>,        // level 1-based → jumlah bintang
    onSelect: (Int) -> Unit,     // level 1-based
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val total = unlockedCount.coerceAtLeast(1) + 2   // +2 node terkunci preview

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue),
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(44.dp),
                onClick = onBack,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("←", color = TextDark, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            strings.roadmapPick,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))

        // ── Peta scroll ──
        val scroll = rememberScrollState()
        val density = LocalDensity.current
        val rowH = 118.dp
        val skyH = 170.dp
        LaunchedEffect(unlockedCount) {
            val targetPx = with(density) { (rowH * (unlockedCount - 1) - 230.dp).coerceAtLeast(0.dp).toPx() }
            scroll.scrollTo(targetPx.roundToInt())
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val mapW = maxWidth
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scroll),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(skyH + rowH * total + 90.dp),
                ) {
                    // Latar: langit (atas) → rumput
                    Canvas(Modifier.fillMaxSize()) {
                        val skyPx = skyH.toPx()
                        drawRect(
                            Brush.verticalGradient(
                                listOf(Color(0xFF6FC4F5), Color(0xFFBEE9FD)),
                                startY = 0f, endY = skyPx,
                            ),
                        )
                        drawRect(
                            Brush.verticalGradient(
                                listOf(Color(0xFF8CD95E), Color(0xFF6FB83F)),
                                startY = skyPx, endY = size.height,
                            ),
                            topLeft = Offset(0f, skyPx),
                        )
                        // Awan
                        drawCloud(110.dp.toPx(), 55.dp.toPx(), 1f)
                        drawCloud(size.width - 150.dp.toPx(), 95.dp.toPx(), 0.8f)
                        drawCloud(300.dp.toPx(), 30.dp.toPx(), 0.6f)
                        // Jalan aspal (S-curve vertikal)
                        val w = size.width
                        val rowPx = rowH.toPx()
                        val pts = List(total) { i ->
                            Offset(
                                if (i % 2 == 0) w * 0.24f else w * 0.76f,
                                skyPx + 70.dp.toPx() + i * rowPx,
                            )
                        }
                        val road = Path()
                        pts.forEachIndexed { i, p ->
                            if (i == 0) road.moveTo(p.x, p.y)
                            else {
                                val prev = pts[i - 1]
                                val midY = (prev.y + p.y) / 2f
                                road.cubicTo(prev.x, midY, p.x, midY, p.x, p.y)
                            }
                        }
                        drawPath(
                            road,
                            Color(0xFF939CA8),
                            style = Stroke(20.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                        // Garis tengah putus-putus
                        val dashes = mutableListOf<Offset>()
                        for (i in 0 until pts.size - 1) {
                            dashes += bezierPts(pts[i], pts[i + 1], 20)
                        }
                        var k = 0
                        while (k < dashes.size - 5) {
                            drawLine(Color.White, dashes[k], dashes[k + 3], strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                            k += 10
                        }
                        // Semak & bunga di pinggir jalan
                        for (i in 0 until total - 1) {
                            val mid = (pts[i].x + pts[i + 1].x) / 2f
                            val my = (pts[i].y + pts[i + 1].y) / 2f
                            val side = if (pts[i].x < pts[i + 1].x) -1f else 1f
                            drawBush(mid + side * w * 0.13f, my, 1f)
                            if (i % 2 == 1) {
                                drawFlower(mid + side * w * 0.24f, my + 34.dp.toPx(), 1f, Color(0xFFEF5350))
                            }
                        }
                    }
                    // Bendera start & piala
                    Text(
                        "🚩",
                        fontSize = 28.sp,
                        modifier = Modifier.offset(
                            x = (mapW * 0.24f).value.dp - 16.dp,
                            y = skyH + 70.dp - 42.dp,
                        ),
                    )
                    Text(
                        "🏆",
                        fontSize = 34.sp,
                        modifier = Modifier.offset(
                            x = (mapW * (if ((total - 1) % 2 == 0) 0.24f else 0.76f)).value.dp - 18.dp,
                            y = skyH + rowH * total + 8.dp,
                        ),
                    )
                    // Node level
                    for (i in 0 until total) {
                        val level = i + 1
                        val locked = level > unlockedCount
                        val isCurrent = !locked && level == unlockedCount
                        val done = !locked && stars.containsKey(level)
                        val cxDp = (mapW * (if (i % 2 == 0) 0.24f else 0.76f)).value
                        val cyDp = (skyH + 70.dp).value + i * rowH.value
                        RoadmapNode(
                            level = level,
                            locked = locked,
                            isCurrent = isCurrent,
                            done = done,
                            onClick = { if (!locked) onSelect(level) },
                            modifier = Modifier.offset(
                                x = (cxDp - 28f).dp,
                                y = (cyDp - 28f).dp,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoadmapNode(
    level: Int,
    locked: Boolean,
    isCurrent: Boolean,
    done: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val size = if (isCurrent) 58.dp else 50.dp
    val bg = when {
        locked -> Color(0xFFC3CBD3)
        isCurrent -> SunYellow
        done -> Color(0xFF66BB6A)
        else -> Color.White
    }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Surface(
            shape = CircleShape,
            color = bg,
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = if (isCurrent) 4.dp else 0.dp,
                    color = Color.White,
                    shape = CircleShape,
                )
                .shadow(6.dp, CircleShape),
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (locked) {
                    Icon(Icons.Filled.Lock, null, tint = Color(0xFF6B727C), modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        "$level",
                        fontSize = if (isCurrent) 21.sp else 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isCurrent || done) Color.White else TextDark,
                    )
                }
            }
        }
        if (done && !isCurrent) {
            Text("⭐", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopEnd))
        }
        if (isCurrent) {
            Text("✨", fontSize = 17.sp, modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.bezierPts(
    a: Offset, b: Offset, steps: Int,
): List<Offset> {
    val midY = (a.y + b.y) / 2f
    return List(steps + 1) { i ->
        val t = i.toFloat() / steps
        val mt = 1 - t
        Offset(
            mt * mt * mt * a.x + 3 * mt * mt * t * a.x + 3 * mt * t * t * b.x + t * t * t * b.x,
            mt * mt * mt * a.y + 3 * mt * mt * t * midY + 3 * mt * t * t * midY + t * t * t * b.y,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(
    cx: Float, cy: Float, s: Float,
) {
    val c = Color.White.copy(alpha = 0.85f)
    drawCircle(c, 20f * s, Offset(cx - 16f * s, cy + 4f * s))
    drawCircle(c, 26f * s, Offset(cx, cy))
    drawCircle(c, 18f * s, Offset(cx + 18f * s, cy + 6f * s))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBush(
    cx: Float, cy: Float, s: Float,
) {
    drawCircle(Color(0xFF66BB6A), 12f * s, Offset(cx, cy))
    drawCircle(Color(0xFF81C784), 10f * s, Offset(cx + 7f * s, cy - 4f * s))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlower(
    cx: Float, cy: Float, s: Float, color: Color,
) {
    repeat(5) { k ->
        val ang = k * 2 * Math.PI / 5
        drawCircle(color, 4f * s, Offset(cx + 6f * s * Math.cos(ang).toFloat(), cy + 6f * s * Math.sin(ang).toFloat()))
    }
    drawCircle(Color(0xFFFFD23C), 3.2f * s, Offset(cx, cy))
}
