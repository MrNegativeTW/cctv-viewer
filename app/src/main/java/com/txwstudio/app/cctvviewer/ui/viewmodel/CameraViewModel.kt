package com.txwstudio.app.cctvviewer.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.txwstudio.app.cctvviewer.data.CameraRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(
    repository: CameraRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val cameras = repository.getCameras().toList().also {
        require(it.isNotEmpty()) { "At least one camera must be configured." }
    }
    private val initialActiveIndex = savedStateHandle.get<Int>(ACTIVE_INDEX)
        ?.coerceIn(cameras.indices) ?: 0
    private val mutableUiState = MutableStateFlow(
        CameraUiState(
            cameras = cameras,
            activeIndex = initialActiveIndex,
            focusedIndex = savedStateHandle.get<Int>(FOCUSED_INDEX)
                ?.coerceIn(cameras.indices) ?: initialActiveIndex,
            cameraBarVisible = savedStateHandle[BAR_VISIBLE] ?: true,
        )
    )
    val uiState = mutableUiState.asStateFlow()

    private var isForeground = false
    private var idleJob: Job? = null

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
                if (action.index in cameras.indices && uiState.value.cameraBarVisible) {
                    updateState { it.copy(focusedIndex = action.index) }
                }
            }
            is CameraAction.ConfirmCamera -> {
                if (action.index in cameras.indices && uiState.value.cameraBarVisible) {
                    updateState { it.copy(activeIndex = action.index, focusedIndex = action.index) }
                    restartIdleTimer()
                }
            }
            CameraAction.ReloadCamera -> {
                updateState { it.copy(reloadVersion = it.reloadVersion + 1) }
                restartIdleTimer()
            }
        }
    }

    // 接收前景狀態，不持有 Activity、Lifecycle 或 WebView。
    fun onForegroundChanged(foreground: Boolean) {
        if (isForeground == foreground) return
        isForeground = foreground
        restartIdleTimer()
    }

    private fun moveFocus(offset: Int) {
        if (!uiState.value.cameraBarVisible) return
        updateState { it.copy(focusedIndex = (it.focusedIndex + offset).coerceIn(cameras.indices)) }
        restartIdleTimer()
    }

    private fun restartIdleTimer() {
        idleJob?.cancel()
        idleJob = null
        if (!isForeground || !uiState.value.cameraBarVisible) return
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
