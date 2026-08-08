package com.hima.ai.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hima.ai.presentation.auth.LoginScreen
import com.hima.ai.presentation.auth.SignUpScreen
import com.hima.ai.presentation.history.HistoryScreen
import com.hima.ai.presentation.home.HomeScreen
import com.hima.ai.presentation.map.MapScreen
import com.hima.ai.presentation.report.analysis.AnalysisScreen
import com.hima.ai.presentation.report.detail.ReportDetailScreen
import com.hima.ai.presentation.report.investigation.InvestigationScreen
import com.hima.ai.presentation.report.newreport.NewReportScreen
import com.hima.ai.presentation.splash.SplashScreen

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
    fun goHome() {
        navController.navigate(HimaDestinations.HOME) {
            popUpTo(HimaDestinations.HOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Switching to the Map tab behaves the same way — no stacked duplicates.
    fun goMap() {
        navController.navigate(HimaDestinations.MAP) {
            popUpTo(HimaDestinations.HOME)
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = HimaDestinations.SPLASH,
        modifier = modifier,
    ) {
        composable(HimaDestinations.SPLASH) {
            SplashScreen(
                onGetStarted = {
                    navController.navigate(HimaDestinations.LOGIN) {
                        popUpTo(HimaDestinations.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(HimaDestinations.LOGIN) {
            LoginScreen(
                onSignInSuccess = {
                    navController.navigate(HimaDestinations.HOME) {
                        popUpTo(HimaDestinations.LOGIN) { inclusive = true }
                    }
                },
                onCreateAccountClick = { navController.navigate(HimaDestinations.SIGN_UP) },
            )
        }

        composable(HimaDestinations.SIGN_UP) {
            SignUpScreen(
                onAccountCreated = {
                    navController.navigate(HimaDestinations.HOME) {
                        popUpTo(HimaDestinations.LOGIN) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onSignInInsteadClick = { navController.popBackStack() },
            )
        }

        composable(HimaDestinations.HOME) {
            HomeScreen(
                onNewReportClick = { navController.navigate(HimaDestinations.NEW_REPORT) },
                onReportClick = { navController.navigate(HimaDestinations.REPORT) },
                onViewAllClick = { navController.navigate(HimaDestinations.HISTORY) },
                onMapClick = ::goMap,
                onMoreClick = {},
            )
        }

        composable(HimaDestinations.MAP) {
            MapScreen(
                onHomeClick = ::goHome,
                onNewReportClick = { navController.navigate(HimaDestinations.NEW_REPORT) },
                onReportsClick = { navController.navigate(HimaDestinations.HISTORY) },
                onMoreClick = {},
                onViewReportClick = { navController.navigate(HimaDestinations.REPORT) },
            )
        }

        composable(HimaDestinations.NEW_REPORT) {
            NewReportScreen(
                onBackClick = { navController.popBackStack() },
                onAnalyzeClick = { navController.navigate(HimaDestinations.ANALYSIS) },
            )
        }

        composable(HimaDestinations.ANALYSIS) {
            AnalysisScreen(
                onBackClick = { navController.popBackStack() },
                onAnalysisComplete = {
                    // Drop the capture + analysis steps so Back from the report
                    // returns to Home rather than replaying the flow.
                    navController.navigate(HimaDestinations.REPORT) {
                        popUpTo(HimaDestinations.NEW_REPORT) { inclusive = true }
                    }
                },
            )
        }

        composable(HimaDestinations.REPORT) {
            ReportDetailScreen(
                onBackClick = { navController.popBackStack() },
                onInvestigateClick = { navController.navigate(HimaDestinations.INVESTIGATION) },
            )
        }

        composable(HimaDestinations.INVESTIGATION) {
            InvestigationScreen(
                onBackClick = { navController.popBackStack() },
                onReportUpdated = { navController.popBackStack() },
            )
        }

        composable(HimaDestinations.HISTORY) {
            HistoryScreen(
                onBackClick = { navController.popBackStack() },
                onReportClick = { navController.navigate(HimaDestinations.REPORT) },
                onHomeClick = ::goHome,
                onNewReportClick = { navController.navigate(HimaDestinations.NEW_REPORT) },
                onMapClick = ::goMap,
                onMoreClick = {},
            )
        }
    }
}
