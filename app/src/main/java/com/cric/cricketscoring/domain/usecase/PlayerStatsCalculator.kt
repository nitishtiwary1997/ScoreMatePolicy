package com.cric.cricketscoring.domain.usecase

import com.cric.cricketscoring.domain.model.Ball
import com.cric.cricketscoring.domain.model.ExtraType
import com.cric.cricketscoring.domain.model.Match
import com.cric.cricketscoring.domain.model.Player
import com.cric.cricketscoring.domain.model.PlayerCareerStats
import com.cric.cricketscoring.domain.model.WicketType

object PlayerStatsCalculator {

    fun calculate(
        matches: List<Match>,
        allPlayers: List<Player>,
        allBalls: List<Ball>
    ): List<PlayerCareerStats> {
        val playerMap = allPlayers.associateBy { it.id }
        val matchMap = matches.associateBy { it.id }
        val ballsByInnings = allBalls.groupBy { it.matchId to it.innings }

        // Keyed by normalised player name so the same person across different
        // matches (each with a unique UUID) is merged into one row.
        val accumulator = mutableMapOf<String, MutableStats>()

        fun nameKey(player: Player) = player.name.trim().lowercase()

        fun getStats(playerId: String): MutableStats? {
            val player = playerMap[playerId] ?: return null
            return accumulator.getOrPut(nameKey(player)) { MutableStats(player) }
        }

        for ((inningsKey, balls) in ballsByInnings) {
            val (matchId, innings) = inningsKey
            matchMap[matchId] ?: continue

            // ── Per-innings batting maps ──────────────────────────────────────
            val batRuns = mutableMapOf<String, Int>()
            val batBalls = mutableMapOf<String, Int>()
            val batFours = mutableMapOf<String, Int>()
            val batSixes = mutableMapOf<String, Int>()
            val batDismissed = mutableSetOf<String>()

            // ── Per-innings bowling maps ──────────────────────────────────────
            val bowlBalls = mutableMapOf<String, Int>()
            val bowlRuns = mutableMapOf<String, Int>()
            val bowlWickets = mutableMapOf<String, Int>()
            // (bowlerId, overNumber) -> (legalBalls, runs) for maiden detection
            val overMap = mutableMapOf<Pair<String, Int>, Pair<Int, Int>>()

            for (ball in balls) {
                // Batting side (wides don't count to batsman balls)
                if (ball.extraType != ExtraType.WIDE) {
                    val br = ball.runsForBatsman
                    batRuns[ball.batsmanId] = (batRuns[ball.batsmanId] ?: 0) + br
                    batBalls[ball.batsmanId] = (batBalls[ball.batsmanId] ?: 0) + 1
                    if (br == 4) batFours[ball.batsmanId] = (batFours[ball.batsmanId] ?: 0) + 1
                    if (br == 6) batSixes[ball.batsmanId] = (batSixes[ball.batsmanId] ?: 0) + 1
                }
                if (ball.isWicket) {
                    batDismissed.add(ball.dismissedPlayerId ?: ball.batsmanId)
                }

                // Bowling side
                val charged = ball.runs + when (ball.extraType) {
                    ExtraType.WIDE, ExtraType.NO_BALL -> ball.extras
                    else -> 0
                }
                bowlRuns[ball.bowlerId] = (bowlRuns[ball.bowlerId] ?: 0) + charged

                if (ball.isLegalDelivery) {
                    bowlBalls[ball.bowlerId] = (bowlBalls[ball.bowlerId] ?: 0) + 1
                    val key = ball.bowlerId to ball.overNumber
                    val prev = overMap[key] ?: (0 to 0)
                    overMap[key] = (prev.first + 1) to (prev.second + charged)
                }

                if (ball.isWicket && ball.wicketType != WicketType.RUN_OUT) {
                    bowlWickets[ball.bowlerId] = (bowlWickets[ball.bowlerId] ?: 0) + 1
                }
            }

            // Maiden overs
            val bowlMaidens = mutableMapOf<String, Int>()
            for ((key, pair) in overMap) {
                if (pair.first == 6 && pair.second == 0) {
                    bowlMaidens[key.first] = (bowlMaidens[key.first] ?: 0) + 1
                }
            }

            // ── Accumulate batting ────────────────────────────────────────────
            for ((id, runs) in batRuns) {
                val s = getStats(id) ?: continue
                val balls = batBalls[id] ?: 0
                val dismissed = batDismissed.contains(id)

                s.matchIds.add(matchId)
                s.battingInnings++
                s.totalRuns += runs
                s.totalBalls += balls
                s.fours += batFours[id] ?: 0
                s.sixes += batSixes[id] ?: 0
                if (!dismissed) s.notOuts++
                if (runs == 0 && dismissed) s.ducks++
                if (runs >= 100) s.hundreds++ else if (runs >= 50) s.fifties++
                if (runs > s.highScore) {
                    s.highScore = runs
                    s.highScoreNotOut = !dismissed
                } else if (runs == s.highScore && !dismissed) {
                    s.highScoreNotOut = true
                }
            }

            // ── Accumulate bowling ────────────────────────────────────────────
            for ((id, legalBalls) in bowlBalls) {
                val s = getStats(id) ?: continue
                val runs = bowlRuns[id] ?: 0
                val wkts = bowlWickets[id] ?: 0
                val maidens = bowlMaidens[id] ?: 0

                s.matchIds.add(matchId)
                s.ballsBowled += legalBalls
                s.runsConceded += runs
                s.wickets += wkts
                s.maidens += maidens

                if (wkts > s.bestBowlingWickets ||
                    (wkts == s.bestBowlingWickets && wkts > 0 && runs < s.bestBowlingRuns)
                ) {
                    s.bestBowlingWickets = wkts
                    s.bestBowlingRuns = runs
                }
                if (wkts >= 5) s.fiveWicketHauls++
                else if (wkts >= 4) s.fourWicketHauls++
            }
        }

        return accumulator.values.map { it.freeze() }
    }

    private class MutableStats(val player: Player) {
        val matchIds = mutableSetOf<String>()
        var battingInnings = 0; var totalRuns = 0; var totalBalls = 0
        var highScore = 0; var highScoreNotOut = false
        var notOuts = 0; var fours = 0; var sixes = 0
        var ducks = 0; var fifties = 0; var hundreds = 0
        var ballsBowled = 0; var runsConceded = 0; var wickets = 0
        var maidens = 0; var bestBowlingWickets = 0; var bestBowlingRuns = 0
        var fourWicketHauls = 0; var fiveWicketHauls = 0

        fun freeze() = PlayerCareerStats(
            player = player, matchIds = matchIds.toSet(),
            battingInnings = battingInnings, totalRuns = totalRuns, totalBalls = totalBalls,
            highScore = highScore, highScoreNotOut = highScoreNotOut,
            notOuts = notOuts, fours = fours, sixes = sixes,
            ducks = ducks, fifties = fifties, hundreds = hundreds,
            ballsBowled = ballsBowled, runsConceded = runsConceded, wickets = wickets,
            maidens = maidens, bestBowlingWickets = bestBowlingWickets,
            bestBowlingRuns = bestBowlingRuns,
            fourWicketHauls = fourWicketHauls, fiveWicketHauls = fiveWicketHauls
        )
    }
}
