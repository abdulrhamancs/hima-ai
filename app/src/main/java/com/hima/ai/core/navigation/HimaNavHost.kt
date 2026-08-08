package com.hima.ai.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hima.ai.core.designsystem.theme.HimaEasing
import com.hima.ai.core.designsystem.theme.HimaMotionDuration
import com.hima.ai.presentation.auth.LoginScreen
import com.hima.ai.presentation.auth.SignUpScreen
import com.hima.ai.presentation.history.HistoryScreen
import com.hima.ai.presentation.home.HomeScreen
import com.hima.ai.presentation.map.MapScreen
import com.hima.ai.presentation.more.MoreScreen
import com.hima.ai.presentation.report.analysis.AnalysisScreen
import com.hima.ai.presentation.report.detail.ReportDetailScreen
import com.hima.ai.presentation.report.investigation.InvestigationScreen
import com.hima.ai.presentation.report.newreport.NewReportScreen
import com.hima.ai.presentation.splash.SplashScreen

// One consistent crossfade for every screen change in the app — no slide,
// no scale, just opacity on a smooth ease so it stays quick and unobtrusive.
private val ScreenFadeSpec = tween<Float>(HimaMotionDuration.ScreenPush, easing = HimaEasing)

/**
 * True while this entry is still on screen rather than already torn down.
 *
 * Guards the delayed confirmations (save, "applied to report") from firing
 * after the user has pressed Back, which would pop one screen too many.
 *
 * Deliberately STARTED rather than RESUMED: an entry only becomes RESUMED once
 * its enter transition finishes, so a RESUMED check silently swallows any tap
 * made during the screen's fade-in. Duplicate pushes from a fast double-tap are
 * handled by launchSingleTop instead, which costs nothing when the tap is real.
 */
private fun NavBackStackEntry.isCurrent(): Boolean =
    lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

/**
 * The prototype's navigation graph. Every screen is reachable and every back
 * action returns somewhere sensible, so the flow can be walked end to end.
 */
@Composable
fun HimaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    // Returning to Home clears the flow above it rather than stacking copies.
    fun goHome(from: NavBackStackEntry) {
        if (!from.isCurrent()) return
        navController.navigate(HimaDestinations.HOME) {
            popUpTo(HimaDestinations.HOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Every bottom-nav tab resolves back onto Home rather than stacking, so
    // Back from any tab leaves the app instead of replaying the hops taken.
    fun goTab(from: NavBackStackEntry, route: String) {
        if (!from.isCurrent()) return
        navController.navigate(route) {
            popUpTo(HimaDestinations.HOME)
            launchSingleTop = true
        }
    }

    // launchSingleTop keeps a fast double-tap from stacking the same screen
    // twice. Distinct routes (two different reports) still both open.
    fun push(from: NavBackStackEntry, route: String) {
        if (!from.isCurrent()) return
        navController.navigate(route) { launchSingleTop = true }
    }

    fun back(from: NavBackStackEntry) {
        if (!from.isCurrent()) return
        navController.popBackStack()
    }

    NavHost(
        navController = navController,
        startDestination = HimaDestinations.SPLASH,
        modifier = modifier,
        enterTransition = { fadeIn(ScreenFadeSpec) },
        exitTransition = { fadeOut(ScreenFadeSpec) },
        popEnterTransition = { fadeIn(ScreenFadeSpec) },
        popExitTransition = { fadeOut(ScreenFadeSpec) },
    ) {
        composable(HimaDestinations.SPLASH) { entry ->
            SplashScreen(
                onGetStarted = {
                    if (entry.isCurrent()) {
                        navController.navigate(HimaDestinations.LOGIN) {
                            popUpTo(HimaDestinations.SPLASH) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(HimaDestinations.LOGIN) { entry ->
            LoginScreen(
                onSignInSuccess = {
                    if (entry.isCurrent()) {
                        navController.navigate(HimaDestinations.HOME) {
                            popUpTo(HimaDestinations.LOGIN) { inclusive = true }
                        }
                    }
                },
                onCreateAccountClick = { push(entry, HimaDestinations.SIGN_UP) },
            )
        }

        composable(HimaDestinations.SIGN_UP) { entry ->
            SignUpScreen(
                onAccountCreated = {
                    if (entry.isCurrent()) {
                        navController.navigate(HimaDestinations.HOME) {
                            popUpTo(HimaDestinations.LOGIN) { inclusive = true }
                        }
                    }
                },
                onBackClick = { back(entry) },
                onSignInInsteadClick = { back(entry) },
            )
        }

        composable(HimaDestinations.HOME) { entry ->
            HomeScreen(
                onNewReportClick = { push(entry, HimaDestinations.NEW_REPORT) },
                onReportClick = { id -> push(entry, HimaDestinations.report(id)) },
                onViewAllClick = { goTab(entry, HimaDestinations.HISTORY) },
                onMapClick = { goTab(entry, HimaDestinations.MAP) },
                onMoreClick = { goTab(entry, HimaDestinations.MORE) },
            )
        }

        composable(HimaDestinations.MAP) { entry ->
            MapScreen(
                onHomeClick = { goHome(entry) },
                onNewReportClick = { push(entry, HimaDestinations.NEW_REPORT) },
                onReportsClick = { goTab(entry, HimaDestinations.HISTORY) },
                onMoreClick = { goTab(entry, HimaDestinations.MORE) },
                onViewReportClick = { id -> push(entry, HimaDestinations.report(id)) },
            )
        }

        composable(HimaDestinations.NEW_REPORT) { entry ->
            NewReportScreen(
                onBackClick = { back(entry) },
                onAnalyzeClick = { push(entry, HimaDestinations.ANALYSIS) },
            )
        }

        composable(HimaDestinations.ANALYSIS) { entry ->
            AnalysisScreen(
                onBackClick = { back(entry) },
                onAnalysisComplete = {
                    // Drop the capture + analysis steps so Back from the report
                    // returns to Home rather than replaying the flow. No id —
                    // this is the report the flow just produced.
                    if (entry.isCurrent()) {
                        navController.navigate(HimaDestinations.report()) {
                            popUpTo(HimaDestinations.NEW_REPORT) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(
            route = HimaDestinations.REPORT_ROUTE,
            arguments = listOf(
                navArgument(HimaDestinations.REPORT_ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            ReportDetailScreen(
                onBackClick = { back(entry) },
                onInvestigateClick = { push(entry, HimaDestinations.INVESTIGATION) },
                onViewOnMapClick = { goTab(entry, HimaDestinations.MAP) },
                onSavedClick = {
                    // Saving completes the flow, so land on History with the
                    // saved report rather than leaving the user on a dead end.
                    goTab(entry, HimaDestinations.HISTORY)
                },
            )
        }

        composable(HimaDestinations.INVESTIGATION) { entry ->
            InvestigationScreen(
                onBackClick = { back(entry) },
                onReportUpdated = { back(entry) },
            )
        }

        composable(HimaDestinations.HISTORY) { entry ->
            HistoryScreen(
                onBackClick = { back(entry) },
                onReportClick = { id -> push(entry, HimaDestinations.report(id)) },
                onHomeClick = { goHome(entry) },
                onNewReportClick = { push(entry, HimaDestinations.NEW_REPORT) },
                onMapClick = { goTab(entry, HimaDestinations.MAP) },
                onMoreClick = { goTab(entry, HimaDestinations.MORE) },
            )
        }

        composable(HimaDestinations.MORE) { entry ->
            MoreScreen(
                onSignOutClick = {
                    // Clear the whole graph so Back cannot re-enter the app
                    // after signing out.
                    if (entry.isCurrent()) {
                        navController.navigate(HimaDestinations.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                },
                onHomeClick = { goHome(entry) },
                onMapClick = { goTab(entry, HimaDestinations.MAP) },
                onNewReportClick = { push(entry, HimaDestinations.NEW_REPORT) },
                onReportsClick = { goTab(entry, HimaDestinations.HISTORY) },
            )
        }
    }
}
