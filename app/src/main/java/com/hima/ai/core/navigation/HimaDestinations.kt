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
    const val REPORT = "report"
    const val INVESTIGATION = "investigation"
    const val HISTORY = "history"
}
