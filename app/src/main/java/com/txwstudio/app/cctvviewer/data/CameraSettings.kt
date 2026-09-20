package com.txwstudio.app.cctvviewer.data

import java.net.URI

object CameraSettings {
    const val COUNT = 8
    const val MAX_NAME_LENGTH = 80
    const val MAX_URL_LENGTH = 2048

    fun validate(cameras: List<Camera>): List<Camera> {
        if (cameras.size != COUNT) throw CameraValidationException(CameraValidationError.COUNT)
        return cameras.mapIndexed { index, camera ->
            // 空白代表使用介面語系的預設名稱，不把翻譯後的文字存進使用者資料。
            val name = camera.name.trim()
            val url = camera.url.trim()
            if (name.length > MAX_NAME_LENGTH || name.any { it.isISOControl() }) {
                throw CameraValidationException(CameraValidationError.NAME, index + 1)
            }
            if (url.length > MAX_URL_LENGTH) throw CameraValidationException(CameraValidationError.URL_LENGTH, index + 1)
            if (url.isNotEmpty()) {
                val uri = runCatching { URI(url) }.getOrNull()
                val valid = uri != null &&
                    (uri.scheme.equals("http", true) || uri.scheme.equals("https", true)) &&
                    !uri.host.isNullOrBlank() && (uri.port == -1 || uri.port in 1..65535)
                if (!valid) throw CameraValidationException(CameraValidationError.URL, index + 1)
            }
            Camera(name, url)
        }
    }
}
