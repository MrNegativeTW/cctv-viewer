package com.txwstudio.app.cctvviewer.ui.preview

import com.txwstudio.app.cctvviewer.data.Camera
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.txwstudio.app.cctvviewer.R

// 預覽只使用示範名稱及空白網址，不讀取 BuildConfig 或本機私人設定。
internal object CameraPreviewData {
    @Composable
    fun cameras() = List(8) { index -> Camera(name = stringResource(R.string.camera_number, index + 1), url = "") }
}
