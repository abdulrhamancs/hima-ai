package com.hima.ai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.hima.ai.core.designsystem.theme.Bg
import com.hima.ai.core.designsystem.theme.DarkBg
import com.hima.ai.core.designsystem.theme.HimaTheme
import com.hima.ai.core.navigation.HimaNavHost
import com.hima.ai.core.util.ThemePreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity host. Extends [AppCompatActivity] so AndroidX's per-app
 * language API applies and persists the Arabic/English switch below API 33 —
 * the switch recreates this activity, which is also what flips the layout
 * direction for RTL.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkModeOverride by themePreferences.darkModeOverride.collectAsStateWithLifecycle()
            val darkTheme = darkModeOverride ?: isSystemInDarkTheme()
            val view = LocalView.current
            SideEffect {
                val systemBars = WindowCompat.getInsetsController(window, view)
                systemBars.isAppearanceLightStatusBars = !darkTheme
                systemBars.isAppearanceLightNavigationBars = !darkTheme
                updateSystemBarColors(darkTheme)
            }
            HimaTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                HimaNavHost(navController = navController)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun updateSystemBarColors(darkTheme: Boolean) {
        val background = if (darkTheme) DarkBg else Bg
        window.statusBarColor = background.toArgb()
        window.navigationBarColor = background.toArgb()
        // The window background is what shows through mid-crossfade, while the
        // outgoing and incoming screens are both partly transparent. It comes
        // from android:windowBackground, which resource qualifiers resolve
        // against the *system* dark mode — so an in-app theme override that
        // disagrees with the system (dark app on a light phone) left a light
        // window flashing behind every transition. Keep it in step with the
        // theme actually being drawn rather than the one the system assumes.
        window.setBackgroundDrawable(background.toArgb().toDrawable())
    }

}
