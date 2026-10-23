package com.example

import com.example.model.KenBurnsIntensity
import com.example.model.SamplePhotos
import com.example.model.ScreensaverSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun defaultSettings_meetMinimumRequirements() {
        val settings = ScreensaverSettings()
        // Minimum idle timer is 5 minutes
        assertTrue("Idle timer should be >= 5 minutes", settings.idleTimerMinutes >= 5)
        // Minimum slideshow duration is 1 minute
        assertTrue("Slideshow timer should be >= 1 minute", settings.slideshowDurationMinutes >= 1)
        // Default should be 1 min (60,000ms)
        assertEquals(60_000L, settings.effectiveSlideDurationMs)
        // Default idle is 5 min (300,000ms)
        assertEquals(300_000L, settings.effectiveIdleTimerMs)
        // Ken Burns enabled by default
        assertTrue(settings.kenBurnsEnabled)
        // Infinite shuffle enabled by default
        assertTrue(settings.infiniteShuffle)
    }

    @Test
    fun curatedWallpapers_areLoaded() {
        val wallpapers = SamplePhotos.curatedWallpapers
        assertTrue("Curated photos should not be empty", wallpapers.isNotEmpty())
        assertTrue("At least 6 wallpapers curated", wallpapers.size >= 6)
    }

    @Test
    fun testPreviewSpeed_overridesSlideDurationWhenSet() {
        val settings = ScreensaverSettings(
            slideshowDurationMinutes = 2,
            testPreviewSpeedSeconds = 10
        )
        assertEquals(10_000L, settings.effectiveSlideDurationMs)
    }
}
