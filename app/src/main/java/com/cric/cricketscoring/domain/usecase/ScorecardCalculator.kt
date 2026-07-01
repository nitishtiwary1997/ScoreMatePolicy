package com.cric.cricketscoring.domain.usecase

import com.cric.cricketscoring.domain.model.*

object ScorecardCalculator {

    fun calculateInnings(
        inningsNumber: Int,
        balls: List<Ball>,
        players: List<Player>,
        battingTeamName: String,
        bowlingTeamName: String,
        onStrikeId: String,
        offStrikeId: String,
        currentBowlerId: String,
        isCompleted: Boolean,
        totalOvers: Int,
        target: Int? = null
    ): InningsScore {
        val playerMap = players.associateBy { it.id }

        var totalRuns = 0
        var wickets = 0
        var wides = 0
        var noBalls = 0
        var byes = 0
        var legByes = 0

        val batsmanRunsMap = mutableMapOf<String, Int>()
        val batsmanBallsMap = mutableMapOf<String, Int>()
        val batsmanFoursMap = mutableMapOf<String, Int>()
        val batsmanSixesMap = mutableMapOf<String, Int>()
        val batsmanDismissalMap = mutableMapOf<String, String>()

        val bowlerLegalBallsMap = mutableMapOf<String, Int>()
        val bowlerRunsMap = mutableMapOf<String, Int>()
        val bowlerWicketsMap = mutableMapOf<String, Int>()
        val bowlerWidesMap = mutableMapOf<String, Int>()
        val bowlerNoBallsMap = mutableMapOf<String, Int>()
        // keyed by (bowlerId, overNumber) — for maiden detection
        val overRunsMap = mutableMapOf<Pair<String, Int>, Int>()
        val overLegalBallsMap = mutableMapOf<Pair<String, Int>, Int>()

        var totalLegalBalls = 0
        val fowList = mutableListOf<FallOfWicket>()

        for (ball in balls) {
            totalRuns += ball.totalRuns

            when (ball.extraType) {
                ExtraType.WIDE -> {
                    wides += ball.extras
                    bowlerWidesMap[ball.bowlerId] = (bowlerWidesMap[ball.bowlerId] ?: 0) + 1
                }
                ExtraType.NO_BALL -> {
                    noBalls += 1
                    bowlerNoBallsMap[ball.bowlerId] = (bowlerNoBallsMap[ball.bowlerId] ?: 0) + 1
                }
                ExtraType.BYE -> byes += ball.extras
                ExtraType.LEG_BYE -> legByes += ball.extras
                null -> Unit
            }

            // Bowler charged runs: all except byes/leg-byes
            val bowlerChargedRuns = ball.runs + when (ball.extraType) {
                ExtraType.WIDE, ExtraType.NO_BALL -> ball.extras
                else -> 0
            }
            bowlerRunsMap[ball.bowlerId] = (bowlerRunsMap[ball.bowlerId] ?: 0) + bowlerChargedRuns

            val overKey = Pair(ball.bowlerId, ball.overNumber)
            overRunsMap[overKey] = (overRunsMap[overKey] ?: 0) + bowlerChargedRuns

            if (ball.isLegalDelivery) {
                totalLegalBalls++
                bowlerLegalBallsMap[ball.bowlerId] = (bowlerLegalBallsMap[ball.bowlerId] ?: 0) + 1
                overLegalBallsMap[overKey] = (overLegalBallsMap[overKey] ?: 0) + 1
            }

            // Batsman stats — wides don't count to batsman
            if (ball.extraType != ExtraType.WIDE) {
                val batsmanRuns = ball.runsForBatsman
                batsmanRunsMap[ball.batsmanId] = (batsmanRunsMap[ball.batsmanId] ?: 0) + batsmanRuns
                batsmanBallsMap[ball.batsmanId] = (batsmanBallsMap[ball.batsmanId] ?: 0) + 1
                if (batsmanRuns == 4) batsmanFoursMap[ball.batsmanId] = (batsmanFoursMap[ball.batsmanId] ?: 0) + 1
                if (batsmanRuns == 6) batsmanSixesMap[ball.batsmanId] = (batsmanSixesMap[ball.batsmanId] ?: 0) + 1
            }

            if (ball.isWicket) {
                val dismissedId = ball.dismissedPlayerId ?: ball.batsmanId
                val bowlerName = playerMap[ball.bowlerId]?.name ?: ""
                val f1Id = ball.fielderIds.getOrNull(0)
                val f2Id = ball.fielderIds.getOrNull(1)
                val f1Name = f1Id?.let { playerMap[it]?.name }.orEmpty()
                val f2Name = f2Id?.let { playerMap[it]?.name }

                when (ball.wicketType) {
                    WicketType.RETIRED_HURT -> {
                        // Not out — no wicket, no FoW, no bowler credit. Can return later.
                        batsmanDismissalMap[dismissedId] = "retired hurt"
                    }
                    WicketType.RETIRED_OUT -> {
                        // Out — counts as wicket, FoW recorded. Bowler does NOT get credit
                        // (same convention as run out).
                        wickets++
                        val dismissalDesc = "retired out"
                        batsmanDismissalMap[dismissedId] = dismissalDesc
                        val fowOver = "${totalLegalBalls / 6}.${totalLegalBalls % 6}"
                        fowList.add(FallOfWicket(
                            wicketNumber  = wickets,
                            playerName    = playerMap[dismissedId]?.name ?: "",
                            dismissalInfo = dismissalDesc,
                            score         = totalRuns,
                            overDisplay   = fowOver
                        ))
                    }
                    else -> {
                        wickets++
                        val dismissalDesc = when (ball.wicketType) {
                            WicketType.BOWLED     -> "b $bowlerName"
                            WicketType.CAUGHT     -> if (f1Id == null || f1Id == ball.bowlerId) "c & b $bowlerName"
                                                    else "c $f1Name b $bowlerName"
                            WicketType.LBW        -> "lbw b $bowlerName"
                            WicketType.RUN_OUT    -> when {
                                                        f1Name.isEmpty() -> "run out"
                                                        f2Name != null   -> "run out ($f1Name/$f2Name)"
                                                        else             -> "run out ($f1Name)"
                                                    }
                            WicketType.STUMPED    -> if (f1Name.isEmpty()) "st b $bowlerName"
                                                    else "st $f1Name b $bowlerName"
                            WicketType.HIT_WICKET -> "hit wkt b $bowlerName"
                            else                  -> "out"
                        }
                        batsmanDismissalMap[dismissedId] = dismissalDesc
                        if (ball.wicketType != WicketType.RUN_OUT) {
                            bowlerWicketsMap[ball.bowlerId] = (bowlerWicketsMap[ball.bowlerId] ?: 0) + 1
                        }
                        val fowOver = "${totalLegalBalls / 6}.${totalLegalBalls % 6}"
                        fowList.add(FallOfWicket(
                            wicketNumber  = wickets,
                            playerName    = playerMap[dismissedId]?.name ?: "",
                            dismissalInfo = dismissalDesc,
                            score         = totalRuns,
                            overDisplay   = fowOver
                        ))
                    }
                }
            }
        }

        val completedOvers = totalLegalBalls / 6
        val legalBallsInCurrentOver = totalLegalBalls % 6

        // Batsmen who have been at the crease
        val allBatsmanIds = (batsmanRunsMap.keys + setOf(onStrikeId, offStrikeId))
            .filter { it.isNotEmpty() }.distinct()

        val batsmen = allBatsmanIds.mapNotNull { id ->
            val player = playerMap[id] ?: return@mapNotNull null
            // If the batsman is currently active (returned after retire hurt), clear dismissal
            val activeAtCrease = id == onStrikeId || id == offStrikeId
            val rawDismissal = batsmanDismissalMap[id]
            val dismissalInfo = if (rawDismissal == "retired hurt" && activeAtCrease) null else rawDismissal
            BatsmanScore(
                player = player,
                runs = batsmanRunsMap[id] ?: 0,
                balls = batsmanBallsMap[id] ?: 0,
                fours = batsmanFoursMap[id] ?: 0,
                sixes = batsmanSixesMap[id] ?: 0,
                isOnStrike = id == onStrikeId,
                dismissalInfo = dismissalInfo
            )
        }

        // Maiden overs: complete overs (6 legal balls) where bowler conceded 0 runs
        val bowlerMaidensMap = mutableMapOf<String, Int>()
        for ((key, legalBalls) in overLegalBallsMap) {
            if (legalBalls == 6 && (overRunsMap[key] ?: 0) == 0) {
                val bowlerId = key.first
                bowlerMaidensMap[bowlerId] = (bowlerMaidensMap[bowlerId] ?: 0) + 1
            }
        }

        // Bowlers who have bowled
        val allBowlerIds = (bowlerLegalBallsMap.keys + setOf(currentBowlerId))
            .filter { it.isNotEmpty() }.distinct()

        val bowlers = allBowlerIds.mapNotNull { id ->
            val player = playerMap[id] ?: return@mapNotNull null
            BowlerStats(
                player = player,
                totalLegalBalls = bowlerLegalBallsMap[id] ?: 0,
                runs = bowlerRunsMap[id] ?: 0,
                wickets = bowlerWicketsMap[id] ?: 0,
                wides = bowlerWidesMap[id] ?: 0,
                noBalls = bowlerNoBallsMap[id] ?: 0,
                maidens = bowlerMaidensMap[id] ?: 0
            )
        }

        // Description of last ball
        val lastBall = balls.lastOrNull()
        val lastBallDesc = lastBall?.let { b ->
            when {
                b.isWicket && b.wicketType == WicketType.RETIRED_HURT -> "RH"
                b.isWicket && b.wicketType == WicketType.RETIRED_OUT  -> "RO"
                b.isWicket -> "W"
                b.extraType == ExtraType.WIDE -> "Wd+${b.totalRuns}"
                b.extraType == ExtraType.NO_BALL -> "Nb+${b.runs}"
                b.extraType == ExtraType.BYE -> "${b.extras}B"
                b.extraType == ExtraType.LEG_BYE -> "${b.extras}LB"
                b.runs == 0 -> "·"
                else -> "${b.runs}"
            }
        } ?: ""

        return InningsScore(
            inningsNumber = inningsNumber,
            battingTeamName = battingTeamName,
            bowlingTeamName = bowlingTeamName,
            totalOvers = totalOvers,
            totalRuns = totalRuns,
            wickets = wickets,
            completedOvers = completedOvers,
            legalBallsInCurrentOver = legalBallsInCurrentOver,
            wides = wides,
            noBalls = noBalls,
            byes = byes,
            legByes = legByes,
            batsmen = batsmen,
            bowlers = bowlers,
            fallOfWickets = fowList,
            currentBatsmanOnStrikeId = onStrikeId,
            currentBatsmanOffStrikeId = offStrikeId,
            currentBowlerId = currentBowlerId,
            isCompleted = isCompleted,
            target = target,
            lastBallDescription = lastBallDesc
        )
    }
}
