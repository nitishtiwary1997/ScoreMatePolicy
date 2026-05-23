package com.nitish.cricketscoringapp.data.repository

import com.nitish.cricketscoringapp.data.local.dao.FixtureDao
import com.nitish.cricketscoringapp.data.local.dao.PointsEntryDao
import com.nitish.cricketscoringapp.data.local.dao.TournamentDao
import com.nitish.cricketscoringapp.data.local.dao.TournamentPlayerDao
import com.nitish.cricketscoringapp.data.local.dao.TournamentTeamDao
import com.nitish.cricketscoringapp.data.local.entity.FixtureEntity
import com.nitish.cricketscoringapp.data.local.entity.PointsEntryEntity
import com.nitish.cricketscoringapp.data.local.entity.TournamentEntity
import com.nitish.cricketscoringapp.data.local.entity.TournamentPlayerEntity
import com.nitish.cricketscoringapp.data.local.entity.TournamentTeamEntity
import com.nitish.cricketscoringapp.data.remote.FirebaseTournamentDataSource
import com.nitish.cricketscoringapp.domain.model.BallType
import com.nitish.cricketscoringapp.domain.model.BattingStyle
import com.nitish.cricketscoringapp.domain.model.BowlingStyle
import com.nitish.cricketscoringapp.domain.model.Fixture
import com.nitish.cricketscoringapp.domain.model.FixtureStage
import com.nitish.cricketscoringapp.domain.model.FixtureStatus
import com.nitish.cricketscoringapp.domain.model.PlayerRole
import com.nitish.cricketscoringapp.domain.model.PointsEntry
import com.nitish.cricketscoringapp.domain.model.Tournament
import com.nitish.cricketscoringapp.domain.model.TournamentMatchFormat
import com.nitish.cricketscoringapp.domain.model.TournamentPlayer
import com.nitish.cricketscoringapp.domain.model.TournamentStatus
import com.nitish.cricketscoringapp.domain.model.TournamentTeam
import com.nitish.cricketscoringapp.domain.model.TournamentType
import com.nitish.cricketscoringapp.domain.repository.TournamentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TournamentRepositoryImpl @Inject constructor(
    private val tournamentDao: TournamentDao,
    private val teamDao: TournamentTeamDao,
    private val playerDao: TournamentPlayerDao,
    private val fixtureDao: FixtureDao,
    private val pointsDao: PointsEntryDao,
    private val firebase: FirebaseTournamentDataSource
) : TournamentRepository {

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Tournament ────────────────────────────────────────────────────────────

    override fun getTournamentsByUser(userId: String): Flow<List<Tournament>> =
        tournamentDao.getTournamentsByUser(userId).map { list -> list.map { it.toDomain() } }

    override fun getPublicTournaments(): Flow<List<Tournament>> =
        tournamentDao.getPublicTournaments().map { list -> list.map { it.toDomain() } }

    override fun getTournamentById(id: String): Flow<Tournament?> =
        tournamentDao.getTournamentById(id).map { it?.toDomain() }

    override suspend fun getTournamentByIdSync(id: String): Tournament? =
        tournamentDao.getTournamentByIdSync(id)?.toDomain()

    override suspend fun createTournament(tournament: Tournament) {
        tournamentDao.upsertTournament(tournament.toEntity())
        syncScope.launch {
            runCatching { firebase.saveTournament(tournament) }
            runCatching { tournamentDao.markSynced(tournament.id) }
        }
    }

    override suspend fun updateTournament(tournament: Tournament) {
        tournamentDao.upsertTournament(tournament.toEntity())
        syncScope.launch {
            runCatching { firebase.saveTournament(tournament) }
            runCatching { tournamentDao.markSynced(tournament.id) }
        }
    }

    override suspend fun updateTournamentStatus(id: String, status: TournamentStatus) {
        tournamentDao.updateStatus(id, status.name)
        syncScope.launch {
            val tournament = tournamentDao.getTournamentByIdSync(id)?.toDomain() ?: return@launch
            runCatching { firebase.saveTournament(tournament.copy(status = status)) }
        }
    }

    override suspend fun deleteTournament(id: String) {
        tournamentDao.deleteTournament(id)
        syncScope.launch { runCatching { firebase.deleteTournament(id) } }
    }

    // ── Teams ─────────────────────────────────────────────────────────────────

    override fun getTeamsByTournament(tournamentId: String): Flow<List<TournamentTeam>> =
        teamDao.getTeamsByTournament(tournamentId).map { list -> list.map { it.toDomain() } }

    override suspend fun getTeamsByTournamentSync(tournamentId: String): List<TournamentTeam> =
        teamDao.getTeamsByTournamentSync(tournamentId).map { it.toDomain() }

    override fun getTeamById(teamId: String): Flow<TournamentTeam?> =
        teamDao.getTeamById(teamId).map { it?.toDomain() }

    override suspend fun getTeamByIdSync(teamId: String): TournamentTeam? =
        teamDao.getTeamByIdSync(teamId)?.toDomain()

    override suspend fun upsertTeam(team: TournamentTeam) {
        teamDao.upsertTeam(team.toEntity())
        syncScope.launch { runCatching { firebase.saveTeam(team) } }
    }

    override suspend fun updateCaptain(teamId: String, captainId: String) {
        teamDao.updateCaptain(teamId, captainId)
        syncScope.launch {
            val team = teamDao.getTeamByIdSync(teamId)?.toDomain() ?: return@launch
            runCatching { firebase.saveTeam(team.copy(captainPlayerId = captainId)) }
        }
    }

    override suspend fun deleteTeam(teamId: String) {
        teamDao.deleteTeam(teamId)
        syncScope.launch { runCatching { firebase.deleteTeam(teamId) } }
    }

    override suspend fun getTeamCount(tournamentId: String): Int =
        teamDao.getTeamCount(tournamentId)

    // ── Players ───────────────────────────────────────────────────────────────

    override fun getPlayersByTeam(teamId: String): Flow<List<TournamentPlayer>> =
        playerDao.getPlayersByTeam(teamId).map { list -> list.map { it.toDomain() } }

    override suspend fun getPlayersByTeamSync(teamId: String): List<TournamentPlayer> =
        playerDao.getPlayersByTeamSync(teamId).map { it.toDomain() }

    override fun getPlayersByTournament(tournamentId: String): Flow<List<TournamentPlayer>> =
        playerDao.getPlayersByTournament(tournamentId).map { list -> list.map { it.toDomain() } }

    override suspend fun getPlayersByTournamentSync(tournamentId: String): List<TournamentPlayer> =
        playerDao.getPlayersByTournamentSync(tournamentId).map { it.toDomain() }

    override suspend fun getPlayerByIdSync(playerId: String): TournamentPlayer? =
        playerDao.getPlayerByIdSync(playerId)?.toDomain()

    override suspend fun upsertPlayer(player: TournamentPlayer) {
        playerDao.upsertPlayer(player.toEntity())
        syncScope.launch { runCatching { firebase.savePlayer(player) } }
    }

    override suspend fun upsertPlayers(players: List<TournamentPlayer>) {
        playerDao.upsertPlayers(players.map { it.toEntity() })
        syncScope.launch { runCatching { firebase.savePlayers(players) } }
    }

    override suspend fun deletePlayer(playerId: String) {
        playerDao.deletePlayer(playerId)
        syncScope.launch { runCatching { firebase.deletePlayer(playerId) } }
    }

    override suspend fun deletePlayersByTeam(teamId: String) {
        playerDao.deletePlayersByTeam(teamId)
    }

    override suspend fun getPlayerCount(teamId: String): Int =
        playerDao.getPlayerCount(teamId)

    // ── Fixtures ──────────────────────────────────────────────────────────────

    override fun getFixturesByTournament(tournamentId: String): Flow<List<Fixture>> =
        fixtureDao.getFixturesByTournament(tournamentId).map { list -> list.map { it.toDomain() } }

    override suspend fun getFixturesByTournamentSync(tournamentId: String): List<Fixture> =
        fixtureDao.getFixturesByTournamentSync(tournamentId).map { it.toDomain() }

    override fun getFixturesByStage(tournamentId: String, stage: FixtureStage): Flow<List<Fixture>> =
        fixtureDao.getFixturesByStage(tournamentId, stage.name).map { list -> list.map { it.toDomain() } }

    override fun getLiveFixtures(tournamentId: String): Flow<List<Fixture>> =
        fixtureDao.getLiveFixtures(tournamentId).map { list -> list.map { it.toDomain() } }

    override fun getUpcomingFixtures(tournamentId: String): Flow<List<Fixture>> =
        fixtureDao.getUpcomingFixtures(tournamentId).map { list -> list.map { it.toDomain() } }

    override fun getCompletedFixtures(tournamentId: String): Flow<List<Fixture>> =
        fixtureDao.getCompletedFixtures(tournamentId).map { list -> list.map { it.toDomain() } }

    override suspend fun getFixtureByIdSync(fixtureId: String): Fixture? =
        fixtureDao.getFixtureByIdSync(fixtureId)?.toDomain()

    override suspend fun getFixtureByMatchId(matchId: String): Fixture? =
        fixtureDao.getFixtureByMatchId(matchId)?.toDomain()

    override suspend fun upsertFixture(fixture: Fixture) {
        fixtureDao.upsertFixture(fixture.toEntity())
        syncScope.launch { runCatching { firebase.saveFixture(fixture) } }
    }

    override suspend fun upsertFixtures(fixtures: List<Fixture>) {
        fixtureDao.upsertFixtures(fixtures.map { it.toEntity() })
        syncScope.launch { runCatching { firebase.saveFixtures(fixtures) } }
    }

    override suspend fun linkMatchToFixture(fixtureId: String, matchId: String) {
        fixtureDao.linkMatch(fixtureId, matchId)
        syncScope.launch {
            val fixture = fixtureDao.getFixtureByIdSync(fixtureId)?.toDomain() ?: return@launch
            runCatching { firebase.saveFixture(fixture) }
        }
    }

    override suspend fun completeFixture(fixtureId: String, winnerId: String?, resultSummary: String) {
        fixtureDao.completeFixture(
            fixtureId     = fixtureId,
            status        = FixtureStatus.COMPLETED.name,
            winnerId      = winnerId,
            resultSummary = resultSummary
        )
        syncScope.launch {
            val fixture = fixtureDao.getFixtureByIdSync(fixtureId)?.toDomain() ?: return@launch
            runCatching { firebase.saveFixture(fixture) }
        }
    }

    override suspend fun deleteFixturesByStage(tournamentId: String, stage: FixtureStage) {
        fixtureDao.deleteFixturesByStage(tournamentId, stage.name)
    }

    override suspend fun getCompletedGroupMatchCount(tournamentId: String): Int =
        fixtureDao.getCompletedGroupMatchCount(tournamentId)

    override suspend fun getTotalGroupMatchCount(tournamentId: String): Int =
        fixtureDao.getTotalGroupMatchCount(tournamentId)

    // ── Points Table ──────────────────────────────────────────────────────────

    override fun getPointsTable(tournamentId: String): Flow<List<PointsEntry>> =
        pointsDao.getPointsTable(tournamentId).map { list -> list.map { it.toDomain() } }

    override suspend fun getPointsTableSync(tournamentId: String): List<PointsEntry> =
        pointsDao.getPointsTableSync(tournamentId).map { it.toDomain() }

    override suspend fun savePointsTable(tournamentId: String, entries: List<PointsEntry>) {
        pointsDao.deleteByTournament(tournamentId)
        pointsDao.upsertEntries(entries.map { it.toEntity() })
        syncScope.launch { runCatching { firebase.savePointsTable(entries) } }
    }

    // ── Cloud Sync ────────────────────────────────────────────────────────────

    override suspend fun syncTournamentToCloud(tournamentId: String) {
        val tournament = tournamentDao.getTournamentByIdSync(tournamentId)?.toDomain() ?: return
        runCatching { firebase.saveTournament(tournament) }
        tournamentDao.markSynced(tournamentId)

        val teams = teamDao.getTeamsByTournamentSync(tournamentId).map { it.toDomain() }
        teams.forEach { runCatching { firebase.saveTeam(it) } }

        val players = playerDao.getPlayersByTournamentSync(tournamentId).map { it.toDomain() }
        runCatching { firebase.savePlayers(players) }

        val fixtures = fixtureDao.getFixturesByTournamentSync(tournamentId).map { it.toDomain() }
        runCatching { firebase.saveFixtures(fixtures) }

        val points = pointsDao.getPointsTableSync(tournamentId).map { it.toDomain() }
        runCatching { firebase.savePointsTable(points) }
    }

    override suspend fun syncAllTournamentsFromCloud(userId: String) {
        val remoteTournaments = runCatching {
            firebase.fetchTournamentsForUser(userId)
        }.getOrDefault(emptyList())

        remoteTournaments.forEach { tournament ->
            tournamentDao.upsertTournament(tournament.toEntity().copy(isSynced = true))

            val teams = firebase.fetchTeamsForTournament(tournament.id)
            teamDao.upsertTeams(teams.map { it.toEntity().copy(isSynced = true) })

            val players = firebase.fetchPlayersForTournament(tournament.id)
            playerDao.upsertPlayers(players.map { it.toEntity().copy(isSynced = true) })

            val fixtures = firebase.fetchFixturesForTournament(tournament.id)
            fixtureDao.upsertFixtures(fixtures.map { it.toEntity().copy(isSynced = true) })
        }
    }

    // ── Mappers: Entity → Domain ──────────────────────────────────────────────

    private fun TournamentEntity.toDomain() = Tournament(
        id              = id,
        name            = name,
        description     = description,
        organizerName   = organizerName,
        organizerContact = organizerContact,
        bannerUrl       = bannerUrl,
        logoUrl         = logoUrl,
        venue           = venue,
        startDate       = startDate,
        endDate         = endDate,
        matchFormat     = safeEnum<TournamentMatchFormat>(matchFormat, TournamentMatchFormat.T20),
        customOvers     = customOvers,
        ballType        = safeEnum<BallType>(ballType, BallType.LEATHER),
        tournamentType  = safeEnum<TournamentType>(tournamentType, TournamentType.LEAGUE),
        status          = safeEnum<TournamentStatus>(status, TournamentStatus.UPCOMING),
        isPublic        = isPublic,
        entryFee        = entryFee,
        prizeDetails    = prizeDetails,
        rules           = rules,
        maxTeams        = maxTeams,
        playersPerTeam  = playersPerTeam,
        createdByUserId = createdByUserId,
        createdAt       = createdAt
    )

    private fun TournamentTeamEntity.toDomain() = TournamentTeam(
        id                   = id,
        tournamentId         = tournamentId,
        name                 = name,
        shortName            = shortName,
        logoUrl              = logoUrl,
        jerseyColorPrimary   = jerseyColorPrimary,
        jerseyColorSecondary = jerseyColorSecondary,
        captainPlayerId      = captainPlayerId,
        viceCaptainPlayerId  = viceCaptainPlayerId,
        homeGround           = homeGround,
        registeredAt         = registeredAt
    )

    private fun TournamentPlayerEntity.toDomain() = TournamentPlayer(
        id            = id,
        tournamentId  = tournamentId,
        teamId        = teamId,
        name          = name,
        imageUrl      = imageUrl,
        jerseyNumber  = jerseyNumber,
        role          = safeEnum<PlayerRole>(role, PlayerRole.BATSMAN),
        battingStyle  = safeEnum<BattingStyle>(battingStyle, BattingStyle.RIGHT_HAND),
        bowlingStyle  = safeEnum<BowlingStyle>(bowlingStyle, BowlingStyle.NONE),
        dateOfBirth   = dateOfBirth,
        contactNumber = contactNumber
    )

    private fun FixtureEntity.toDomain() = Fixture(
        id            = id,
        tournamentId  = tournamentId,
        matchId       = matchId,
        team1Id       = team1Id,
        team2Id       = team2Id,
        stage         = safeEnum<FixtureStage>(stage, FixtureStage.GROUP),
        groupName     = groupName,
        matchNumber   = matchNumber,
        scheduledAt   = scheduledAt,
        venue         = venue,
        status        = safeEnum<FixtureStatus>(status, FixtureStatus.UPCOMING),
        winnerId      = winnerId,
        resultSummary = resultSummary
    )

    private fun PointsEntryEntity.toDomain() = PointsEntry(
        id                 = id,
        tournamentId       = tournamentId,
        teamId             = teamId,
        teamName           = teamName,
        teamLogoUrl        = teamLogoUrl,
        matchesPlayed      = matchesPlayed,
        won                = won,
        lost               = lost,
        tied               = tied,
        noResult           = noResult,
        abandoned          = abandoned,
        points             = points,
        totalRunsScored    = totalRunsScored,
        totalOversFaced    = totalOversFaced,
        totalRunsConceded  = totalRunsConceded,
        totalOversBowled   = totalOversBowled,
        nrr                = nrr,
        rank               = rank,
        isQualified        = isQualified
    )

    // ── Mappers: Domain → Entity ──────────────────────────────────────────────

    private fun Tournament.toEntity() = TournamentEntity(
        id              = id,
        name            = name,
        description     = description,
        organizerName   = organizerName,
        organizerContact = organizerContact,
        bannerUrl       = bannerUrl,
        logoUrl         = logoUrl,
        venue           = venue,
        startDate       = startDate,
        endDate         = endDate,
        matchFormat     = matchFormat.name,
        customOvers     = customOvers,
        ballType        = ballType.name,
        tournamentType  = tournamentType.name,
        status          = status.name,
        isPublic        = isPublic,
        entryFee        = entryFee,
        prizeDetails    = prizeDetails,
        rules           = rules,
        maxTeams        = maxTeams,
        playersPerTeam  = playersPerTeam,
        createdByUserId = createdByUserId,
        createdAt       = createdAt,
        isSynced        = false
    )

    private fun TournamentTeam.toEntity() = TournamentTeamEntity(
        id                   = id,
        tournamentId         = tournamentId,
        name                 = name,
        shortName            = shortName,
        logoUrl              = logoUrl,
        jerseyColorPrimary   = jerseyColorPrimary,
        jerseyColorSecondary = jerseyColorSecondary,
        captainPlayerId      = captainPlayerId,
        viceCaptainPlayerId  = viceCaptainPlayerId,
        homeGround           = homeGround,
        registeredAt         = registeredAt,
        isSynced             = false
    )

    private fun TournamentPlayer.toEntity() = TournamentPlayerEntity(
        id            = id,
        tournamentId  = tournamentId,
        teamId        = teamId,
        name          = name,
        imageUrl      = imageUrl,
        jerseyNumber  = jerseyNumber,
        role          = role.name,
        battingStyle  = battingStyle.name,
        bowlingStyle  = bowlingStyle.name,
        dateOfBirth   = dateOfBirth,
        contactNumber = contactNumber,
        isSynced      = false
    )

    private fun Fixture.toEntity() = FixtureEntity(
        id            = id,
        tournamentId  = tournamentId,
        matchId       = matchId,
        team1Id       = team1Id,
        team2Id       = team2Id,
        stage         = stage.name,
        groupName     = groupName,
        matchNumber   = matchNumber,
        scheduledAt   = scheduledAt,
        venue         = venue,
        status        = status.name,
        winnerId      = winnerId,
        resultSummary = resultSummary,
        isSynced      = false
    )

    private fun PointsEntry.toEntity() = PointsEntryEntity(
        id                = id,
        tournamentId      = tournamentId,
        teamId            = teamId,
        teamName          = teamName,
        teamLogoUrl       = teamLogoUrl,
        matchesPlayed     = matchesPlayed,
        won               = won,
        lost              = lost,
        tied              = tied,
        noResult          = noResult,
        abandoned         = abandoned,
        points            = points,
        totalRunsScored   = totalRunsScored,
        totalOversFaced   = totalOversFaced,
        totalRunsConceded = totalRunsConceded,
        totalOversBowled  = totalOversBowled,
        nrr               = nrr,
        rank              = rank,
        isQualified       = isQualified,
        isSynced          = false
    )

    private inline fun <reified T : Enum<T>> safeEnum(value: String, default: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(default)
}
