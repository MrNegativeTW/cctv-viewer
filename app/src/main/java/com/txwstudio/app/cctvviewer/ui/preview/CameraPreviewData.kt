package com.txwstudio.app.cctvviewer.ui.preview

import com.txwstudio.app.cctvviewer.data.Camera

// 預覽只使用示範名稱及空白網址，不讀取 BuildConfig 或本機私人設定。
internal object CameraPreviewData {
    val cameras = List(8) { index -> Camera(name = "攝影機 ${index + 1}", url = "") }
}
