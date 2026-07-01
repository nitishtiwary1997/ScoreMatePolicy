package com.cric.cricketscoring.domain.model

data class PlayerCareerStats(
    val player: Player,
    val matchIds: Set<String> = emptySet(),
    // Batting
    val battingInnings: Int = 0,
    val totalRuns: Int = 0,
    val totalBalls: Int = 0,
    val highScore: Int = 0,
    val highScoreNotOut: Boolean = false,
    val notOuts: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val ducks: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    // Bowling
    val ballsBowled: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val maidens: Int = 0,
    val bestBowlingWickets: Int = 0,
    val bestBowlingRuns: Int = 0,
    val fourWicketHauls: Int = 0,
    val fiveWicketHauls: Int = 0
) {
    val matchesPlayed: Int get() = matchIds.size

    val battingAverage: Double
        get() {
            val dismissals = battingInnings - notOuts
            return if (dismissals == 0) totalRuns.toDouble() else totalRuns.toDouble() / dismissals
        }

    val strikeRate: Double
        get() = if (totalBalls == 0) 0.0 else totalRuns * 100.0 / totalBalls

    val economy: Double
        get() = if (ballsBowled == 0) 0.0 else runsConceded * 6.0 / ballsBowled

    val bowlingAverage: Double
        get() = if (wickets == 0) 0.0 else runsConceded.toDouble() / wickets

    val bowlingStrikeRate: Double
        get() = if (wickets == 0) 0.0 else ballsBowled.toDouble() / wickets

    val oversBowled: String
        get() = "${ballsBowled / 6}.${ballsBowled % 6}"

    val highScoreDisplay: String
        get() = if (highScoreNotOut) "$highScore*" else "$highScore"

    val bestBowlingDisplay: String
        get() = if (bestBowlingWickets == 0) "-" else "$bestBowlingWickets/$bestBowlingRuns"

    val isBatter: Boolean get() = battingInnings > 0
    val isBowler: Boolean get() = ballsBowled > 0
    val isAllRounder: Boolean get() = isBatter && isBowler
}
