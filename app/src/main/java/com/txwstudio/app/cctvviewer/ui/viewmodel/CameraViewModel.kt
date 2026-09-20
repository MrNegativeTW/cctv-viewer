package com.txwstudio.app.cctvviewer.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.txwstudio.app.cctvviewer.data.Camera
import com.txwstudio.app.cctvviewer.data.CameraRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CameraViewModel(
    private val repository: CameraRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(
        CameraUiState(
            activeIndex = savedStateHandle[ACTIVE_INDEX] ?: 0,
            focusedIndex = savedStateHandle[FOCUSED_INDEX] ?: 0,
            cameraBarVisible = savedStateHandle[BAR_VISIBLE] ?: true,
        )
    )
    val uiState = mutableUiState.asStateFlow()
    private var isForeground = false
    private var idleJob: Job? = null

    init {
        viewModelScope.launch {
            repository.cameras.catch {
                updateState { it.copy(configurationError = true) }
            }.collect { cameras ->
                if (cameras.isEmpty()) {
                    updateState { it.copy(configurationError = true) }
                    return@collect
                }
                val previous = uiState.value
                val active = previous.activeIndex.coerceIn(cameras.indices)
                val changedUrl = previous.activeCamera?.let {
                    it.url.trim() != cameras[active].url.trim()
                } ?: false
                updateState {
                    it.copy(
                        cameras = cameras.toList(),
                        activeIndex = active,
                        focusedIndex = it.focusedIndex.coerceIn(0..cameras.size),
                        reloadVersion = it.reloadVersion + if (changedUrl) 1 else 0,
                        configurationError = false,
                    )
                }
                if (previous.cameras.isEmpty()) restartIdleTimer()
            }
        }
    }

    suspend fun readCameraSettings(): List<Camera> = repository.cameras.first()
    suspend fun saveCameraSettings(cameras: List<Camera>) = repository.saveCameras(cameras)

    fun onAction(action: CameraAction) {
        when (action) {
            CameraAction.UserInteraction -> restartIdleTimer()
            CameraAction.ShowCameraBar -> {
                updateState { it.copy(cameraBarVisible = true) }
                restartIdleTimer()
            }
            CameraAction.HideCameraBar -> {
                updateState { it.copy(cameraBarVisible = false) }
                restartIdleTimer()
            }
            CameraAction.FocusPreviousCamera -> moveFocus(-1)
            CameraAction.FocusNextCamera -> moveFocus(1)
            is CameraAction.CameraFocused -> {
                if (action.index in 0..uiState.value.cameras.size && uiState.value.cameraBarVisible) {
                    updateState { it.copy(focusedIndex = action.index) }
                }
            }
            is CameraAction.ConfirmCamera -> {
                if (action.index in uiState.value.cameras.indices && uiState.value.cameraBarVisible) {
                    updateState { it.copy(activeIndex = action.index, focusedIndex = action.index) }
                    restartIdleTimer()
                }
            }
            CameraAction.ReloadCamera -> {
                updateState { it.copy(reloadVersion = it.reloadVersion + 1) }
                restartIdleTimer()
            }
            CameraAction.OpenSettings -> {
                updateState { it.copy(settingsVisible = true) }
                restartIdleTimer()
            }
            CameraAction.CloseSettings -> {
                updateState { it.copy(settingsVisible = false) }
                restartIdleTimer()
            }
        }
    }

    fun onForegroundChanged(foreground: Boolean) {
        if (isForeground == foreground) return
        isForeground = foreground
        restartIdleTimer()
    }

    private fun moveFocus(offset: Int) {
        if (!uiState.value.cameraBarVisible || uiState.value.cameras.isEmpty()) return
        // 最後一個項目為「手機設定」。
        updateState { it.copy(focusedIndex = (it.focusedIndex + offset).coerceIn(0..it.cameras.size)) }
        restartIdleTimer()
    }

    private fun restartIdleTimer() {
        idleJob?.cancel()
        idleJob = null
        val state = uiState.value
        if (!isForeground || !state.cameraBarVisible || state.settingsVisible || state.cameras.isEmpty()) return
        idleJob = viewModelScope.launch {
            delay(CAMERA_BAR_IDLE_TIMEOUT_MS)
            updateState { it.copy(cameraBarVisible = false) }
        }
    }

    private fun updateState(transform: (CameraUiState) -> CameraUiState) {
        val state = transform(mutableUiState.value)
        savedStateHandle[ACTIVE_INDEX] = state.activeIndex
        savedStateHandle[FOCUSED_INDEX] = state.focusedIndex
        savedStateHandle[BAR_VISIBLE] = state.cameraBarVisible
        mutableUiState.value = state
    }

    companion object {
        private const val ACTIVE_INDEX = "active_camera_index"
        private const val FOCUSED_INDEX = "focused_camera_index"
        private const val BAR_VISIBLE = "camera_bar_visible"
        private const val CAMERA_BAR_IDLE_TIMEOUT_MS = 10_000L

        fun factory(repository: CameraRepository) = viewModelFactory {
            initializer { CameraViewModel(repository, createSavedStateHandle()) }
        }
    }
}
