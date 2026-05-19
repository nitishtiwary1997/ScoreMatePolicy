package com.nitish.cricketscoringapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nitish.cricketscoringapp.presentation.createMatch.CreateMatchScreen
import com.nitish.cricketscoringapp.presentation.home.HomeScreen
import com.nitish.cricketscoringapp.presentation.login.LoginScreen
import com.nitish.cricketscoringapp.presentation.scoring.ScoringScreen
import com.nitish.cricketscoringapp.presentation.stats.PlayerStatsScreen
import com.nitish.cricketscoringapp.presentation.summary.MatchSummaryScreen
import com.nitish.cricketscoringapp.presentation.toss.TossScreen

@Composable
fun AppNavGraph(navController: NavHostController, startOnLogin: Boolean) {
    val startDestination = if (startOnLogin) Screen.Login.route else Screen.Home.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onSignedIn = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNewMatch = { navController.navigate(Screen.CreateMatch.route) },
                onMatchClick = { matchId, status ->
                    val route = when (status) {
                        "TOSS" -> Screen.Toss.createRoute(matchId)
                        "INNINGS_1", "INNINGS_2" -> Screen.Scoring.createRoute(matchId)
                        "COMPLETED" -> Screen.Summary.createRoute(matchId)
                        else -> Screen.Scoring.createRoute(matchId)
                    }
                    navController.navigate(route)
                },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onPlayerStats = { navController.navigate(Screen.PlayerStats.route) }
            )
        }

        composable(Screen.PlayerStats.route) {
            PlayerStatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.CreateMatch.route) {
            CreateMatchScreen(
                onMatchCreated = { matchId ->
                    navController.navigate(Screen.Toss.createRoute(matchId)) {
                        popUpTo(Screen.CreateMatch.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Toss.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) {
            TossScreen(
                onMatchStarted = { matchId ->
                    navController.navigate(Screen.Scoring.createRoute(matchId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Scoring.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) {
            ScoringScreen(
                onMatchComplete = { matchId ->
                    navController.navigate(Screen.Summary.createRoute(matchId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Summary.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) {
            MatchSummaryScreen(
                onBack = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) } }
            )
        }
    }
}
