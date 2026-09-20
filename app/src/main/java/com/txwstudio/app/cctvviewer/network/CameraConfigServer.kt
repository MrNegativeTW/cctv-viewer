package com.txwstudio.app.cctvviewer.network

import com.txwstudio.app.cctvviewer.data.Camera
import com.txwstudio.app.cctvviewer.data.CameraSettings
import com.txwstudio.app.cctvviewer.data.CameraValidationException
import com.txwstudio.app.cctvviewer.i18n.AppLanguage
import com.txwstudio.app.cctvviewer.i18n.WebMessage
import com.txwstudio.app.cctvviewer.i18n.WebStrings
import fi.iki.elonen.NanoHTTPD
import java.io.EOFException
import java.io.IOException
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.runBlocking

class CameraConfigServer(
    private val address: String,
    private val readCameras: suspend () -> List<Camera>,
    private val saveCameras: suspend (List<Camera>) -> Unit,
    private val stringsFor: (AppLanguage) -> WebStrings,
) : NanoHTTPD(address, 0) {
    private val token = SecureRandom().let { random ->
        buildString {
            repeat(TOKEN_LENGTH) {
                append(TOKEN_ALPHABET[random.nextInt(TOKEN_ALPHABET.length)])
            }
        }
    }
    private val path = "/$token"
    val url: String get() = "http://$address:$listeningPort$path"

    override fun serve(session: IHTTPSession): Response {
        val strings = stringsFor(AppLanguage.fromAcceptLanguage(session.headers["accept-language"]))
        if (!MessageDigest.isEqual(session.uri.toByteArray(), path.toByteArray())) {
            return response(Response.Status.NOT_FOUND, strings.text(WebMessage.WEB_INVALID_LINK), strings)
        }
        return try {
            when (session.method) {
                Method.GET -> runBlocking {
                    val message = if (session.parameters["saved"]?.firstOrNull() == "1")
                        strings.text(WebMessage.WEB_SAVED) else ""
                    response(Response.Status.OK, CameraSettingsPage.render(readCameras(), path, strings, message), strings, true)
                }
                Method.POST -> save(session, strings)
                else -> response(Response.Status.METHOD_NOT_ALLOWED, strings.text(WebMessage.WEB_METHOD_NOT_ALLOWED), strings)
            }
        } catch (error: CameraValidationException) {
            response(Response.Status.BAD_REQUEST, strings.validationError(error), strings)
        } catch (error: WebRequestException) {
            response(Response.Status.BAD_REQUEST, strings.text(error.messageKey), strings)
        } catch (_: IllegalArgumentException) {
            response(Response.Status.BAD_REQUEST, strings.text(WebMessage.WEB_INVALID_CONFIG), strings)
        } catch (_: IOException) {
            response(Response.Status.INTERNAL_ERROR, strings.text(WebMessage.WEB_STORAGE_FAILED), strings)
        }
    }

    private fun save(session: IHTTPSession, strings: WebStrings): Response {
        val origin = session.headers["origin"]
        if (origin != null && origin != "http://$address:$listeningPort") {
            return response(Response.Status.FORBIDDEN, strings.text(WebMessage.WEB_WRONG_ORIGIN), strings)
        }
        requireRequest(session.headers["content-type"]?.substringBefore(';')?.trim()
            ?.equals("application/x-www-form-urlencoded", true) == true, WebMessage.WEB_UNSUPPORTED_FORMAT)
        val length = session.headers["content-length"]?.toIntOrNull()
        if (length == null || length !in 1..MAX_BODY_BYTES) throw WebRequestException(WebMessage.WEB_BODY_INVALID)
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
            requireRequest(parts.size == 2, WebMessage.WEB_INVALID_CONFIG)
            val key = URLDecoder.decode(parts[0], "UTF-8")
            requireRequest(key !in fields, WebMessage.WEB_DUPLICATE_FIELD)
            fields[key] = URLDecoder.decode(parts[1], "UTF-8")
        }
        requireRequest(fields.size == CameraSettings.COUNT * 2, WebMessage.VALIDATION_COUNT)
        val cameras = (1..CameraSettings.COUNT).map { index ->
            Camera(
                fields["camera.$index.name"] ?: throw WebRequestException(WebMessage.WEB_MISSING_NAME),
                fields["camera.$index.url"] ?: throw WebRequestException(WebMessage.WEB_MISSING_URL),
            )
        }
        val validated = CameraSettings.validate(cameras)
        runBlocking { saveCameras(validated) }
        return response(Response.Status.REDIRECT_SEE_OTHER, strings.text(WebMessage.WEB_SAVED), strings)
            .apply { addHeader("Location", "$path?saved=1") }
    }

    private fun response(status: Response.Status, content: String, strings: WebStrings, html: Boolean = false): Response =
        newFixedLengthResponse(status, if (html) "text/html; charset=utf-8" else "text/plain; charset=utf-8", content).apply {
            addHeader("Content-Language", strings.language.tag)
            addHeader("Vary", "Accept-Language")
            addHeader("Cache-Control", "no-store")
            // no-referrer makes browser form POSTs send Origin: null and fail origin validation.
            addHeader("Referrer-Policy", "same-origin")
            addHeader("X-Content-Type-Options", "nosniff")
            addHeader("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; form-action 'self'; frame-ancestors 'none'; base-uri 'none'")
            closeConnection(true)
        }

    private fun requireRequest(condition: Boolean, message: WebMessage) {
        if (!condition) throw WebRequestException(message)
    }

    private class WebRequestException(val messageKey: WebMessage) : IllegalArgumentException(messageKey.name)

    companion object {
        private const val TOKEN_LENGTH = 6
        private const val TOKEN_ALPHABET = "abcdefghijklmnopqrstuvwxyz"
        private const val MAX_BODY_BYTES = 256 * 1024
    }
}
