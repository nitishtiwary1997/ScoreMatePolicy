package com.nitish.cricketscoringapp.domain.rules

import com.nitish.cricketscoringapp.domain.model.Fixture
import com.nitish.cricketscoringapp.domain.model.FixtureStage
import com.nitish.cricketscoringapp.domain.model.TournamentTeam
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FixtureGenerator @Inject constructor() {

    companion object {
        // Default 4-hour window per match (gap between scheduled starts)
        private const val DEFAULT_MATCH_GAP_MS = 4 * 60 * 60 * 1000L
        private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L
    }

    /**
     * Round-robin: every team plays every other team exactly once.
     * Produces N*(N-1)/2 fixtures ordered by scheduledAt.
     */
    fun generateRoundRobin(
        teams: List<TournamentTeam>,
        tournamentId: String,
        startDate: Long,
        matchGapMs: Long = DEFAULT_MATCH_GAP_MS
    ): List<Fixture> {
        require(teams.size >= 2) { "Need at least 2 teams for round-robin" }

        val fixtures = mutableListOf<Fixture>()
        var matchNumber = 1
        var scheduledAt = startDate

        for (i in teams.indices) {
            for (j in i + 1 until teams.size) {
                fixtures += Fixture(
                    tournamentId = tournamentId,
                    team1Id      = teams[i].id,
                    team2Id      = teams[j].id,
                    stage        = FixtureStage.GROUP,
                    groupName    = "A",
                    matchNumber  = matchNumber++,
                    scheduledAt  = scheduledAt
                )
                scheduledAt += matchGapMs
            }
        }
        return fixtures
    }

    /**
     * Double round-robin (home + away): every team plays every other team twice.
     * Leg 2 starts the day after leg 1 ends.
     */
    fun generateDoubleRoundRobin(
        teams: List<TournamentTeam>,
        tournamentId: String,
        startDate: Long,
        matchGapMs: Long = DEFAULT_MATCH_GAP_MS
    ): List<Fixture> {
        val leg1 = generateRoundRobin(teams, tournamentId, startDate, matchGapMs)
        val leg2Start = (leg1.lastOrNull()?.scheduledAt ?: startDate) + ONE_DAY_MS
        val leg2 = generateRoundRobin(teams, tournamentId, leg2Start, matchGapMs)
            .mapIndexed { idx, f ->
                f.copy(
                    team1Id     = f.team2Id,
                    team2Id     = f.team1Id,
                    matchNumber = leg1.size + idx + 1
                )
            }
        return leg1 + leg2
    }

    /**
     * Multi-group round-robin: splits teams into groups of [teamsPerGroup].
     * Each group plays its own round-robin; group names are A, B, C…
     */
    fun generateGroupStage(
        teams: List<TournamentTeam>,
        tournamentId: String,
        startDate: Long,
        teamsPerGroup: Int = 4,
        matchGapMs: Long = DEFAULT_MATCH_GAP_MS
    ): List<Fixture> {
        val fixtures = mutableListOf<Fixture>()
        val groups = teams.chunked(teamsPerGroup)
        var matchNumber = 1
        var scheduledAt = startDate

        groups.forEachIndexed { groupIndex, groupTeams ->
            val groupName = ('A' + groupIndex).toString()
            for (i in groupTeams.indices) {
                for (j in i + 1 until groupTeams.size) {
                    fixtures += Fixture(
                        tournamentId = tournamentId,
                        team1Id      = groupTeams[i].id,
                        team2Id      = groupTeams[j].id,
                        stage        = FixtureStage.GROUP,
                        groupName    = groupName,
                        matchNumber  = matchNumber++,
                        scheduledAt  = scheduledAt
                    )
                    scheduledAt += matchGapMs
                }
            }
        }
        return fixtures
    }

    /**
     * Knockout bracket from a ranked list of qualified teams.
     * Teams are seeded: [1st vs last], [2nd vs second-last], etc.
     */
    fun generateKnockout(
        qualifiedTeams: List<TournamentTeam>,
        tournamentId: String,
        stage: FixtureStage,
        startDate: Long,
        matchGapMs: Long = DEFAULT_MATCH_GAP_MS,
        matchNumberOffset: Int = 1
    ): List<Fixture> {
        require(qualifiedTeams.size % 2 == 0) { "Knockout requires even number of teams" }

        val fixtures = mutableListOf<Fixture>()
        val high = qualifiedTeams.take(qualifiedTeams.size / 2)
        val low  = qualifiedTeams.drop(qualifiedTeams.size / 2).reversed()

        high.zip(low).forEachIndexed { idx, (teamA, teamB) ->
            fixtures += Fixture(
                tournamentId = tournamentId,
                team1Id      = teamA.id,
                team2Id      = teamB.id,
                stage        = stage,
                matchNumber  = matchNumberOffset + idx,
                scheduledAt  = startDate + idx * matchGapMs
            )
        }
        return fixtures
    }

    /**
     * Generates the full knockout draw based on how many teams qualified.
     * 8 teams → QF + SF + F, 4 teams → SF + F, 2 teams → F only.
     */
    fun generateKnockoutDraw(
        qualifiedTeams: List<TournamentTeam>,
        tournamentId: String,
        startDate: Long,
        matchGapMs: Long = DEFAULT_MATCH_GAP_MS
    ): List<Fixture> {
        val all = mutableListOf<Fixture>()
        var matchNumber = 1
        var schedAt = startDate

        when (qualifiedTeams.size) {
            8 -> {
                all += generateKnockout(qualifiedTeams, tournamentId, FixtureStage.QUARTER_FINAL, schedAt, matchGapMs, matchNumber)
                matchNumber += 4
                schedAt += 4 * matchGapMs + ONE_DAY_MS
                // SF and Final will be generated when QF results are known
            }
            4 -> {
                all += generateKnockout(qualifiedTeams, tournamentId, FixtureStage.SEMI_FINAL, schedAt, matchGapMs, matchNumber)
                matchNumber += 2
                schedAt += 2 * matchGapMs + ONE_DAY_MS
                // Final will be generated when SF results are known
            }
            2 -> {
                all += generateKnockout(qualifiedTeams, tournamentId, FixtureStage.FINAL, schedAt, matchGapMs, matchNumber)
            }
        }
        return all
    }
}
