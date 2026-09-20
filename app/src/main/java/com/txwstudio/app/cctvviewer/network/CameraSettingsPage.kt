package com.txwstudio.app.cctvviewer.network

import com.txwstudio.app.cctvviewer.data.Camera
import com.txwstudio.app.cctvviewer.data.CameraSettings
import com.txwstudio.app.cctvviewer.i18n.WebMessage
import com.txwstudio.app.cctvviewer.i18n.WebStrings

internal object CameraSettingsPage {
    fun render(cameras: List<Camera>, action: String, strings: WebStrings, message: String = ""): String {
        fun text(key: WebMessage) = escape(strings.text(key))
        val fields = cameras.mapIndexed { index, camera ->
            val number = index + 1
            """
            <fieldset>
              <legend>${escape(strings.text(WebMessage.CAMERA_NUMBER, number))}</legend>
              <label for="name-$number">${text(WebMessage.WEB_NAME)}</label>
              <input id="name-$number" name="camera.$number.name" value="${escape(camera.name)}" placeholder="${escape(strings.text(WebMessage.CAMERA_NUMBER, number))}" maxlength="${CameraSettings.MAX_NAME_LENGTH}" autocomplete="off">
              <label for="url-$number">${text(WebMessage.WEB_URL)}</label>
              <input id="url-$number" name="camera.$number.url" type="url" value="${escape(camera.url)}" maxlength="${CameraSettings.MAX_URL_LENGTH}" placeholder="${text(WebMessage.WEB_URL_PLACEHOLDER)}" autocapitalize="none" spellcheck="false" autocomplete="off">
            </fieldset>
            """.trimIndent()
        }.joinToString("\n")
        return """
            <!doctype html>
            <html lang="${strings.language.htmlTag}">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <meta name="referrer" content="no-referrer">
              <title>${text(WebMessage.WEB_TITLE)}</title>
              <style>
                * { box-sizing: border-box; }
                body { margin: 0; padding: 24px 16px; background: #101815; color: #e0e4df; font-family: system-ui, sans-serif; }
                main { max-width: 720px; margin: auto; }
                h1 { margin: 0 0 12px; font-size: 28px; }
                p { color: #bfc9c2; line-height: 1.7; }
                fieldset { min-width: 0; border: 1px solid #42584f; border-radius: 18px; margin: 24px 0; padding: 20px; }
                legend { color: #9bdcd0; padding: 0 8px; font-weight: 600; }
                label { display: block; margin: 12px 0 6px; }
                input { width: 100%; padding: 12px; border: 1px solid #89938c; border-radius: 10px; background: #1c2b25; color: #fff; font: inherit; font-size: 16px; }
                input:focus { outline: 2px solid #9bdcd0; outline-offset: 2px; }
                button { width: 100%; border: 0; border-radius: 16px; padding: 16px; background: #9bdcd0; color: #00382f; font: inherit; font-weight: 700; cursor: pointer; }
                .notice { border: 1px solid #9bdcd0; padding: 16px; border-radius: 12px; color: #e0e4df; }
              </style>
            </head>
            <body><main>
              <h1>${text(WebMessage.WEB_TITLE)}</h1>
              <p>${text(WebMessage.WEB_INTRO)}</p>
              ${if (message.isEmpty()) "" else "<p class=\"notice\" role=\"status\">${escape(message)}</p>"}
              <form method="post" action="${escape(action)}" accept-charset="UTF-8">
                $fields
                <button type="submit">${text(WebMessage.WEB_SAVE)}</button>
              </form>
              <p>${text(WebMessage.WEB_SESSION_HINT)}</p>
            </main></body>
            </html>
        """.trimIndent()
    }

    private fun escape(value: String): String = value.replace("&", "&amp;")
        .replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
}
