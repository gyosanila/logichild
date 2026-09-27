package com.gyosanila.logichild

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.LocalStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

// Semua angka di file ini diambil dari docs/design/mockups/board.html (dunia 2880×864,
// layar 540×960, 24 kotak di X(i)=80+118i). Ubah mockup dulu, baru angka di sini.

internal const val WORLD_W = 2880f
private const val WORLD_H = 864f
private const val LOOP = 4f
private val YARR = intArrayOf(553, 615, 583, 489, 428, 461, 553, 615, 583, 489, 428, 461,
    553, 615, 583, 489, 428, 461, 553, 615, 583, 489, 428, 461)
internal fun ax(i: Int) = 80f + i * 118f
internal fun ay(i: Int) = YARR[i].toFloat()
private fun prand(n: Double): Float { val s = sin(n * 127.13) * 43758.5453; return (s - floor(s)).toFloat() }
private fun frost(x: Float) = ((x - 1820f) / 420f).coerceIn(0f, 1f)
internal fun c(hex: Long) = Color(hex or 0xFF000000)
private fun svg(d: String): Path = PathParser().parsePathString(d).toPath()

private enum class Season(val x0: Float, val patch: Color) {
    Spring(0f, c(0x63B247)), Summer(729f, c(0x4FA62E)), Autumn(1437f, c(0x8C9238)), Winter(2145f, c(0xBCD4E6));
    companion object { fun at(x: Float) = entries.last { x >= it.x0 || it == Spring } }
}

internal class Art {
    val road: Path = Path().apply {
        val k = 1.05f
        val p = (0 until 24).map { Offset(ax(it), ay(it)) }
        moveTo(p[0].x, p[0].y)
        for (i in 0 until p.size - 1) {
            val p0 = p.getOrElse(i - 1) { p[i] }; val p1 = p[i]; val p2 = p[i + 1]; val p3 = p.getOrElse(i + 2) { p[i + 1] }
            cubicTo(p1.x + (p2.x - p0.x) / 6 * k, p1.y + (p2.y - p0.y) / 6 * k,
                p2.x - (p3.x - p1.x) / 6 * k, p2.y - (p3.y - p1.y) / 6 * k, p2.x, p2.y)
        }
    }
    val blob44 = blob(44f, 44f * .86f); val blob46 = blob(46f, 46f * .86f)
    val hi44 = blob(44f * .72f, 44f * .6f); val hi46 = blob(46f * .72f, 46f * .6f)
    val ring = blob(57f, 57f * .86f)
    val appleBody = svg("M0,-2 C-9,-9 -14,-2 -12,4 C-10,10 -3,13 0,11 C3,13 10,10 12,4 C14,-2 9,-9 0,-2 Z")
    val appleLeaf = svg("M1,-6 C3,-11 8,-12 10,-10 C8,-6 4,-5 1,-6 Z")
    val bowL = svg("M0,-11 C-8,-18 -14,-13 -9,-10 C-5,-8 -2,-10 0,-11 Z")
    val bowR = svg("M0,-11 C8,-18 14,-13 9,-10 C5,-8 2,-10 0,-11 Z")
    val shackle = svg("M-7,-2 v-4 a7,7 0 0 1 14,0 v4")
    val cup = svg("M-13,-22 h26 v9 a13,13 0 0 1 -26,0 Z")
    val handles = svg("M-13,-20 c-8,0 -9,12 1,13 M13,-20 c8,0 9,12 -1,13")
    val base1 = svg("M-11,-1 h22 l3,7 h-28 Z")
    val pineA = svg("M0,-98 L28,-52 L-28,-52 Z"); val pineB = svg("M0,-74 L34,-26 L-34,-26 Z")
    val snowA = svg("M0,-98 L18,-68 L-18,-68 Z"); val snowB = svg("M0,-72 L22,-44 L-22,-44 Z")
    val tuft = svg("M0,0 C-6,-16 -14,-22 -20,-24 M0,0 C0,-20 2,-30 6,-34 M0,0 C8,-14 14,-18 20,-20")
    val hills = listOf(
        svg("M0,258 C 180,164 330,196 470,248 C 560,280 620,288 700,288 L0,288 Z") to c(0x6FB7A0),
        svg("M1050,288 C 1150,196 1290,182 1400,236 C 1490,278 1600,288 1700,288 Z") to c(0x6FB7A0),
        svg("M1900,288 C 2010,188 2170,178 2280,240 C 2380,284 2500,288 2620,288 Z") to c(0x6FB7A0),
        svg("M420,288 C 540,208 660,202 780,254 C 850,284 900,288 960,288 Z") to c(0x5FA98F),
        svg("M1480,288 C 1590,214 1720,208 1840,260 C 1900,285 1960,288 2020,288 Z") to c(0x5FA98F),
    )
    val horizon = svg("M0,250 C 300,238 620,262 900,248 C 1200,234 1500,260 1800,246 C 2100,232 2450,258 2880,244 L2880,322 L0,322 Z")
    val leaf = svg("M0,0 C-6,-3 -7.5,-7 -3,-10 C2.5,-8 5,-4 0,0 Z")
    val beanie = svg("M-17,0 C-17,-20 17,-20 17,0 Z")

    private fun blob(rx: Float, ry: Float): Path {
        val a = List(8) { i ->
            val ang = i * PI / 4; val rr = 1 + (prand(i * 3.1) - .5f) * .14f
            Offset((cos(ang) * rx * rr).toFloat(), (sin(ang) * ry * rr).toFloat())
        }
        return Path().apply {
            moveTo(a[0].x, a[0].y)
            for (j in 1..a.size) {
                val q = a[j % a.size]; val pr = a[j - 1]
                quadraticBezierTo(pr.x, pr.y, (pr.x + q.x) / 2 + (pr.y - q.y) * .16f, (pr.y + q.y) / 2 + (q.x - pr.x) * .16f)
            }
            close()
        }
    }
}

internal inline fun DrawScope.at(x: Float, y: Float, s: Float = 1f, rot: Float = 0f, block: DrawScope.() -> Unit) =
    withTransform({ translate(x, y); if (rot != 0f) rotate(rot, Offset.Zero); if (s != 1f) scale(s, s, Offset.Zero) }, block)

internal fun DrawScope.star(cx: Float, cy: Float, r: Float, fill: Color, stroke: Color? = null, sw: Float = 0f) {
    val p = Path()
    for (i in 0 until 10) {
        val a = -PI / 2 + i * PI / 5; val rr = if (i % 2 == 1) r * .44f else r
        val x = cx + (cos(a) * rr).toFloat(); val y = cy + (sin(a) * rr).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    drawPath(p, fill)
    if (stroke != null) drawPath(p, stroke, style = Stroke(sw, join = StrokeJoin.Round))
}

internal fun DrawScope.apple(art: Art, x: Float, y: Float, s: Float) = at(x, y, s) {
    drawPath(art.appleBody, c(0xFF6B6B)); drawPath(art.appleLeaf, c(0x4FAE2E))
    drawRoundRect(c(0x6B4423), Offset(-1f, -8f), Size(2f, 5f), CornerRadius(1f))
}

private fun DrawScope.gift(art: Art, x: Float, y: Float, s: Float) = at(x, y, s) {
    drawRoundRect(c(0xA78BFA), Offset(-11f, -6f), Size(22f, 16f), CornerRadius(3f))
    drawRoundRect(c(0xC4B1FF), Offset(-12f, -11f), Size(24f, 7f), CornerRadius(3f))
    drawRect(c(0xFFC94D), Offset(-2.5f, -11f), Size(5f, 21f))
    drawPath(art.bowL, c(0xFFC94D)); drawPath(art.bowR, c(0xFFC94D))
}

private fun DrawScope.controller(x: Float, y: Float, s: Float) = at(x, y, s) {
    drawRoundRect(c(0x3F7FD6), Offset(-12f, -8f), Size(24f, 16f), CornerRadius(7f))
    drawRoundRect(Color.White, Offset(-7f, -2.6f), Size(7f, 2.6f), CornerRadius(1.3f))
    drawRoundRect(Color.White, Offset(-4.7f, -4.9f), Size(2.6f, 7f), CornerRadius(1.3f))
    drawCircle(c(0xFFC94D), 2.1f, Offset(6f, -1.6f)); drawCircle(c(0xFF8C8C), 2.1f, Offset(9.6f, 1.8f))
}

private val RAINBOW = listOf(c(0xFF6B6B), c(0xFFC94D), c(0x7ED957), c(0x4FB0E8), c(0xA78BFA))
private fun DrawScope.rainbow(x: Float, y: Float, s: Float, width: Float = 2.6f, r0: Float = 12f, step: Float = 2f, cols: List<Color> = RAINBOW) =
    at(x, y, s) {
        cols.forEachIndexed { i, col ->
            val r = r0 - i * step
            drawArc(col, 180f, 180f, false, Offset(-r, -r), Size(r * 2, r * 2), style = Stroke(width))
        }
    }

private fun DrawScope.lockIcon(art: Art, x: Float, y: Float, s: Float) = at(x, y, s) {
    val col = c(0x7D7D7D)
    drawPath(art.shackle, col, style = Stroke(3.4f, cap = StrokeCap.Round))
    drawRoundRect(col, Offset(-10f, -2f), Size(20f, 15f), CornerRadius(3.4f))
    drawCircle(Color.White.copy(alpha = .85f), 2.2f, Offset(0f, 5f))
}

private fun DrawScope.trophy(art: Art, x: Float, y: Float, s: Float, gold: Boolean) = at(x, y, s) {
    val g1 = if (gold) c(0xFFD24D) else c(0xD7D7D7); val g2 = if (gold) c(0xE0A22B) else c(0xB4B4B4)
    drawPath(art.cup, g1); drawPath(art.handles, g2, style = Stroke(3.4f, cap = StrokeCap.Round))
    drawRect(g2, Offset(-3f, -9f), Size(6f, 9f)); drawPath(art.base1, g1)
    drawRect(g2, Offset(-16f, 6f), Size(32f, 6f)); drawRect(Color.White.copy(alpha = .38f), Offset(-9f, -19f), Size(6f, 13f))
}

private fun DrawScope.flag(x: Float, y: Float, s: Float) = at(x, y, s) {
    drawRoundRect(c(0x8B5E3C), Offset(-1.2f, -20f), Size(2.4f, 22f), CornerRadius(1.2f))
    drawPath(Path().apply { moveTo(1.4f, -19f); lineTo(15f, -14.5f); lineTo(1.4f, -10f); close() }, c(0xE04747))
}

internal fun DrawScope.cloud(x: Float, y: Float, s: Float, op: Float, face: Boolean) = at(x, y, s) {
    val w = Color.White.copy(alpha = op)
    fun e(cx: Float, cy: Float, rx: Float, ry: Float, col: Color) = drawOval(col, Offset(cx - rx, cy - ry), Size(rx * 2, ry * 2))
    e(0f, 0f, 46f, 24f, w); e(-34f, 8f, 30f, 17f, w); e(32f, 9f, 34f, 19f, w); e(6f, -14f, 28f, 19f, w)
    e(0f, 10f, 46f, 14f, c(0xEAF6FF).copy(alpha = op))
    if (face) {
        drawCircle(c(0x3A2415), 4.8f, Offset(-13f, -5f)); drawCircle(c(0x3A2415), 4.8f, Offset(13f, -5f))
        drawCircle(Color.White, 1.8f, Offset(-11.4f, -6.6f)); drawCircle(Color.White, 1.8f, Offset(14.6f, -6.6f))
        drawArc(c(0x3A2415), 20f, 140f, false, Offset(-9f, -4f), Size(18f, 10f), style = Stroke(2.8f, cap = StrokeCap.Round))
        e(-26f, 3f, 5.6f, 3.7f, c(0xFF9C9C).copy(alpha = .7f)); e(26f, 3f, 5.6f, 3.7f, c(0xFF9C9C).copy(alpha = .7f))
    }
}

private fun DrawScope.tree(art: Art, x: Float, y: Float, s: Float, kind: Season, rot: Float) = at(x, y, s, rot) {
    if (kind == Season.Winter) {
        drawRoundRect(c(0x7A5334), Offset(-6f, -24f), Size(12f, 28f), CornerRadius(4f))
        drawPath(art.pineA, c(0x3F7E52)); drawPath(art.pineB, c(0x356F47))
        drawPath(art.snowA, Color.White.copy(alpha = .85f)); drawPath(art.snowB, Color.White.copy(alpha = .75f))
        return@at
    }
    val cols = when (kind) {
        Season.Autumn -> listOf(0xE08A2E, 0xD2762A, 0xC9702A, 0xEFA23C, 0xF7BE6A)
        Season.Spring -> listOf(0xF7A8C4, 0xFBB8D0, 0xF59BBC, 0xFFC9DD, 0xFFFFFF)
        else -> listOf(0x4FAE4A, 0x5CBB52, 0x47A043, 0x5CBB52, 0x8ADA7A)
    }.map { c(it.toLong()) }
    drawRoundRect(c(0x8B5E3C), Offset(-5f, -26f), Size(10f, 30f), CornerRadius(4f))
    drawCircle(cols[0], 26f, Offset(0f, -42f)); drawCircle(cols[1], 17f, Offset(-20f, -28f))
    drawCircle(cols[2], 18f, Offset(20f, -29f)); drawCircle(cols[3], 19f, Offset(0f, -60f))
    drawCircle(cols[4].copy(alpha = .6f), 8f, Offset(-8f, -50f))
}

internal fun DrawScope.bush(x: Float, y: Float, s: Float, snow: Float) = at(x, y, s) {
    drawOval(Color.Black.copy(alpha = .13f), Offset(-34f, -3f), Size(68f, 18f))
    drawCircle(c(0x4FAE4A), 17f, Offset(-18f, -8f)); drawCircle(c(0x4FAE4A), 18f, Offset(16f, -9f))
    drawCircle(c(0x5CBB52), 20f, Offset(0f, -20f)); drawCircle(c(0x8ADA7A).copy(alpha = .7f), 8f, Offset(-7f, -26f))
    if (snow > .25f) drawOval(Color.White.copy(alpha = .88f * snow), Offset(-30f, -31f), Size(60f, 22f))
}

internal fun DrawScope.flower(x: Float, y: Float, s: Float, col: Color) = at(x, y, s) {
    drawRoundRect(c(0x3F8F34), Offset(-1.6f, 0f), Size(3.2f, 17f), CornerRadius(1.6f))
    for (i in 0 until 5) {
        val a = i * 72 * PI / 180
        drawCircle(col, 4.8f, Offset((cos(a) * 7).toFloat(), (sin(a) * 7).toFloat()))
    }
    drawCircle(c(0xFFE066), 4f, Offset.Zero)
}

private fun DrawScope.mushroom(x: Float, y: Float, s: Float) = at(x, y, s) {
    drawOval(Color.Black.copy(alpha = .12f), Offset(-12f, -2f), Size(24f, 8f))
    drawRoundRect(c(0xFFF3DC), Offset(-5f, -9f), Size(10f, 12f), CornerRadius(4f))
    drawArc(c(0xFF6B6B), 180f, 180f, true, Offset(-14f, -19f), Size(28f, 22f))
    drawCircle(Color.White, 2.6f, Offset(-5f, -12f)); drawCircle(Color.White, 2f, Offset(6f, -10f))
}

private fun DrawScope.crystal(x: Float, y: Float, s: Float) = at(x, y, s) {
    drawOval(Color.Black.copy(alpha = .12f), Offset(-13f, -1f), Size(26f, 8f))
    drawPath(Path().apply { moveTo(0f, -30f); lineTo(11f, -6f); lineTo(4f, 3f); lineTo(-5f, 3f); lineTo(-12f, -7f); close() }, c(0x8ED8F0))
    drawPath(Path().apply { moveTo(0f, -30f); lineTo(11f, -6f); lineTo(0f, -2f); close() }, Color.White.copy(alpha = .45f))
}

private fun DrawScope.pumpkin(x: Float, y: Float, s: Float) = at(x, y, s) {
    drawOval(Color.Black.copy(alpha = .14f), Offset(-19f, -3f), Size(38f, 12f))
    drawOval(c(0xF08A2E), Offset(-19f, -24f), Size(38f, 30f))
    drawOval(c(0xF7A24A).copy(alpha = .55f), Offset(-5f, -24f), Size(10f, 28f))
    drawRoundRect(c(0x4F8A2E), Offset(-2.6f, -26f), Size(5.2f, 7f), CornerRadius(2f))
}

private fun DrawScope.snowman(x: Float, y: Float, s: Float) = at(x, y, s) {
    drawOval(c(0xA9C6DC).copy(alpha = .5f), Offset(-24f, -3f), Size(48f, 14f))
    drawCircle(Color.White, 17f, Offset(0f, -14f)); drawCircle(Color.White, 12f, Offset(0f, -42f))
    drawCircle(c(0x503018), 1.7f, Offset(-4.6f, -44f)); drawCircle(c(0x503018), 1.7f, Offset(4.6f, -44f))
    drawPath(Path().apply { moveTo(0f, -41f); lineTo(9f, -38f); lineTo(0f, -35f); close() }, c(0xF08A2E))
    drawRect(c(0xE04747), Offset(-11f, -31f), Size(22f, 5f))
}

private val DECO_COLORS = mapOf('r' to c(0xFF6B6B), 'y' to c(0xFFC94D), 'p' to c(0xA78BFA), 'b' to c(0x4FB0E8))
// x, y, jenis, skala, warna (board.html DECO + MID + garden set)
private val DECO: List<Triple<FloatArray, Char, Char>> = listOf(
    "-6,404,B,1.2", "-40,664,F,1.2,r", "150,262,B,.85", "206,676,C,1", "300,286,F,1.1,y", "352,668,M,1",
    "470,268,B,1", "524,704,F,1.2,p", "618,262,C,.9", "684,668,B,1.1", "766,288,F,1,r", "852,706,F,1.2,y",
    "936,264,B,.9", "1004,652,M,.95", "1096,260,F,1.1,b", "1174,692,B,1.15", "1284,284,C,1.05", "1344,664,F,1.1,r",
    "1454,262,B,.85", "1524,700,F,1.2,y", "1636,274,M,1", "1714,650,B,1", "1824,260,F,1.1,p", "1894,692,C,.95",
    "1994,284,B,1.1", "2074,664,F,1.2,r", "2184,262,F,1,b", "2254,700,B,.9", "2374,280,M,1", "2444,664,F,1.2,y",
    "2554,262,B,1", "2634,692,C,1.1", "2734,286,F,1.1,r", "2824,664,B,1.05",
    "92,364,F,.95,b", "168,326,F,.8,y", "292,392,F,.85,r", "440,300,B,.48", "588,336,F,.85,p", "700,392,B,.5",
    "812,366,F,.85,y", "960,304,B,.6", "1046,340,F,.95,r", "1254,300,B,.52", "1392,356,F,.9,p", "1550,308,B,.58",
    "1698,354,F,.9,y", "1846,302,B,.54", "1994,364,F,.85,p", "2106,354,M,.9", "2290,350,F,.9,r", "2438,300,B,.52",
    "2586,362,F,.85,b", "2790,352,F,.9,r",
    "1520,684,P,1.25", "1694,662,P,1", "2230,686,S,1.25", "2750,678,S,.95",
).map { row ->
    val t = row.split(",")
    Triple(floatArrayOf(t[0].toFloat(), t[1].toFloat(), t[3].toFloat()), t[2][0], t.getOrNull(4)?.get(0) ?: 'r')
}

/** Beruang + baju dari apel. Koordinat topi/syal = titik pada adventure_bear.png (448 px). */
private fun DrawScope.bear(art: Art, img: ImageBitmap, x: Float, feetY: Float, size: Float, outfits: Int, squash: Float = 1f, rot: Float = 0f) =
    withTransform({ translate(x, feetY); rotate(rot, Offset.Zero); scale(1f, squash, Offset.Zero) }) {
        val left = -size / 2; val top = -.968f * size; val k = size / 448f
        drawImage(img, dstOffset = IntOffset(left.toInt(), top.toInt()), dstSize = IntSize(size.toInt(), size.toInt()), filterQuality = FilterQuality.Medium)
        // ponytail: 2 baju (syal, topi) lalu berulang ganti warna; tambah item baru di sini kalau mau variasi.
        if (outfits >= 1) at(left + 226 * k, top + 262 * k, k * 3.4f, 10f) {
            val col = if ((outfits - 1) / 2 % 2 == 0) c(0x4FB0E8) else c(0x7ED957)
            drawRoundRect(col, Offset(-22f, -5f), Size(44f, 11f), CornerRadius(5.5f))
            drawRoundRect(col, Offset(6f, 2f), Size(9f, 20f), CornerRadius(4f))
            drawRect(Color.White.copy(alpha = .5f), Offset(-14f, -5f), Size(3f, 11f)); drawRect(Color.White.copy(alpha = .5f), Offset(-2f, -5f), Size(3f, 11f))
        }
        if (outfits >= 2) at(left + 258 * k, top + 60 * k, k * 3.6f, 12f) {
            val col = if ((outfits - 2) / 2 % 2 == 0) c(0xFF6B6B) else c(0xA78BFA)
            drawPath(art.beanie, col)
            drawRoundRect(Color.White, Offset(-19f, -3f), Size(38f, 8f), CornerRadius(4f))
            drawCircle(Color.White, 5f, Offset(0f, -17f))
        }
    }

/** Dunia papan (koordinat world 2880×864). Dipakai Papan & pratinjau di Home. */
internal fun DrawScope.drawWorld(
    art: Art, bearImg: ImageBitmap, numbers: List<TextLayoutResult>, startLabel: TextLayoutResult,
    state: AdventureState, t: Float, visW: Float, cam: Float, hopFrom: Int, hopTo: Int, f: Float,
) {
    val arc = sin(f * PI).toFloat()
    val i0 = hopFrom - 1; val i1 = hopTo - 1
    val bx = ax(i0) + (ax(i1) - ax(i0)) * f; val by = ay(i0) + (ay(i1) - ay(i0)) * f
    val cur = hopTo - 1; val moving = f < 1f
    fun wsin(ph: Float) = sin((t / LOOP + ph) * 2 * PI).toFloat()
    val bob = sin(t * PI).toFloat()
    // langit + awan (parallax 0.1)
    drawRect(Brush.verticalGradient(0f to c(0x8FD4F7), .72f to c(0xBFE9FF), 1f to c(0xE7F8FF), endY = 260f), size = Size(visW, 260f))
    at(-cam * .1f, 0f) {
        drawCircle(Brush.radialGradient(listOf(c(0xFFF3B0).copy(alpha = .95f), c(0xFFE066).copy(alpha = 0f)), Offset(470f, 96f), 120f), 120f, Offset(470f, 96f))
        drawCircle(c(0xFFE98A), 42f, Offset(470f, 96f))
        for ((cx, cy, sc, op, face) in listOf(
            listOf(190f, 112f, 1.05f, .97f, 1f), listOf(742f, 74f, .8f, .92f, 0f), listOf(1180f, 138f, 1.12f, .95f, 1f),
            listOf(1742f, 88f, .72f, .88f, 0f), listOf(2300f, 122f, .96f, .92f, 0f), listOf(2680f, 68f, .82f, .9f, 0f),
        )) cloud(cx + wsin(cx / 420f) * 11f, cy + bob * 3f, sc, op, face > 0f)
        rainbow(330f + 112f, 258f, 1f, 10f, 112f, 11f, RAINBOW + c(0xA78BFA))
    }
    at(-cam, 0f) {
        art.hills.forEach { (p, col) -> drawPath(p, col.copy(alpha = .9f)) }
        // tanah per musim
        drawRect(Brush.horizontalGradient(
            0f to c(0xA6DE78), .22f to c(0x8ACD5E), .28f to c(0x8FD24F), .46f to c(0x63B537), .52f to c(0xD3D06A),
            .62f to c(0xC3C259), .72f to c(0xCFD79C), .80f to c(0xE4EDD6), .88f to c(0xF2F9FD), 1f to c(0xDCEBF5),
            startX = 0f, endX = WORLD_W), Offset(0f, 250f), Size(WORLD_W, 614f))
        drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = .3f), .22f to Color.Transparent, .6f to Color.Transparent,
            1f to Color.Black.copy(alpha = .1f), startY = 250f, endY = 864f), Offset(0f, 250f), Size(WORLD_W, 614f))
        drawPath(art.horizon, Color.White.copy(alpha = .15f))
        for (gi in 0 until 46) {
            val gx = prand(gi * 3.7) * WORLD_W; val gy = 300 + prand(gi * 5.1) * 520; val gs = .7f + prand(gi * 7.3) * .8f
            val fr = frost(gx)
            drawOval((if (fr > .5f) c(0xCFE2F0) else Season.at(gx).patch).copy(alpha = .16f * (1 - fr * .7f)),
                Offset(gx - 58 * gs, gy - 16 * gs), Size(116 * gs, 32 * gs))
        }
        for (ti in 0 until 14) {
            val tx = 120 + ti * 205 + prand(ti * 9.1) * 70; val fr = frost(tx)
            val kind = if (fr > .6f) Season.Winter else if (fr > .12f) Season.Autumn else Season.at(tx)
            tree(art, tx, 262f, .8f + prand(ti * 2.3) * .35f, kind, wsin(tx / 260f) * 2.6f)
        }
        for (fb in 0 until 78) {
            val fx = 8 + fb * 37 + prand(fb * 3.9) * 22; val fy = 736 + prand(fb * 5.5) * 112
            at(fx, fy, .8f + prand(fb * 7.1) * .75f, wsin(fx / 160f) * 8f) { drawPath(art.tuft, lerp(c(0x3F8F34), c(0xDCEBF5), frost(fx)), style = Stroke(3.8f, cap = StrokeCap.Round)) }
        }
        // jalan pasir
        at(0f, 6f) { drawPath(art.road, c(0xB8834B).copy(alpha = .55f), style = Stroke(80f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
        drawPath(art.road, c(0xC8955B), style = Stroke(78f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(art.road, c(0xEFCF97), style = Stroke(70f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(art.road, c(0xF7E3BC), style = Stroke(48f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(art.road, c(0xE8C48D).copy(alpha = .9f), style = Stroke(6f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 26f))))
        clipRect(2145f, 240f, WORLD_W, 740f) {
            drawPath(art.road, Color.White.copy(alpha = .45f), style = Stroke(78f, cap = StrokeCap.Round))
            drawPath(art.road, c(0xF2FAFF).copy(alpha = .5f), style = Stroke(52f, cap = StrokeCap.Round))
        }
        for ((v, kind, col) in DECO) {
            val (x, y, sc) = Triple(v[0], v[1], v[2]); val fr = frost(x); val cold = fr > .25f
            when (kind) {
                'B' -> bush(x, y, sc, fr)
                'F' -> flower(x, y, sc, if (cold) c(0xEAF4FF) else DECO_COLORS.getValue(col))
                'C' -> crystal(x, y, sc)
                'M' -> mushroom(x, y, sc)
                'P' -> pumpkin(x, y, sc)
                else -> snowman(x, y, sc)
            }
        }
        // partikel musim (kelopak, kilau, daun, salju)
        for (px in 0 until 60) {
            val baseX = 20 + px * 47.5f + prand(px * 4.1) * 30
            val x = baseX + wsin(px * .13f) * 30; val y = 250 + (prand(px * 6.3) * 560 + t * 140) % 560
            val rot = prand(px * 8.8) * 90 - 45 + t * (if (px % 2 == 1) 46 else -46)
            val sc = .8f + prand(px * 3.1) * .7f
            when (Season.at(baseX)) {
                Season.Spring -> at(x, y, sc, rot) { drawOval(c(0xFFB6C9).copy(alpha = .8f), Offset(-7f, -3.4f), Size(14f, 6.8f)) }
                Season.Summer -> at(x, y, sc) { drawCircle(c(0xFFF7C2).copy(alpha = .85f), 4f, Offset.Zero) }
                Season.Autumn -> at(x, y, sc, rot) { drawPath(art.leaf, listOf(c(0xE08A2E), c(0xC9702A), c(0xEFA23C), c(0xD2762A))[px % 4].copy(alpha = .9f)) }
                Season.Winter -> drawCircle(Color.White.copy(alpha = .95f), 2.4f + prand(px * 2.6) * 2.4f, Offset(x, y))
            }
        }
        // kotak
        for (i in 0 until 24) {
            val isCur = i == cur && !moving; val done = i < cur; val locked = i > cur + 4
            val (top, rim) = when {
                done -> c(0x7ED957) to c(0x5CB93A); isCur -> c(0xFFD24D) to c(0xE8A81F)
                locked -> c(0xCFCFCF) to c(0xAFAFAF); else -> c(0xFFF3DC) to c(0xE3CBA6)
            }
            val r = if (isCur) 46f else 44f
            at(ax(i), ay(i)) {
                val a = if (locked) .72f else 1f
                if (isCur) drawCircle(Brush.radialGradient(listOf(c(0xFFF0B0).copy(alpha = .85f), c(0xFFD24D).copy(alpha = 0f)), Offset.Zero, r + 20), r + 20, Offset.Zero)
                drawOval(Color.Black.copy(alpha = .14f * a), Offset(-r * .95f, r * .62f - r * .34f), Size(r * 1.9f, r * .68f))
                val blob = if (isCur) art.blob46 else art.blob44
                at(0f, 5f) { drawPath(blob, rim.copy(alpha = a)) }
                drawPath(blob, top.copy(alpha = a))
                at(-6f, -7f) { drawPath(if (isCur) art.hi46 else art.hi44, Color.White.copy(alpha = if (locked) .16f else .3f)) }
                if (isCur) drawPath(art.ring, c(0xFFC94D), style = Stroke(4f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 13f), t * 12)))
                val bxr = r * .62f; val byr = -r * .54f
                val (ring, icon) = when (AdventureBoard.tileEffect(i + 1)) {
                    AdventureTileEffect.Star -> c(0xFFC94D) to 's'
                    AdventureTileEffect.MiniGame -> c(0x3F7FD6) to 'g'
                    AdventureTileEffect.Bonus -> c(0xA78BFA) to 'r'
                    AdventureTileEffect.Collect -> c(0xFF6B6B) to 'a'
                    AdventureTileEffect.Gift -> c(0xA78BFA) to 'h'
                    else -> Color.Transparent to ' '
                }
                if (icon != ' ') {
                    drawCircle(Color.White, 14f, Offset(bxr, byr)); drawCircle(ring, 14f, Offset(bxr, byr), style = Stroke(3f))
                    when (icon) {
                        's' -> star(bxr, byr, 9.6f, c(0xFFC94D)); 'g' -> controller(bxr, byr, .62f)
                        'r' -> rainbow(bxr, byr + 2, .62f); 'a' -> apple(art, bxr, byr, .72f); else -> gift(art, bxr, byr + 1, .66f)
                    }
                }
                when (i) {
                    0 -> at(0f, -r * 1.42f) {
                        drawRoundRect(c(0x8B5E3C), Offset(-38f, -17f), Size(76f, 34f), CornerRadius(9f))
                        drawRoundRect(c(0xA9744F), Offset(-34f, -13f), Size(68f, 26f), CornerRadius(7f))
                        drawRect(c(0x8B5E3C), Offset(-3f, 16f), Size(6f, 14f))
                        drawText(startLabel, topLeft = Offset(-startLabel.size.width / 2f, -startLabel.size.height / 2f))
                    }
                    23 -> trophy(art, 0f, -r * .95f, .95f, state.mapComplete)
                    else -> {
                        val n = numbers[i]; val o = Offset(-n.size.width / 2f, -n.size.height / 2f - 4f)
                        drawText(n, color = Color.White, topLeft = o, drawStyle = Stroke(4f, join = StrokeJoin.Round))
                        drawText(n, topLeft = o)
                    }
                }
                if (locked) lockIcon(art, 0f, -2f, 1.35f)
            }
        }
        // beruang (lompat 1 kotak, ± 74 unit ke atas)
        drawOval(Color.Black.copy(alpha = .22f - .12f * arc), Offset(bx - (26 - 9 * arc), by - 26 - 7), Size((26 - 9 * arc) * 2, 14f))
        if (moving && f > .03f && f < .92f) {
            val dust = c(0xFFF6DE).copy(alpha = .8f * (1 - f))
            drawCircle(dust, 9 + 22 * f, Offset(ax(i0) - 16, ay(i0) - 8)); drawCircle(dust, 6 + 15 * f, Offset(ax(i0) + 18, ay(i0) - 14))
        }
        if (moving) bear(art, bearImg, bx, by - 28 - arc * 74, 122f, state.outfits, 1 + .07f * arc, -7 * arc)
        else bear(art, bearImg, bx, by - 28 + bob * 3.2f, 122f, state.outfits, 1 + bob * .014f)
        if (!moving) listOf(floatArrayOf(-46f, -30f, 6f), floatArrayOf(48f, -14f, 4.4f), floatArrayOf(-30f, 26f, 4f), floatArrayOf(58f, 20f, 5f))
            .forEachIndexed { k, sp -> star(bx + sp[0], by + sp[1], sp[2], Color.White.copy(alpha = .55f + k * .12f)) }
        for (fi in 0 until 16) {
            val fx = 40f + fi * 180
            at(fx, 700 + prand(fi * 6.6) * 24, .9f + prand(fi * 4.4) * .5f, wsin(fx / 150f) * 8f) {
                drawPath(art.tuft, lerp(c(0x3F8F34), c(0xDCEBF5), frost(fx)).copy(alpha = .85f), style = Stroke(4f, cap = StrokeCap.Round))
            }
        }
    }
}

/** Label kotak 1..24 + papan "MULAI", diukur sekali. */
@Composable
internal fun rememberWorldText(): Pair<List<TextLayoutResult>, TextLayoutResult> {
    val strings = LocalStrings.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    return remember(density, strings) {
        val st = TextStyle(fontSize = with(density) { 32f.toSp() }, fontWeight = FontWeight.Bold, color = c(0x4A2E14))
        (1..24).map { measurer.measure(it.toString(), st) } to
            measurer.measure(strings.adventureStart, TextStyle(fontSize = with(density) { 17f.toSp() }, fontWeight = FontWeight.Bold, color = c(0xFFF8EC)))
    }
}


@Composable
fun AdventureScreen(
    state: AdventureState,
    onStateChange: (AdventureState) -> Unit,
    onMiniGame: (game: String) -> Unit,
    onBack: () -> Unit,
    /** Bintang dari mini-game yang barusan selesai (0 = tidak ada); ditampilkan sekali. */
    earnedStars: Int = 0,
    onEarnedShown: () -> Unit = {},
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val art = remember { Art() }
    val board = remember { AdventureBoard() }
    val bearImg = ImageBitmap.imageResource(R.drawable.adventure_bear)
    val sounds = remember { GameSounds(context) }
    DisposableEffect(Unit) { onDispose { sounds.release() } }
    val scope = rememberCoroutineScope()
    val (numbers, startLabel) = rememberWorldText()

    val time by rememberInfiniteTransition(label = "t").animateFloat(
        0f, 40f, infiniteRepeatable(tween(40_000, easing = LinearEasing), RepeatMode.Restart), label = "t",
    )
    val hop = remember { Animatable(1f) }
    var hopFrom by remember { mutableIntStateOf(state.position) }
    var hopTo by remember { mutableIntStateOf(state.position) }
    var busy by remember { mutableStateOf(false) }
    var chip by remember { mutableStateOf<Pair<Char, Int>?>(null) } // 's' bintang / 'a' apel
    val appleFly = remember { Animatable(1f) }
    var outfitShow by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.position) { if (!busy) { hopFrom = state.position; hopTo = state.position } }

    LaunchedEffect(earnedStars) {
        if (earnedStars > 0) { chip = 's' to earnedStars; sounds.reward(earnedStars); onEarnedShown() }
    }
    LaunchedEffect(chip) { if (chip != null) { delay(1800); chip = null } }

    fun walk() {
        if (busy || state.position >= AdventureBoard.MAP_LENGTH) return
        busy = true
        scope.launch {
            try {
                val move = board.step(state.position)
                var at = state.position
                while (at < move.position) {
                    hopFrom = at; hopTo = at + 1
                    sounds.move()
                    hop.snapTo(0f); hop.animateTo(1f, tween(650, easing = LinearEasing))
                    at++
                }
                val next = state.land(move)
                onStateChange(next)
                when (move.effect) {
                    AdventureTileEffect.Star, AdventureTileEffect.Gift -> { chip = 's' to 1; sounds.sparkle() }
                    AdventureTileEffect.Collect -> {
                        sounds.sparkle(); appleFly.snapTo(0f); appleFly.animateTo(1f, tween(800))
                        if (next.outfits > state.outfits) { outfitShow = next.outfits; sounds.reward(5) } else chip = 'a' to 1
                    }
                    AdventureTileEffect.MiniGame -> { delay(700); onMiniGame(adventureMiniGame(next.miniGameIndex - 1)) }
                    AdventureTileEffect.Finish -> sounds.reward(5)
                    else -> Unit
                }
            } finally { busy = false }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(c(0x8FD4F7))) {
        val u: Dp = maxWidth / 540
        val worldTopPx = with(density) { (u * 100).toPx() }
        // --- dunia (satu Canvas, kamera ikut beruang) ---
        Canvas(Modifier.fillMaxSize()) {
            val t = time
            val s = (size.height - worldTopPx) / WORLD_H
            val visW = size.width / s
            val f = hop.value
            val i0 = hopFrom - 1; val i1 = hopTo - 1
            val bx = ax(i0) + (ax(i1) - ax(i0)) * f; val by = ay(i0) + (ay(i1) - ay(i0)) * f
            val cam = (bx - 250f * visW / 540f).coerceIn(0f, WORLD_W - visW)

            withTransform({ translate(0f, worldTopPx); scale(s, s, Offset.Zero) }) {
                drawWorld(art, bearImg, numbers, startLabel, state, t, visW, cam, hopFrom, hopTo, f)
            }
            // apel terbang ke HUD
            if (appleFly.value < 1f) {
                val p = appleFly.value
                val sx = (bx - cam) * s; val sy = worldTopPx + (by - 110) * s
                val ex = size.width - 120 * u.toPx(); val ey = 44 * u.toPx()
                val x = sx + (ex - sx) * p; val y = sy + (ey - sy) * p - sin(p * PI).toFloat() * 120 * u.toPx()
                apple(art, x, y, 2.2f * u.toPx() * (1 + .6f * sin(p * PI).toFloat()))
            }
        }

        // --- HUD ---
        Column(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(u * 88).background(Brush.verticalGradient(listOf(c(0x7FC3EE), c(0x8FD4F7), Color.Transparent)))) {
                Row(Modifier.fillMaxSize().padding(horizontal = u * 16), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(u * 44).shadow(4.dp, CircleShape).clip(CircleShape).background(Color.White).clickable(onClick = onBack),
                        contentAlignment = Alignment.Center) {
                        Canvas(Modifier.size(u * 24)) {
                            val k = size.width / 24
                            drawPath(Path().apply { moveTo(14.5f * k, 5 * k); lineTo(7.5f * k, 12 * k); lineTo(14.5f * k, 19 * k) }, c(0x1E3A5F),
                                style = Stroke(3 * k, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                    Spacer(Modifier.width(u * 8))
                    Row(Modifier.weight(1f, fill = false).height(u * 46).shadow(4.dp, RoundedCornerShape(u * 20)).clip(RoundedCornerShape(u * 20))
                        .background(Brush.verticalGradient(listOf(c(0xA9744F), c(0x8B5E3C)))).padding(horizontal = u * 13),
                        verticalAlignment = Alignment.CenterVertically) {
                        Canvas(Modifier.size(u * 22)) { val k = size.width / 28; at(size.width / 2, size.height / 2, k) { drawCircle(c(0xFFF3DC), 13f, Offset.Zero); flag(0f, 4f, .8f) } }
                        Spacer(Modifier.width(u * 6))
                        Text(strings.adventureMapName.format(state.adventureNumber), color = c(0xFFF8EC), fontWeight = FontWeight.Bold, fontSize = (u.value * 15).sp, maxLines = 1)
                    }
                    Spacer(Modifier.weight(.01f))
                    HudPill(u, "${state.stars}") { star(size.width / 2, size.height / 2, size.width * .44f, c(0xFFC94D), c(0xE0A22B), size.width * .06f) }
                    Spacer(Modifier.width(u * 7))
                    HudPill(u, "${state.fruits}") { apple(art, size.width / 2, size.height / 2 + size.width * .08f, size.width / 24) }
                }
            }
            // pita progres 24 kotak
            Box(Modifier.fillMaxWidth().padding(horizontal = u * 16).height(u * 12)) {
                val pct = (hopTo.toFloat() / AdventureBoard.MAP_LENGTH).coerceIn(0f, 1f)
                Box(Modifier.fillMaxSize().clip(RoundedCornerShape(u * 6)).background(c(0x0C2842).copy(alpha = .28f))) {
                    Box(Modifier.fillMaxWidth(pct).fillMaxHeight().background(Brush.horizontalGradient(listOf(c(0xFFD98A), c(0xFFB347), c(0xFF8C42)))))
                }
                Row(Modifier.fillMaxSize().padding(horizontal = u * 5), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    repeat(24) { i ->
                        val big = i % 6 == 0
                        Box(Modifier.size(if (big) u * 6 else u * 3).clip(CircleShape)
                            .background(if (i < hopTo) c(0xFFF8E6) else Color.White.copy(alpha = if (big) .85f else .5f)))
                    }
                }
            }
        }

        chip?.let { (kind, n) ->
            Row(Modifier.align(Alignment.TopCenter).padding(top = u * 130).shadow(6.dp, RoundedCornerShape(50)).clip(RoundedCornerShape(50))
                .background(Color.White).padding(horizontal = u * 18, vertical = u * 8), verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(u * 30)) {
                    if (kind == 's') star(size.width / 2, size.height / 2, size.width * .46f, c(0xFFC94D), c(0xE0A22B), 2f)
                    else apple(art, size.width / 2, size.height / 2 + 2, size.width / 26)
                }
                Text(" +$n", color = c(0x1E3A5F), fontWeight = FontWeight.Black, fontSize = (u.value * 26).sp)
            }
        }

        // --- bawah: papan kayu posisi + tombol JALAN ---
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(u * 134)
            .background(Brush.verticalGradient(listOf(Color.Transparent, c(0x0E2A12).copy(alpha = .16f))))) {
            Row(Modifier.align(Alignment.BottomStart).padding(start = u * 16, bottom = u * 30).height(u * 40)
                .shadow(4.dp, RoundedCornerShape(u * 16)).clip(RoundedCornerShape(u * 16))
                .background(Brush.verticalGradient(listOf(c(0xA9744F), c(0x8B5E3C)))).padding(horizontal = u * 13),
                verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(u * 20)) { at(size.width / 2, size.height / 2) { paw(size.width / 24, c(0xFFF8EC)) } }
                Spacer(Modifier.width(u * 6))
                Text(strings.adventureStop.format(hopTo), color = c(0xFFF8EC), fontWeight = FontWeight.Bold, fontSize = (u.value * 13).sp)
            }
            val canWalk = !busy && state.position < AdventureBoard.MAP_LENGTH
            Box(Modifier.align(Alignment.BottomCenter).size(u * 132).alpha(if (canWalk) 1f else .55f)
                .clickable(remember { MutableInteractionSource() }, null, enabled = canWalk) { walk() },
                contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val r = size.width / 2; val k = size.width / 132; val b = sin(time * PI).toFloat()
                    drawCircle(Brush.radialGradient(listOf(c(0xFFF3BC).copy(alpha = .95f), c(0xFFD24D).copy(alpha = 0f))), r * .94f)
                    drawCircle(Color.White.copy(alpha = .2f + kotlin.math.abs(b) * .22f), (60 + b * 2.5f) * k, style = Stroke(4.5f * k))
                    drawOval(Color.Black.copy(alpha = .22f), Offset(r - 34 * k, r + 38 * k), Size(68 * k, 16 * k))
                    drawRoundRect(c(0xFFF7E6), Offset(r - 42 * k, r - 42 * k), Size(84 * k, 84 * k), CornerRadius(18.5f * k))
                    drawRoundRect(c(0xE0A22B), Offset(r - 42 * k, r - 42 * k), Size(84 * k, 84 * k), CornerRadius(18.5f * k), style = Stroke(4.6f * k))
                    at(r, r - 12 * k) { paw(1.9f * k, c(0x8B5E3C)) }
                }
                Text(strings.adventureWalk, color = c(0x8B5E3C), fontWeight = FontWeight.Black, fontSize = (u.value * 15).sp,
                    modifier = Modifier.offset(y = u * 22))
            }
        }

        if (outfitShow > 0) OutfitReward(u, art, bearImg, outfitShow, time, strings.adventureNewOutfit) { outfitShow = 0 }
        if (state.mapComplete && !busy) MapComplete(u, art, bearImg, state, time,
            onReplay = { onStateChange(state.restartMap()) },
            onNext = { onStateChange(state.nextAdventure()) },
            onMenu = onBack)
    }
}

private fun DrawScope.paw(k: Float, col: Color) = at(0f, 0f, k) {
    drawOval(col, Offset(-6f, -2f), Size(12f, 10f))
    drawCircle(col, 2.6f, Offset(-5.6f, -3.6f)); drawCircle(col, 2.8f, Offset(0f, -5.4f)); drawCircle(col, 2.6f, Offset(5.6f, -3.6f))
}

@Composable
private fun HudPill(u: Dp, value: String, icon: DrawScope.() -> Unit) {
    Row(Modifier.height(u * 36).shadow(3.dp, RoundedCornerShape(u * 18)).clip(RoundedCornerShape(u * 18)).background(Color.White)
        .padding(horizontal = u * 11), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(u * 20), onDraw = icon)
        Spacer(Modifier.width(u * 5))
        Text(value, color = c(0x1E3A5F), fontWeight = FontWeight.Bold, fontSize = (u.value * 17).sp)
    }
}

private fun DrawScope.confetti(t: Float) {
    val cols = listOf(c(0xFFC94D), c(0xFF6B6B), c(0x7ED957), c(0x4FB0E8), c(0xA78BFA), Color.White)
    for (i in 0 until 70) {
        val x = prand(i * 1.7) * size.width; val y = (prand(i * 3.3) * size.height + t * size.height * .12f) % size.height
        at(x, y, rot = prand(i * 5.5) * 360 + t * 90) {
            drawRoundRect(cols[i % 6].copy(alpha = .55f + prand(i * 7.0) * .45f), Offset(-6f, -4f), Size(8 + prand(i.toDouble()) * 8, 5 + prand(i * 2.0) * 5), CornerRadius(2f))
        }
    }
}

/** Layar Hadiah: tiap 5 apel beruang dapat baju baru. Tap / 2,8 dtk → tutup. */
@Composable
private fun OutfitReward(u: Dp, art: Art, img: ImageBitmap, outfits: Int, time: Float, label: String, onDone: () -> Unit) {
    LaunchedEffect(outfits) { delay(2800); onDone() }
    Box(Modifier.fillMaxSize().background(c(0x0D2740).copy(alpha = .6f)).clickable(onClick = onDone), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            confetti(time)
            val k = size.width / 540
            drawCircle(c(0xFFD24D).copy(alpha = .2f), 170 * k, Offset(size.width / 2, size.height * .42f))
            bear(art, img, size.width / 2, size.height * .42f + 150 * k, 300 * k, outfits)
        }
        Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = (u.value * 30).sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = u * 220))
    }
}

@Composable
private fun StatRow(u: Dp, label: String, value: String) = Row(Modifier.fillMaxWidth().padding(vertical = u * 7)) {
    Text(label, color = c(0x1E3A5F), fontWeight = FontWeight.Bold, fontSize = (u.value * 16).sp, modifier = Modifier.weight(1f))
    Text(value, color = c(0x7B8FA3), fontWeight = FontWeight.Bold, fontSize = (u.value * 16).sp)
}

/** Kotak 24: perayaan peta tamat (board.html v=3). */
@Composable
private fun MapComplete(u: Dp, art: Art, img: ImageBitmap, state: AdventureState, time: Float, onReplay: () -> Unit, onNext: () -> Unit, onMenu: () -> Unit) {
    val strings = LocalStrings.current
    Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, null) {}) {
        Canvas(Modifier.fillMaxSize()) {
            val k = size.width / 540
            drawRect(c(0x0D2740).copy(alpha = .5f))
            at(0f, 0f, k) {
                for (ri in 0 until 18) {
                    val a = ri * 20 * PI / 180; val b = (ri * 20 + 9) * PI / 180
                    drawPath(Path().apply { moveTo(270f, 380f); lineTo(270 + sin(a).toFloat() * 300, 380 - cos(a).toFloat() * 300)
                        lineTo(270 + sin(b).toFloat() * 300, 380 - cos(b).toFloat() * 300); close() }, c(0xFFE9A8).copy(alpha = .18f))
                }
                drawCircle(c(0xFFD24D).copy(alpha = .16f), 196f, Offset(270f, 380f))
                drawRect(c(0x8B5E3C), Offset(146f, 442f), Size(248f, 40f))
                drawOval(c(0xC08B5C), Offset(146f, 416f), Size(248f, 44f))
                trophy(art, 270f, 372f, 2.65f, true)
                bear(art, img, 166f, 456f, 128f, state.outfits, rot = -6f)
                star(228f, 124f, 22f, c(0xFFC94D), Color.White, 3f); star(312f, 124f, 22f, c(0xFFC94D), Color.White, 3f)
                star(270f, 120f, 30f, c(0xFFF3DC), c(0xFFC94D), 4f)
                drawRoundRect(c(0x8B5E3C), Offset(98f, 178f), Size(344f, 68f), CornerRadius(22f))
                drawRoundRect(c(0xA9744F), Offset(106f, 185f), Size(328f, 54f), CornerRadius(18f))
            }
            confetti(time)
        }
        Text(strings.adventureMapDone.format(state.adventureNumber), color = c(0xFFF8EC), fontWeight = FontWeight.Black, fontSize = (u.value * 28).sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = u * 192))
        Column(Modifier.align(Alignment.TopCenter).padding(top = u * 500, start = u * 52, end = u * 52).fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(u * 26)).clip(RoundedCornerShape(u * 26)).background(Color.White)
            .padding(horizontal = u * 22, vertical = u * 20)) {
            StatRow(u, strings.adventureStarsTotal, "${state.stars}")
            StatRow(u, strings.adventureApplesTotal, "${state.fruits}")
            StatRow(u, strings.adventureTilesPassed, "24 / 24")
            Column(Modifier.padding(top = u * 14), verticalArrangement = Arrangement.spacedBy(u * 8)) {
                Box(Modifier.fillMaxWidth().height(u * 48).clip(RoundedCornerShape(u * 16))
                    .background(Brush.verticalGradient(listOf(c(0x7ED957), c(0x4FAE2E)))).clickable(onClick = onNext), contentAlignment = Alignment.Center) {
                    Text(strings.adventureNext.format(state.adventureNumber + 1), color = Color.White, fontWeight = FontWeight.Black, fontSize = (u.value * 16).sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(u * 10)) {
                    Box(Modifier.weight(1f).height(u * 42).clip(RoundedCornerShape(u * 14))
                        .background(c(0xFFF3DC)).clickable(onClick = onReplay), contentAlignment = Alignment.Center) {
                        Text(strings.playAgain, color = c(0x8B5E3C), fontWeight = FontWeight.Bold, fontSize = (u.value * 14).sp)
                    }
                    Box(Modifier.weight(1f).height(u * 42).clip(RoundedCornerShape(u * 14))
                        .border(u * 2, c(0xDBE6F0), RoundedCornerShape(u * 14)).clickable(onClick = onMenu), contentAlignment = Alignment.Center) {
                        Text(strings.adventureMenu, color = c(0x1E3A5F), fontWeight = FontWeight.Bold, fontSize = (u.value * 14).sp)
                    }
                }
            }
        }
    }
}
