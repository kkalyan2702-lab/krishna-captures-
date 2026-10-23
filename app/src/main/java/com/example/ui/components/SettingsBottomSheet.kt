package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KenBurnsIntensity
import com.example.model.ScreensaverSettings
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.BackgroundBlack
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    settings: ScreensaverSettings,
    onDismiss: () -> Unit,
    onIdleTimerChanged: (Int) -> Unit,
    onIdleTimerEnabledChanged: (Boolean) -> Unit,
    onSlideshowDurationChanged: (Int) -> Unit,
    onTestSpeedChanged: (Int?) -> Unit,
    onKenBurnsEnabledChanged: (Boolean) -> Unit,
    onKenBurnsIntensityChanged: (KenBurnsIntensity) -> Unit,
    onCrossfadeDurationChanged: (Int) -> Unit,
    onShuffleChanged: (Boolean) -> Unit,
    onCropToFillChanged: (Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundBlack,
        dragHandle = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                BoxPillHandle()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Screensaver Settings",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize idle triggers, slide duration, and cinematic effects",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Idle Timer Section (Minimum 5 minutes requirement)
            SettingsCard(
                title = "Idle Screensaver Timer",
                subtitle = "Wait time with no activity before launching screensaver (Min 5 mins)",
                icon = Icons.Default.HourglassEmpty
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-launch on idle", color = TextPrimary, fontSize = 14.sp)
                    Switch(
                        checked = settings.idleTimerEnabled,
                        onCheckedChange = onIdleTimerEnabledChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundBlack,
                            checkedTrackColor = AmberPrimary
                        ),
                        modifier = Modifier.testTag("switch_idle_timer_enabled")
                    )
                }

                if (settings.idleTimerEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Active: ${settings.idleTimerMinutes} minutes of inactivity",
                        color = AmberSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 30).forEach { mins ->
                            val selected = settings.idleTimerMinutes == mins
                            PresetChip(
                                label = "${mins}m",
                                selected = selected,
                                onClick = { onIdleTimerChanged(mins) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = settings.idleTimerMinutes.toFloat(),
                        onValueChange = { onIdleTimerChanged(it.toInt().coerceAtLeast(5)) },
                        valueRange = 5f..60f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberPrimary,
                            activeTrackColor = AmberPrimary,
                            inactiveTrackColor = SurfaceHighlight
                        ),
                        modifier = Modifier.testTag("slider_idle_timer")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Slideshow Timer Section (Minimum 1 minute requirement)
            SettingsCard(
                title = "Photo Display Duration",
                subtitle = "Time each photo is displayed (Min 1 min per photo)",
                icon = Icons.Default.Timer
            ) {
                val isTestPreview = settings.testPreviewSpeedSeconds != null

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isTestPreview) "Preview Mode: ${settings.testPreviewSpeedSeconds}s" else "Standard: ${settings.slideshowDurationMinutes} min per photo",
                            color = AmberSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isTestPreview) "Fast preview for inspection" else "Relaxed, distraction-free screensaver pace",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PresetChip(
                            label = "10s Preview",
                            selected = settings.testPreviewSpeedSeconds == 10,
                            onClick = {
                                if (settings.testPreviewSpeedSeconds == 10) onTestSpeedChanged(null) else onTestSpeedChanged(10)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 5, 10).forEach { mins ->
                        val selected = !isTestPreview && settings.slideshowDurationMinutes == mins
                        PresetChip(
                            label = "${mins} min",
                            selected = selected,
                            onClick = {
                                onTestSpeedChanged(null)
                                onSlideshowDurationChanged(mins)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = settings.slideshowDurationMinutes.toFloat(),
                    onValueChange = {
                        onTestSpeedChanged(null)
                        onSlideshowDurationChanged(it.toInt().coerceAtLeast(1))
                    },
                    valueRange = 1f..15f,
                    steps = 13,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberPrimary,
                        activeTrackColor = AmberPrimary,
                        inactiveTrackColor = SurfaceHighlight
                    ),
                    modifier = Modifier.testTag("slider_slideshow_duration")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Cinematic Engine Settings
            SettingsCard(
                title = "Cinematic Motion & Effects",
                subtitle = "Crossfade transitions and Ken Burns camera movements",
                icon = Icons.Default.Movie
            ) {
                // Ken Burns Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Ken Burns Zoom Effect", color = TextPrimary, fontSize = 14.sp)
                        Text("Slow pan and subtle scale across each photo", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = settings.kenBurnsEnabled,
                        onCheckedChange = onKenBurnsEnabledChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundBlack,
                            checkedTrackColor = AmberPrimary
                        ),
                        modifier = Modifier.testTag("switch_ken_burns")
                    )
                }

                if (settings.kenBurnsEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KenBurnsIntensity.values().forEach { intensity ->
                            val selected = settings.kenBurnsIntensity == intensity
                            PresetChip(
                                label = intensity.label.substringBefore(' '),
                                selected = selected,
                                onClick = { onKenBurnsIntensityChanged(intensity) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = SurfaceHighlight
                )

                // Crossfade Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Crossfade Speed: ${settings.crossfadeDurationMs / 1000f}s",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
                Slider(
                    value = settings.crossfadeDurationMs.toFloat(),
                    onValueChange = { onCrossfadeDurationChanged(it.toInt()) },
                    valueRange = 1000f..4000f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberPrimary,
                        activeTrackColor = AmberPrimary,
                        inactiveTrackColor = SurfaceHighlight
                    ),
                    modifier = Modifier.testTag("slider_crossfade")
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = SurfaceHighlight
                )

                // Infinite Shuffle Loop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Infinite Shuffle Loop", color = TextPrimary, fontSize = 14.sp)
                        Text("Randomize photo sequence indefinitely", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = settings.infiniteShuffle,
                        onCheckedChange = onShuffleChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundBlack,
                            checkedTrackColor = AmberPrimary
                        ),
                        modifier = Modifier.testTag("switch_infinite_shuffle")
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = SurfaceHighlight
                )

                // Crop to fill vs Fit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Edge-to-Edge Fill Crop", color = TextPrimary, fontSize = 14.sp)
                        Text("Fills entire screen without letterboxing", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = settings.cropToFill,
                        onCheckedChange = onCropToFillChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundBlack,
                            checkedTrackColor = AmberPrimary
                        ),
                        modifier = Modifier.testTag("switch_crop_to_fill")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberPrimary,
                    contentColor = BackgroundBlack
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button")
            ) {
                Text("Done", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceHighlight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AmberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) AmberPrimary else SurfaceHighlight
    val textColor = if (selected) BackgroundBlack else TextPrimary

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun BoxPillHandle() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .width(42.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(TextMuted.copy(alpha = 0.5f))
    )
}
