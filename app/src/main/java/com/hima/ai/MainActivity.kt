package com.hima.ai

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import com.hima.ai.core.designsystem.theme.HimaTheme
import com.hima.ai.core.navigation.HimaNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. Extends [AppCompatActivity] so AndroidX's per-app
 * language API applies and persists the Arabic/English switch below API 33 —
 * the switch recreates this activity, which is also what flips the layout
 * direction for RTL.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dismissSystemSplashImmediately()
        setContent {
            HimaTheme {
                val navController = rememberNavController()
                HimaNavHost(navController = navController)
            }
        }
    }

    /**
     * Hands off from the system splash the instant it appears, instead of
     * waiting for its reveal animation to finish.
     *
     * That animation can stall: the splash layer fades to alpha 0 but never
     * completes, leaving this activity's window parented under the
     * `starting_reveal` leash and held at `mShownAlpha=0.0` — drawn, at 60fps,
     * but never composited. The result is a black screen with a live app
     * behind it.
     *
     * `android:windowDisablePreview` in the theme is meant to prevent this, but
     * it is a pre-Android-12 attribute and the platform still creates a
     * SplashScreen in some launch paths, so it cannot be relied on alone.
     * Removing the view here ends the transition deterministically. Nothing is
     * lost visually: the app's own branded splash is the first Compose screen.
     */
    private fun dismissSystemSplashImmediately() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        runCatching {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }
    }
}
