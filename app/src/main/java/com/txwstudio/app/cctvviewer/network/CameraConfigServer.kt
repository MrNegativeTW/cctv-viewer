package com.txwstudio.app.cctvviewer.network

import com.txwstudio.app.cctvviewer.data.Camera
import com.txwstudio.app.cctvviewer.data.CameraSettings
import fi.iki.elonen.NanoHTTPD
import java.io.EOFException
import java.io.IOException
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import kotlinx.coroutines.runBlocking

class CameraConfigServer(
    private val address: String,
    private val readCameras: suspend () -> List<Camera>,
    private val saveCameras: suspend (List<Camera>) -> Unit,
) : NanoHTTPD(address, 0) {
    private val token = ByteArray(18).also { SecureRandom().nextBytes(it) }
        .let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }
    private val path = "/$token"
    val url: String get() = "http://$address:$listeningPort$path"

    override fun serve(session: IHTTPSession): Response {
        if (!MessageDigest.isEqual(session.uri.toByteArray(), path.toByteArray())) {
            return response(Response.Status.NOT_FOUND, "連結無效，請重新掃描電視上的 QR Code。")
        }
        return try {
            when (session.method) {
                Method.GET -> runBlocking {
                    val message = if (session.parameters["saved"]?.firstOrNull() == "1")
                        "已儲存，電視設定已更新。" else ""
                    response(Response.Status.OK, CameraSettingsPage.render(readCameras(), path, message), true)
                }
                Method.POST -> save(session)
                else -> response(Response.Status.METHOD_NOT_ALLOWED, "不支援此操作。")
            }
        } catch (error: IllegalArgumentException) {
            response(Response.Status.BAD_REQUEST, error.message ?: "設定內容無效。")
        } catch (_: IOException) {
            response(Response.Status.INTERNAL_ERROR, "無法儲存或讀取設定，請稍後重試。")
        }
    }

    private fun save(session: IHTTPSession): Response {
        val origin = session.headers["origin"]
        if (origin != null && origin != "http://$address:$listeningPort") {
            return response(Response.Status.FORBIDDEN, "請使用電視顯示的設定連結。")
        }
        require(session.headers["content-type"]?.substringBefore(';')?.trim()
            ?.equals("application/x-www-form-urlencoded", true) == true) { "不支援的設定格式。" }
        val length = session.headers["content-length"]?.toIntOrNull()
        require(length != null && length in 1..MAX_BODY_BYTES) { "設定內容過大或格式無效。" }
        val body = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val read = session.inputStream.read(body, offset, length - offset)
            if (read < 0) throw EOFException()
            offset += read
        }
        val fields = mutableMapOf<String, String>()
        body.toString(Charsets.UTF_8).split('&').forEach { pair ->
            val parts = pair.split('=', limit = 2)
            require(parts.size == 2) { "設定格式無效。" }
            val key = URLDecoder.decode(parts[0], "UTF-8")
            require(key !in fields) { "設定包含重複欄位。" }
            fields[key] = URLDecoder.decode(parts[1], "UTF-8")
        }
        require(fields.size == CameraSettings.COUNT * 2) { "請提供完整的八組攝影機設定。" }
        val cameras = (1..CameraSettings.COUNT).map { index ->
            Camera(
                requireNotNull(fields["camera.$index.name"]) { "缺少攝影機名稱欄位。" },
                requireNotNull(fields["camera.$index.url"]) { "缺少攝影機網址欄位。" },
            )
        }
        val validated = CameraSettings.validate(cameras)
        runBlocking { saveCameras(validated) }
        return response(Response.Status.REDIRECT_SEE_OTHER, "已儲存。")
            .apply { addHeader("Location", "$path?saved=1") }
    }

    private fun response(status: Response.Status, content: String, html: Boolean = false): Response =
        newFixedLengthResponse(status, if (html) "text/html; charset=utf-8" else "text/plain; charset=utf-8", content).apply {
            addHeader("Cache-Control", "no-store")
            addHeader("Referrer-Policy", "no-referrer")
            addHeader("X-Content-Type-Options", "nosniff")
            addHeader("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; form-action 'self'; frame-ancestors 'none'; base-uri 'none'")
            closeConnection(true)
        }

    companion object {
        private const val MAX_BODY_BYTES = 256 * 1024
    }
}
