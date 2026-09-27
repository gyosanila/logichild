package com.gyosanila.logichild

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.LocalStrings

/** Progres satu mini game untuk tile Home. */
data class HomeGame(val title: String, val level: Int, val stars: Int, val onClick: () -> Unit)

private val NAVY = c(0x1E3A5F)
private val GREY = c(0x7B8FA3)
private val GOLD = c(0xFFC94D)

/**
 * Home sesuai docs/design/DESIGN-home-hub.md (varian 1, koordinat 540×960, pil Pengaturan dihapus).
 * Pratinjau = drawWorld() yang sama dengan Papan, jadi tidak bisa beda dari papan asli.
 */
@Composable
fun HomeScreen(
    adventure: AdventureState,
    games: List<HomeGame>, // urutan: warna, pola, mobil, buah
    onContinue: () -> Unit,
    onSettings: () -> Unit,
) {
    val strings = LocalStrings.current
    val art = remember { Art() }
    val bearImg = ImageBitmap.imageResource(R.drawable.adventure_bear)
    val (numbers, startLabel) = rememberWorldText()
    val time by rememberInfiniteTransition(label = "home").animateFloat(
        0f, 40f, infiniteRepeatable(tween(40_000, easing = LinearEasing), RepeatMode.Restart), label = "t",
    )
    val season = ((adventure.position - 1) / 6).coerceIn(0, 3)

    BoxWithConstraints(Modifier.fillMaxSize().background(c(0x8FD4F7))) {
        // ponytail: skala lebar-saja (u = lebar/540) seperti Papan; layar sangat pendek bisa terpotong bawah — tambah scroll kalau ada laporan.
        val u: Dp = maxWidth / 540
        Canvas(Modifier.fillMaxSize()) { homeBackdrop(art, u.toPx(), time) }

        // header: logo + nama + gear bergembok
        Row(Modifier.offset(u * 16, u * 12).width(u * 508).height(u * 52), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.ic_app_logo), null, Modifier.size(u * 46).shadow(4.dp, RoundedCornerShape(u * 15)).clip(RoundedCornerShape(u * 15)))
            Spacer(Modifier.width(u * 9))
            Column(Modifier.height(u * 46).shadow(4.dp, RoundedCornerShape(u * 15)).clip(RoundedCornerShape(u * 15))
                .background(Brush.verticalGradient(listOf(c(0xA9744F), c(0x8B5E3C)))).padding(horizontal = u * 15),
                verticalArrangement = Arrangement.Center) {
                Text(strings.appName, color = c(0xFFF8EC), fontWeight = FontWeight.Bold, fontSize = (u.value * 17).sp, lineHeight = (u.value * 19).sp)
                Text("v${BuildConfig.VERSION_NAME}", color = c(0xF6DFC6), fontSize = (u.value * 11).sp, lineHeight = (u.value * 13).sp)
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(u * 46).shadow(4.dp, CircleShape).clip(CircleShape).background(Color.White).clickable(onClick = onSettings),
                contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(u * 26)) { gear(size.width / 26f) }
            }
        }

        // panggung peta
        Box(Modifier.offset(u * 14, u * 72).width(u * 512).height(u * 348).shadow(12.dp, RoundedCornerShape(u * 30))
            .clip(RoundedCornerShape(u * 30)).background(Brush.verticalGradient(0f to c(0xB07C52), .7f to c(0x8B5E3C), 1f to c(0x7A4F31)))
            .clickable(onClick = onContinue)) {
            Row(Modifier.offset(u * 24, u * 9).width(u * 464).height(u * 48), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.homeContinue, color = c(0xFFF8EC), fontWeight = FontWeight.Bold, fontSize = (u.value * 18.5f).sp, maxLines = 1)
                    Text(strings.homeContinueSub.format(adventure.adventureNumber, adventure.position), color = c(0xF3DFC6),
                        fontSize = (u.value * 11.5f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                StarChip(u, adventure.stars, c(0xFFF4D6))
            }
            // jendela pratinjau: papan asli, zoom di sekitar beruang (varian 1)
            Canvas(Modifier.offset(u * 10, u * 62).width(u * 492).height(u * 230).clip(RoundedCornerShape(u * 20))
                .border(u * 5, c(0xF0DCBB), RoundedCornerShape(u * 20)).clipToBounds()) {
                // world y 340..760 (kotak + beruang) mengisi jendela; x fokus sekitar beruang
                val s = size.height / 420f
                val visW = size.width / s
                val bx = ax(adventure.position - 1)
                val cam = (bx - visW * .45f).coerceIn(0f, WORLD_W - visW)
                withTransform({ translate(0f, -340f * s); scale(s, s, Offset.Zero) }) {
                    drawWorld(art, bearImg, numbers, startLabel, adventure, time, visW, cam, adventure.position, adventure.position, 1f)
                }
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = .22f)), startY = size.height - 70.dp.toPx()),
                    Offset(0f, size.height - 70.dp.toPx()))
            }
            // batang musim
            Row(Modifier.offset(u * 18, u * 302).width(u * 476), horizontalArrangement = Arrangement.spacedBy(u * 9)) {
                repeat(4) { i ->
                    Box(Modifier.weight(1f).height(u * 9).clip(RoundedCornerShape(u * 6)).background(
                        when { i < season -> c(0x7ED957); i == season -> GOLD; else -> Color.White.copy(alpha = .3f) }))
                }
            }
            Row(Modifier.offset(u * 18, u * 318).width(u * 476), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(4) { i ->
                    val on = i == season
                    Box(Modifier.size(u * 27).then(if (on) Modifier.border(u * 3, GOLD, CircleShape) else Modifier).clip(CircleShape)
                        .background(if (on) Color.White else Color.White.copy(alpha = .22f)), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.size(u * 20)) { seasonIcon(art, i, size.width / 30f) }
                    }
                }
            }
        }

        // tombol utama melayang di tepi bawah panggung
        Row(Modifier.offset(u * 74, u * 400).width(u * 392).height(u * 60).shadow(10.dp, RoundedCornerShape(u * 22))
            .clip(RoundedCornerShape(u * 22)).background(Brush.verticalGradient(listOf(c(0x8AE765), c(0x4FAE2E))))
            .clickable(onClick = onContinue), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(u * 22)) {
                val k = size.width / 22f
                drawPath(Path().apply { moveTo(5 * k, 3 * k); lineTo(19 * k, 11 * k); lineTo(5 * k, 19 * k); close() }, Color.White)
            }
            Spacer(Modifier.width(u * 11))
            Text(strings.homePlay, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (u.value * 20).sp)
        }

        // judul seksi
        Row(Modifier.offset(u * 16, u * 476).width(u * 508), verticalAlignment = Alignment.CenterVertically) {
            Text(strings.homeMiniGame, color = NAVY, fontWeight = FontWeight.Bold, fontSize = (u.value * 11.5f).sp, letterSpacing = (u.value * 1.5f).sp,
                modifier = Modifier.shadow(2.dp, RoundedCornerShape(u * 13)).clip(RoundedCornerShape(u * 13)).background(Color.White)
                    .padding(horizontal = u * 14, vertical = u * 8))
            Spacer(Modifier.weight(1f))
            StarChip(u, games.sumOf { it.stars }, c(0xFFF4D6))
        }

        // rak mainan: tinggi & kemiringan beda
        val tops = listOf(518, 532, 686, 670)
        val rots = listOf(-2.2f, 2f, 2.4f, -1.8f)
        val cols = listOf(c(0xB49BFB) to c(0x8B6BEA), c(0xFBB4CE) to c(0xEE8FB4), c(0x5FBDF0) to c(0x3A97D6), c(0x8BDF63) to c(0x5CB93A))
        games.take(4).forEachIndexed { i, g ->
            Column(Modifier.offset(u * (16 + (i % 2) * 262), u * tops[i]).width(u * 246).height(u * (if (i == 0 || i == 3) 160 else 142))
                .rotate(rots[i]).shadow(8.dp, RoundedCornerShape(u * 30)).clip(RoundedCornerShape(u * 30)).background(Color.White)
                .clickable(onClick = g.onClick)) {
                Box(Modifier.fillMaxWidth().height(u * 88).background(Brush.verticalGradient(listOf(cols[i].first, cols[i].second))),
                    contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawOval(Color.White.copy(alpha = .2f), Offset(-30 * u.toPx(), -40 * u.toPx()), Size(200 * u.toPx(), 110 * u.toPx()))
                        gameIcon(art, i, size.width / 2, size.height / 2, u.toPx())
                    }
                    Row(Modifier.align(Alignment.TopEnd).padding(u * 11).clip(RoundedCornerShape(u * 13)).background(Color.White.copy(alpha = .95f))
                        .padding(horizontal = u * 8, vertical = u * 3), verticalAlignment = Alignment.CenterVertically) {
                        Canvas(Modifier.size(u * 16)) { star(size.width / 2, size.height / 2, size.width * .46f, GOLD, c(0xE0A22B), 1.4f) }
                        Text(" ${g.stars}", color = c(0x8A6D1F), fontWeight = FontWeight.Bold, fontSize = (u.value * 12.5f).sp)
                    }
                }
                Column(Modifier.padding(horizontal = u * 14, vertical = u * 9)) {
                    Text(g.title, color = NAVY, fontWeight = FontWeight.Bold, fontSize = (u.value * 15.5f).sp, maxLines = 1)
                    Text(strings.homeGameSub.format(g.level, g.stars), color = GREY, fontSize = (u.value * 11.5f).sp, maxLines = 1)
                }
            }
        }

        // baris bawah
        Row(Modifier.offset(u * 16, u * 844).width(u * 508), horizontalArrangement = Arrangement.spacedBy(u * 16)) {
            MiniCard(u, strings.adventureMapName.format(adventure.adventureNumber), strings.homeBoardSub, onContinue) { mapIcon(size.width / 34f) }
            MiniCard(u, strings.homeGift, strings.homeGiftSub.format(adventure.fruits, adventure.outfits), onContinue) {
                apple(art, size.width / 2, size.height / 2 + size.width * .06f, size.width / 22f)
            }
        }
    }
}

@Composable
private fun StarChip(u: Dp, n: Int, bg: Color) = Row(Modifier.clip(RoundedCornerShape(u * 14)).background(bg)
    .padding(start = u * 9, end = u * 11, top = u * 5, bottom = u * 5), verticalAlignment = Alignment.CenterVertically) {
    Canvas(Modifier.size(u * 19)) { star(size.width / 2, size.height / 2, size.width * .46f, GOLD, c(0xE0A22B), 1.4f) }
    Spacer(Modifier.width(u * 6))
    Text("$n", color = c(0x8A6D1F), fontWeight = FontWeight.Bold, fontSize = (u.value * 15).sp)
}

@Composable
private fun RowScope.MiniCard(u: Dp, title: String, sub: String, onClick: () -> Unit, icon: DrawScope.() -> Unit) =
    Row(Modifier.weight(1f).height(u * 74).shadow(8.dp, RoundedCornerShape(u * 24)).clip(RoundedCornerShape(u * 24)).background(Color.White)
        .clickable(onClick = onClick).padding(horizontal = u * 15), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(u * 32), onDraw = icon)
        Spacer(Modifier.width(u * 12))
        Column(Modifier.weight(1f)) {
            Text(title, color = NAVY, fontWeight = FontWeight.Bold, fontSize = (u.value * 12.5f).sp, maxLines = 1)
            Text(sub, color = GREY, fontSize = (u.value * 11.5f).sp, maxLines = 1)
        }
        Canvas(Modifier.size(u * 20)) {
            val k = size.width / 22f
            drawPath(Path().apply { moveTo(8 * k, 4 * k); lineTo(15 * k, 11 * k); lineTo(8 * k, 18 * k) }, c(0xC3D2DF),
                style = Stroke(3.2f * k, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

/** Langit + matahari + awan + pohon sakura + rumput + pagar semak (home.html #bg/#field/#amb). */
private fun DrawScope.homeBackdrop(art: Art, k: Float, t: Float) = withTransform({ scale(k, k, Offset.Zero) }) {
    drawRect(Brush.verticalGradient(0f to c(0x77BEEB), .6f to c(0xBFE9FF), 1f to c(0xE7F8FF), endY = 400f), size = Size(540f, 400f))
    drawCircle(Brush.radialGradient(listOf(c(0xFFF6C2).copy(alpha = .95f), c(0xFFE066).copy(alpha = 0f)), Offset(118f, 300f), 96f), 96f, Offset(118f, 300f))
    drawCircle(c(0xFFEB9B), 32f, Offset(118f, 300f))
    val drift = kotlin.math.sin(t / 4f * 2 * kotlin.math.PI).toFloat() * 6f
    cloud(104f + drift, 214f, .86f, 1f, true); cloud(330f - drift, 60f, .64f, 1f, false); cloud(468f + drift, 168f, .54f, 1f, false)
    for ((x, y, s) in listOf(Triple(46f, 392f, 1f), Triple(506f, 404f, .86f))) at(x, y, s) {
        drawRoundRect(c(0x7A5334), Offset(-8f, -64f), Size(14f, 64f), CornerRadius(5f))
        for ((cx, cy, r, col) in listOf(listOf(-30f, -72f, 26f, 0xF7A8C4), listOf(6f, -92f, 30f, 0xFBB8D0), listOf(34f, -70f, 25f, 0xF59BBC), listOf(4f, -70f, 30f, 0xFFC9DD)))
            drawCircle(c((col as Number).toLong()), r as Float, Offset(cx as Float, cy as Float))
    }
    drawRect(Brush.verticalGradient(0f to c(0xA6DE78), .22f to c(0x82CD58), .78f to c(0x5FAE3E), 1f to c(0x4E9C38), startY = 356f, endY = 960f),
        Offset(0f, 356f), Size(540f, 2000f))
    for (i in 0 until 7) bush(40f + i * 78, 946f + (i % 2) * 6, .95f, 0f)
    bush(4f, 760f, 1.1f, 0f); bush(534f, 742f, 1f, 0f); bush(28f, 632f, .8f, 0f); bush(516f, 646f, .85f, 0f)
    for ((x, y, col) in listOf(Triple(6f, 404f, 0xFF6B6BL), Triple(534f, 428f, 0xA78BFAL), Triple(6f, 806f, 0x4FB0E8L),
        Triple(534f, 820f, 0xFFC94DL), Triple(150f, 930f, 0xFF6B6BL), Triple(390f, 930f, 0xA78BFAL))) flower(x, y, .8f, c(col))
}

private fun DrawScope.gear(k: Float) = withTransform({ translate(13 * k, 13 * k); scale(k, k, Offset.Zero) }) {
    for (a in 0 until 6) withTransform({ rotate(a * 60f, Offset.Zero) }) { drawRoundRect(NAVY, Offset(-2.6f, -12.4f), Size(5.2f, 5.4f), CornerRadius(1.8f)) }
    drawCircle(NAVY, 8.6f, Offset.Zero); drawCircle(Color.White, 3.6f, Offset.Zero)
    // gembok orang tua
    drawCircle(GOLD, 6.5f, Offset(9f, 9f)); drawCircle(Color.White, 6.5f, Offset(9f, 9f), style = Stroke(1.6f))
    drawRoundRect(c(0x8B5E3C), Offset(6f, 8.5f), Size(6f, 4.4f), CornerRadius(1.2f))
    drawArc(c(0x8B5E3C), 180f, 180f, false, Offset(7.1f, 5.6f), Size(3.8f, 5.6f), style = Stroke(1.3f))
}

private fun DrawScope.seasonIcon(art: Art, i: Int, k: Float) = withTransform({ translate(15 * k, 15 * k); scale(k, k, Offset.Zero) }) {
    when (i) {
        0 -> { drawLine(c(0xCFE8B0), Offset(0f, 12f), Offset.Zero, 2.6f, StrokeCap.Round)
            drawPath(Path().apply { moveTo(-8f, -10f); quadraticTo(-7f, 1f, 0f, 1f); quadraticTo(7f, 1f, 8f, -10f); quadraticTo(4f, -5f, 0f, -13f); quadraticTo(-4f, -5f, -8f, -10f); close() }, c(0xFF6B6B)) }
        1 -> { drawCircle(c(0xFFB347), 6.4f, Offset.Zero)
            for (a in 0 until 8) withTransform({ rotate(a * 45f, Offset.Zero) }) { drawRoundRect(c(0xFFB347), Offset(-1.5f, -13.4f), Size(3f, 4.4f), CornerRadius(1.5f)) } }
        2 -> at(0f, 0f, 1.6f) { drawPath(art.leaf, c(0xE08A2E)) }
        else -> for (a in listOf(0f, 60f, 120f)) withTransform({ rotate(a, Offset.Zero) }) { drawLine(c(0x8FCBF0), Offset(0f, -11f), Offset(0f, 11f), 3f, StrokeCap.Round) }
    }
}

private fun DrawScope.mapIcon(k: Float) = withTransform({ translate(17 * k, 17 * k); scale(k, k, Offset.Zero) }) {
    val road = Path().apply { moveTo(-13f, -4f); quadraticTo(-2f, 10f, 13f, -6f) }
    drawPath(road, c(0xF2D7A3), style = Stroke(6f, cap = StrokeCap.Round))
    for ((p, col) in listOf(Offset(-12f, -5f) to 0x7ED957L, Offset(1f, 4f) to 0xFFD24DL, Offset(13f, -6f) to 0xFFF3DCL)) {
        drawCircle(c(col), 5.2f, p); drawCircle(Color.White, 5.2f, p, style = Stroke(2f))
    }
    drawLine(c(0x8B5E3C), Offset(13f, -15f), Offset(13f, 5f), 2.4f)
    drawPath(Path().apply { moveTo(13f, -14f); lineTo(23f, -10.4f); lineTo(13f, -6.8f); close() }, c(0xE04747))
}

/** Ikon mini game (home.html iconColor/iconPattern/iconCar/iconFruit), pusat (cx,cy), satuan k=u px. */
private fun DrawScope.gameIcon(art: Art, i: Int, cx: Float, cy: Float, k: Float) = withTransform({ translate(cx, cy); scale(k, k, Offset.Zero) }) {
    when (i) {
        0 -> for ((p, col) in listOf(Offset(-12f, -8f) to 0xFF6B6BL, Offset(12f, -8f) to 0x4FB0E8L, Offset(-12f, 14f) to 0xFFC94DL, Offset(12f, 14f) to 0xF7A8C4L))
            drawCircle(c(col), 13.5f, p)
        1 -> { drawRoundRect(Color.White.copy(alpha = .35f), Offset(-29f, -29f), Size(58f, 58f), CornerRadius(14f))
            for ((x, y, col) in listOf(Triple(-27f, -27f, 0xF7A8C4L), Triple(0f, -27f, 0xA78BFAL), Triple(-27f, 0f, 0xA78BFAL)))
                drawRoundRect(c(col), Offset(x, y), Size(24f, 24f), CornerRadius(8f))
            drawRoundRect(Color.White, Offset(0f, 0f), Size(24f, 24f), CornerRadius(8f), style = Stroke(3f))
            drawCircle(Color.White, 3f, Offset(12f, 12f)) }
        2 -> { drawRoundRect(c(0xE04747), Offset(-28f, -8f), Size(56f, 21f), CornerRadius(9f))
            drawPath(Path().apply { moveTo(-17f, -8f); quadraticTo(-12f, -23f, -1f, -23f); quadraticTo(12f, -23f, 16f, -8f); close() }, c(0xFF6B6B))
            drawRoundRect(c(0xCFE6F5), Offset(-11f, -20f), Size(23f, 13f), CornerRadius(6f))
            for (x in listOf(-16f, 16f)) { drawCircle(c(0x503018), 8f, Offset(x, 15f)); drawCircle(Color.White.copy(alpha = .7f), 2.8f, Offset(x, 15f)) }
            drawRoundRect(c(0xFFE066), Offset(-24f, -3f), Size(9f, 7f), CornerRadius(3f)) }
        else -> apple(art, 0f, 0f, 2.6f)
    }
}
