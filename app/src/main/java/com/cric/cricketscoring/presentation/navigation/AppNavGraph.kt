package com.cric.cricketscoring.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cric.cricketscoring.presentation.createMatch.CreateMatchScreen
import com.cric.cricketscoring.presentation.home.HomeScreen
import com.cric.cricketscoring.presentation.login.LoginScreen
import com.cric.cricketscoring.presentation.scoring.ScoringScreen
import com.cric.cricketscoring.presentation.stats.PlayerStatsScreen
import com.cric.cricketscoring.presentation.summary.MatchSummaryScreen
import com.cric.cricketscoring.presentation.toss.TossScreen
import com.cric.cricketscoring.presentation.tournament.create.CreateTournamentScreen
import com.cric.cricketscoring.presentation.tournament.list.TournamentListScreen
import com.cric.cricketscoring.presentation.tournament.dashboard.TournamentDashboardScreen
import com.cric.cricketscoring.presentation.tournament.fixture.FixtureDetailScreen
import com.cric.cricketscoring.presentation.tournament.fixture.FixtureListScreen
import com.cric.cricketscoring.presentation.tournament.points.PointsTableScreen
import com.cric.cricketscoring.presentation.livescore.LiveScoreScreen
import com.cric.cricketscoring.presentation.tournament.bracket.KnockoutBracketScreen
import com.cric.cricketscoring.presentation.tournament.stats.TournamentStatsScreen
import com.cric.cricketscoring.presentation.tournament.team.PlayerProfileScreen
import com.cric.cricketscoring.presentation.tournament.team.TeamDetailScreen
import com.cric.cricketscoring.presentation.tournament.team.TeamManagementScreen
import com.cric.cricketscoring.presentation.rules.CricketRulesScreen

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
                onPlayerStats = { navController.navigate(Screen.PlayerStats.route) },
                onTournaments = { navController.navigate(Screen.TournamentList.route) }
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

        // ── Tournament Routes ─────────────────────────────────────────────────

        composable(Screen.TournamentList.route) {
            TournamentListScreen(
                onBack = { navController.popBackStack() },
                onCreateTournament = { navController.navigate(Screen.CreateTournament.route) },
                onTournamentClick = { tournamentId ->
                    navController.navigate(Screen.TournamentDashboard.createRoute(tournamentId))
                }
            )
        }

        composable(Screen.CreateTournament.route) {
            CreateTournamentScreen(
                onBack = { navController.popBackStack() },
                onTournamentCreated = { tournamentId ->
                    navController.navigate(Screen.TournamentDashboard.createRoute(tournamentId)) {
                        popUpTo(Screen.TournamentList.route)
                    }
                }
            )
        }

        composable(
            route = Screen.TournamentDashboard.route,
            arguments = listOf(navArgument("tournamentId") { type = NavType.StringType })
        ) {
            val tournamentId = it.arguments?.getString("tournamentId") ?: return@composable
            TournamentDashboardScreen(
                onBack = { navController.popBackStack() },
                onTeams = { navController.navigate(Screen.TeamManagement.createRoute(tournamentId)) },
                onFixtures = { navController.navigate(Screen.FixtureList.createRoute(tournamentId)) },
                onPointsTable = { navController.navigate(Screen.PointsTable.createRoute(tournamentId)) },
                onKnockoutBracket = { navController.navigate(Screen.KnockoutBracket.createRoute(tournamentId)) },
                onStats = { navController.navigate(Screen.TournamentStats.createRoute(tournamentId)) },
                onLiveFixture = { matchId -> navController.navigate(Screen.Scoring.createRoute(matchId)) }
            )
        }

        composable(
            route = Screen.TeamManagement.route,
            arguments = listOf(navArgument("tournamentId") { type = NavType.StringType })
        ) {
            val tournamentId = it.arguments?.getString("tournamentId") ?: return@composable
            TeamManagementScreen(
                onBack = { navController.popBackStack() },
                onTeamClick = { teamId ->
                    navController.navigate(Screen.TeamDetail.createRoute(tournamentId, teamId))
                }
            )
        }

        composable(
            route = Screen.TeamDetail.route,
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.StringType },
                navArgument("teamId") { type = NavType.StringType }
            )
        ) {
            val tournamentId = it.arguments?.getString("tournamentId") ?: return@composable
            TeamDetailScreen(
                onBack = { navController.popBackStack() },
                onPlayerClick = { playerId ->
                    navController.navigate(Screen.PlayerProfile.createRoute(tournamentId, playerId))
                }
            )
        }

        composable(
            route = Screen.PlayerProfile.route,
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.StringType },
                navArgument("playerId") { type = NavType.StringType }
            )
        ) {
            PlayerProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.FixtureList.route,
            arguments = listOf(navArgument("tournamentId") { type = NavType.StringType })
        ) {
            val tournamentId = it.arguments?.getString("tournamentId") ?: return@composable
            FixtureListScreen(
                onBack = { navController.popBackStack() },
                onFixtureClick = { fixtureId ->
                    navController.navigate(Screen.FixtureDetail.createRoute(tournamentId, fixtureId))
                }
            )
        }

        composable(
            route = Screen.FixtureDetail.route,
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.StringType },
                navArgument("fixtureId") { type = NavType.StringType }
            )
        ) {
            FixtureDetailScreen(
                onBack = { navController.popBackStack() },
                onMatchStarted = { matchId ->
                    navController.navigate(Screen.Toss.createRoute(matchId)) {
                        popUpTo(Screen.FixtureDetail.route)
                    }
                },
                onMatchClick = { matchId ->
                    navController.navigate(Screen.Scoring.createRoute(matchId))
                },
                onWatchLive = { matchId ->
                    navController.navigate(Screen.LiveScore.createRoute(matchId))
                }
            )
        }

        composable(
            route = Screen.LiveScore.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) {
            LiveScoreScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.PointsTable.route,
            arguments = listOf(navArgument("tournamentId") { type = NavType.StringType })
        ) {
            PointsTableScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.KnockoutBracket.route,
            arguments = listOf(navArgument("tournamentId") { type = NavType.StringType })
        ) {
            KnockoutBracketScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.TournamentStats.route,
            arguments = listOf(navArgument("tournamentId") { type = NavType.StringType })
        ) {
            TournamentStatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.CricketRules.route) {
            CricketRulesScreen(onBack = { navController.popBackStack() })
        }
    }
}
