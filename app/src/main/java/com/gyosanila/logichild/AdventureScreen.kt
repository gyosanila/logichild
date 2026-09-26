package com.gyosanila.logichild

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.LocalStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun AdventureScreen(
    state: AdventureState,
    onStateChange: (AdventureState) -> Unit,
    onMiniGame: (game: String) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val scroller = rememberScrollState()
    var rolling by remember { mutableStateOf(false) }
    var dice by remember { mutableIntStateOf(1) }
    val stepProgress = Animatable(0f)
    val scope = rememberCoroutineScope()
    var announcedEffect by remember { mutableStateOf<AdventureTileEffect?>(null) }
    val board = remember { AdventureBoard() }
    val tileWidth = 116.dp
    val count = AdventureBoard.MAP_LENGTH

    LaunchedEffect(state.position) {
        val target = ((state.position - 1) * 116 - 110).coerceAtLeast(0)
        delay(80)
        scroller.animateScrollTo(target)
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF9ADBF5)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(onClick = onBack, shape = CircleShape, color = Color.White, shadowElevation = 3.dp) {
                Text("‹", fontSize = 30.sp, color = Color(0xFF1E3A5F), modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(strings.adventureTitle, color = Color(0xFF1E3A5F), fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text(strings.adventurePosition.format(state.position), color = Color(0xFF55728A), fontSize = 12.sp)
            }
            StatPill("⭐", state.stars.toString())
            Spacer(Modifier.width(6.dp))
            StatPill("🍎", state.fruits.toString())
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(4) { zone ->
                val active = (state.position - 1) / 6 == zone
                Box(Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (active) listOf(Color(0xFF67C94C), Color(0xFFFFB347), Color(0xFFE28A3A), Color(0xFF84C9F4))[zone] else Color.White.copy(alpha=.45f)),
                    contentAlignment = Alignment.Center) {
                    Text(listOf("🌷", "☀️", "🍂", "❄️")[zone], fontSize = 16.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val density = LocalDensity.current
            val viewportHeight = maxHeight
            val worldWidth = tileWidth * count + 20.dp
            Box(Modifier.fillMaxSize().horizontalScroll(scroller)) {
                Box(Modifier.width(worldWidth).fillMaxSize()) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawRect(Brush.verticalGradient(listOf(Color(0xFFB8E9FF), Color(0xFF83D35B), Color(0xFF5EAF3D))))
                        for (i in 0..40) {
                            val x = i * 97f
                            val y = size.height * .84f + (i % 3) * 10f
                            drawCircle(Color(0xFF4E9C38), 12f, Offset(x, y))
                        }
                        val points = (0 until count).map { i ->
                            val x = with(density) { (58.dp + tileWidth * i).toPx() }
                            val y = size.height * (.46f + .23f * sin(i * Math.PI / 2.8).toFloat())
                            Offset(x, y)
                        }
                        val road = Path()
                        points.forEachIndexed { i, p ->
                            if (i == 0) road.moveTo(p.x, p.y) else {
                                val prev = points[i - 1]; val dx = p.x - prev.x
                                road.cubicTo(prev.x + dx * .42f, prev.y, p.x - dx * .42f, p.y, p.x, p.y)
                            }
                        }
                        drawPath(road, Color(0xFFB5672E), style = Stroke(68.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(road, Color(0xFFF3B65D), style = Stroke(55.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(road, Color(0xFFFFE4A0), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        repeat(count) { idx ->
                            val stop = idx + 1
                            val done = stop < state.position
                            val current = stop == state.position
                            val wave = sin(idx * Math.PI / 2.8).toFloat()
                            val yOffset = viewportHeight * (.23f * wave - .04f)
                            Column(
                                Modifier.width(tileWidth).padding(horizontal = 8.dp).offset(y = yOffset),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                val emoji = when (stop) {
                                    1 -> "🚩"; 3, 9, 15, 19, 23 -> "⭐"; 4, 8, 12, 17, 21 -> "🎮"
                                    6, 18 -> "🌈"; 7, 14, 20 -> "🍎"; 11, 22 -> "🎁"; 24 -> "🏁"
                                    else -> "🌼"
                                }
                                Box(Modifier.size(if (current) 82.dp else 68.dp).shadow(6.dp, CircleShape)
                                    .clip(CircleShape).background(if (current) Color(0xFFFFD24D) else if (done) Color(0xFF65C54D) else Color.White),
                                    contentAlignment = Alignment.Center) {
                                    if (current) {
                                        Image(
                                            painter = painterResource(R.drawable.adventure_bear),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(78.dp),
                                        )
                                    } else Text(emoji, fontSize = 25.sp)
                                }
                                Text("$stop", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    color = Color(0xFF204060), modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        if (announcedEffect != null) {
            val label = when (announcedEffect) {
                AdventureTileEffect.Star -> "⭐ +1"
                AdventureTileEffect.Collect -> "🍎 +1"
                AdventureTileEffect.Gift -> "🎁"
                AdventureTileEffect.MiniGame -> strings.adventureChooseGame
                AdventureTileEffect.Finish -> strings.adventureFinish
                AdventureTileEffect.Bonus -> "🌈 +2"
                else -> "✨"
            }
            Text(label, color = Color(0xFF1E3A5F), fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(4.dp))
        } else Spacer(Modifier.height(32.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            color = Color(0xFFF8F6E9),
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 10.dp,
        ) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.adventurePosition.format(state.position), color = Color(0xFF31532A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("🎲", fontSize = 23.sp)
                    Text(" ${if (rolling) "..." else dice}", color = Color(0xFF1E3A5F), fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(8.dp))
                Surface(
                    onClick = {
                        if (!rolling && state.position < AdventureBoard.MAP_LENGTH) {
                            rolling = true
                            announcedEffect = null
                            val steps = board.roll()
                            dice = steps
                            scope.launch {
                                try {
                                    repeat(steps) {
                                        delay(420)
                                        val moved = board.move(state.position + it, 1)
                                        val interim = state.copy(position = moved.position)
                                        onStateChange(interim)
                                        stepProgress.animateTo(1f, tween(100))
                                        stepProgress.snapTo(0f)
                                    }
                                    val landing = board.moveAndResolve(state.position, steps)
                                    var updated = state.copy(position = landing.position)
                                    when (landing.effect) {
                                        AdventureTileEffect.Star -> updated = updated.copy(stars = updated.stars + 1)
                                        AdventureTileEffect.Collect -> updated = updated.copy(fruits = updated.fruits + 1)
                                        AdventureTileEffect.Gift -> updated = updated.copy(stars = updated.stars + 1)
                                        AdventureTileEffect.Finish -> updated = updated.copy(mapComplete = true)
                                        else -> Unit
                                    }
                                    announcedEffect = landing.effect
                                    onStateChange(updated)
                                    if (landing.effect == AdventureTileEffect.MiniGame) {
                                        delay(700)
                                        onMiniGame(listOf("color", "pattern", "fruit", "kart").random())
                                    }
                                } finally {
                                    rolling = false
                                }
                            }
                        }
                    },
                    enabled = !rolling && state.position < AdventureBoard.MAP_LENGTH,
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF58B83B),
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (state.position >= AdventureBoard.MAP_LENGTH) strings.adventureFinish else strings.adventureRoll,
                            color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(icon: String, value: String) {
    Surface(shape = RoundedCornerShape(15.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 15.sp); Text(" $value", fontWeight = FontWeight.Bold, color = Color(0xFF1E3A5F), fontSize = 13.sp)
        }
    }
}
