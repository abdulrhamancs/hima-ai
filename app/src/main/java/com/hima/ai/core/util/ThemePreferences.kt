package com.hima.ai.core.util

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** App-owned appearance preference, persisted independently of authentication. */
@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _darkModeOverride = MutableStateFlow(
        if (preferences.contains(KEY_DARK_MODE)) preferences.getBoolean(KEY_DARK_MODE, false) else null,
    )

    /** Null means follow the current system appearance until the user makes a choice. */
    val darkModeOverride: StateFlow<Boolean?> = _darkModeOverride.asStateFlow()

    fun setDarkModeEnabled(enabled: Boolean) {
        if (_darkModeOverride.value == enabled) return
        preferences.edit { putBoolean(KEY_DARK_MODE, enabled) }
        _darkModeOverride.value = enabled
    }

    private companion object {
        const val PREFERENCES_NAME = "hima_appearance"
        const val KEY_DARK_MODE = "dark_mode_enabled"
    }
}
