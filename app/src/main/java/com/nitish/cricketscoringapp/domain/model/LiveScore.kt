package com.nitish.cricketscoringapp.domain.model

data class BatsmanScore(
    val player: Player,
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isOnStrike: Boolean = false,
    val dismissalInfo: String? = null
) {
    val strikeRate: Float
        get() = if (balls == 0) 0f else (runs.toFloat() / balls) * 100f

    val isOut: Boolean get() = dismissalInfo != null
}

data class BowlerStats(
    val player: Player,
    val totalLegalBalls: Int = 0,
    val runs: Int = 0,
    val wickets: Int = 0,
    val wides: Int = 0,
    val noBalls: Int = 0,
    val maidens: Int = 0
) {
    val overs: Int get() = totalLegalBalls / 6
    val ballsInCurrentOver: Int get() = totalLegalBalls % 6
    val oversDisplay: String get() = "$overs.$ballsInCurrentOver"
    val economy: Float
        get() {
            val totalOversFloat = overs + ballsInCurrentOver / 6f
            return if (totalOversFloat == 0f) 0f else runs / totalOversFloat
        }
}

data class FallOfWicket(
    val wicketNumber: Int,
    val playerName: String,
    val dismissalInfo: String,
    val score: Int,
    val overDisplay: String
)

data class InningsScore(
    val inningsNumber: Int,
    val battingTeamName: String,
    val bowlingTeamName: String,
    val totalOvers: Int,
    val totalRuns: Int = 0,
    val wickets: Int = 0,
    val completedOvers: Int = 0,
    val legalBallsInCurrentOver: Int = 0,
    val wides: Int = 0,
    val noBalls: Int = 0,
    val byes: Int = 0,
    val legByes: Int = 0,
    val batsmen: List<BatsmanScore> = emptyList(),
    val bowlers: List<BowlerStats> = emptyList(),
    val fallOfWickets: List<FallOfWicket> = emptyList(),
    val currentBatsmanOnStrikeId: String = "",
    val currentBatsmanOffStrikeId: String = "",
    val currentBowlerId: String = "",
    val isCompleted: Boolean = false,
    val target: Int? = null,
    val lastBallDescription: String = ""
) {
    val extras: Int get() = wides + noBalls + byes + legByes
    val oversDisplay: String get() = "$completedOvers.$legalBallsInCurrentOver"
    val oversRemaining: Int get() = totalOvers - completedOvers
    val runRate: Float
        get() {
            val total = completedOvers + legalBallsInCurrentOver / 6f
            return if (total == 0f) 0f else totalRuns / total
        }
    val requiredRuns: Int? get() = target?.let { (it - totalRuns).coerceAtLeast(0) }
    val requiredBallsRemaining: Int
        get() = (totalOvers * 6) - (completedOvers * 6 + legalBallsInCurrentOver)
    val requiredRunRate: Float?
        get() {
            val rr = requiredRuns ?: return null
            val balls = requiredBallsRemaining
            return if (balls == 0) 0f else (rr.toFloat() / balls) * 6
        }
}
