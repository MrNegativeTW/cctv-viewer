package com.txwstudio.app.cctvviewer.ui.viewmodel

import com.txwstudio.app.cctvviewer.data.Camera

data class CameraUiState(
    val cameras: List<Camera> = emptyList(),
    val activeIndex: Int = 0,
    val focusedIndex: Int = 0,
    val cameraBarVisible: Boolean = true,
    val reloadVersion: Int = 0,
    val settingsVisible: Boolean = false,
    val configurationError: Boolean = false,
) {
    val activeCamera: Camera? get() = cameras.getOrNull(activeIndex)
}
