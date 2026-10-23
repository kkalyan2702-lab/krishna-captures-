package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.KenBurnsIntensity
import com.example.model.PhotoItem
import com.example.model.PhotoSource
import com.example.model.ScreensaverSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "krishna_captures_prefs")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val IDLE_TIMER_MINUTES = intPreferencesKey("idle_timer_minutes")
        val IDLE_TIMER_ENABLED = booleanPreferencesKey("idle_timer_enabled")
        val SLIDESHOW_DURATION_MINUTES = intPreferencesKey("slideshow_duration_minutes")
        val TEST_PREVIEW_SPEED_SECONDS = intPreferencesKey("test_preview_speed_seconds")
        val KEN_BURNS_ENABLED = booleanPreferencesKey("ken_burns_enabled")
        val KEN_BURNS_INTENSITY = stringPreferencesKey("ken_burns_intensity")
        val CROSSFADE_DURATION_MS = intPreferencesKey("crossfade_duration_ms")
        val INFINITE_SHUFFLE = booleanPreferencesKey("infinite_shuffle")
        val CROP_TO_FILL = booleanPreferencesKey("crop_to_fill")
        val SHOW_CAPTION = booleanPreferencesKey("show_caption")
        val CUSTOM_PHOTOS_JSON = stringPreferencesKey("custom_photos_json")
        val CUSTOM_FOLDERS_JSON = stringPreferencesKey("custom_folders_json")
    }

    val settingsFlow: Flow<ScreensaverSettings> = context.dataStore.data.map { prefs ->
        val idleMins = prefs[PreferencesKeys.IDLE_TIMER_MINUTES] ?: 5
        val idleEnabled = prefs[PreferencesKeys.IDLE_TIMER_ENABLED] ?: true
        val slideMins = prefs[PreferencesKeys.SLIDESHOW_DURATION_MINUTES] ?: 1
        val testSpeed = prefs[PreferencesKeys.TEST_PREVIEW_SPEED_SECONDS]?.takeIf { it > 0 }
        val kenBurns = prefs[PreferencesKeys.KEN_BURNS_ENABLED] ?: true
        val intensityStr = prefs[PreferencesKeys.KEN_BURNS_INTENSITY] ?: KenBurnsIntensity.GENTLE.name
        val intensity = try {
            KenBurnsIntensity.valueOf(intensityStr)
        } catch (_: Exception) {
            KenBurnsIntensity.GENTLE
        }
        val crossfade = prefs[PreferencesKeys.CROSSFADE_DURATION_MS] ?: 2000
        val shuffle = prefs[PreferencesKeys.INFINITE_SHUFFLE] ?: true
        val crop = prefs[PreferencesKeys.CROP_TO_FILL] ?: true
        val caption = prefs[PreferencesKeys.SHOW_CAPTION] ?: false

        ScreensaverSettings(
            idleTimerMinutes = idleMins.coerceAtLeast(5),
            idleTimerEnabled = idleEnabled,
            slideshowDurationMinutes = slideMins.coerceAtLeast(1),
            testPreviewSpeedSeconds = testSpeed,
            kenBurnsEnabled = kenBurns,
            kenBurnsIntensity = intensity,
            crossfadeDurationMs = crossfade,
            infiniteShuffle = shuffle,
            cropToFill = crop,
            showPhotoCaptionOnTransition = caption
        )
    }

    suspend fun updateSettings(update: (ScreensaverSettings) -> ScreensaverSettings) {
        context.dataStore.edit { prefs ->
            val currentIdle = (prefs[PreferencesKeys.IDLE_TIMER_MINUTES] ?: 5).coerceAtLeast(5)
            val currentSlide = (prefs[PreferencesKeys.SLIDESHOW_DURATION_MINUTES] ?: 1).coerceAtLeast(1)
            val currentTest = prefs[PreferencesKeys.TEST_PREVIEW_SPEED_SECONDS]?.takeIf { it > 0 }
            val currentKen = prefs[PreferencesKeys.KEN_BURNS_ENABLED] ?: true
            val currentIntensity = try {
                KenBurnsIntensity.valueOf(prefs[PreferencesKeys.KEN_BURNS_INTENSITY] ?: KenBurnsIntensity.GENTLE.name)
            } catch (_: Exception) {
                KenBurnsIntensity.GENTLE
            }
            val currentCrossfade = prefs[PreferencesKeys.CROSSFADE_DURATION_MS] ?: 2000
            val currentShuffle = prefs[PreferencesKeys.INFINITE_SHUFFLE] ?: true
            val currentCrop = prefs[PreferencesKeys.CROP_TO_FILL] ?: true
            val currentCaption = prefs[PreferencesKeys.SHOW_CAPTION] ?: false
            val currentIdleEnabled = prefs[PreferencesKeys.IDLE_TIMER_ENABLED] ?: true

            val current = ScreensaverSettings(
                idleTimerMinutes = currentIdle,
                idleTimerEnabled = currentIdleEnabled,
                slideshowDurationMinutes = currentSlide,
                testPreviewSpeedSeconds = currentTest,
                kenBurnsEnabled = currentKen,
                kenBurnsIntensity = currentIntensity,
                crossfadeDurationMs = currentCrossfade,
                infiniteShuffle = currentShuffle,
                cropToFill = currentCrop,
                showPhotoCaptionOnTransition = currentCaption
            )

            val updated = update(current)

            prefs[PreferencesKeys.IDLE_TIMER_MINUTES] = updated.idleTimerMinutes.coerceAtLeast(5)
            prefs[PreferencesKeys.IDLE_TIMER_ENABLED] = updated.idleTimerEnabled
            prefs[PreferencesKeys.SLIDESHOW_DURATION_MINUTES] = updated.slideshowDurationMinutes.coerceAtLeast(1)
            if (updated.testPreviewSpeedSeconds != null) {
                prefs[PreferencesKeys.TEST_PREVIEW_SPEED_SECONDS] = updated.testPreviewSpeedSeconds
            } else {
                prefs.remove(PreferencesKeys.TEST_PREVIEW_SPEED_SECONDS)
            }
            prefs[PreferencesKeys.KEN_BURNS_ENABLED] = updated.kenBurnsEnabled
            prefs[PreferencesKeys.KEN_BURNS_INTENSITY] = updated.kenBurnsIntensity.name
            prefs[PreferencesKeys.CROSSFADE_DURATION_MS] = updated.crossfadeDurationMs
            prefs[PreferencesKeys.INFINITE_SHUFFLE] = updated.infiniteShuffle
            prefs[PreferencesKeys.CROP_TO_FILL] = updated.cropToFill
            prefs[PreferencesKeys.SHOW_CAPTION] = updated.showPhotoCaptionOnTransition
        }
    }

    val customPhotosFlow: Flow<List<PhotoItem>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[PreferencesKeys.CUSTOM_PHOTOS_JSON] ?: "[]"
        deserializePhotos(jsonStr)
    }

    suspend fun saveCustomPhotos(photos: List<PhotoItem>) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CUSTOM_PHOTOS_JSON] = serializePhotos(photos)
        }
    }

    val customFoldersFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[PreferencesKeys.CUSTOM_FOLDERS_JSON] ?: "[]"
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {}
        list
    }

    suspend fun saveCustomFolders(folders: List<String>) {
        context.dataStore.edit { prefs ->
            val array = JSONArray()
            folders.forEach { array.put(it) }
            prefs[PreferencesKeys.CUSTOM_FOLDERS_JSON] = array.toString()
        }
    }

    private fun serializePhotos(photos: List<PhotoItem>): String {
        val array = JSONArray()
        for (item in photos) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("uriString", item.uriString)
            obj.put("title", item.title)
            obj.put("subtitle", item.subtitle)
            obj.put("source", item.source.name)
            obj.put("dateAdded", item.dateAdded)
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializePhotos(jsonStr: String): List<PhotoItem> {
        val result = mutableListOf<PhotoItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val src = try {
                    PhotoSource.valueOf(obj.optString("source", PhotoSource.USER_FILE.name))
                } catch (_: Exception) {
                    PhotoSource.USER_FILE
                }
                result.add(
                    PhotoItem(
                        id = obj.getString("id"),
                        uriString = obj.getString("uriString"),
                        title = obj.optString("title", "Imported Photo"),
                        subtitle = obj.optString("subtitle", "Local Storage"),
                        source = src,
                        dateAdded = obj.optLong("dateAdded", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return result
    }
}
