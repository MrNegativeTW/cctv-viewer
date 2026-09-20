package com.txwstudio.app.cctvviewer.data

import com.txwstudio.app.cctvviewer.BuildConfig

object BuildConfigCameraDefaults {
    fun load(): List<Camera> = BuildConfig.CAMERA_NAMES.indices.map { index ->
        Camera(BuildConfig.CAMERA_NAMES[index], BuildConfig.CAMERA_URLS[index])
    }
}
