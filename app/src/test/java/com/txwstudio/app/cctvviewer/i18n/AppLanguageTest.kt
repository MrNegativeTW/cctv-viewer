package com.txwstudio.app.cctvviewer.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun defaultsToTraditionalChinese() {
        listOf(null, "", "fr-FR", "invalid;q=no", "en-US;q=0", "*").forEach {
            assertEquals(AppLanguage.TRADITIONAL_CHINESE, AppLanguage.fromAcceptLanguage(it))
        }
    }

    @Test
    fun selectsAllSupportedLanguagesAndCommonRegionalVariants() {
        mapOf(
            "zh-TW" to AppLanguage.TRADITIONAL_CHINESE,
            "zh-HK" to AppLanguage.TRADITIONAL_CHINESE,
            "zh" to AppLanguage.TRADITIONAL_CHINESE,
            "zh-CN" to AppLanguage.SIMPLIFIED_CHINESE,
            "zh-SG" to AppLanguage.SIMPLIFIED_CHINESE,
            "en-US" to AppLanguage.ENGLISH,
            "en-GB" to AppLanguage.ENGLISH,
            "ja" to AppLanguage.JAPANESE,
            "ja-JP" to AppLanguage.JAPANESE,
            "ko" to AppLanguage.KOREAN,
            "ko-KR" to AppLanguage.KOREAN,
        ).forEach { (header, expected) -> assertEquals(expected, AppLanguage.fromAcceptLanguage(header)) }
    }

    @Test
    fun explicitChineseScriptTakesPriorityOverRegion() {
        assertEquals(AppLanguage.TRADITIONAL_CHINESE, AppLanguage.fromAcceptLanguage("zh-Hant-CN"))
        assertEquals(AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.fromAcceptLanguage("zh-Hans-TW"))
    }

    @Test
    fun respectsQualityOrderingAndSkipsUnsupportedOrExcludedLanguages() {
        assertEquals(AppLanguage.KOREAN, AppLanguage.fromAcceptLanguage("en-US;q=0.5,ko-KR;q=0.9"))
        assertEquals(AppLanguage.JAPANESE, AppLanguage.fromAcceptLanguage("fr-FR,ja-JP;q=0.8,en-US;q=0.5"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromAcceptLanguage("zh-CN;q=0,en-US;q=0.8"))
    }
}
