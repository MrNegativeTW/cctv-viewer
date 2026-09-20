package com.txwstudio.app.cctvviewer.ui.component

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.MaterialTheme
import com.txwstudio.app.cctvviewer.data.Camera
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
