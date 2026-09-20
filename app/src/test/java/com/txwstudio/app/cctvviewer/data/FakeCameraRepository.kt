package com.txwstudio.app.cctvviewer.data

import kotlinx.coroutines.flow.MutableStateFlow

class FakeCameraRepository(initial: List<Camera>) : CameraRepository {
    override val cameras = MutableStateFlow(initial)
    override suspend fun saveCameras(cameras: List<Camera>) {
        this.cameras.value = CameraSettings.validate(cameras)
    }
}
