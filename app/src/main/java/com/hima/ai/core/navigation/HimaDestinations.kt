package com.hima.ai.core.navigation

/**
 * App navigation routes, matching the prototype flow:
 * Splash -> Login (<-> Sign up) -> Home -> New report -> AI analysis ->
 * Final report <-> AI investigation, with Reports history reachable from Home
 * and the bottom navigation.
 */
object HimaDestinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME = "home"
    const val MAP = "map"
    const val MAP_ARG_REPORT_ID = "focusReportId"
    const val MAP_ROUTE = "map?$MAP_ARG_REPORT_ID={$MAP_ARG_REPORT_ID}"
    const val NEW_REPORT = "new_report"
    const val ANALYSIS = "analysis"
    /** Circular-economy decision screen shown before opening the persisted report. */
    const val RECYCLABLE_RESULT = "recyclable_result"
    const val INVESTIGATION = "investigation"
    const val HISTORY = "history"
    const val MORE = "more"
    const val BADGES = "badges"
    const val BADGES_TIERS = "badges/tiers"
    const val MORE_CONTACT = "more/contact"
    const val MORE_FAQ = "more/faq"
    const val MORE_PRIVACY = "more/privacy"
    const val MORE_TERMS = "more/terms"
    const val MORE_RATE_APP = "more/rate_app"

    /** Arg naming which capture path to run — a [com.hima.ai.data.mock.CaptureSource] name. */
    const val CAPTURE_ARG_SOURCE = "source"
    const val CAPTURE_ROUTE = "capture/{$CAPTURE_ARG_SOURCE}"

    fun capture(source: String): String = "capture/$source"

    fun map(reportId: String? = null): String =
        if (reportId.isNullOrBlank()) MAP else "$MAP?$MAP_ARG_REPORT_ID=$reportId"

    /** Query-arg name carrying which report to open. */
    const val REPORT_ARG_ID = "reportId"

    /**
     * The final-report route. [REPORT_ARG_ID] remains optional for graph
     * compatibility, but persisted reports are always opened with their id.
     */
    const val REPORT_ROUTE = "report?$REPORT_ARG_ID={$REPORT_ARG_ID}"

    /** Builds a report route, omitting the optional argument only when no id is supplied. */
    fun report(reportId: String? = null): String =
        if (reportId.isNullOrBlank()) "report" else "report?$REPORT_ARG_ID=$reportId"
}
