package com.txwstudio.app.cctvviewer.ui.screen

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.MaterialTheme
import com.txwstudio.app.cctvviewer.ui.component.CameraSelectionBar
import com.txwstudio.app.cctvviewer.ui.component.CameraWebView
import com.txwstudio.app.cctvviewer.ui.preview.CameraPreviewData
import com.txwstudio.app.cctvviewer.ui.theme.CCTVViewerTheme
import com.txwstudio.app.cctvviewer.ui.viewmodel.CameraAction
import com.txwstudio.app.cctvviewer.ui.viewmodel.CameraUiState

@Composable
fun CameraScreen(state: CameraUiState, onAction: (CameraAction) -> Unit) {
    val viewerFocus = remember { FocusRequester() }
    val retryFocus = remember { FocusRequester() }
    val barTransition = remember { MutableTransitionState(state.cameraBarVisible) }
    barTransition.targetState = state.cameraBarVisible
    val barReady = barTransition.isIdle && barTransition.currentState
    var hasLoadError by remember(state.activeIndex, state.reloadVersion) { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .focusRequester(viewerFocus)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown || event.type == KeyEventType.KeyUp) {
                    onAction(CameraAction.UserInteraction)
                }
                when (event.key) {
                    Key.DirectionUp, Key.DirectionDown -> {
                        if (event.type == KeyEventType.KeyDown) {
                            onAction(
                                if (event.key == Key.DirectionDown) CameraAction.ShowCameraBar
                                else CameraAction.HideCameraBar
                            )
                        }
                        true
                    }
                    Key.DirectionLeft, Key.DirectionRight -> {
                        if (barReady && event.type == KeyEventType.KeyDown) {
                            onAction(
                                if (event.key == Key.DirectionLeft) CameraAction.FocusPreviousCamera
                                else CameraAction.FocusNextCamera
                            )
                        }
                        true
                    }
                    else -> false
                }
            }
            .focusable(enabled = !barReady),
    ) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            key(state.activeIndex, state.reloadVersion) {
                CameraWebView(
                    camera = state.activeCamera,
                    onReload = { onAction(CameraAction.ReloadCamera) },
                    onLoadErrorChanged = { hasLoadError = it },
                    retryFocusRequester = retryFocus,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        CameraSelectionBar(
            cameras = state.cameras,
            activeIndex = state.activeIndex,
            focusedIndex = state.focusedIndex,
            transition = barTransition,
            onCameraFocused = { onAction(CameraAction.CameraFocused(it)) },
            onCameraConfirmed = { onAction(CameraAction.ConfirmCamera(it)) },
        )
    }

    LaunchedEffect(barReady, hasLoadError) {
        if (!barReady) {
            if (hasLoadError) retryFocus.requestFocus() else viewerFocus.requestFocus()
        }
    }
}

@Preview(name = "電視・攝影機清單", widthDp = 960, heightDp = 540, locale = "zh-rTW")
@Composable
private fun CameraScreenPreview() {
    CCTVViewerTheme {
        CameraScreen(state = CameraUiState(cameras = CameraPreviewData.cameras), onAction = {})
    }
}

@Preview(name = "電視・全畫面", widthDp = 960, heightDp = 540, locale = "zh-rTW")
@Composable
private fun CameraFullscreenPreview() {
    CCTVViewerTheme {
        CameraScreen(
            state = CameraUiState(cameras = CameraPreviewData.cameras, cameraBarVisible = false),
            onAction = {},
        )
    }
}
