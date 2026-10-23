package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.model.KenBurnsIntensity
import com.example.model.PhotoItem

@Composable
fun CinematicPhotoRenderer(
    photo: PhotoItem,
    photoIndex: Int,
    kenBurnsEnabled: Boolean,
    intensity: KenBurnsIntensity,
    cropToFill: Boolean,
    slideDurationMs: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Ken Burns Animation: Slow, gentle scale & pan drifting across the photo
    val infiniteTransition = rememberInfiniteTransition(label = "ken_burns_motion_$photoIndex")

    // Motion parameters varied per photo index
    val startScale = remember(photoIndex, intensity) {
        if (photoIndex % 2 == 0) 1.0f else intensity.scaleRange
    }
    val targetScale = remember(photoIndex, intensity) {
        if (photoIndex % 2 == 0) intensity.scaleRange else 1.0f
    }
    val panDirectionX = remember(photoIndex) {
        when (photoIndex % 3) {
            0 -> 1f
            1 -> -1f
            else -> 0f
        }
    }
    val panDirectionY = remember(photoIndex) {
        when (photoIndex % 3) {
            0 -> -1f
            1 -> 1f
            else -> 1f
        }
    }

    val animatedScale by infiniteTransition.animateFloat(
        initialValue = startScale,
        targetValue = targetScale,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (slideDurationMs.coerceAtLeast(8000L)).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_anim"
    )

    val animatedPanFactor by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (slideDurationMs.coerceAtLeast(8000L)).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pan_anim"
    )

    val currentScale = if (kenBurnsEnabled) animatedScale else 1.0f
    val maxPanOffset = if (kenBurnsEnabled) 36f else 0f
    val panX = animatedPanFactor * maxPanOffset * panDirectionX
    val panY = animatedPanFactor * maxPanOffset * panDirectionY

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.uriString)
                .crossfade(true)
                .build(),
            contentDescription = photo.title,
            contentScale = if (cropToFill) ContentScale.Crop else ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = currentScale
                    scaleY = currentScale
                    translationX = panX
                    translationY = panY
                },
            loading = {
                ProceduralScenicBackground(
                    seedIndex = photoIndex,
                    modifier = Modifier.fillMaxSize()
                )
            },
            error = {
                // If offline or URI unreachable, show atmospheric scenic landscape with title
                ProceduralScenicBackground(
                    seedIndex = photoIndex,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )
    }
}
