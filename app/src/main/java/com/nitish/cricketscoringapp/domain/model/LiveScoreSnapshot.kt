package com.nitish.cricketscoringapp.domain.model

/**
 * Flat Firestore document stored at live_scores/{matchId}.
 * All values are primitive so Firestore can serialize/deserialize without a converter.
 * Pre-formatted display strings (runRate, requiredRunRate, strikerSR, bowlerEcon)
 * are computed by the scorer device to keep the viewer read-only.
 */
data class LiveScoreSnapshot(
    val matchId: String = "",
    val tournamentId: String = "",
    val fixtureId: String = "",

    // Teams
    val team1Name: String = "",
    val team2Name: String = "",
    val currentInnings: Int = 1,

    // Innings 1
    val inn1BattingTeam: String = "",
    val inn1Runs: Int = 0,
    val inn1Wickets: Int = 0,
    val inn1Overs: String = "0.0",
    val inn1IsCompleted: Boolean = false,

    // Innings 2
    val inn2BattingTeam: String = "",
    val inn2Runs: Int = 0,
    val inn2Wickets: Int = 0,
    val inn2Overs: String = "0.0",
    val target: Int = 0,
    val requiredRuns: Int = 0,
    val requiredBalls: Int = 0,

    // Run rates (pre-formatted)
    val currentRunRate: String = "0.00",
    val requiredRunRate: String = "-",

    // On-strike batsman
    val strikerName: String = "",
    val strikerRuns: Int = 0,
    val strikerBalls: Int = 0,
    val strikerFours: Int = 0,
    val strikerSixes: Int = 0,
    val strikerSR: String = "0.0",

    // Non-striker
    val nonStrikerName: String = "",
    val nonStrikerRuns: Int = 0,
    val nonStrikerBalls: Int = 0,

    // Current bowler
    val bowlerName: String = "",
    val bowlerWickets: Int = 0,
    val bowlerRuns: Int = 0,
    val bowlerOvers: String = "0.0",
    val bowlerEcon: String = "0.00",

    val lastBallDesc: String = "",
    val status: String = "LIVE",   // LIVE | COMPLETED | INNINGS_BREAK
    val updatedAt: Long = 0L
)
