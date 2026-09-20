package com.txwstudio.app.cctvviewer.data

import java.net.URI

object CameraSettings {
    const val COUNT = 8
    const val MAX_NAME_LENGTH = 80
    const val MAX_URL_LENGTH = 2048

    fun validate(cameras: List<Camera>): List<Camera> {
        require(cameras.size == COUNT) { "請提供完整的八組攝影機設定。" }
        return cameras.mapIndexed { index, camera ->
            val name = camera.name.trim().ifBlank { "攝影機 ${index + 1}" }
            val url = camera.url.trim()
            require(name.length <= MAX_NAME_LENGTH && name.none { it.isISOControl() }) {
                "第 ${index + 1} 支攝影機的名稱過長或包含無效字元。"
            }
            require(url.length <= MAX_URL_LENGTH) { "第 ${index + 1} 支攝影機的網址過長。" }
            if (url.isNotEmpty()) {
                val uri = runCatching { URI(url) }.getOrNull()
                require(uri != null &&
                    (uri.scheme.equals("http", true) || uri.scheme.equals("https", true)) &&
                    !uri.host.isNullOrBlank() && (uri.port == -1 || uri.port in 1..65535)
                ) { "第 ${index + 1} 支攝影機需要有效的 HTTP 或 HTTPS 網址。" }
            }
            Camera(name, url)
        }
    }
}
