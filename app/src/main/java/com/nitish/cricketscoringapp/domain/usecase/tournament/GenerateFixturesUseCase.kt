package com.nitish.cricketscoringapp.domain.usecase.tournament

import com.nitish.cricketscoringapp.domain.model.PointsEntry
import com.nitish.cricketscoringapp.domain.model.TournamentType
import com.nitish.cricketscoringapp.domain.rules.FixtureGenerator
import com.nitish.cricketscoringapp.domain.repository.TournamentRepository
import javax.inject.Inject

class GenerateFixturesUseCase @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val fixtureGenerator: FixtureGenerator
) {
    /**
     * Generates GROUP stage fixtures for the tournament and initialises the
     * points table entries for every registered team (all zeros).
     *
     * @param startDate epoch-ms of the first scheduled match
     * @param matchGapMs gap in ms between consecutive match start times
     * @param teamsPerGroup only relevant for LEAGUE_PLUS_KNOCKOUT with many teams
     */
    suspend operator fun invoke(
        tournamentId: String,
        startDate: Long,
        matchGapMs: Long = 4 * 60 * 60 * 1000L,
        teamsPerGroup: Int = 4
    ) {
        val tournament = tournamentRepository.getTournamentByIdSync(tournamentId) ?: return
        val teams = tournamentRepository.getTeamsByTournamentSync(tournamentId)

        if (teams.size < 2) return

        val fixtures = when (tournament.tournamentType) {
            TournamentType.LEAGUE,
            TournamentType.LEAGUE_PLUS_KNOCKOUT -> {
                if (teams.size > 8) {
                    // Multi-group when more than 8 teams
                    fixtureGenerator.generateGroupStage(
                        teams        = teams,
                        tournamentId = tournamentId,
                        startDate    = startDate,
                        teamsPerGroup = teamsPerGroup,
                        matchGapMs   = matchGapMs
                    )
                } else {
                    fixtureGenerator.generateRoundRobin(
                        teams        = teams,
                        tournamentId = tournamentId,
                        startDate    = startDate,
                        matchGapMs   = matchGapMs
                    )
                }
            }
            TournamentType.KNOCKOUT -> {
                fixtureGenerator.generateKnockoutDraw(
                    qualifiedTeams = teams,
                    tournamentId   = tournamentId,
                    startDate      = startDate,
                    matchGapMs     = matchGapMs
                )
            }
        }

        tournamentRepository.upsertFixtures(fixtures)

        // Initialise points table with zero-entries for every team
        val existingEntries = tournamentRepository.getPointsTableSync(tournamentId)
        val existingTeamIds = existingEntries.map { it.teamId }.toSet()
        val newEntries = teams
            .filter { it.id !in existingTeamIds }
            .map { team ->
                PointsEntry(
                    tournamentId = tournamentId,
                    teamId       = team.id,
                    teamName     = team.name,
                    teamLogoUrl  = team.logoUrl
                )
            }
        if (newEntries.isNotEmpty()) {
            val allEntries = existingEntries + newEntries
            tournamentRepository.savePointsTable(tournamentId, allEntries)
        }
    }
}
