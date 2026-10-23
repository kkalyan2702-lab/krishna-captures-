package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color(0xFF1E1500),
    primaryContainer = Color(0xFF452A00),
    onPrimaryContainer = AmberSecondary,
    secondary = AmberSecondary,
    onSecondary = Color(0xFF1E1500),
    secondaryContainer = Color(0xFF3B2700),
    onSecondaryContainer = Color(0xFFFFDFA6),
    tertiary = AccentCyan,
    onTertiary = Color(0xFF001F2A),
    background = BackgroundBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = Color(0xFF1E293B)
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
