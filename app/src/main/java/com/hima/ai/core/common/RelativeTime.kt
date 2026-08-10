package com.hima.ai.core.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.hima.ai.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/** "3 minutes ago" / "5 hours ago" for anything recent, an absolute short
 *  date beyond a day — the same two styles the mock report timestamps
 *  already use (see time_may9-style strings), just computed instead of fixed. */
@Composable
fun relativeTimeLabel(instant: Instant, now: Instant = Instant.now()): String {
    val minutes = ChronoUnit.MINUTES.between(instant, now).coerceAtLeast(0)
    val hours = ChronoUnit.HOURS.between(instant, now)
    val days = ChronoUnit.DAYS.between(instant, now)
    return when {
        minutes < 1 -> stringResource(R.string.time_just_now)
        minutes < 60 -> stringResource(R.string.time_ago_minutes, minutes.toInt())
        hours < 24 -> stringResource(R.string.time_ago_hours, hours.toInt())
        hours < 48 -> stringResource(R.string.time_yesterday)
        days < 7 -> stringResource(R.string.time_ago_days, days.toInt())
        else -> {
            val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale).withZone(ZoneId.systemDefault())
            formatter.format(instant)
        }
    }
}
