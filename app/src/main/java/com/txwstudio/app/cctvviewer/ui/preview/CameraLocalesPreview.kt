package com.txwstudio.app.cctvviewer.ui.preview

import androidx.compose.ui.tooling.preview.Preview

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Preview(name = "繁體中文", widthDp = 960, heightDp = 540, locale = "zh-rTW")
@Preview(name = "简体中文", widthDp = 960, heightDp = 540, locale = "zh-rCN")
@Preview(name = "English (US)", widthDp = 960, heightDp = 540, locale = "en-rUS")
@Preview(name = "日本語", widthDp = 960, heightDp = 540, locale = "ja-rJP")
@Preview(name = "한국어", widthDp = 960, heightDp = 540, locale = "ko-rKR")
internal annotation class CameraLocalesPreview
