package com.txwstudio.app.cctvviewer.data

fun interface CameraRepository {
    fun getCameras(): List<Camera>
}
