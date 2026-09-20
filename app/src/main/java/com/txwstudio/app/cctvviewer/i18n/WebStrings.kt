package com.txwstudio.app.cctvviewer.i18n

import com.txwstudio.app.cctvviewer.R
import com.txwstudio.app.cctvviewer.data.CameraValidationError
import com.txwstudio.app.cctvviewer.data.CameraValidationException

enum class WebMessage(val resourceId: Int) {
    CAMERA_NUMBER(R.string.camera_number),
    WEB_TITLE(R.string.web_title),
    WEB_INTRO(R.string.web_intro),
    WEB_NAME(R.string.web_name),
    WEB_URL(R.string.web_url),
    WEB_URL_PLACEHOLDER(R.string.web_url_placeholder),
    WEB_SAVE(R.string.web_save),
    WEB_SESSION_HINT(R.string.web_session_hint),
    WEB_SAVED(R.string.web_saved),
    WEB_INVALID_LINK(R.string.web_invalid_link),
    WEB_METHOD_NOT_ALLOWED(R.string.web_method_not_allowed),
    WEB_INVALID_CONFIG(R.string.web_invalid_config),
    WEB_STORAGE_FAILED(R.string.web_storage_failed),
    WEB_WRONG_ORIGIN(R.string.web_wrong_origin),
    WEB_UNSUPPORTED_FORMAT(R.string.web_unsupported_format),
    WEB_BODY_INVALID(R.string.web_body_invalid),
    WEB_DUPLICATE_FIELD(R.string.web_duplicate_field),
    WEB_MISSING_NAME(R.string.web_missing_name),
    WEB_MISSING_URL(R.string.web_missing_url),
    VALIDATION_COUNT(R.string.validation_count),
    VALIDATION_NAME(R.string.validation_name),
    VALIDATION_URL_LENGTH(R.string.validation_url_length),
    VALIDATION_URL(R.string.validation_url),
}

class WebStrings(
    val language: AppLanguage,
    private val resolve: (WebMessage, Array<out Any>) -> String,
) {
    fun text(message: WebMessage, vararg arguments: Any): String = resolve(message, arguments)

    fun validationError(error: CameraValidationException): String = when (error.reason) {
        CameraValidationError.COUNT -> text(WebMessage.VALIDATION_COUNT)
        CameraValidationError.NAME -> text(WebMessage.VALIDATION_NAME, error.cameraNumber)
        CameraValidationError.URL_LENGTH -> text(WebMessage.VALIDATION_URL_LENGTH, error.cameraNumber)
        CameraValidationError.URL -> text(WebMessage.VALIDATION_URL, error.cameraNumber)
    }
}
