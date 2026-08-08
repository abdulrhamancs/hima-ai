package com.hima.ai

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
        setContent {
            HimaTheme {
                val navController = rememberNavController()
                HimaNavHost(navController = navController)
            }
        }
    }
}
