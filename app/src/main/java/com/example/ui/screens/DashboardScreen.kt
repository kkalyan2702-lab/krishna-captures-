package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KenBurnsIntensity
import com.example.ui.ScreensaverUiState
import com.example.ui.ScreensaverViewModel
import com.example.ui.components.TvButton
import com.example.ui.components.TvPhotoCard
import com.example.ui.components.TvPresetChip
import com.example.ui.components.TvSettingsDialog
import com.example.ui.components.TvStepperControl
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
fun DashboardScreen(
    state: ScreensaverUiState,
    viewModel: ScreensaverViewModel,
    modifier: Modifier = Modifier
) {
    var showSettingsModal by remember { mutableStateOf(false) }

    // Android Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addPickedPhotos(uris)
        }
    }

    // Android SAF Folder Tree Picker Launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            viewModel.addFolder(treeUri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            // TV overscan safe margin (36.dp horizontal, 20.dp vertical)
            .padding(horizontal = 36.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // TV Hero Header (10-foot scale)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AmberPrimary, Color(0xFFD97706))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Krishna Captures",
                        tint = BackgroundBlack,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column {
                    Text(
                        text = "Krishna Captures",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Android TV Screensaver • 10-Foot Leanback Studio",
                        color = AmberSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            // TV Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings Dialog Button
                TvButton(
                    text = "Settings",
                    icon = Icons.Default.Settings,
                    onClick = { showSettingsModal = true }
                )

                // Primary Start Screensaver Button
                TvButton(
                    text = "Start Screensaver",
                    icon = Icons.Default.PlayArrow,
                    isPrimary = true,
                    onClick = { viewModel.startScreensaver() }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Notification Banner
        if (state.notificationMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E3A8A))
                    .border(1.5.dp, Color(0xFF60A5FA), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = state.notificationMessage,
                    color = Color(0xFFEFF6FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // TV Status Banner
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (state.settings.idleTimerEnabled) Color(0xFF10B981) else Color(0xFF64748B))
                    )
                    Text(
                        text = if (state.settings.idleTimerEnabled) "TV Screensaver Armed" else "Idle Timer Disabled",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (state.settings.idleTimerEnabled) {
                    val mins = state.idleRemainingSeconds / 60
                    val secs = state.idleRemainingSeconds % 60
                    Text(
                        text = "Auto-starts in ${mins}m ${secs}s of TV inactivity",
                        color = AmberSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // TV Photo Grid / Horizontal Strip Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Wallpapers & Photo Collection (${state.photos.size})",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Use D-pad arrows to navigate. Press Select on any photo to launch directly.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // TV Management Actions
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TvButton(
                    text = "Add Photos",
                    icon = Icons.Default.AddPhotoAlternate,
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                TvButton(
                    text = if (state.isLoadingFolder) "Scanning..." else "Add Folder",
                    icon = Icons.Default.FolderOpen,
                    onClick = { folderPickerLauncher.launch(null) }
                )

                TvButton(
                    text = "Reset Curated",
                    icon = Icons.Default.Refresh,
                    onClick = { viewModel.resetToCuratedPhotos() }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // D-pad Navigable Photo Cards Strip
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(state.photos, key = { _, photo -> photo.id }) { index, photo ->
                TvPhotoCard(
                    photo = photo,
                    onClick = {
                        viewModel.startScreensaverAt(index)
                    },
                    onDelete = {
                        viewModel.removePhoto(photo)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Settings Dashboard Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "TV Remote Settings Dashboard",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "All controls respond to D-pad Left/Right arrows and Select button",
                    color = AmberSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Idle Screensaver Timer Setting (D-pad accessible Stepper & Chips)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceHighlight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Idle Screensaver Timer",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Auto-activates screensaver after inactivity (Minimum 5 mins required)",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = state.settings.idleTimerEnabled,
                        onCheckedChange = { viewModel.setIdleTimerEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundBlack,
                            checkedTrackColor = AmberPrimary
                        )
                    )
                }

                if (state.settings.idleTimerEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TvStepperControl(
                            valueLabel = "${state.settings.idleTimerMinutes}",
                            unit = "minutes",
                            minReached = state.settings.idleTimerMinutes <= 5,
                            onDecrement = {
                                viewModel.setIdleTimerMinutes((state.settings.idleTimerMinutes - 5).coerceAtLeast(5))
                            },
                            onIncrement = {
                                viewModel.setIdleTimerMinutes(state.settings.idleTimerMinutes + 5)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            listOf(5, 10, 15, 30, 60).forEach { mins ->
                                TvPresetChip(
                                    label = "${mins}m",
                                    selected = state.settings.idleTimerMinutes == mins,
                                    onClick = { viewModel.setIdleTimerMinutes(mins) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Photo Display Duration Setting (D-pad accessible Stepper & Chips)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Photo Display Duration",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Time each wallpaper remains on TV screen (Minimum 1 min per photo required)",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val isPreview = state.settings.testPreviewSpeedSeconds != null

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TvStepperControl(
                        valueLabel = if (isPreview) "${state.settings.testPreviewSpeedSeconds}" else "${state.settings.slideshowDurationMinutes}",
                        unit = if (isPreview) "seconds" else "minutes",
                        minReached = !isPreview && state.settings.slideshowDurationMinutes <= 1,
                        onDecrement = {
                            viewModel.setTestPreviewSpeedSeconds(null)
                            viewModel.setSlideshowDurationMinutes((state.settings.slideshowDurationMinutes - 1).coerceAtLeast(1))
                        },
                        onIncrement = {
                            viewModel.setTestPreviewSpeedSeconds(null)
                            viewModel.setSlideshowDurationMinutes(state.settings.slideshowDurationMinutes + 1)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1.8f)
                    ) {
                        listOf(1, 2, 5, 10).forEach { mins ->
                            TvPresetChip(
                                label = "${mins} min",
                                selected = !isPreview && state.settings.slideshowDurationMinutes == mins,
                                onClick = {
                                    viewModel.setTestPreviewSpeedSeconds(null)
                                    viewModel.setSlideshowDurationMinutes(mins)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        TvPresetChip(
                            label = "10s Preview",
                            selected = isPreview,
                            onClick = {
                                if (isPreview) viewModel.setTestPreviewSpeedSeconds(null) else viewModel.setTestPreviewSpeedSeconds(10)
                            },
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Cinematic Engine Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Cinematic Motion Engine",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Ken Burns zoom and pan, crossfade transition, and infinite shuffle",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ken Burns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Ken Burns Zoom & Pan", color = TextPrimary, fontSize = 14.sp)
                        Text("Gentle camera motion drifting across wallpaper details", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = state.settings.kenBurnsEnabled,
                        onCheckedChange = { viewModel.setKenBurnsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundBlack,
                            checkedTrackColor = AmberPrimary
                        )
                    )
                }

                if (state.settings.kenBurnsEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KenBurnsIntensity.values().forEach { intensity ->
                            TvPresetChip(
                                label = intensity.label,
                                selected = state.settings.kenBurnsIntensity == intensity,
                                onClick = { viewModel.setKenBurnsIntensity(intensity) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = SurfaceHighlight
                )

                // Crossfade Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Crossfade Duration: ${state.settings.crossfadeDurationMs / 1000f}s",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1000, 2000, 3000, 4000).forEach { ms ->
                            TvPresetChip(
                                label = "${ms / 1000}s",
                                selected = state.settings.crossfadeDurationMs == ms,
                                onClick = { viewModel.setCrossfadeDurationMs(ms) }
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = SurfaceHighlight
                )

                // Infinite Shuffle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Infinite Shuffle Loop", color = TextPrimary, fontSize = 14.sp)
                        Text("Endless random photo sequence without repeating", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = state.settings.infiniteShuffle,
                        onCheckedChange = { viewModel.setInfiniteShuffle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundBlack,
                            checkedTrackColor = AmberPrimary
                        )
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = SurfaceHighlight
                )

                // Edge-to-Edge Fill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Edge-to-Edge Fill Crop", color = TextPrimary, fontSize = 14.sp)
                        Text("Fills 16:9 TV screen without black bars", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = state.settings.cropToFill,
                        onCheckedChange = { viewModel.setCropToFill(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundBlack,
                            checkedTrackColor = AmberPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    // Modal TV Settings Dialog
    if (showSettingsModal) {
        TvSettingsDialog(
            settings = state.settings,
            onDismiss = { showSettingsModal = false },
            onIdleTimerChanged = { viewModel.setIdleTimerMinutes(it) },
            onIdleTimerEnabledChanged = { viewModel.setIdleTimerEnabled(it) },
            onSlideshowDurationChanged = { viewModel.setSlideshowDurationMinutes(it) },
            onTestSpeedChanged = { viewModel.setTestPreviewSpeedSeconds(it) },
            onKenBurnsEnabledChanged = { viewModel.setKenBurnsEnabled(it) },
            onKenBurnsIntensityChanged = { viewModel.setKenBurnsIntensity(it) },
            onCrossfadeDurationChanged = { viewModel.setCrossfadeDurationMs(it) },
            onShuffleChanged = { viewModel.setInfiniteShuffle(it) },
            onCropToFillChanged = { viewModel.setCropToFill(it) }
        )
    }
}
