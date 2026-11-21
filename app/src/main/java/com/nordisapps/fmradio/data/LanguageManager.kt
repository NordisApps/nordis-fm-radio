package com.nordisapps.fmradio.data

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "language_settings"
    private const val LANGUAGE_KEY = "selected_language"
    private const val DEFAULT_LANGUAGE = "en"

    val SUPPORTED_LANGUAGES = setOf("en", "ru")

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLanguage(context: Context): String {
        val saved = prefs(context).getString(LANGUAGE_KEY, null)
        if (saved != null && saved in SUPPORTED_LANGUAGES) return saved

        val system = ConfigurationCompat.getLocales(context.resources.configuration)
            .get(0)?.language
        return if (system != null && system in SUPPORTED_LANGUAGES) system else DEFAULT_LANGUAGE
    }

    fun saveLanguage(context: Context, langCode: String) {
        prefs(context).edit { putString(LANGUAGE_KEY, langCode) }
    }

    fun wrap(context: Context): Context {
        val config = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(getLanguage(context)))
        }
        return context.createConfigurationContext(config)
    }
}