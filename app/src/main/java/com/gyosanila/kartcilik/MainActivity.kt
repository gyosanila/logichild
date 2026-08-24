package com.gyosanila.kartcilik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.kartcilik.ui.BerryPurple
import com.gyosanila.kartcilik.ui.GrassGreen
import com.gyosanila.kartcilik.ui.KartRed
import com.gyosanila.kartcilik.ui.OceanBlue
import com.gyosanila.kartcilik.ui.SkyBlue
import com.gyosanila.kartcilik.ui.SunYellow
import com.gyosanila.kartcilik.ui.TextDark
import kotlinx.coroutines.delay

enum class GameChoice { Menu, Kart, Fruit }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAds(applicationContext)
        setContent {
            KartCilikTheme {
                var splash by remember { mutableStateOf(true) }
                if (splash) {
                    SplashView()
                    LaunchedEffect(Unit) {
                        loadInterstitial(this@MainActivity)
                        delay(2200)
                        showInterstitialIfReady(this@MainActivity)
                        splash = false
                    }
                } else {
                    MainNav()
                }
            }
        }
    }
}

@Composable
private fun MainNav() {
    var game by rememberSaveable { mutableStateOf(GameChoice.Menu) }
    BackHandler(enabled = game != GameChoice.Menu) { game = GameChoice.Menu }
    when (game) {
        GameChoice.Menu -> MainMenuScreen(
            onKart = { game = GameChoice.Kart },
            onFruit = { game = GameChoice.Fruit },
        )
        GameChoice.Kart -> KartGameScreen(onBack = { game = GameChoice.Menu })
        GameChoice.Fruit -> FruitGameScreen(onBack = { game = GameChoice.Menu })
    }
}

@Composable
fun KartCilikTheme(content: @Composable () -> Unit) {
    // Selalu terang — aplikasi balita, dark mode gak relevan
    val colors = lightColorScheme(
        primary = KartRed,
        secondary = OceanBlue,
        tertiary = BerryPurple,
        background = SkyBlue,
        surface = GrassGreen,
        onPrimary = Color.White,
        onBackground = TextDark,
        onSurface = TextDark,
    )
    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}

@Composable
private fun SplashView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎮", fontSize = 72.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Logichild",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Belajar logika sambil main!",
                color = Color.White,
                fontSize = 15.sp,
            )
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
private fun MainMenuScreen(
    onKart: () -> Unit,
    onFruit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎮", fontSize = 56.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Logichild",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Pilih game-nya!",
            color = Color.White,
            fontSize = 16.sp,
        )
        Spacer(Modifier.height(32.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SunYellow,
            onClick = onKart,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text("🚗", fontSize = 52.sp)
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(
                        "Main Mobil",
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        "Susun langkah, mobil sampai finish!",
                        color = TextDark,
                        fontSize = 14.sp,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GrassGreen,
            onClick = onFruit,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text("🍎", fontSize = 52.sp)
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(
                        "Petik Buah",
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        "Susun perintah, robot panen buah!",
                        color = TextDark,
                        fontSize = 14.sp,
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Tanpa internet • Untuk balita 3+",
            color = Color.White,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(12.dp))
        AdBanner()
    }
}
