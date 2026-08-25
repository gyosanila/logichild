package com.gyosanila.kartcilik

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gyosanila.kartcilik.ui.AppStrings
import com.gyosanila.kartcilik.ui.BerryPurple
import com.gyosanila.kartcilik.ui.ConeOrange
import com.gyosanila.kartcilik.ui.KartRed
import com.gyosanila.kartcilik.ui.OceanBlue
import com.gyosanila.kartcilik.ui.TextDark

/** Tipe controller dari Pengaturan ("kart" | "simple"). */
@Composable
fun rememberControllerType(): String {
    val context = LocalContext.current
    return remember {
        context.getSharedPreferences("kartcilik_prefs", Context.MODE_PRIVATE)
            .getString("controller_type", "kart") ?: "kart"
    }
}

/** Spesifikasi satu tombol perintah. */
data class CmdSpec(
    val label: String,
    val color: Color,
    val darker: Color,
    val emoji: String? = null,
    val icon: ImageVector? = null,
    val onClick: () -> Unit = {},
)

/**
 * Controller permainan — 2 tipe (bisa diganti di Pengaturan):
 * - "kart"  : panel tombol bulat besar (Kiri/Maju/Kanan) + panel action
 * - "simple": tombol kotak sebaris + tombol JALAN lebar
 */
@Composable
fun GameController(
    controllerType: String,
    dirCmds: List<CmdSpec>,     // Kiri, Maju, Kanan
    actionCmds: List<CmdSpec>,  // opsional (mis. Petik 🍎)
    onPlay: () -> Unit,
    onReset: () -> Unit,
    canEdit: Boolean,
    playEnabled: Boolean,
    resetEnabled: Boolean,
    strings: AppStrings,
    modifier: Modifier = Modifier,
) {
    if (controllerType == "simple") {
        SimpleController(dirCmds, actionCmds, onPlay, onReset, canEdit, playEnabled, resetEnabled, strings, modifier)
    } else {
        KartStyleController(dirCmds, actionCmds, onPlay, onReset, canEdit, playEnabled, resetEnabled, strings, modifier)
    }
}

// ─── Tipe "kart": panel tombol bulat besar ─────────────────────────

@Composable
private fun KartStyleController(
    dirCmds: List<CmdSpec>,
    actionCmds: List<CmdSpec>,
    onPlay: () -> Unit,
    onReset: () -> Unit,
    canEdit: Boolean,
    playEnabled: Boolean,
    resetEnabled: Boolean,
    strings: AppStrings,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White.copy(alpha = 0.18f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                dirCmds.forEachIndexed { i, c ->
                    if (i > 0) Spacer(Modifier.width(22.dp))
                    BigRoundButton(
                        label = c.label,
                        color = c.color,
                        darker = c.darker,
                        icon = c.icon,
                        emoji = c.emoji,
                        enabled = canEdit,
                        size = 76.dp,
                        onClick = c.onClick,
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White.copy(alpha = 0.26f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                actionCmds.forEach { c ->
                    BigRoundButton(
                        label = c.label,
                        color = c.color,
                        darker = c.darker,
                        icon = c.icon,
                        emoji = c.emoji,
                        enabled = canEdit,
                        size = 64.dp,
                        iconSize = 30.dp,
                        onClick = c.onClick,
                    )
                    Spacer(Modifier.width(26.dp))
                }
                BigRoundButton(
                    label = strings.cmdPlay,
                    color = Color(0xFF66BB6A),
                    darker = Color(0xFF2E7D32),
                    icon = Icons.Filled.PlayArrow,
                    enabled = playEnabled,
                    size = 96.dp,
                    iconSize = 58.dp,
                    shadow = 10.dp,
                    fontSize = 15.sp,
                    onClick = onPlay,
                )
                Spacer(Modifier.width(26.dp))
                BigRoundButton(
                    label = strings.cmdReset,
                    color = Color(0xFF90A4AE),
                    darker = Color(0xFF607D8B),
                    icon = Icons.Filled.Refresh,
                    enabled = resetEnabled,
                    size = 64.dp,
                    iconSize = 32.dp,
                    shadow = 5.dp,
                    onClick = onReset,
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ─── Tipe "simple": tombol kotak sebaris + JALAN lebar ────────────

@Composable
private fun SimpleController(
    dirCmds: List<CmdSpec>,
    actionCmds: List<CmdSpec>,
    onPlay: () -> Unit,
    onReset: () -> Unit,
    canEdit: Boolean,
    playEnabled: Boolean,
    resetEnabled: Boolean,
    strings: AppStrings,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (dirCmds + actionCmds).forEach { c ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = c.color,
                    onClick = { if (canEdit) c.onClick() },
                    modifier = Modifier.weight(1f),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 10.dp),
                    ) {
                        if (c.emoji != null) {
                            Text(c.emoji, fontSize = 24.sp)
                        } else {
                            Icon(
                                c.icon ?: Icons.Filled.PlayArrow,
                                c.label,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        Text(
                            c.label,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF43A047),
                onClick = onPlay,
                enabled = playEnabled,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 14.dp),
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                    Text(
                        strings.cmdPlay,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                onClick = onReset,
                enabled = resetEnabled,
                modifier = Modifier.size(52.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Refresh,
                        strings.cmdReset,
                        tint = if (resetEnabled) TextDark else TextDark.copy(alpha = 0.35f),
                    )
                }
            }
        }
    }
}

// ─── Tombol bulat besar (dipakai tipe "kart") ─────────────────────

@Composable
fun BigRoundButton(
    label: String,
    color: Color,
    darker: Color,
    icon: ImageVector? = null,
    emoji: String? = null,
    enabled: Boolean = true,
    size: Dp = 72.dp,
    iconSize: Dp = 36.dp,
    shadow: Dp = 6.dp,
    fontSize: TextUnit = 12.sp,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = tween(90),
        label = "btnScale",
    )
    val bg = Brush.verticalGradient(listOf(color, darker))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(shadow, CircleShape)
                .background(if (enabled) bg else Brush.verticalGradient(listOf(Color(0xFFB0BEC5), Color(0xFF90A4AE))), CircleShape)
                .border(
                    width = if (enabled) 0.dp else 2.dp,
                    color = Color.White.copy(alpha = 0.35f),
                    shape = CircleShape,
                )
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(color = Color.White.copy(alpha = 0.35f)),
                    enabled = enabled,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (emoji != null) {
                Text(emoji, fontSize = iconSize.value.sp)
            } else {
                Icon(
                    icon!!,
                    label,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}
