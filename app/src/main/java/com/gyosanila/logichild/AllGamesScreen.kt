package com.gyosanila.logichild

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.LocalStrings

@Composable
fun AllGamesScreen(
    onSelectGame: (GameChoice) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val games = listOf(
        Triple(strings.playCar, "🚗", GameChoice.RoadmapKart),
        Triple(strings.playFruit, "🍎", GameChoice.RoadmapFruit),
        Triple(strings.playPattern, "🧩", GameChoice.RoadmapPattern),
        Triple(strings.playColor, "🎨", GameChoice.RoadmapColor),
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF8CA55))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Toolbar(emoji = "📚", title = strings.homeAllGames.format(4), onBack = onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                games.forEach { (name, emoji, choice) ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .clickable { onSelectGame(choice) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$emoji $name",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
