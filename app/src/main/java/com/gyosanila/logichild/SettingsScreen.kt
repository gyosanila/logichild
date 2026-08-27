package com.gyosanila.logichild

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.logichild.ui.LocalStrings
import com.gyosanila.logichild.ui.SkyBlue
import com.gyosanila.logichild.ui.TextDark

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLanguageChange: (String) -> Unit,
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
    val strings = LocalStrings.current

    var lang by remember { mutableStateOf(prefs.getString("lang", "id") ?: "id") }
    var controller by remember { mutableStateOf(prefs.getString("controller_type", "kart") ?: "kart") }
    var timerMin by remember { mutableStateOf(prefs.getInt("timer_minutes", 0)) }
    var shadowMode by remember { mutableStateOf(readShadowMode(prefs)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "⚙️ ${strings.settingsTitle}",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                onClick = onBack,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Icon(Icons.Filled.Home, null, tint = TextDark)
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        // ── Bahasa ──
        SettingSection(strings.langLabel) {
            OptionRow(
                options = listOf(strings.langId to "id", strings.langEn to "en"),
                selected = lang,
                onSelect = { v ->
                    lang = v
                    prefs.edit().putString("lang", v).apply()
                    onLanguageChange(v)
                },
            )
        }
        Spacer(Modifier.height(16.dp))

        // ── Tipe kontrol ──
        SettingSection(strings.controllerLabel) {
            OptionRow(
                options = listOf(strings.controllerKart to "kart", strings.controllerSimple to "simple"),
                selected = controller,
                onSelect = { v ->
                    controller = v
                    prefs.edit().putString("controller_type", v).apply()
                },
            )
        }
        Spacer(Modifier.height(16.dp))

        // ── Timer layar ──
        SettingSection(strings.timerLabel) {
            OptionRow(
                options = listOf(
                    strings.timerOff to "0",
                    "5" to "5",
                    "10" to "10",
                    "15" to "15",
                    "30" to "30",
                ).map { (label, v) ->
                    val show = if (v == "0") label else String.format(strings.timerMin, v.toInt())
                    show to v
                },
                selected = timerMin.toString(),
                onSelect = { v ->
                    timerMin = v.toInt()
                    prefs.edit().putInt("timer_minutes", v.toInt()).apply()
                },
            )
            Spacer(Modifier.height(6.dp))
            Text(
                strings.timerNote,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
            )
        }
        Spacer(Modifier.height(16.dp))

        // ── Shadow preview ──
        SettingSection(strings.shadowLabel) {
            OptionRow(
                options = listOf(strings.shadowAuto to "auto", strings.shadowActive to "on", strings.shadowOff to "off"),
                selected = shadowMode,
                onSelect = { v ->
                    shadowMode = v
                    prefs.edit()
                        .putString("shadow_mode", v)
                        .remove("shadow_preview")
                        .apply()
                },
            )
        }
    }
}

@Composable
private fun SettingSection(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.22f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                title,
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun OptionRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (label, value) ->
            val isSel = value == selected
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSel) TextDark.copy(alpha = 0.25f) else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.10f),
                onClick = { onSelect(value) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(
                        if (isSel) "● " else "○ ",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 15.sp,
                    )
                    Text(
                        label,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 15.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
