package com.cric.cricketscoring.domain.repository

import com.cric.cricketscoring.domain.model.Fixture
import com.cric.cricketscoring.domain.model.FixtureStage
import com.cric.cricketscoring.domain.model.PointsEntry
import com.cric.cricketscoring.domain.model.Tournament
import com.cric.cricketscoring.domain.model.TournamentPlayer
import com.cric.cricketscoring.domain.model.TournamentTeam
import kotlinx.coroutines.flow.Flow

interface TournamentRepository {

    // ── Tournament ────────────────────────────────────────────────────────────

    fun getTournamentsByUser(userId: String): Flow<List<Tournament>>
    fun getPublicTournaments(): Flow<List<Tournament>>
    fun getTournamentById(id: String): Flow<Tournament?>
    suspend fun getTournamentByIdSync(id: String): Tournament?
    suspend fun createTournament(tournament: Tournament)
    suspend fun updateTournament(tournament: Tournament)
    suspend fun updateTournamentStatus(id: String, status: com.cric.cricketscoring.domain.model.TournamentStatus)
    suspend fun deleteTournament(id: String)

    // ── Teams ─────────────────────────────────────────────────────────────────

    fun getTeamsByTournament(tournamentId: String): Flow<List<TournamentTeam>>
    suspend fun getTeamsByTournamentSync(tournamentId: String): List<TournamentTeam>
    fun getTeamById(teamId: String): Flow<TournamentTeam?>
    suspend fun getTeamByIdSync(teamId: String): TournamentTeam?
    suspend fun upsertTeam(team: TournamentTeam)
    suspend fun updateCaptain(teamId: String, captainId: String)
    suspend fun deleteTeam(teamId: String)
    suspend fun getTeamCount(tournamentId: String): Int

    // ── Players ───────────────────────────────────────────────────────────────

    fun getPlayersByTeam(teamId: String): Flow<List<TournamentPlayer>>
    suspend fun getPlayersByTeamSync(teamId: String): List<TournamentPlayer>
    fun getPlayersByTournament(tournamentId: String): Flow<List<TournamentPlayer>>
    suspend fun getPlayersByTournamentSync(tournamentId: String): List<TournamentPlayer>
    suspend fun getPlayerByIdSync(playerId: String): TournamentPlayer?
    suspend fun upsertPlayer(player: TournamentPlayer)
    suspend fun upsertPlayers(players: List<TournamentPlayer>)
    suspend fun deletePlayer(playerId: String)
    suspend fun deletePlayersByTeam(teamId: String)
    suspend fun getPlayerCount(teamId: String): Int

    // ── Fixtures ──────────────────────────────────────────────────────────────

    fun getFixturesByTournament(tournamentId: String): Flow<List<Fixture>>
    suspend fun getFixturesByTournamentSync(tournamentId: String): List<Fixture>
    fun getFixturesByStage(tournamentId: String, stage: FixtureStage): Flow<List<Fixture>>
    fun getLiveFixtures(tournamentId: String): Flow<List<Fixture>>
    fun getUpcomingFixtures(tournamentId: String): Flow<List<Fixture>>
    fun getCompletedFixtures(tournamentId: String): Flow<List<Fixture>>
    suspend fun getFixtureByIdSync(fixtureId: String): Fixture?
    suspend fun getFixtureByMatchId(matchId: String): Fixture?
    suspend fun upsertFixture(fixture: Fixture)
    suspend fun upsertFixtures(fixtures: List<Fixture>)
    suspend fun linkMatchToFixture(fixtureId: String, matchId: String)
    suspend fun completeFixture(fixtureId: String, winnerId: String?, resultSummary: String)
    suspend fun deleteFixturesByStage(tournamentId: String, stage: FixtureStage)
    suspend fun getCompletedGroupMatchCount(tournamentId: String): Int
    suspend fun getTotalGroupMatchCount(tournamentId: String): Int

    // ── Points Table ──────────────────────────────────────────────────────────

    fun getPointsTable(tournamentId: String): Flow<List<PointsEntry>>
    suspend fun getPointsTableSync(tournamentId: String): List<PointsEntry>
    suspend fun savePointsTable(tournamentId: String, entries: List<PointsEntry>)

    // ── Cloud Sync ────────────────────────────────────────────────────────────

    suspend fun syncTournamentToCloud(tournamentId: String)
    suspend fun syncAllTournamentsFromCloud(userId: String)
}
