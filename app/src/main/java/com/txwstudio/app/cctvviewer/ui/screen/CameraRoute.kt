package com.txwstudio.app.cctvviewer.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.txwstudio.app.cctvviewer.ui.viewmodel.CameraViewModel
import com.txwstudio.app.cctvviewer.ui.viewmodel.CameraAction
import com.txwstudio.app.cctvviewer.ui.component.CameraSettingsDialog

@Composable
fun CameraRoute(viewModel: CameraViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(viewModel) {
        viewModel.onForegroundChanged(true)
        onPauseOrDispose { viewModel.onForegroundChanged(false) }
    }

    CameraScreen(state = state, onAction = viewModel::onAction)
    if (state.settingsVisible) {
        CameraSettingsDialog(
            readCameras = viewModel::readCameraSettings,
            saveCameras = viewModel::saveCameraSettings,
            onDismiss = { viewModel.onAction(CameraAction.CloseSettings) },
        )
    }
}
