package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.sin

/**
 * Procedural scenic painter that renders high-aesthetic mountain twilight or starry landscapes.
 * Used as a zero-latency fallback and rich cinematic backdrop.
 */
@Composable
fun ProceduralScenicBackground(
    seedIndex: Int,
    modifier: Modifier = Modifier
) {
    // Determine color theme based on seed
    val theme = remember(seedIndex) {
        when (seedIndex % 4) {
            0 -> ScenicTheme(
                skyTop = Color(0xFF0F172A),
                skyMid = Color(0xFF431407),
                skyBottom = Color(0xFFF97316),
                mountainBack = Color(0xFF31151F),
                mountainFront = Color(0xFF13070C)
            )
            1 -> ScenicTheme(
                skyTop = Color(0xFF020617),
                skyMid = Color(0xFF0F2B48),
                skyBottom = Color(0xFF0284C7),
                mountainBack = Color(0xFF082032),
                mountainFront = Color(0xFF030D16)
            )
            2 -> ScenicTheme(
                skyTop = Color(0xFF110826),
                skyMid = Color(0xFF3B0764),
                skyBottom = Color(0xFFD946EF),
                mountainBack = Color(0xFF220935),
                mountainFront = Color(0xFF0C0314)
            )
            else -> ScenicTheme(
                skyTop = Color(0xFF061517),
                skyMid = Color(0xFF042F2C),
                skyBottom = Color(0xFF14B8A6),
                mountainBack = Color(0xFF062121),
                mountainFront = Color(0xFF020C0D)
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(theme.skyTop, theme.skyMid, theme.skyBottom),
                startY = 0f,
                endY = h * 0.85f
            )
        )

        // Distant Mountain Ridge
        val backRidge = Path().apply {
            moveTo(0f, h * 0.65f)
            var x = 0f
            while (x <= w) {
                val y = h * 0.60f + (sin(x * 0.005f + seedIndex) * 60f) + (sin(x * 0.015f) * 25f)
                lineTo(x, y)
                x += 20f
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = backRidge, color = theme.mountainBack)

        // Foreground Mountain Ridge
        val frontRidge = Path().apply {
            moveTo(0f, h * 0.78f)
            var x = 0f
            while (x <= w) {
                val y = h * 0.75f + (sin(x * 0.008f + seedIndex * 2f) * 75f) + (sin(x * 0.02f) * 35f)
                lineTo(x, y)
                x += 15f
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = frontRidge, color = theme.mountainFront)

        // Subtle stars in upper third
        for (i in 0..25) {
            val starX = (sin(i * 137.5f) * 0.5f + 0.5f) * w
            val starY = (sin(i * 31.7f) * 0.5f + 0.5f) * (h * 0.45f)
            val starAlpha = (sin(i.toFloat() + seedIndex) * 0.3f + 0.7f).coerceIn(0.2f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = starAlpha),
                radius = if (i % 5 == 0) 2.5f else 1.5f,
                center = Offset(starX, starY)
            )
        }
    }
}

private data class ScenicTheme(
    val skyTop: Color,
    val skyMid: Color,
    val skyBottom: Color,
    val mountainBack: Color,
    val mountainFront: Color
)
