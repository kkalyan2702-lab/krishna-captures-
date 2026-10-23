package com.example.ui.screens

import android.app.Activity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScreensaverUiState
import com.example.ui.ScreensaverViewModel
import com.example.ui.components.CinematicPhotoRenderer
import com.example.ui.components.TvSettingsDialog
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.BackgroundBlack
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ScreensaverScreen(
    state: ScreensaverUiState,
    viewModel: ScreensaverViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }
    val playPauseFocusRequester = remember { FocusRequester() }
    val screenFocusRequester = remember { FocusRequester() }

    // Keep screen on while screensaver is active
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Auto-focus play/pause when controls become visible, or screen focus when hidden
    LaunchedEffect(state.controlsVisible) {
        if (state.controlsVisible) {
            try {
                playPauseFocusRequester.requestFocus()
            } catch (_: Exception) {}
        } else {
            try {
                screenFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // Hardware TV Remote Back button
    BackHandler {
        if (state.controlsVisible) {
            viewModel.exitToDashboard()
        } else {
            viewModel.onUserInteraction()
        }
    }

    val photos = state.photos
    val currentPhoto = photos.getOrNull(state.currentPhotoIndex)
    val nextPhoto = photos.getOrNull(state.nextPhotoIndex)

    val crossfadeAlpha by animateFloatAsState(
        targetValue = if (state.isCrossfading) 1.0f else 0.0f,
        animationSpec = tween(
            durationMillis = state.settings.crossfadeDurationMs,
            easing = LinearEasing
        ),
        label = "crossfade_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(screenFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                viewModel.onUserInteraction()
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Escape, Key.Back -> {
                            viewModel.exitToDashboard()
                            true
                        }
                        Key.Spacebar, Key.DirectionCenter, Key.Enter, Key.MediaPlayPause -> {
                            viewModel.togglePlayPause()
                            true
                        }
                        Key.DirectionRight, Key.MediaFastForward, Key.MediaNext -> {
                            viewModel.nextPhoto()
                            true
                        }
                        Key.DirectionLeft, Key.MediaRewind, Key.MediaPrevious -> {
                            viewModel.previousPhoto()
                            true
                        }
                        Key.DirectionUp -> {
                            showSettingsDialog = true
                            true
                        }
                        Key.DirectionDown -> {
                            viewModel.onUserInteraction()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        // Base Layer: Current Photo
        if (currentPhoto != null) {
            CinematicPhotoRenderer(
                photo = currentPhoto,
                photoIndex = state.currentPhotoIndex,
                kenBurnsEnabled = state.settings.kenBurnsEnabled,
                intensity = state.settings.kenBurnsIntensity,
                cropToFill = state.settings.cropToFill,
                slideDurationMs = state.settings.effectiveSlideDurationMs,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Crossfade Layer: Next Photo
        if (nextPhoto != null && (state.isCrossfading || crossfadeAlpha > 0.01f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(crossfadeAlpha)
            ) {
                CinematicPhotoRenderer(
                    photo = nextPhoto,
                    photoIndex = state.nextPhotoIndex,
                    kenBurnsEnabled = state.settings.kenBurnsEnabled,
                    intensity = state.settings.kenBurnsIntensity,
                    cropToFill = state.settings.cropToFill,
                    slideDurationMs = state.settings.effectiveSlideDurationMs,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // TV Remote Minimalist Overlay (Auto-hides after 4s)
        AnimatedVisibility(
            visible = state.controlsVisible,
            enter = fadeIn(tween(250)) + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut(tween(400)) + slideOutVertically(targetOffsetY = { -it / 2 }),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GlassOverlay,
                                Color.Transparent,
                                Color.Transparent,
                                GlassOverlay
                            )
                        )
                    )
            ) {
                // TV Top Bar (Title, Photo Name, Counter, Exit)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 40.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TvRemoteIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit to Dashboard (Back/Esc)",
                            onClick = { viewModel.exitToDashboard() }
                        )

                        Column {
                            Text(
                                text = "Krishna Captures",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentPhoto != null) {
                                Text(
                                    text = "${currentPhoto.title} • ${currentPhoto.subtitle}",
                                    color = AmberSecondary,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Photo Counter Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GlassSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Photo ${state.currentPhotoIndex + 1} of ${photos.size}",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // TV Settings Button
                        TvRemoteIconButton(
                            icon = Icons.Default.Settings,
                            contentDescription = "Open TV Settings",
                            onClick = { showSettingsDialog = true }
                        )
                    }
                }

                // TV Remote Bottom Control Bar (D-pad navigable)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floating TV Glass Remote Bar
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(36.dp))
                            .background(GlassSurface)
                            .border(1.5.dp, GlassBorder, RoundedCornerShape(36.dp))
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Previous Photo (D-pad Left)
                        TvRemoteIconButton(
                            icon = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Photo (D-pad Left)",
                            onClick = { viewModel.previousPhoto() },
                            size = 52.dp,
                            iconSize = 32.dp
                        )

                        // Play / Pause Button (D-pad Center / Select) - Primary Focus Target
                        TvRemoteIconButton(
                            icon = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (state.isPaused) "Play" else "Pause",
                            onClick = { viewModel.togglePlayPause() },
                            isPrimary = true,
                            size = 64.dp,
                            iconSize = 36.dp,
                            focusRequester = playPauseFocusRequester
                        )

                        // Next Photo (D-pad Right)
                        TvRemoteIconButton(
                            icon = Icons.Default.SkipNext,
                            contentDescription = "Next Photo (D-pad Right)",
                            onClick = { viewModel.nextPhoto() },
                            size = 52.dp,
                            iconSize = 32.dp
                        )

                        // Shuffle Toggle
                        TvRemoteIconButton(
                            icon = Icons.Default.Shuffle,
                            contentDescription = "Infinite Shuffle",
                            onClick = { viewModel.setInfiniteShuffle(!state.settings.infiniteShuffle) },
                            tint = if (state.settings.infiniteShuffle) AmberSecondary else TextMuted,
                            size = 52.dp,
                            iconSize = 26.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "TV Remote: D-pad Left/Right = Prev/Next • Select = Play/Pause • Up = Settings • Back = Exit",
                        color = AmberSecondary.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Modal TV Settings Dialog
    if (showSettingsDialog) {
        TvSettingsDialog(
            settings = state.settings,
            onDismiss = { showSettingsDialog = false },
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

/**
 * TV Remote Icon Button with high-contrast glowing focus outline and scale animation
 */
@Composable
private fun TvRemoteIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
    tint: Color = TextPrimary,
    focusRequester: FocusRequester? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "tv_icon_scale"
    )

    val bg = when {
        isFocused -> AmberSecondary
        isPrimary -> AmberPrimary
        else -> GlassSurface
    }

    val iconColor = when {
        isFocused || isPrimary -> BackgroundBlack
        else -> tint
    }

    Box(
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .size(size)
            .scale(animatedScale)
            .clip(CircleShape)
            .background(bg)
            .border(
                width = if (isFocused) 3.5.dp else 1.dp,
                color = if (isFocused) Color.White else GlassBorder,
                shape = CircleShape
            )
            .focusable(interactionSource = interactionSource)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter)
                ) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
