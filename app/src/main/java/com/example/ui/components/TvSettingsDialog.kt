package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.KenBurnsIntensity
import com.example.model.ScreensaverSettings
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.BackgroundBlack
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TvSettingsDialog(
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .border(2.dp, AmberPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Screensaver Settings",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Navigate with D-pad arrows and press Center to select",
                            color = AmberSecondary,
                            fontSize = 13.sp
                        )
                    }

                    TvButton(
                        text = "Close",
                        onClick = onDismiss,
                        icon = Icons.Default.Close
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Idle Timer Card (D-pad Stepper & Chips)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = AmberPrimary
                                )
                                Column {
                                    Text(
                                        text = "Idle Screensaver Timer",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Wait time before auto-starting screensaver (Min 5 mins)",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Switch(
                                checked = settings.idleTimerEnabled,
                                onCheckedChange = onIdleTimerEnabledChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BackgroundBlack,
                                    checkedTrackColor = AmberPrimary
                                )
                            )
                        }

                        if (settings.idleTimerEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                TvStepperControl(
                                    valueLabel = "${settings.idleTimerMinutes}",
                                    unit = "minutes",
                                    minReached = settings.idleTimerMinutes <= 5,
                                    onDecrement = {
                                        onIdleTimerChanged((settings.idleTimerMinutes - 5).coerceAtLeast(5))
                                    },
                                    onIncrement = {
                                        onIdleTimerChanged(settings.idleTimerMinutes + 5)
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    listOf(5, 10, 15, 30).forEach { mins ->
                                        TvPresetChip(
                                            label = "${mins}m",
                                            selected = settings.idleTimerMinutes == mins,
                                            onClick = { onIdleTimerChanged(mins) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Photo Display Duration Card (D-pad Stepper & Chips)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = AmberPrimary
                            )
                            Column {
                                Text(
                                    text = "Photo Display Duration",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Duration each photo is displayed (Min 1 min per photo)",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val isPreview = settings.testPreviewSpeedSeconds != null

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            TvStepperControl(
                                valueLabel = if (isPreview) "${settings.testPreviewSpeedSeconds}" else "${settings.slideshowDurationMinutes}",
                                unit = if (isPreview) "seconds" else "minutes",
                                minReached = !isPreview && settings.slideshowDurationMinutes <= 1,
                                onDecrement = {
                                    onTestSpeedChanged(null)
                                    onSlideshowDurationChanged((settings.slideshowDurationMinutes - 1).coerceAtLeast(1))
                                },
                                onIncrement = {
                                    onTestSpeedChanged(null)
                                    onSlideshowDurationChanged(settings.slideshowDurationMinutes + 1)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                listOf(1, 2, 5, 10).forEach { mins ->
                                    TvPresetChip(
                                        label = "${mins}m",
                                        selected = !isPreview && settings.slideshowDurationMinutes == mins,
                                        onClick = {
                                            onTestSpeedChanged(null)
                                            onSlideshowDurationChanged(mins)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                TvPresetChip(
                                    label = "10s",
                                    selected = isPreview,
                                    onClick = {
                                        if (isPreview) onTestSpeedChanged(null) else onTestSpeedChanged(10)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Cinematic Engine Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = AmberPrimary
                            )
                            Text(
                                text = "Cinematic Motion Engine",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Ken Burns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ken Burns Zoom & Pan", color = TextPrimary, fontSize = 14.sp)
                            Switch(
                                checked = settings.kenBurnsEnabled,
                                onCheckedChange = onKenBurnsEnabledChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BackgroundBlack,
                                    checkedTrackColor = AmberPrimary
                                )
                            )
                        }

                        if (settings.kenBurnsEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                KenBurnsIntensity.values().forEach { intensity ->
                                    TvPresetChip(
                                        label = intensity.label,
                                        selected = settings.kenBurnsIntensity == intensity,
                                        onClick = { onKenBurnsIntensityChanged(intensity) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = SurfaceHighlight
                        )

                        // Infinite Shuffle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Infinite Shuffle Loop", color = TextPrimary, fontSize = 14.sp)
                            Switch(
                                checked = settings.infiniteShuffle,
                                onCheckedChange = onShuffleChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BackgroundBlack,
                                    checkedTrackColor = AmberPrimary
                                )
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = SurfaceHighlight
                        )

                        // Edge-to-Edge Fill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Edge-to-Edge Fill Crop", color = TextPrimary, fontSize = 14.sp)
                            Switch(
                                checked = settings.cropToFill,
                                onCheckedChange = onCropToFillChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BackgroundBlack,
                                    checkedTrackColor = AmberPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TvButton(
                    text = "Done (Save & Return)",
                    onClick = onDismiss,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
