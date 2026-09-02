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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.LocalStrings
import com.gyosanila.logichild.ui.SkyBlue
import com.gyosanila.logichild.ui.SunYellow
import com.gyosanila.logichild.ui.TextDark
import kotlin.math.roundToInt

/**
 * Halaman ROADMAP — pilih level di peta jalur berliku.
 *
 * - Langit + awan FIX di atas (tidak ikut scroll), rumput + jalan yang scroll.
 * - Header simetris: tombol ← bulat (ikon vektor, center), judul di tengah layar.
 * - Node: selesai = hijau + meter bintang 5 (★ isi kuning sesuai rating),
 *   aktif/frontier = kuning + ring putih + ✨, kebuka = putih,
 *   terkunci = abu + 🔒 (2 preview di depan).
 * - Auto-scroll ke level frontier. Ukuran node KONSTAN (tidak ada efek zoom).
 */
@Composable
fun RoadmapScreen(
    emoji: String,
    title: String,
    unlockedCount: Int,          // level 1-based yang bisa dimainkan
    stars: Map<Int, Int>,        // level 1-based → jumlah bintang 1..5
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
        // Header pakai komponen Toolbar yang SAMA dengan halaman game.
        Toolbar(
            emoji = emoji,
            title = title,
            onBack = onBack,
        )
        Text(
            strings.roadmapPick,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))

        // ── Peta: langit tetap di atas, dunia (rumput+jalan) scroll ──
        val skyH = 150.dp
        val rowH = 118.dp
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val mapW = maxWidth
            val scrollAreaH = (maxHeight - skyH).coerceAtLeast(0.dp)
            val density = LocalDensity.current

            Column(Modifier.fillMaxSize()) {
                // Langit + awan: FIX (tidak ikut scroll).
                SkyBand(skyH, Modifier.fillMaxWidth().height(skyH))

                // Dunia scroll: rumput + jalan + dekorasi + node.
                val scroll = rememberScrollState()
                LaunchedEffect(unlockedCount) {
                    // Level frontier (terakhir yang kebuka) di-auto-center.
                    val nodeY = 70.dp + rowH * (unlockedCount - 1)
                    val target = (nodeY - scrollAreaH / 2f + rowH / 2f).coerceAtLeast(0.dp)
                    scroll.scrollTo(with(density) { target.toPx() }.roundToInt())
                }

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scroll),
                ) {
                    val worldH = (rowH * total + 90.dp).coerceAtLeast(scrollAreaH)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(worldH),
                    ) {
                        // Rumput (latar penuh) + jalan S-curve + dekorasi.
                        Canvas(Modifier.fillMaxSize()) {
                            val w = size.width
                            drawRect(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF8CD95E), Color(0xFF6FB83F)),
                                    startY = 0f, endY = size.height,
                                ),
                            )
                            // Jalan aspal (S-curve vertikal)
                            val rowPx = rowH.toPx()
                            val pts = List(total) { i ->
                                Offset(
                                    if (i % 2 == 0) w * 0.24f else w * 0.76f,
                                    70.dp.toPx() + i * rowPx,
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
                                y = 70.dp - 42.dp,
                            ),
                        )
                        Text(
                            "🏆",
                            fontSize = 34.sp,
                            modifier = Modifier.offset(
                                x = (mapW * (if ((total - 1) % 2 == 0) 0.24f else 0.76f)).value.dp - 18.dp,
                                y = rowH * total + 8.dp,
                            ),
                        )
                        // Node level
                        for (i in 0 until total) {
                            val level = i + 1
                            val locked = level > unlockedCount
                            val isCurrent = !locked && level == unlockedCount
                            val star = if (locked) 0 else stars[level] ?: 0
                            val cxDp = (mapW * (if (i % 2 == 0) 0.24f else 0.76f)).value
                            val cyDp = 70.dp.value + i * rowH.value
                            val sizeDp = if (isCurrent) 58f else 50f
                            RoadmapNode(
                                level = level,
                                star = star,
                                locked = locked,
                                isCurrent = isCurrent,
                                onClick = { if (!locked) onSelect(level) },
                                modifier = Modifier.offset(
                                    x = (cxDp - sizeDp / 2f).dp,
                                    y = (cyDp - sizeDp / 2f).dp,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Pita langit + awan — posisi tetap, tidak ikut scroll. */
@Composable
private fun SkyBand(height: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.height(height)) {
        val w = size.width
        drawRect(
            Brush.verticalGradient(
                listOf(Color(0xFF6FC4F5), Color(0xFFBEE9FD)),
                startY = 0f, endY = size.height,
            ),
        )
        drawCloud(110.dp.toPx(), 55.dp.toPx(), 1f)
        drawCloud(w - 150.dp.toPx(), 95.dp.toPx(), 0.8f)
        drawCloud(300.dp.toPx(), 30.dp.toPx(), 0.6f)
    }
}

@Composable
private fun RoadmapNode(
    level: Int,
    star: Int,
    locked: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val size = if (isCurrent) 58.dp else 50.dp
    val bg = when {
        locked -> Color(0xFFC3CBD3)
        isCurrent -> SunYellow
        star > 0 -> Color(0xFF66BB6A)
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
                when {
                    locked -> Icon(Icons.Filled.Lock, null, tint = Color(0xFF6B727C), modifier = Modifier.size(18.dp))
                    star > 0 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$level",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                        )
                        Spacer(Modifier.height(1.dp))
                        StarMeter(star)
                    }
                    else -> Text(
                        "$level",
                        fontSize = if (isCurrent) 20.sp else 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isCurrent) Color.White else TextDark,
                    )
                }
            }
        }
        if (isCurrent) {
            Text("✨", fontSize = 17.sp, modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

/**
 * 5 slot bintang mini: ★ kuning sesuai rating, ☆ putih transparan sisanya.
 */
@Composable
private fun StarMeter(star: Int) {
    val n = star.coerceIn(0, 5)
    Row {
        repeat(5) { i ->
            Text(
                if (i < n) "★" else "☆",
                fontSize = 7.sp,
                color = if (i < n) Color(0xFFFFE082) else Color.White.copy(alpha = 0.6f),
            )
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
