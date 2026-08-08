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
    const val NEW_REPORT = "new_report"
    const val ANALYSIS = "analysis"
    const val INVESTIGATION = "investigation"
    const val HISTORY = "history"
    const val MORE = "more"

    /** Arg naming which capture path to run — a [com.hima.ai.data.mock.CaptureSource] name. */
    const val CAPTURE_ARG_SOURCE = "source"
    const val CAPTURE_ROUTE = "capture/{$CAPTURE_ARG_SOURCE}"

    fun capture(source: String): String = "capture/$source"

    /** Query-arg name carrying which report to open. */
    const val REPORT_ARG_ID = "reportId"

    /**
     * The final-report route. [REPORT_ARG_ID] is optional: it is absent for a
     * report that was just analysed (the flow's own result) and present when
     * opening an existing report from Home, History, or a map marker.
     */
    const val REPORT_ROUTE = "report?$REPORT_ARG_ID={$REPORT_ARG_ID}"

    /** Builds the report route, omitting the argument for a freshly analysed report. */
    fun report(reportId: String? = null): String =
        if (reportId.isNullOrBlank()) "report" else "report?$REPORT_ARG_ID=$reportId"
}
