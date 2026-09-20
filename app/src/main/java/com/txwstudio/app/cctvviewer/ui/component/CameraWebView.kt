package com.txwstudio.app.cctvviewer.ui.component

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.txwstudio.app.cctvviewer.data.Camera
import com.txwstudio.app.cctvviewer.R
import com.txwstudio.app.cctvviewer.ui.preview.CameraPreviewData
import com.txwstudio.app.cctvviewer.ui.theme.CCTVViewerTheme
import com.txwstudio.app.cctvviewer.ui.web.CameraPageError
import com.txwstudio.app.cctvviewer.ui.web.CameraPageState
import com.txwstudio.app.cctvviewer.ui.web.CameraPageView

@Composable
fun CameraWebView(
    camera: Camera,
    onReload: () -> Unit,
    onLoadErrorChanged: (Boolean) -> Unit,
    retryFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        // Layoutlib 無法提供完整的 WebView；預覽時不建立原生 View 或連線。
        CameraWebViewPlaceholder(camera.name, modifier)
        return
    }

    val url = camera.url.trim()
    val supported = remember(url) { CameraPageView.supportsUrl(url) }
    var pageState by remember {
        mutableStateOf(CameraPageState(error = if (supported) null else CameraPageError.NOT_CONFIGURED))
    }
    var webView by remember { mutableStateOf<CameraPageView?>(null) }
    var pageGeneration by remember { mutableIntStateOf(0) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    SideEffect { onLoadErrorChanged(pageState.error != null) }

    Box(modifier.background(MaterialTheme.colorScheme.background)) {
        if (supported) {
            key(pageGeneration) {
                AndroidView(
                    factory = { context ->
                        CameraPageView(context, url) { pageState = it }.also { webView = it }
                    },
                    // 限制載入時的網頁背景繪製，避免覆蓋下方攝影機清單。
                    modifier = Modifier.fillMaxSize().clipToBounds().focusProperties { canFocus = false },
                    update = { it.visibility = if (pageState.error == null) View.VISIBLE else View.INVISIBLE },
                    onRelease = CameraPageView::release,
                )
            }
        }
        CameraPageOverlay(pageState, onReload, retryFocusRequester)
    }

    // 原生 View 由介面層持有；即使背景中重組暫停，也必須立即釋放網頁。
    val observedView = webView
    DisposableEffect(lifecycle, observedView) {
        val view = observedView
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> if (view?.isReleased == true) {
                    pageState = CameraPageState()
                    pageGeneration++
                }
                Lifecycle.Event.ON_RESUME -> if (view?.isReleased == false) view.onResume()
                Lifecycle.Event.ON_PAUSE -> if (view?.isReleased == false) view.onPause()
                Lifecycle.Event.ON_STOP -> view?.release()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            view?.release()
        }
    }
}

@Composable
private fun CameraWebViewPlaceholder(cameraName: String, modifier: Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(cameraName, style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(R.string.webview_preview_caption), style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(name = "電視・攝影機網頁區域", widthDp = 960, heightDp = 424, locale = "zh-rTW")
@Composable
private fun CameraWebViewPreview() {
    CCTVViewerTheme {
        CameraWebView(
            camera = CameraPreviewData.cameras().first(),
            onReload = {},
            onLoadErrorChanged = {},
            retryFocusRequester = remember { FocusRequester() },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
