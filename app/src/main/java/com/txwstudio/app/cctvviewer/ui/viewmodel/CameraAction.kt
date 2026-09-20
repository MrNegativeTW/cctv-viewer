package com.txwstudio.app.cctvviewer.ui.viewmodel

sealed interface CameraAction {
    data object UserInteraction : CameraAction
    data object ShowCameraBar : CameraAction
    data object HideCameraBar : CameraAction
    data object FocusPreviousCamera : CameraAction
    data object FocusNextCamera : CameraAction
    data class CameraFocused(val index: Int) : CameraAction
    data class ConfirmCamera(val index: Int) : CameraAction
    data object ReloadCamera : CameraAction
}
