package com.hima.ai.core.util

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Small local-only preferences owned by the More screen. */
@Singleton
class MorePreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _notificationsEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true),
    )

    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        if (_notificationsEnabled.value == enabled) return
        preferences.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }
        _notificationsEnabled.value = enabled
    }

    private companion object {
        const val PREFERENCES_NAME = "hima_more"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}
