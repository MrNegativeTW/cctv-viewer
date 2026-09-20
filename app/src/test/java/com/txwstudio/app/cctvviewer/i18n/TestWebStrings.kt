package com.txwstudio.app.cctvviewer.i18n

import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

object TestWebStrings {
    val resourceRoot: File = File("src/main/res").takeIf { it.isDirectory } ?: File("app/src/main/res")
    val folders = mapOf(
        AppLanguage.TRADITIONAL_CHINESE to "values-zh-rTW",
        AppLanguage.SIMPLIFIED_CHINESE to "values-zh-rCN",
        AppLanguage.ENGLISH to "values-en",
        AppLanguage.JAPANESE to "values-ja",
        AppLanguage.KOREAN to "values-ko",
    )

    fun read(folder: String): Map<String, String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File(resourceRoot, "$folder/strings.xml"))
        val nodes = document.getElementsByTagName("string")
        return (0 until nodes.length).associate { index ->
            val node = nodes.item(index)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
                .replace("\\'", "'").replace("\\\"", "\"")
        }
    }

    fun forLanguage(language: AppLanguage): WebStrings {
        val values = read(folders.getValue(language))
        return WebStrings(language) { message, arguments ->
            String.format(language.locale, values.getValue(message.name.lowercase(Locale.ROOT)), *arguments)
        }
    }
}
