package com.hima.ai.core.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hima.ai.R
import java.time.LocalTime

/**
 * The greeting matching the time on the device clock.
 *
 * Resolved once per composition rather than per frame — the hour doesn't change
 * often enough to be worth recomputing, and a stable value keeps the header
 * from flickering while the Home screen recomposes around live report data.
 *
 * Returns a resource id rather than a formatted string so each locale keeps its
 * own natural phrasing: Arabic uses مساء الخير for both afternoon and evening,
 * where English distinguishes the two.
 */
@StringRes
@Composable
fun rememberTimeOfDayGreeting(): Int {
    val hour = remember { LocalTime.now().hour }
    return when (hour) {
        in 5..11 -> R.string.home_greeting_morning
        in 12..17 -> R.string.home_greeting_afternoon
        else -> R.string.home_greeting_evening
    }
}
