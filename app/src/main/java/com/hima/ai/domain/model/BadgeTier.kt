package com.hima.ai.domain.model

import androidx.annotation.StringRes
import com.hima.ai.R

/**
 * The report-count reward ladder shown on the Badges & Achievements screen.
 * Purely data-driven — [threshold] is the only thing that decides rank, so
 * reordering or adding a tier is a one-line change here; colours live in
 * `core/designsystem` (see `MedalBadge.kt`), same split as [Severity].
 */
enum class BadgeTier(
    @StringRes val nameRes: Int,
    @StringRes val statusRes: Int,
    val threshold: Int,
) {
    PIONEER(R.string.badge_tier_pioneer, R.string.badge_status_pioneer, 1),
    BRONZE(R.string.badge_tier_bronze, R.string.badge_status_bronze, 5),
    SILVER(R.string.badge_tier_silver, R.string.badge_status_silver, 15),
    GOLD(R.string.badge_tier_gold, R.string.badge_status_gold, 30),
    PLATINUM(R.string.badge_tier_platinum, R.string.badge_status_platinum, 60),
    DIAMOND(R.string.badge_tier_diamond, R.string.badge_status_diamond, 100),
    GUARDIAN(R.string.badge_tier_guardian, R.string.badge_status_guardian, 150),
    AMBASSADOR(R.string.badge_tier_ambassador, R.string.badge_status_ambassador, 200),
    ;

    companion object {
        /** Ascending by threshold — the single source of truth for tier order. */
        val ordered: List<BadgeTier> = entries.sortedBy { it.threshold }

        /** The highest tier reached by [reportCount], or the lowest tier if none is reached yet. */
        fun forReportCount(reportCount: Int): BadgeTier =
            ordered.lastOrNull { reportCount >= it.threshold } ?: ordered.first()

        /** The tier after [tier], or null when [tier] is already the top of the ladder. */
        fun next(tier: BadgeTier): BadgeTier? = ordered.getOrNull(ordered.indexOf(tier) + 1)
    }
}
