package com.gyosanila.kartcilik

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.gyosanila.kartcilik.ui.AppStrings
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
import kotlin.random.Random

enum class GameChoice { Menu, Kart, Fruit, Settings }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppActivityHolder.current = this
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
    val tts = remember { TtsSpeaker(context) }

    var game by rememberSaveable { mutableStateOf(GameChoice.Menu) }
    var mathGate by remember { mutableStateOf(false) }

    // Timer layar: countdown + progress bar + auto istirahat + kunci layar
    var breakOverlay by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(prefs.getBoolean("screen_locked", false)) }

    // Back diblokir selama lock/istirahat — harus lewat "Main Lagi" + soal.
    BackHandler(enabled = game != GameChoice.Menu || locked || breakOverlay) {
        if (!locked && !breakOverlay) game = GameChoice.Menu
    }

    var sessionStart by remember { mutableStateOf(System.currentTimeMillis()) }
    val timerMin = prefs.getInt("timer_minutes", 0)
    var remainingSec by remember { mutableStateOf(timerMin * 60) }

    LaunchedEffect(timerMin, breakOverlay, locked) {
        if (timerMin > 0 && !breakOverlay && !locked) {
            while (true) {
                val elapsed = ((System.currentTimeMillis() - sessionStart) / 1000L).toInt()
                val rem = (timerMin * 60 - elapsed).coerceAtLeast(0)
                remainingSec = rem
                if (rem <= 0) {
                    breakOverlay = true
                    prefs.edit().putBoolean("screen_locked", true).apply()
                    tts.speak("Waktu bermain sudah habis, teman. Waktunya istirahat.")
                    break
                }
                delay(1000)
            }
        }
    }
    // Beberapa detik setelah peringatan → "kunci" layar app (parent gate)
    LaunchedEffect(breakOverlay) {
        if (breakOverlay) {
            delay(6000)
            locked = true
        }
    }

    // Alur buka kunci: tombol Main Lagi → iklan → soal kabataku → bebas
    var pendingMath by remember { mutableStateOf(false) }
    fun requestUnlock() {
        AppActivityHolder.current?.let { act -> showInterstitialIfReady(act) }
        pendingMath = true
    }
    fun completeUnlock() {
        prefs.edit().putBoolean("screen_locked", false).apply()
        pendingMath = false
        locked = false
        breakOverlay = false
        sessionStart = System.currentTimeMillis()
        remainingSec = timerMin * 60
    }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        if (timerMin > 0 && !breakOverlay && !locked) {
            TimerBar(remainingSec, timerMin * 60)
        }
        Box(Modifier.weight(1f)) {
            when (game) {
                GameChoice.Menu -> MainMenuScreen(
                    onKart = { game = GameChoice.Kart },
                    onFruit = { game = GameChoice.Fruit },
                    onSettings = { mathGate = true },
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

    if (mathGate) {
        MathGateDialog(
            strings = strings,
            onSuccess = {
                mathGate = false
                game = GameChoice.Settings
            },
            onDismiss = { mathGate = false },
            dismissable = true,
        )
    }
    if (pendingMath) {
        MathGateDialog(
            strings = strings,
            onSuccess = { completeUnlock() },
            onDismiss = {},
            dismissable = false,
        )
    }
    if (breakOverlay && !locked) {
        BreakOverlay(
            strings = strings,
            onKeepPlaying = { requestUnlock() },
        )
    }
    if (locked) {
        LockScreen(
            strings = strings,
            onKeepPlaying = { requestUnlock() },
        )
    }
}

/** Bar countdown timer layar di bagian paling atas. */
@Composable
private fun TimerBar(remainingSec: Int, totalSec: Int) {
    val progress = if (totalSec > 0) remainingSec.toFloat() / totalSec else 0f
    val mm = remainingSec / 60
    val ss = remainingSec % 60
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xE61B5E20))
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp),
            color = SunYellow,
            trackColor = Color.White.copy(alpha = 0.25f),
        )
        Text(
            "⏱ %02d:%02d".format(mm, ss),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
        )
    }
}

/** Gerbang pengaturan: soal kabataku (× / ÷) dulu. */
@Composable
private fun MathGateDialog(
    strings: AppStrings,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    dismissable: Boolean = true,
) {
    val rng = remember { Random(System.currentTimeMillis()) }
    val (qa, qb, op) = remember {
        val a = 2 + rng.nextInt(8)
        val b = 2 + rng.nextInt(8)
        if (rng.nextBoolean()) Triple(a * b, b, '÷') else Triple(a, b, '×')
    }
    val answer = if (op == '×') qa * qb else qa / qb
    var input by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (dismissable) onDismiss() },
        title = { Text("🧮 ${strings.settingsTitle}") },
        text = {
            Column {
                Text(
                    "Jawab dulu ya:  $qa $op $qb = ?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter(Char::isDigit).take(3) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { Text("?") },
                )
                if (wrong) {
                    Spacer(Modifier.height(6.dp))
                    Text("Salah, coba lagi! 🙈", color = KartRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (input.toIntOrNull() == answer) {
                    onSuccess()
                } else {
                    wrong = true
                    input = ""
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
    )
}

/** Layar "terkunci" setelah waktu habis — harus Main Lagi → iklan → soal. */
@Composable
private fun LockScreen(strings: AppStrings, onKeepPlaying: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1F0F)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔒", fontSize = 64.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                strings.breakTitle,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                strings.lockAsk,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
            )
            Spacer(Modifier.height(24.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF43A047),
                onClick = onKeepPlaying,
            ) {
                Text(
                    strings.keepPlaying,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                )
            }
        }
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
            Image(
                painter = painterResource(R.drawable.ic_app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(32.dp)),
            )
            Spacer(Modifier.height(14.dp))
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
            Spacer(Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.22f),
                modifier = Modifier.padding(horizontal = 32.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Text(strings.splashBenefit1, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(strings.splashBenefit2, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(strings.splashBenefit3, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(28.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
private fun BreakOverlay(strings: com.gyosanila.kartcilik.ui.AppStrings, onKeepPlaying: () -> Unit) {
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
                // Hanya "Main Lagi" — gak ada tombol lanjut biasa; harus lewat iklan + soal.
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF43A047),
                    onClick = onKeepPlaying,
                ) {
                    Text(
                        strings.keepPlaying,
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
        Image(
            painter = painterResource(R.drawable.ic_app_logo),
            contentDescription = null,
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(26.dp)),
        )
        Spacer(Modifier.height(10.dp))
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
