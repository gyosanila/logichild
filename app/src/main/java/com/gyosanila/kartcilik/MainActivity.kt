package com.gyosanila.kartcilik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.gyosanila.kartcilik.ui.BerryPurple
import com.gyosanila.kartcilik.ui.GrassGreen
import com.gyosanila.kartcilik.ui.KartRed
import com.gyosanila.kartcilik.ui.OceanBlue
import com.gyosanila.kartcilik.ui.SkyBlue
import com.gyosanila.kartcilik.ui.SunYellow
import com.gyosanila.kartcilik.ui.TextDark

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KartCilikTheme {
                KartGameScreen()
            }
        }
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
        onPrimary = androidx.compose.ui.graphics.Color.White,
        onBackground = TextDark,
        onSurface = TextDark,
    )
    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
