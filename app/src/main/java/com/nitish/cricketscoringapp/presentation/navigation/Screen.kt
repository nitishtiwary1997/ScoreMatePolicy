package com.nitish.cricketscoringapp.presentation.navigation

sealed class Screen(val route: String) {
    // ── Existing ──────────────────────────────────────────────────────────────
    object Login        : Screen("login")
    object Home         : Screen("home")
    object CreateMatch  : Screen("create_match")
    object PlayerStats  : Screen("player_stats")
    object Toss         : Screen("toss/{matchId}") {
        fun createRoute(matchId: String) = "toss/$matchId"
    }
    object Scoring      : Screen("scoring/{matchId}") {
        fun createRoute(matchId: String) = "scoring/$matchId"
    }
    object Summary      : Screen("summary/{matchId}") {
        fun createRoute(matchId: String) = "summary/$matchId"
    }

    // ── Tournament ────────────────────────────────────────────────────────────
    object TournamentList     : Screen("tournament_list")
    object CreateTournament   : Screen("create_tournament")
    object TournamentDashboard : Screen("tournament/{tournamentId}") {
        fun createRoute(id: String) = "tournament/$id"
    }
    object TeamManagement     : Screen("tournament/{tournamentId}/teams") {
        fun createRoute(id: String) = "tournament/$id/teams"
    }
    object TeamDetail         : Screen("tournament/{tournamentId}/team/{teamId}") {
        fun createRoute(tId: String, teamId: String) = "tournament/$tId/team/$teamId"
    }
    object PlayerProfile      : Screen("tournament/{tournamentId}/player/{playerId}") {
        fun createRoute(tId: String, playerId: String) = "tournament/$tId/player/$playerId"
    }
    object FixtureList        : Screen("tournament/{tournamentId}/fixtures") {
        fun createRoute(id: String) = "tournament/$id/fixtures"
    }
    object FixtureDetail      : Screen("tournament/{tournamentId}/fixture/{fixtureId}") {
        fun createRoute(tId: String, fId: String) = "tournament/$tId/fixture/$fId"
    }
    object PointsTable        : Screen("tournament/{tournamentId}/points") {
        fun createRoute(id: String) = "tournament/$id/points"
    }
    object KnockoutBracket    : Screen("tournament/{tournamentId}/bracket") {
        fun createRoute(id: String) = "tournament/$id/bracket"
    }
    object TournamentStats    : Screen("tournament/{tournamentId}/stats") {
        fun createRoute(id: String) = "tournament/$id/stats"
    }

    // ── Live Score ────────────────────────────────────────────────────────────
    object LiveScore          : Screen("live_score/{matchId}") {
        fun createRoute(matchId: String) = "live_score/$matchId"
    }

    // ── Cricket Rules ─────────────────────────────────────────────────────────
    object CricketRules       : Screen("cricket_rules")
}
