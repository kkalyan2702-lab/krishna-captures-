package com.example.model

enum class KenBurnsIntensity(val label: String, val scaleRange: Float) {
    SLOW("Slow & Subtle", 1.08f),
    GENTLE("Gentle Cinematic", 1.15f),
    DEEP("Deep Pan & Zoom", 1.25f)
}

data class ScreensaverSettings(
    // Idle timer in minutes (Minimum 5 mins as required)
    val idleTimerMinutes: Int = 5,
    val idleTimerEnabled: Boolean = true,

    // Slideshow duration in seconds (Minimum 60 seconds = 1 minute as required)
    val slideshowDurationMinutes: Int = 1,
    // Optional fast test preview mode for emulator verification (e.g. 10s or 15s)
    val testPreviewSpeedSeconds: Int? = null,

    // Cinematic Engine options
    val kenBurnsEnabled: Boolean = true,
    val kenBurnsIntensity: KenBurnsIntensity = KenBurnsIntensity.GENTLE,
    val crossfadeDurationMs: Int = 2000,
    val infiniteShuffle: Boolean = true,
    val cropToFill: Boolean = true,
    val showPhotoCaptionOnTransition: Boolean = false
) {
    /**
     * Resolves effective slide duration in milliseconds.
     */
    val effectiveSlideDurationMs: Long
        get() {
            return if (testPreviewSpeedSeconds != null && testPreviewSpeedSeconds > 0) {
                testPreviewSpeedSeconds * 1000L
            } else {
                slideshowDurationMinutes.coerceAtLeast(1) * 60 * 1000L
            }
        }

    val effectiveIdleTimerMs: Long
        get() = idleTimerMinutes.coerceAtLeast(5) * 60 * 1000L
}
