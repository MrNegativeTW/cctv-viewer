package com.txwstudio.app.cctvviewer.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.txwstudio.app.cctvviewer.R
import com.txwstudio.app.cctvviewer.data.Camera
import com.txwstudio.app.cctvviewer.i18n.AndroidWebStrings
import com.txwstudio.app.cctvviewer.ui.preview.CameraLocalesPreview
import com.txwstudio.app.cctvviewer.network.CameraConfigServer
import com.txwstudio.app.cctvviewer.network.LocalNetworkAddress
import com.txwstudio.app.cctvviewer.ui.theme.CCTVViewerTheme
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext

@Composable
fun CameraSettingsDialog(
    readCameras: suspend () -> List<Camera>,
    saveCameras: suspend (List<Camera>) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentRead by rememberUpdatedState(readCameras)
    val currentSave by rememberUpdatedState(saveCameras)
    var url by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(lifecycle, context) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var server: CameraConfigServer? = null
            url = null
            failed = false
            try {
                withContext(Dispatchers.IO) {
                    val address = LocalNetworkAddress.find(context)
                        ?: throw IllegalStateException("No local network")
                    CameraConfigServer(address, { currentRead() }, { currentSave(it) },
                        { language -> AndroidWebStrings.create(context, language) }).also {
                        server = it
                        it.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true)
                    }
                }
                url = server?.url
                awaitCancellation()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                failed = true
            } finally {
                // 包含啟動途中取消的情況；不能只依賴背景中的 Compose 重組關閉服務。
                server?.stop()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CameraSettingsContent(url, failed, onDismiss)
    }
}

@Composable
private fun CameraSettingsContent(url: String?, failed: Boolean, onDismiss: () -> Unit) {
    val closeFocus = remember { FocusRequester() }
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.width(780.dp).background(colors.surface, RoundedCornerShape(24.dp)).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(R.string.phone_settings), style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface)
        if (url != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                val bitmap = remember(url) {
                    val matrix = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 512, 512,
                        mapOf(EncodeHintType.MARGIN to 4))
                    val pixels = IntArray(512 * 512) { index ->
                        if (matrix[index % 512, index / 512]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    }
                    Bitmap.createBitmap(pixels, 512, 512, Bitmap.Config.ARGB_8888).asImageBitmap()
                }
                Image(bitmap, contentDescription = stringResource(R.string.settings_qr_description),
                    modifier = Modifier.size(192.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.settings_instructions), style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface)
                    Text(url, style = MaterialTheme.typography.bodyMedium, color = colors.primary)
                    Text(stringResource(R.string.settings_session_hint), style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant)
                }
            }
        } else {
            Text(stringResource(if (failed) R.string.settings_server_failed else R.string.settings_starting),
                style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            CameraButton(onClick = onDismiss, modifier = Modifier.focusRequester(closeFocus)) {
                Text(stringResource(R.string.close_settings))
            }
        }
    }
    LaunchedEffect(Unit) { closeFocus.requestFocus() }
}

@CameraLocalesPreview
@Composable
private fun CameraSettingsPreview() {
    CCTVViewerTheme {
        CameraSettingsContent("http://192.0.2.10:8080/preview", false, {})
    }
}
