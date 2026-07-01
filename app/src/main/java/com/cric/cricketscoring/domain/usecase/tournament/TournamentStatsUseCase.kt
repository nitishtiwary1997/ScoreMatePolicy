package com.cric.cricketscoring.domain.usecase.tournament

import com.cric.cricketscoring.domain.model.Ball
import com.cric.cricketscoring.domain.model.Match
import com.cric.cricketscoring.domain.model.Player
import com.cric.cricketscoring.domain.model.PlayerCareerStats
import com.cric.cricketscoring.domain.model.PlayerStatLine
import com.cric.cricketscoring.domain.model.TournamentStats
import com.cric.cricketscoring.domain.repository.MatchRepository
import com.cric.cricketscoring.domain.repository.TournamentRepository
import com.cric.cricketscoring.domain.usecase.PlayerStatsCalculator
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Aggregates batting and bowling stats across every completed match in a tournament.
 * Re-uses [PlayerStatsCalculator] so the logic stays in one place.
 *
 * Player identity is merged by name (same as career stats) because PlayerEntity IDs
 * are per-match UUIDs. Phase 7 will link TournamentPlayer IDs directly to PlayerEntity
 * so the merge becomes ID-based.
 */
class TournamentStatsUseCase @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val matchRepository: MatchRepository
) {
    companion object {
        private const val MIN_BALLS_FOR_SR   = 20   // min balls faced to qualify for best-SR list
        private const val MIN_OVERS_FOR_ECON = 2    // min overs bowled to qualify for best-economy list
        private const val TOP_N              = 10   // items in each leaderboard
    }

    suspend operator fun invoke(tournamentId: String): TournamentStats {
        val fixtures = tournamentRepository.getFixturesByTournamentSync(tournamentId)
            .filter { it.matchId != null }

        if (fixtures.isEmpty()) return TournamentStats(tournamentId = tournamentId)

        // Collect all match data across tournament
        val allMatches   = mutableListOf<Match>()
        val allPlayers   = mutableListOf<Player>()
        val allBalls     = mutableListOf<Ball>()
        // playerName → teamName (first occurrence wins)
        val playerTeam   = mutableMapOf<String, String>()

        for (fixture in fixtures) {
            val matchId = fixture.matchId ?: continue
            val match   = matchRepository.getMatchByIdSync(matchId) ?: continue
            allMatches.add(match)

            val players = matchRepository.getPlayersForMatch(matchId).first()
            allPlayers.addAll(players)

            // Record team name for each player based on their team number in the match
            players.forEach { player ->
                val teamName = if (player.team == 1) match.team1Name else match.team2Name
                playerTeam.putIfAbsent(player.name.trim().lowercase(), teamName)
            }

            allBalls.addAll(matchRepository.getBallsForInningsSync(matchId, 1))
            allBalls.addAll(matchRepository.getBallsForInningsSync(matchId, 2))
        }

        val stats = PlayerStatsCalculator.calculate(allMatches, allPlayers, allBalls)

        return TournamentStats(
            tournamentId  = tournamentId,
            orangeCap     = stats.maxByOrNull { it.totalRuns }
                                ?.toStatLine(playerTeam, primaryFn = { "${it.totalRuns} runs" }, secondaryFn = { "HS: ${it.highScore}" }),
            purpleCap     = stats.maxByOrNull { it.wickets }
                                ?.toStatLine(playerTeam, primaryFn = { "${it.wickets} wkts" }, secondaryFn = { "BB: ${it.bestBowlingWickets}/${it.bestBowlingRuns}" }),
            mostRuns      = stats.sortedByDescending { it.totalRuns }
                                .take(TOP_N)
                                .map { it.toStatLine(playerTeam, { "${it.totalRuns}" }, { "SR: ${it.strikeRate}" }) },
            mostWickets   = stats.sortedByDescending { it.wickets }
                                .take(TOP_N)
                                .map { it.toStatLine(playerTeam, { "${it.wickets}" }, { "Econ: ${it.economy}" }) },
            highestScore  = stats.sortedByDescending { it.highScore }
                                .take(TOP_N)
                                .map { it.toStatLine(playerTeam, { "${it.highScore}${if (it.highScoreNotOut) "*" else ""}" }, { "SR: ${it.strikeRate}" }) },
            bestBowling   = stats.filter { it.bestBowlingWickets > 0 }
                                .sortedWith(compareByDescending<PlayerCareerStats> { it.bestBowlingWickets }.thenBy { it.bestBowlingRuns })
                                .take(TOP_N)
                                .map { it.toStatLine(playerTeam, { "${it.bestBowlingWickets}/${it.bestBowlingRuns}" }, { "Econ: ${it.economy}" }) },
            bestStrikeRate = stats.filter { it.totalBalls >= MIN_BALLS_FOR_SR }
                                .sortedByDescending { it.strikeRate }
                                .take(TOP_N)
                                .map { it.toStatLine(playerTeam, { String.format("%.1f", it.strikeRate) }, { "${it.totalRuns} runs" }) },
            bestEconomy   = stats.filter { it.ballsBowled >= MIN_OVERS_FOR_ECON * 6 }
                                .filter { it.economy > 0 }
                                .sortedBy { it.economy }
                                .take(TOP_N)
                                .map { it.toStatLine(playerTeam, { String.format("%.2f", it.economy) }, { "${it.wickets} wkts" }) },
            mostFifties   = stats.filter { it.fifties > 0 }
                                .sortedByDescending { it.fifties }
                                .take(TOP_N)
                                .map { it.toStatLine(playerTeam, { "${it.fifties}" }, { "${it.totalRuns} runs" }) },
            mostHundreds  = stats.filter { it.hundreds > 0 }
                                .sortedByDescending { it.hundreds }
                                .take(TOP_N)
                                .map { it.toStatLine(playerTeam, { "${it.hundreds}" }, { "${it.totalRuns} runs" }) },
            mostCatches   = emptyList()  // Phase 7: fielding stats from ball data
        )
    }

    private fun PlayerCareerStats.toStatLine(
        playerTeamMap: Map<String, String>,
        primaryFn: (PlayerCareerStats) -> String,
        secondaryFn: (PlayerCareerStats) -> String
    ): PlayerStatLine {
        val nameKey  = player.name.trim().lowercase()
        val teamName = playerTeamMap[nameKey] ?: ""
        return PlayerStatLine(
            playerId       = player.id,
            playerName     = player.name,
            teamId         = "",       // resolved in Phase 7
            teamName       = teamName,
            primaryValue   = primaryFn(this),
            secondaryValue = secondaryFn(this)
        )
    }
}
