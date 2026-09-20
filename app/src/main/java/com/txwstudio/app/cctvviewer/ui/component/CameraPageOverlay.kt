package com.txwstudio.app.cctvviewer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.txwstudio.app.cctvviewer.R
import com.txwstudio.app.cctvviewer.ui.theme.CCTVViewerTheme
import com.txwstudio.app.cctvviewer.ui.web.CameraPageError
import com.txwstudio.app.cctvviewer.ui.web.CameraPageState

@Composable
internal fun BoxScope.CameraPageOverlay(
    state: CameraPageState,
    onReload: () -> Unit,
    retryFocusRequester: FocusRequester,
) {
    val colors = MaterialTheme.colorScheme
    state.error?.let { error ->
        val message = when (error) {
            CameraPageError.NOT_CONFIGURED -> R.string.camera_not_configured
            CameraPageError.LOAD_FAILED -> R.string.camera_load_failed
            CameraPageError.CERTIFICATE_ERROR -> R.string.camera_certificate_error
        }
        Column(
            modifier = Modifier.fillMaxSize().background(colors.background).padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(message),
                modifier = Modifier.widthIn(max = 600.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            CameraButton(onClick = onReload, modifier = Modifier.focusRequester(retryFocusRequester)) {
                Text(stringResource(R.string.retry))
            }
        }
    }
    if (state.progress < 100 && state.error == null) {
        Box(Modifier.fillMaxWidth().height(4.dp).align(Alignment.BottomCenter)
            .background(colors.surfaceVariant).progressSemantics(state.progress / 100f)) {
            Box(Modifier.fillMaxWidth(state.progress / 100f).height(4.dp).background(colors.primary))
        }
    }
}

@Preview(name = "電視・載入進度", widthDp = 960, heightDp = 424, locale = "zh-rTW")
@Composable
private fun CameraPageLoadingPreview() {
    CameraPageStatePreview(CameraPageState(progress = 55))
}

@Preview(name = "電視・尚未設定", widthDp = 960, heightDp = 424, locale = "zh-rTW")
@Composable
private fun CameraPageNotConfiguredPreview() {
    CameraPageStatePreview(CameraPageState(error = CameraPageError.NOT_CONFIGURED))
}

@Preview(name = "電視・載入失敗", widthDp = 960, heightDp = 424, locale = "zh-rTW")
@Composable
private fun CameraPageLoadFailedPreview() {
    CameraPageStatePreview(CameraPageState(error = CameraPageError.LOAD_FAILED))
}

@Preview(name = "電視・憑證錯誤", widthDp = 960, heightDp = 424, locale = "zh-rTW")
@Composable
private fun CameraPageCertificateErrorPreview() {
    CameraPageStatePreview(CameraPageState(error = CameraPageError.CERTIFICATE_ERROR))
}

@Composable
private fun CameraPageStatePreview(state: CameraPageState) {
    CCTVViewerTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            CameraPageOverlay(state, onReload = {}, retryFocusRequester = remember { FocusRequester() })
        }
    }
}
