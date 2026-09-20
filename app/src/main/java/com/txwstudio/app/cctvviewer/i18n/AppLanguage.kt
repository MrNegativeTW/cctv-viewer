package com.txwstudio.app.cctvviewer.i18n

import java.util.Locale

enum class AppLanguage(val tag: String, val htmlTag: String) {
    TRADITIONAL_CHINESE("zh-TW", "zh-Hant-TW"),
    SIMPLIFIED_CHINESE("zh-CN", "zh-Hans-CN"),
    ENGLISH("en-US", "en-US"),
    JAPANESE("ja-JP", "ja-JP"),
    KOREAN("ko-KR", "ko-KR");

    val locale: Locale get() = Locale.forLanguageTag(tag)

    companion object {
        fun fromAcceptLanguage(header: String?): AppLanguage {
            val ranges = runCatching { Locale.LanguageRange.parse(header.orEmpty()) }.getOrDefault(emptyList())
            for (range in ranges) {
                if (range.weight <= 0) continue
                val locale = Locale.forLanguageTag(range.range)
                when (locale.language) {
                    "zh" -> return when {
                        locale.script.equals("Hant", true) -> TRADITIONAL_CHINESE
                        locale.script.equals("Hans", true) -> SIMPLIFIED_CHINESE
                        locale.country in setOf("CN", "SG") -> SIMPLIFIED_CHINESE
                        else -> TRADITIONAL_CHINESE
                    }
                    "en" -> return ENGLISH
                    "ja" -> return JAPANESE
                    "ko" -> return KOREAN
                }
                if (range.range == "*") return TRADITIONAL_CHINESE
            }
            return TRADITIONAL_CHINESE
        }
    }
}
