package com.gyosanila.logichild

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.GrassGreen
import com.gyosanila.logichild.ui.LocalStrings
import com.gyosanila.logichild.ui.SkyBlue
import com.gyosanila.logichild.ui.SunYellow
import com.gyosanila.logichild.ui.TextDark

/**
 * Roadmap level ala Duolingo: jalur zigzag, node selesai (hijau + bintang),
 * node aktif (kuning besar), node terkunci (abu).
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.25f),
                onClick = onBack,
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("←", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
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
        Spacer(Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(unlockedCount.coerceAtLeast(1)) { i ->
                RoadmapNode(
                    level = i + 1,
                    starCount = stars[i + 1],
                    isCurrent = (i + 1) == unlockedCount,
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Composable
private fun RoadmapNode(
    level: Int,
    starCount: Int?,   // null = belum dikerjakan
    isCurrent: Boolean,
    onSelect: (Int) -> Unit,
) {
    val done = starCount != null
    val leftSide = level % 2 == 1
    val nodeSize = if (isCurrent) 68.dp else 58.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp),
    ) {
        // Garis penghubung (zigzag vertikal).
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width * (if (leftSide) 0.24f else 0.76f)
            val cy = size.height / 2f
            val lineColor = if (done) GrassGreen else Color.White.copy(alpha = 0.45f)
            // dari atas ke tengah node
            drawLine(
                color = lineColor,
                start = Offset(cx, 0f),
                end = Offset(cx, cy),
                strokeWidth = 12f,
                cap = StrokeCap.Round,
            )
            // dari tengah node ke bawah (lanjutan jalur)
            drawLine(
                color = lineColor,
                start = Offset(cx, cy),
                end = Offset(cx, size.height),
                strokeWidth = 12f,
                cap = StrokeCap.Round,
            )
        }

        val nodeColor = when {
            done -> GrassGreen
            isCurrent -> SunYellow
            else -> Color.White.copy(alpha = 0.55f)
        }
        Surface(
            shape = CircleShape,
            color = nodeColor,
            onClick = { onSelect(level) },
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(if (leftSide) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 26.dp)
                .size(nodeSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    done -> Text(
                        "$starCount★",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                    )
                    else -> Text(
                        "$level",
                        color = if (isCurrent) TextDark else Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }

        // Label level kecil di sisi node.
        Text(
            "Level $level",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(
                    if (leftSide) Alignment.CenterEnd else Alignment.CenterStart
                )
                .padding(horizontal = 18.dp),
        )
    }
}
