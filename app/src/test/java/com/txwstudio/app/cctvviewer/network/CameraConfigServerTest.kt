package com.txwstudio.app.cctvviewer.network

import com.txwstudio.app.cctvviewer.data.Camera
import com.txwstudio.app.cctvviewer.i18n.AppLanguage
import com.txwstudio.app.cctvviewer.i18n.TestWebStrings
import com.txwstudio.app.cctvviewer.i18n.WebMessage
import java.net.Socket
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLEncoder
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CameraConfigServerTest {
    private var cameras = List(8) { Camera("攝影機 ${it + 1}", "https://example.invalid/${it + 1}") }
    private lateinit var server: CameraConfigServer
    private var saves = 0

    @Before
    fun start() {
        server = CameraConfigServer("127.0.0.1", { cameras }, { cameras = it; saves++ }, TestWebStrings::forLanguage)
        server.start(2000, true)
    }

    @After
    fun stop() { server.stop() }

    private data class Result(val status: Int, val body: String, val location: String?, val cache: String?, val language: String?, val vary: String?, val referrerPolicy: String?)

    private fun request(url: String = server.url, body: String? = null, origin: String? = null, acceptLanguage: String? = null): Result {
        val uri = URI(url)
        val bytes = body?.toByteArray(Charsets.UTF_8)
        val target = uri.rawPath + (uri.rawQuery?.let { "?$it" } ?: "")
        val headers = buildString {
            append("${if (body == null) "GET" else "POST"} $target HTTP/1.1\r\n")
            append("Host: ${uri.authority}\r\nConnection: close\r\n")
            if (bytes != null) {
                append("Content-Type: application/x-www-form-urlencoded; charset=utf-8\r\n")
                append("Content-Length: ${bytes.size}\r\n")
            }
            if (origin != null) append("Origin: $origin\r\n")
            if (acceptLanguage != null) append("Accept-Language: $acceptLanguage\r\n")
            append("\r\n")
        }
        return Socket().use { socket ->
            socket.connect(InetSocketAddress(uri.host, uri.port), 2000)
            socket.soTimeout = 2000
            socket.getOutputStream().apply {
                write(headers.toByteArray(Charsets.UTF_8))
                // 超出限制時只送標頭，驗證服務在讀取內容之前就拒絕。
                if (bytes != null && bytes.size <= 256 * 1024) write(bytes)
                flush()
            }
            val response = socket.getInputStream().readBytes().toString(Charsets.UTF_8)
            val sections = response.split("\r\n\r\n", limit = 2)
            val lines = sections[0].split("\r\n")
            val fields = lines.drop(1).associate {
                val pair = it.split(':', limit = 2)
                pair[0].lowercase() to pair[1].trim()
            }
            Result(lines[0].split(' ')[1].toInt(), sections.getOrElse(1) { "" }, fields["location"], fields["cache-control"], fields["content-language"], fields["vary"], fields["referrer-policy"])
        }
    }

    private fun form(values: List<Camera> = cameras) = values.flatMapIndexed { index, camera ->
        listOf("camera.${index + 1}.name" to camera.name, "camera.${index + 1}.url" to camera.url)
    }.joinToString("&") { (key, value) ->
        "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
    }

    @Test
    fun settingsPathUsesSixLowercaseLettersAndRequiresAnExactMatch() {
        val uri = URI(server.url)
        assertTrue(uri.path.matches(Regex("/[a-z]{6}")))
        assertEquals(200, request().status)
        val base = "${uri.scheme}://${uri.authority}"
        assertEquals(404, request(base + uri.path.uppercase(Locale.ROOT)).status)
        assertEquals(404, request(server.url.dropLast(1)).status)
        assertEquals(404, request(server.url + "x").status)
    }

    @Test
    fun servesEightEscapedFieldsOnlyWithSessionLink() {
        cameras = cameras.map { it.copy(name = "\"><script>alert(1)</script>&中文") }
        val page = request()
        assertEquals(200, page.status)
        assertEquals(8, Regex("<fieldset>").findAll(page.body).count())
        assertTrue(page.body.contains("&lt;script&gt;"))
        assertFalse(page.body.contains("<script>"))
        assertEquals("no-store", page.cache)
        assertEquals(404, request("http://127.0.0.1:${server.listeningPort}/").status)
        assertEquals(404, request("http://127.0.0.1:${server.listeningPort}/wrong-token").status)
    }

    @Test
    fun savesUtf8AndQueryParametersAndRedirectsToConfirmation() {
        val page = request()
        // Both policies must preserve the browser's Origin on a same-origin form POST.
        assertEquals("same-origin", page.referrerPolicy)
        assertTrue(page.body.contains("<meta name=\"referrer\" content=\"same-origin\">"))
        val edited = cameras.mapIndexed { index, camera ->
            if (index == 0) Camera("新名稱 & \"測試\"", "http://192.0.2.1/view?x=a+b&token=abc%2Fdef") else camera
        }
        val origin = URI(server.url).let { "${it.scheme}://${it.authority}" }
        val result = request(body = form(edited), origin = origin)
        assertEquals(303, result.status)
        assertEquals(edited, cameras)
        assertEquals(1, saves)
        assertTrue(request("$origin${result.location}").body.contains("已儲存"))
    }

    @Test
    fun invalidOrPartialInputDoesNotSaveAnything() {
        val original = cameras
        assertEquals(400, request(body = form(cameras.map { it.copy(url = "file:///etc/passwd") })).status)
        assertEquals(400, request(body = "camera.1.name=test").status)
        assertEquals(400, request(body = form() + "&camera.1.name=duplicate").status)
        assertEquals(original, cameras)
        assertEquals(0, saves)
    }

    @Test
    fun rejectsCrossOriginAndOversizedRequests() {
        assertEquals(403, request(body = form(), origin = "https://example.invalid").status)
        assertEquals(403, request(body = form(), origin = "null").status)
        assertEquals(400, request(body = "x=" + "a".repeat(300_000)).status)
        assertEquals(0, saves)
    }

    @Test
    fun closingAndReopeningInvalidatesPreviousLink() {
        val oldPath = URI(server.url).path
        server.stop()
        server = CameraConfigServer("127.0.0.1", { cameras }, { cameras = it }, TestWebStrings::forLanguage)
        server.start(2000, true)
        assertEquals(404, request("http://127.0.0.1:${server.listeningPort}$oldPath").status)
        assertEquals(200, request().status)
    }
    @Test
    fun rendersFormsAndValidationErrorsInEveryLanguage() {
        cameras = cameras.map { it.copy(name = "自訂名稱") }
        AppLanguage.entries.forEach { language ->
            val strings = TestWebStrings.forLanguage(language)
            val page = request(acceptLanguage = language.tag)
            assertEquals(language.tag, page.language)
            assertEquals("Accept-Language", page.vary)
            assertTrue(page.body.contains("<html lang=\"${language.htmlTag}\">"))
            assertTrue(page.body.contains(strings.text(WebMessage.WEB_TITLE)))
            assertTrue(page.body.contains("value=\"自訂名稱\""))
            val invalid = cameras.mapIndexed { index, camera ->
                if (index == 2) camera.copy(url = "javascript:alert(1)") else camera
            }
            val error = request(body = form(invalid), acceptLanguage = language.tag)
            assertEquals(400, error.status)
            assertEquals(strings.text(WebMessage.VALIDATION_URL, 3), error.body)
            assertEquals(language.tag, error.language)
            val unauthorized = request("http://127.0.0.1:${server.listeningPort}/invalid", acceptLanguage = language.tag)
            assertEquals(strings.text(WebMessage.WEB_INVALID_LINK), unauthorized.body)
        }
        assertEquals(0, saves)
    }

    @Test
    fun savedMessageAndBlankNamePlaceholderFollowBrowserLanguage() {
        AppLanguage.entries.forEach { language ->
            val strings = TestWebStrings.forLanguage(language)
            val result = request(body = form(List(8) { Camera("", "") }), acceptLanguage = language.tag)
            assertEquals(303, result.status)
            assertTrue(cameras.all { it.name.isEmpty() })
            val page = request("http://127.0.0.1:${server.listeningPort}${result.location}", acceptLanguage = language.tag)
            assertTrue(page.body.contains(strings.text(WebMessage.WEB_SAVED)))
            assertTrue(page.body.contains("placeholder=\"${strings.text(WebMessage.CAMERA_NUMBER, 1)}\""))
        }
    }

    @Test
    fun unsupportedBrowserLanguageFallsBackToTraditionalChinese() {
        val page = request(acceptLanguage = "fr-FR")
        assertEquals("zh-TW", page.language)
        assertTrue(page.body.contains("監視器設定"))
        assertEquals("en-US", request(acceptLanguage = "fr-FR,en-US;q=0.8").language)
    }

}
