package com.txwstudio.app.cctvviewer.data

import kotlinx.coroutines.flow.Flow

interface CameraRepository {
    val cameras: Flow<List<Camera>>
    suspend fun saveCameras(cameras: List<Camera>)
}
