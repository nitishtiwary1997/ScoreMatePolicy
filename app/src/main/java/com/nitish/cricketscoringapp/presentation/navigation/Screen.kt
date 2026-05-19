package com.nitish.cricketscoringapp.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object CreateMatch : Screen("create_match")
    object Toss : Screen("toss/{matchId}") {
        fun createRoute(matchId: String) = "toss/$matchId"
    }
    object Scoring : Screen("scoring/{matchId}") {
        fun createRoute(matchId: String) = "scoring/$matchId"
    }
    object Summary : Screen("summary/{matchId}") {
        fun createRoute(matchId: String) = "summary/$matchId"
    }
    object PlayerStats : Screen("player_stats")
}
