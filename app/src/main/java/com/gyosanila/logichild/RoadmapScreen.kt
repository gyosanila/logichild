package com.gyosanila.logichild

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.LocalStrings
import com.gyosanila.logichild.ui.SunYellow
import com.gyosanila.logichild.ui.TextDark
import kotlin.math.cos
import kotlin.math.sin

/** Shared horizontal adventure map used by all three games. */
@Composable
fun RoadmapScreen(
    emoji: String,
    title: String,
    unlockedCount: Int,
    stars: Map<Int, Int>,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val total = unlockedCount.coerceAtLeast(1) + 2
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8CA55)),
    ) {
        Toolbar(emoji = emoji, title = title, onBack = onBack)
        Text(
            strings.roadmapPick,
            color = Color(0xFF6B4025),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
        )
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val colW = 156.dp
            val worldW = (colW * total + 100.dp).coerceAtLeast(maxWidth)
            val worldH = maxHeight.coerceAtLeast(470.dp)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scroll),
            ) {
                Box(
                    modifier = Modifier
                        .width(worldW)
                        .height(worldH),
                ) {
                    AdventureMapCanvas(total, colW, Modifier.fillMaxSize())
                    Text(
                        "🚩",
                        fontSize = 34.sp,
                        modifier = Modifier.offset(x = 46.dp, y = 238.dp),
                    )
                    Text(
                        "🏆",
                        fontSize = 40.sp,
                        modifier = Modifier.offset(x = colW * total - 66.dp, y = 236.dp),
                    )
                    Text(
                        "START",
                        color = Color(0xFF6B4025),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.offset(x = 35.dp, y = 300.dp),
                    )
                    Text(
                        "FINISH",
                        color = Color(0xFF6B4025),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.offset(x = colW * total - 88.dp, y = 300.dp),
                    )
                    repeat(total) { index ->
                        val level = index + 1
                        val node = mapNode(index, colW)
                        val locked = level > unlockedCount
                        val current = !locked && level == unlockedCount
                        RoadmapNode(
                            level = level,
                            star = if (locked) 0 else stars[level] ?: 0,
                            locked = locked,
                            current = current,
                            onClick = { if (!locked) onSelect(level) },
                            modifier = Modifier.offset(
                                x = node.x - node.radius,
                                y = node.y - node.radius,
                            ),
                        )
                    }
                }
            }
        }
        Text(
            "SWIPE  ←  →   •   PILIH LEVEL PETUALANGAN",
            color = Color(0xFF6B4025),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFEFB2))
                .padding(vertical = 8.dp),
        )
    }
}

private data class NodePosition(val x: androidx.compose.ui.unit.Dp, val y: androidx.compose.ui.unit.Dp, val radius: androidx.compose.ui.unit.Dp)

private fun mapNode(index: Int, colW: androidx.compose.ui.unit.Dp): NodePosition {
    val y = when (index % 6) {
        0 -> 294.dp
        1 -> 190.dp
        2 -> 330.dp
        3 -> 145.dp
        4 -> 275.dp
        else -> 165.dp
    }
    val radius = if (index % 6 == 3) 52.dp else 45.dp
    return NodePosition(78.dp + colW * index, y, radius)
}

@Composable
private fun AdventureMapCanvas(total: Int, colW: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    Canvas(modifier) {
        val points = (0 until total).map { i ->
            Offset((78.dp + colW * i).toPx(), mapNode(i, colW).y.toPx())
        }
        drawRect(Brush.verticalGradient(listOf(Color(0xFFF8CA55), Color(0xFFECAF43))))
        val road = Path()
        points.forEachIndexed { i, point ->
            if (i == 0) road.moveTo(point.x, point.y)
            else {
                val previous = points[i - 1]
                val dx = point.x - previous.x
                road.cubicTo(
                    previous.x + dx * .28f, previous.y,
                    point.x - dx * .28f, point.y,
                    point.x, point.y,
                )
            }
        }
        drawPath(road, Color(0xFFB5672E), style = Stroke(width = 112.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(road, Color(0xFFE88E3E), style = Stroke(width = 104.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(road, Color(0xFFF6B65B), style = Stroke(width = 80.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawDashedRoad(points)
        for (i in 0 until total + 2) {
            val x = (36.dp + colW * i).toPx()
            val y = if (i % 2 == 0) 95.dp.toPx() else 445.dp.toPx()
            drawBush(x, y, if (i % 3 == 0) 1.15f else .85f)
            if (i % 2 == 1) drawFlower(x + 35.dp.toPx(), y + 38.dp.toPx(), Color(0xFFEF6672))
            if (i % 4 == 0) drawCrystal(x + 65.dp.toPx(), y + 8.dp.toPx())
        }
    }
}

private fun DrawScope.drawDashedRoad(points: List<Offset>) {
    for (i in 0 until points.size - 1) {
        val a = points[i]
        val b = points[i + 1]
        for (step in 0 until 8) {
            val start = step / 8f
            val end = (step + .42f) / 8f
            drawLine(
                Color(0xFFFFE6A0),
                Offset(a.x + (b.x - a.x) * start, a.y + (b.y - a.y) * start),
                Offset(a.x + (b.x - a.x) * end, a.y + (b.y - a.y) * end),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun RoadmapNode(
    level: Int,
    star: Int,
    locked: Boolean,
    current: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val radius = if (current) 52.dp else 45.dp
    val color = when {
        locked -> Color(0xFFAAB5C2)
        current -> SunYellow
        star > 0 -> Color(0xFF7860A5)
        else -> Color(0xFF8C78B5)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(38.dp),
                color = color,
                onClick = onClick,
                modifier = Modifier
                    .size(radius * 2)
                    .shadow(7.dp, RoundedCornerShape(38.dp))
                    .border(5.dp, if (current) Color.White else Color(0xFF574073), RoundedCornerShape(38.dp)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (locked) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF3D4855), modifier = Modifier.size(27.dp))
                    } else {
                        Text(
                            "$level",
                            color = Color.White,
                            fontSize = if (current) 38.sp else 34.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                }
            }
            if (current) Text("✦", color = Color.White, fontSize = 25.sp, modifier = Modifier.offset(y = (-55).dp))
        }
        if (!locked) StarBadge(star) else Spacer(Modifier.height(38.dp))
    }
}

@Composable
private fun StarBadge(star: Int) {
    val count = star.coerceIn(0, 5)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFFF8DA),
        shadowElevation = 3.dp,
        modifier = Modifier
            .padding(top = 7.dp)
            .height(34.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            repeat(5) { i ->
                Text(
                    if (i < count) "★" else "☆",
                    color = if (i < count) Color(0xFFF0B928) else Color(0xFFB9A77B),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

private fun DrawScope.drawBush(x: Float, y: Float, scale: Float) {
    drawCircle(Color(0xFF3F973F), 25f * scale, Offset(x - 20f * scale, y))
    drawCircle(Color(0xFF56B34A), 29f * scale, Offset(x + 7f * scale, y - 10f * scale))
    drawCircle(Color(0xFF32863B), 21f * scale, Offset(x + 32f * scale, y + 4f * scale))
}

private fun DrawScope.drawFlower(x: Float, y: Float, color: Color) {
    repeat(5) { k ->
        val angle = k * 2 * Math.PI / 5
        drawCircle(color, 8f, Offset(x + cos(angle).toFloat() * 13f, y + sin(angle).toFloat() * 13f))
    }
    drawCircle(Color(0xFFFFD22F), 6f, Offset(x, y))
}

private fun DrawScope.drawCrystal(x: Float, y: Float) {
    val p = Path().apply {
        moveTo(x, y - 28f); lineTo(x + 20f, y - 5f); lineTo(x + 10f, y + 25f)
        lineTo(x - 16f, y + 20f); lineTo(x - 25f, y - 5f); close()
    }
    drawPath(p, Color(0xFF6E8DE0), style = Stroke(3f, join = StrokeJoin.Round))
    drawPath(p, Color(0xFF6E8DE0))
    drawLine(Color(0xFFC8E5FF), Offset(x - 3f, y - 20f), Offset(x + 7f, y + 14f), 4f)
}
