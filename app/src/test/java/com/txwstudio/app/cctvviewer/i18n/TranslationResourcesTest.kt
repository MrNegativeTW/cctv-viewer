package com.txwstudio.app.cctvviewer.i18n

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationResourcesTest {
    @Test
    fun allLanguagesHaveCompleteTranslationsAndMatchingFormatArguments() {
        val baseline = TestWebStrings.read("values")
        assertEquals(baseline, TestWebStrings.read("values-zh-rTW"))
        val placeholders = Regex("%[0-9]+\\\$[ds]")
        TestWebStrings.folders.forEach { (language, folder) ->
            val translated = TestWebStrings.read(folder)
            assertEquals(language.tag, baseline.keys, translated.keys)
            baseline.forEach { (key, value) ->
                assertTrue("${language.tag}: $key is empty", translated.getValue(key).isNotBlank())
                assertEquals("${language.tag}: $key", placeholders.findAll(value).map { it.value }.toList(),
                    placeholders.findAll(translated.getValue(key)).map { it.value }.toList())
            }
            WebMessage.entries.forEach { key ->
                TestWebStrings.forLanguage(language).text(key, 1)
            }
        }
    }

    @Test
    fun localeConfigAdvertisesTheFiveSupportedLanguages() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File(TestWebStrings.resourceRoot, "xml/locales_config.xml"))
        val nodes = doc.getElementsByTagName("locale")
        val tags = (0 until nodes.length).map { nodes.item(it).attributes.getNamedItem("android:name").nodeValue }
        assertEquals(AppLanguage.entries.map { it.tag }, tags)
    }
}
