package com.txwstudio.app.cctvviewer.ui.web

data class CameraPageState(
    val progress: Int = 0,
    val error: CameraPageError? = null,
)

enum class CameraPageError {
    NOT_CONFIGURED,
    LOAD_FAILED,
    CERTIFICATE_ERROR,
}
