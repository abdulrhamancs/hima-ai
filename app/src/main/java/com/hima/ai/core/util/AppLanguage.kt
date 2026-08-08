package com.hima.ai.core.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** The app's supported languages. Arabic is the default (base `values/`); English is opt-in. */
enum class AppLanguage(val tag: String) {
    ARABIC("ar"),
    ENGLISH("en"),
}

/**
 * Switches the app's display language app-wide via AndroidX's per-app language
 * API. Persisted automatically (see the AppLocalesMetadataHolderService entry in
 * AndroidManifest.xml) and applied by recreating the activity, which also flips
 * the layout direction for RTL.
 */
fun setAppLanguage(language: AppLanguage) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
}

/** The currently active app language, defaulting to Arabic (the base resource set). */
fun currentAppLanguage(): AppLanguage {
    val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    return if (tags.startsWith("en")) AppLanguage.ENGLISH else AppLanguage.ARABIC
}

/** Flips between Arabic and English. */
fun toggleAppLanguage() {
    setAppLanguage(
        if (currentAppLanguage() == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC,
    )
}
