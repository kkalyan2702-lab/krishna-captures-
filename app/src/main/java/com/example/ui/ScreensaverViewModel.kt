package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PhotoFolderHelper
import com.example.data.SettingsRepository
import com.example.model.KenBurnsIntensity
import com.example.model.PhotoItem
import com.example.model.SamplePhotos
import com.example.model.ScreensaverSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    DASHBOARD,
    SCREENSAVER
}

data class ScreensaverUiState(
    val currentScreen: AppScreen = AppScreen.DASHBOARD,
    val settings: ScreensaverSettings = ScreensaverSettings(),
    val photos: List<PhotoItem> = SamplePhotos.curatedWallpapers,
    val currentPhotoIndex: Int = 0,
    val nextPhotoIndex: Int = 1,
    val isCrossfading: Boolean = false,
    val isPaused: Boolean = false,
    val controlsVisible: Boolean = false,
    val idleRemainingSeconds: Long = 300L,
    val isLoadingFolder: Boolean = false,
    val folderCount: Int = 0,
    val notificationMessage: String? = null
)

class ScreensaverViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ScreensaverUiState())
    val uiState: StateFlow<ScreensaverUiState> = _uiState.asStateFlow()

    private var slideshowJob: Job? = null
    private var idleCheckJob: Job? = null
    private var controlsAutoHideJob: Job? = null
    private var lastInteractionTime = System.currentTimeMillis()

    init {
        // Observe settings
        viewModelScope.launch {
            repository.settingsFlow.collectLatest { newSettings ->
                _uiState.update { it.copy(settings = newSettings) }
            }
        }

        // Observe custom photos & folders
        viewModelScope.launch {
            repository.customPhotosFlow.collectLatest { customList ->
                val combined = if (customList.isEmpty()) {
                    SamplePhotos.curatedWallpapers
                } else {
                    SamplePhotos.curatedWallpapers + customList
                }
                _uiState.update { state ->
                    val validIndex = if (state.currentPhotoIndex >= combined.size) 0 else state.currentPhotoIndex
                    val nextIdx = if (combined.size > 1) (validIndex + 1) % combined.size else validIndex
                    state.copy(
                        photos = combined,
                        currentPhotoIndex = validIndex,
                        nextPhotoIndex = nextIdx
                    )
                }
            }
        }

        // Start idle detector for dashboard
        startIdleDetector()
    }

    fun onUserInteraction() {
        lastInteractionTime = System.currentTimeMillis()
        if (_uiState.value.currentScreen == AppScreen.SCREENSAVER) {
            // Show controls and schedule auto-hide
            _uiState.update { it.copy(controlsVisible = true) }
            scheduleControlsAutoHide()
        }
    }

    private fun scheduleControlsAutoHide() {
        controlsAutoHideJob?.cancel()
        controlsAutoHideJob = viewModelScope.launch {
            delay(4000) // Auto-hide after 4 seconds of stillness
            _uiState.update { it.copy(controlsVisible = false) }
        }
    }

    fun toggleControlsVisibility() {
        onUserInteraction()
        _uiState.update { it.copy(controlsVisible = !it.controlsVisible) }
        if (_uiState.value.controlsVisible) {
            scheduleControlsAutoHide()
        }
    }

    private fun startIdleDetector() {
        idleCheckJob?.cancel()
        idleCheckJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_uiState.value.currentScreen == AppScreen.DASHBOARD && _uiState.value.settings.idleTimerEnabled) {
                    val idleThresholdMs = _uiState.value.settings.effectiveIdleTimerMs
                    val elapsed = System.currentTimeMillis() - lastInteractionTime
                    val remainingMs = (idleThresholdMs - elapsed).coerceAtLeast(0)
                    _uiState.update { it.copy(idleRemainingSeconds = remainingMs / 1000) }

                    if (elapsed >= idleThresholdMs) {
                        // Automatically launch screensaver!
                        startScreensaver()
                    }
                }
            }
        }
    }

    fun startScreensaverAt(index: Int) {
        lastInteractionTime = System.currentTimeMillis()
        val photos = _uiState.value.photos
        if (photos.isEmpty()) return
        val validIndex = index.coerceIn(0, photos.size - 1)
        val nextIdx = if (photos.size > 1) (validIndex + 1) % photos.size else validIndex

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.SCREENSAVER,
                currentPhotoIndex = validIndex,
                nextPhotoIndex = nextIdx,
                controlsVisible = true,
                isPaused = false
            )
        }
        scheduleControlsAutoHide()
        startSlideshowLoop()
    }

    fun startScreensaver() {
        lastInteractionTime = System.currentTimeMillis()
        val photos = _uiState.value.photos
        if (photos.isEmpty()) return

        // Prepare shuffle if enabled
        val startingIndex = if (_uiState.value.settings.infiniteShuffle) {
            photos.indices.random()
        } else {
            0
        }
        val nextIdx = if (photos.size > 1) (startingIndex + 1) % photos.size else startingIndex

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.SCREENSAVER,
                currentPhotoIndex = startingIndex,
                nextPhotoIndex = nextIdx,
                controlsVisible = true, // Briefly visible on launch, then fades out
                isPaused = false
            )
        }
        scheduleControlsAutoHide()
        startSlideshowLoop()
    }

    fun exitToDashboard() {
        slideshowJob?.cancel()
        controlsAutoHideJob?.cancel()
        lastInteractionTime = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.DASHBOARD,
                controlsVisible = false
            )
        }
    }

    private fun startSlideshowLoop() {
        slideshowJob?.cancel()
        slideshowJob = viewModelScope.launch {
            while (isActive) {
                val state = _uiState.value
                val slideDuration = state.settings.effectiveSlideDurationMs
                val crossfadeDuration = state.settings.crossfadeDurationMs.toLong()

                // Display current photo for duration
                val waitDuration = (slideDuration - crossfadeDuration).coerceAtLeast(1000L)
                delay(waitDuration)

                // If paused, wait until resumed
                while (_uiState.value.isPaused && isActive) {
                    delay(500)
                }

                // Trigger smooth crossfade transition
                _uiState.update { it.copy(isCrossfading = true) }
                delay(crossfadeDuration)

                // Transition finished: advance photo index
                val currentPhotos = _uiState.value.photos
                if (currentPhotos.isNotEmpty()) {
                    val next = _uiState.value.nextPhotoIndex
                    val nextNext = if (state.settings.infiniteShuffle) {
                        val available = currentPhotos.indices.filter { it != next }
                        if (available.isNotEmpty()) available.random() else next
                    } else {
                        (next + 1) % currentPhotos.size
                    }

                    _uiState.update {
                        it.copy(
                            currentPhotoIndex = next,
                            nextPhotoIndex = nextNext,
                            isCrossfading = false
                        )
                    }
                }
            }
        }
    }

    fun nextPhoto() {
        onUserInteraction()
        val photos = _uiState.value.photos
        if (photos.isEmpty()) return
        val current = _uiState.value.currentPhotoIndex
        val next = if (_uiState.value.settings.infiniteShuffle) {
            val pool = photos.indices.filter { it != current }
            if (pool.isNotEmpty()) pool.random() else (current + 1) % photos.size
        } else {
            (current + 1) % photos.size
        }
        val nextNext = (next + 1) % photos.size

        _uiState.update {
            it.copy(
                currentPhotoIndex = next,
                nextPhotoIndex = nextNext,
                isCrossfading = false
            )
        }
        // Restart slide timer loop
        if (!_uiState.value.isPaused) {
            startSlideshowLoop()
        }
    }

    fun previousPhoto() {
        onUserInteraction()
        val photos = _uiState.value.photos
        if (photos.isEmpty()) return
        val current = _uiState.value.currentPhotoIndex
        val prev = if (current - 1 < 0) photos.size - 1 else current - 1
        val next = (prev + 1) % photos.size

        _uiState.update {
            it.copy(
                currentPhotoIndex = prev,
                nextPhotoIndex = next,
                isCrossfading = false
            )
        }
        if (!_uiState.value.isPaused) {
            startSlideshowLoop()
        }
    }

    fun togglePlayPause() {
        onUserInteraction()
        val newPaused = !_uiState.value.isPaused
        _uiState.update { it.copy(isPaused = newPaused) }
        if (!newPaused) {
            startSlideshowLoop()
        }
    }

    fun addPickedPhotos(uris: List<Uri>) {
        viewModelScope.launch {
            val newItems = PhotoFolderHelper.parsePickedPhotos(getApplication(), uris)
            if (newItems.isNotEmpty()) {
                val currentCustom = repository.customPhotosFlow
                // Collect one shot
                var existing = emptyList<PhotoItem>()
                val job = launch {
                    repository.customPhotosFlow.collect { existing = it }
                }
                delay(100)
                job.cancel()
                val updated = existing + newItems
                repository.saveCustomPhotos(updated)
                showNotification("Added ${newItems.size} photo${if (newItems.size > 1) "s" else ""}")
            }
        }
    }

    fun addFolder(treeUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFolder = true) }
            val newItems = PhotoFolderHelper.queryPhotosFromFolder(getApplication(), treeUri)
            var existingCustom = emptyList<PhotoItem>()
            val job = launch {
                repository.customPhotosFlow.collect { existingCustom = it }
            }
            delay(100)
            job.cancel()

            var existingFolders = emptyList<String>()
            val jobF = launch {
                repository.customFoldersFlow.collect { existingFolders = it }
            }
            delay(100)
            jobF.cancel()

            repository.saveCustomFolders(existingFolders + treeUri.toString())
            repository.saveCustomPhotos(existingCustom + newItems)
            _uiState.update {
                it.copy(
                    isLoadingFolder = false,
                    folderCount = existingFolders.size + 1
                )
            }
            showNotification("Loaded ${newItems.size} photos from folder")
        }
    }

    fun removePhoto(photo: PhotoItem) {
        viewModelScope.launch {
            var existing = emptyList<PhotoItem>()
            val job = launch {
                repository.customPhotosFlow.collect { existing = it }
            }
            delay(100)
            job.cancel()
            val filtered = existing.filter { it.id != photo.id }
            repository.saveCustomPhotos(filtered)
            showNotification("Removed ${photo.title}")
        }
    }

    fun resetToCuratedPhotos() {
        viewModelScope.launch {
            repository.saveCustomPhotos(emptyList())
            repository.saveCustomFolders(emptyList())
            _uiState.update {
                it.copy(
                    photos = SamplePhotos.curatedWallpapers,
                    currentPhotoIndex = 0,
                    nextPhotoIndex = 1,
                    folderCount = 0
                )
            }
            showNotification("Reset to curated photography gallery")
        }
    }

    fun setIdleTimerMinutes(minutes: Int) {
        viewModelScope.launch {
            val clamped = minutes.coerceAtLeast(5)
            repository.updateSettings { it.copy(idleTimerMinutes = clamped) }
            lastInteractionTime = System.currentTimeMillis()
        }
    }

    fun setIdleTimerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(idleTimerEnabled = enabled) }
        }
    }

    fun setSlideshowDurationMinutes(minutes: Int) {
        viewModelScope.launch {
            val clamped = minutes.coerceAtLeast(1)
            repository.updateSettings { it.copy(slideshowDurationMinutes = clamped, testPreviewSpeedSeconds = null) }
        }
    }

    fun setTestPreviewSpeedSeconds(seconds: Int?) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(testPreviewSpeedSeconds = seconds) }
            showNotification(if (seconds != null) "Preview interval set to ${seconds}s" else "Standard slideshow interval restored")
        }
    }

    fun setKenBurnsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(kenBurnsEnabled = enabled) }
        }
    }

    fun setKenBurnsIntensity(intensity: KenBurnsIntensity) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(kenBurnsIntensity = intensity) }
        }
    }

    fun setCrossfadeDurationMs(ms: Int) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(crossfadeDurationMs = ms) }
        }
    }

    fun setInfiniteShuffle(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(infiniteShuffle = enabled) }
        }
    }

    fun setCropToFill(crop: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(cropToFill = crop) }
        }
    }

    fun setShowCaption(show: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(showPhotoCaptionOnTransition = show) }
        }
    }

    private fun showNotification(msg: String) {
        _uiState.update { it.copy(notificationMessage = msg) }
        viewModelScope.launch {
            delay(3000)
            _uiState.update { if (it.notificationMessage == msg) it.copy(notificationMessage = null) else it }
        }
    }
}
