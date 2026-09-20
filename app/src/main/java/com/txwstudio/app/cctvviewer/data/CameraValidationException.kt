package com.txwstudio.app.cctvviewer.data

enum class CameraValidationError { COUNT, NAME, URL_LENGTH, URL }

class CameraValidationException(
    val reason: CameraValidationError,
    val cameraNumber: Int = 0,
) : IllegalArgumentException(reason.name)
