package com.cric.cricketscoring.domain.usecase.tournament

import com.cric.cricketscoring.domain.model.Fixture
import com.cric.cricketscoring.domain.model.FixtureStage
import com.cric.cricketscoring.domain.model.FixtureStatus
import com.cric.cricketscoring.domain.model.PointsEntry
import com.cric.cricketscoring.domain.repository.MatchRepository
import com.cric.cricketscoring.domain.repository.TournamentRepository
import javax.inject.Inject

/**
 * Rebuilds the full points table from completed GROUP fixtures.
 * Idempotent — call it after every match completion; it recalculates from scratch.
 *
 * Points: Win=2, Tie/NoResult=1, Loss=0, Abandoned=1
 * NRR = (total runs scored / total legal overs faced) − (total runs conceded / total legal overs bowled)
 * Ranking: points DESC → NRR DESC → wins DESC → alphabetical
 */
class CalculatePointsTableUseCase @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val matchRepository: MatchRepository
) {

    suspend operator fun invoke(tournamentId: String) {
        val teams = tournamentRepository.getTeamsByTournamentSync(tournamentId)
        if (teams.isEmpty()) return

        // Seed accumulators for every registered team
        val accumulators = teams.associate { it.id to TeamAccumulator(teamId = it.id) }
            .toMutableMap()

        val groupFixtures = tournamentRepository.getFixturesByTournamentSync(tournamentId)
            .filter { it.stage == FixtureStage.GROUP && it.status != FixtureStatus.UPCOMING }

        for (fixture in groupFixtures) {
            val acc1 = accumulators.getOrPut(fixture.team1Id) { TeamAccumulator(fixture.team1Id) }
            val acc2 = accumulators.getOrPut(fixture.team2Id) { TeamAccumulator(fixture.team2Id) }

            when (fixture.status) {
                FixtureStatus.COMPLETED -> processCompleted(fixture, acc1, acc2)
                FixtureStatus.NO_RESULT -> processNoResult(acc1, acc2)
                FixtureStatus.ABANDONED -> processAbandoned(acc1, acc2)
                else -> Unit
            }
        }

        // Existing team metadata (name, logo) from the current table or teams list
        val teamMeta = teams.associate { it.id to (it.name to it.logoUrl) }

        val entries = accumulators.values.mapIndexed { _, acc ->
            val nrr = computeNRR(acc)
            val (name, logo) = teamMeta[acc.teamId] ?: ("" to "")
            PointsEntry(
                tournamentId      = tournamentId,
                teamId            = acc.teamId,
                teamName          = name,
                teamLogoUrl       = logo,
                matchesPlayed     = acc.matchesPlayed,
                won               = acc.won,
                lost              = acc.lost,
                tied              = acc.tied,
                noResult          = acc.noResult,
                abandoned         = acc.abandoned,
                points            = acc.points,
                totalRunsScored   = acc.totalRunsScored,
                totalOversFaced   = acc.totalOversFaced,
                totalRunsConceded = acc.totalRunsConceded,
                totalOversBowled  = acc.totalOversBowled,
                nrr               = nrr
            )
        }

        val ranked = entries
            .sortedWith(
                compareByDescending<PointsEntry> { it.points }
                    .thenByDescending { it.nrr }
                    .thenByDescending { it.won }
                    .thenBy { it.teamName }
            )
            .mapIndexed { idx, entry -> entry.copy(rank = idx + 1) }

        tournamentRepository.savePointsTable(tournamentId, ranked)
    }

    // ── Match result processors ───────────────────────────────────────────────

    private suspend fun processCompleted(
        fixture: Fixture,
        acc1: TeamAccumulator,
        acc2: TeamAccumulator
    ) {
        val matchId = fixture.matchId ?: return
        val match = matchRepository.getMatchByIdSync(matchId) ?: return

        acc1.matchesPlayed++
        acc2.matchesPlayed++

        // Determine who is "team 1" and "team 2" in the match
        val inns1TeamId = if (match.innings1BattingTeam == 1) fixture.team1Id else fixture.team2Id
        val inns2TeamId = if (inns1TeamId == fixture.team1Id) fixture.team2Id else fixture.team1Id

        // Innings run/over totals from ball-by-ball data
        val inns1Balls = matchRepository.getBallsForInningsSync(matchId, 1)
        val inns2Balls = matchRepository.getBallsForInningsSync(matchId, 2)
        val (inns1Runs, inns1Overs) = inningsTotals(inns1Balls)
        val (inns2Runs, inns2Overs) = inningsTotals(inns2Balls)

        // NRR contribution for innings 1 batting team
        accumulatorFor(inns1TeamId, acc1, acc2).apply {
            totalRunsScored   += inns1Runs
            totalOversFaced   += inns1Overs
            totalRunsConceded += inns2Runs
            totalOversBowled  += inns2Overs
        }
        // NRR contribution for innings 2 batting team (mirror)
        accumulatorFor(inns2TeamId, acc1, acc2).apply {
            totalRunsScored   += inns2Runs
            totalOversFaced   += inns2Overs
            totalRunsConceded += inns1Runs
            totalOversBowled  += inns1Overs
        }

        // Win/loss
        when (fixture.winnerId) {
            fixture.team1Id -> { acc1.won++; acc1.points += 2; acc2.lost++ }
            fixture.team2Id -> { acc2.won++; acc2.points += 2; acc1.lost++ }
            else            -> { acc1.tied++; acc1.points++; acc2.tied++; acc2.points++ }
        }
    }

    private fun processNoResult(acc1: TeamAccumulator, acc2: TeamAccumulator) {
        acc1.matchesPlayed++; acc1.noResult++; acc1.points++
        acc2.matchesPlayed++; acc2.noResult++; acc2.points++
    }

    private fun processAbandoned(acc1: TeamAccumulator, acc2: TeamAccumulator) {
        acc1.matchesPlayed++; acc1.abandoned++; acc1.points++
        acc2.matchesPlayed++; acc2.abandoned++; acc2.points++
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun accumulatorFor(
        teamId: String,
        acc1: TeamAccumulator,
        acc2: TeamAccumulator
    ) = if (acc1.teamId == teamId) acc1 else acc2

    private fun inningsTotals(balls: List<com.cric.cricketscoring.domain.model.Ball>): Pair<Int, Double> {
        val runs        = balls.sumOf { it.totalRuns }
        val legalBalls  = balls.count { it.isLegalDelivery }
        val overs       = if (legalBalls == 0) 0.0 else legalBalls / 6.0
        return runs to overs
    }

    private fun computeNRR(acc: TeamAccumulator): Double {
        val runRate    = if (acc.totalOversFaced > 0)  acc.totalRunsScored.toDouble()   / acc.totalOversFaced  else 0.0
        val concedRate = if (acc.totalOversBowled > 0) acc.totalRunsConceded.toDouble() / acc.totalOversBowled else 0.0
        return runRate - concedRate
    }

    // ── Mutable accumulator ───────────────────────────────────────────────────

    private data class TeamAccumulator(
        val teamId: String,
        var matchesPlayed: Int = 0,
        var won: Int = 0,
        var lost: Int = 0,
        var tied: Int = 0,
        var noResult: Int = 0,
        var abandoned: Int = 0,
        var points: Int = 0,
        var totalRunsScored: Int = 0,
        var totalOversFaced: Double = 0.0,
        var totalRunsConceded: Int = 0,
        var totalOversBowled: Double = 0.0
    )
}
