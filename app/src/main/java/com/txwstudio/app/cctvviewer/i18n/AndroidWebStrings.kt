package com.txwstudio.app.cctvviewer.i18n

import android.content.Context
import android.content.res.Configuration

object AndroidWebStrings {
    fun create(context: Context, language: AppLanguage): WebStrings {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(language.locale)
        }
        val resources = context.createConfigurationContext(configuration).resources
        return WebStrings(language) { message, arguments ->
            resources.getString(message.resourceId, *arguments)
        }
    }
}
