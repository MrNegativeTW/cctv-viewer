package com.txwstudio.app.cctvviewer.data

import com.txwstudio.app.cctvviewer.BuildConfig

class BuildConfigCameraRepository : CameraRepository {
    override fun getCameras(): List<Camera> = BuildConfig.CAMERA_NAMES.indices.map { index ->
        Camera(BuildConfig.CAMERA_NAMES[index], BuildConfig.CAMERA_URLS[index])
    }
}
