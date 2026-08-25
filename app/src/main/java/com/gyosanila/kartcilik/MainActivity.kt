package com.gyosanila.kartcilik

import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.gyosanila.kartcilik.ui.BerryPurple
import com.gyosanila.kartcilik.ui.GrassGreen
import com.gyosanila.kartcilik.ui.KartRed
import com.gyosanila.kartcilik.ui.LocalStrings
import com.gyosanila.kartcilik.ui.OceanBlue
import com.gyosanila.kartcilik.ui.SkyBlue
import com.gyosanila.kartcilik.ui.StringsEn
import com.gyosanila.kartcilik.ui.StringsId
import com.gyosanila.kartcilik.ui.SunYellow
import com.gyosanila.kartcilik.ui.TextDark
import kotlinx.coroutines.delay

enum class GameChoice { Menu, Kart, Fruit, Settings }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fullscreen: sembunyikan status bar & nav bar (immersive, swipe untuk muncul).
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        initAds(applicationContext)
        setContent {
            KartCilikTheme {
                val context = LocalContext.current
                val prefs = context.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
                var lang by remember { mutableStateOf(prefs.getString("lang", "id") ?: "id") }
                CompositionLocalProvider(
                    LocalStrings provides if (lang == "en") StringsEn else StringsId,
                ) {
                    var splash by remember { mutableStateOf(true) }
                    if (splash) {
                        SplashView()
                        LaunchedEffect(Unit) {
                            loadInterstitial(this@MainActivity)
                            delay(1500)
                            awaitAndShowInterstitial(this@MainActivity, 2500)
                            splash = false
                        }
                    } else {
                        MainNav(lang) { lang = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainNav(
    lang: String,
    onLanguageChange: (String) -> Unit,
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
    val strings = LocalStrings.current

    var game by rememberSaveable { mutableStateOf(GameChoice.Menu) }
    BackHandler(enabled = game != GameChoice.Menu) { game = GameChoice.Menu }

    // Timer layar (istirahat otomatis)
    var breakOverlay by remember { mutableStateOf(false) }
    var sessionStart by remember { mutableStateOf(System.currentTimeMillis()) }
    val timerMin = prefs.getInt("timer_minutes", 0)
    LaunchedEffect(timerMin, breakOverlay) {
        if (timerMin > 0 && !breakOverlay) {
            while (true) {
                delay(15_000)
                if (System.currentTimeMillis() - sessionStart >= timerMin * 60_000L) {
                    breakOverlay = true
                    break
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (game) {
                GameChoice.Menu -> MainMenuScreen(
                    onKart = { game = GameChoice.Kart },
                    onFruit = { game = GameChoice.Fruit },
                    onSettings = { game = GameChoice.Settings },
                )
                GameChoice.Kart -> KartGameScreen(onBack = { game = GameChoice.Menu })
                GameChoice.Fruit -> FruitGameScreen(onBack = { game = GameChoice.Menu })
                GameChoice.Settings -> SettingsScreen(
                    onBack = { game = GameChoice.Menu },
                    onLanguageChange = onLanguageChange,
                )
            }
        }
        // Satu banner permanen — AdView yang SAMA stay di menu & semua game.
        PersistentBanner()
    }

    if (breakOverlay) {
        BreakOverlay(
            strings = strings,
            onContinue = {
                breakOverlay = false
                sessionStart = System.currentTimeMillis()
            },
        )
    }
}

/** Banner tunggal yang dibuat sekali dan tetap hidup sepanjang app. */
@Composable
private fun PersistentBanner() {
    val context = LocalContext.current
    val adView = remember(context) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AD_UNIT_BANNER
            loadAd(childSafeAdRequest())
        }
    }
    AndroidView(
        factory = { adView },
        modifier = Modifier.fillMaxWidth(),
    )
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
    val strings = LocalStrings.current
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
                strings.appName,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                strings.splashSub,
                color = Color.White,
                fontSize = 15.sp,
            )
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
private fun BreakOverlay(strings: com.gyosanila.kartcilik.ui.AppStrings, onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF21B5E20)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
            ) {
                Text("💤", fontSize = 52.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    strings.breakTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    strings.breakMsg,
                    color = TextDark,
                    fontSize = 15.sp,
                )
                Spacer(Modifier.height(18.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF43A047),
                    onClick = onContinue,
                ) {
                    Text(
                        strings.breakContinue,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MainMenuScreen(
    onKart: () -> Unit,
    onFruit: () -> Unit,
    onSettings: () -> Unit,
) {
    val strings = LocalStrings.current
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
            strings.appName,
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            strings.menuPick,
            color = Color.White,
            fontSize = 16.sp,
        )
        Spacer(Modifier.height(28.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SunYellow,
            onClick = onKart,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text("🚗", fontSize = 48.sp)
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(
                        strings.playCar,
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        strings.playCarDesc,
                        color = TextDark,
                        fontSize = 14.sp,
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GrassGreen,
            onClick = onFruit,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text("🍎", fontSize = 48.sp)
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(
                        strings.playFruit,
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        strings.playFruitDesc,
                        color = TextDark,
                        fontSize = 14.sp,
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.25f),
            onClick = onSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 12.dp),
            ) {
                Text("⚙️", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    strings.settings,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            strings.offlineTag,
            color = Color.White,
            fontSize = 12.sp,
        )
    }
}
