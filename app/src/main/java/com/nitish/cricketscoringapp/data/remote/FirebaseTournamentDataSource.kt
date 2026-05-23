package com.nitish.cricketscoringapp.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTournamentDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun tournamentsCol() = firestore.collection("tournaments")
    private fun teamsCol() = firestore.collection("tournament_teams")
    private fun playersCol() = firestore.collection("tournament_players")
    private fun fixturesCol() = firestore.collection("fixtures")
    private fun pointsCol() = firestore.collection("points_table")

    // ── Tournament ────────────────────────────────────────────────────────────

    suspend fun saveTournament(tournament: Tournament) {
        runCatching {
            tournamentsCol().document(tournament.id)
                .set(tournament.toFirestoreMap(), SetOptions.merge()).await()
        }
    }

    suspend fun deleteTournament(id: String) {
        runCatching { tournamentsCol().document(id).delete().await() }
    }

    suspend fun fetchTournamentsForUser(userId: String): List<Tournament> {
        return runCatching {
            tournamentsCol()
                .whereEqualTo("createdByUserId", userId)
                .get().await()
                .documents.mapNotNull { it.toTournament() }
        }.getOrDefault(emptyList())
    }

    // ── Teams ─────────────────────────────────────────────────────────────────

    suspend fun saveTeam(team: TournamentTeam) {
        runCatching {
            teamsCol().document(team.id)
                .set(team.toFirestoreMap(), SetOptions.merge()).await()
        }
    }

    suspend fun deleteTeam(teamId: String) {
        runCatching { teamsCol().document(teamId).delete().await() }
    }

    suspend fun fetchTeamsForTournament(tournamentId: String): List<TournamentTeam> {
        return runCatching {
            teamsCol()
                .whereEqualTo("tournamentId", tournamentId)
                .get().await()
                .documents.mapNotNull { it.toTeam() }
        }.getOrDefault(emptyList())
    }

    // ── Players ───────────────────────────────────────────────────────────────

    suspend fun savePlayer(player: TournamentPlayer) {
        runCatching {
            playersCol().document(player.id)
                .set(player.toFirestoreMap(), SetOptions.merge()).await()
        }
    }

    suspend fun savePlayers(players: List<TournamentPlayer>) {
        if (players.isEmpty()) return
        runCatching {
            val batch = firestore.batch()
            players.forEach { p ->
                batch.set(playersCol().document(p.id), p.toFirestoreMap(), SetOptions.merge())
            }
            batch.commit().await()
        }
    }

    suspend fun deletePlayer(playerId: String) {
        runCatching { playersCol().document(playerId).delete().await() }
    }

    suspend fun fetchPlayersForTournament(tournamentId: String): List<TournamentPlayer> {
        return runCatching {
            playersCol()
                .whereEqualTo("tournamentId", tournamentId)
                .get().await()
                .documents.mapNotNull { it.toPlayer() }
        }.getOrDefault(emptyList())
    }

    // ── Fixtures ──────────────────────────────────────────────────────────────

    suspend fun saveFixture(fixture: Fixture) {
        runCatching {
            fixturesCol().document(fixture.id)
                .set(fixture.toFirestoreMap(), SetOptions.merge()).await()
        }
    }

    suspend fun saveFixtures(fixtures: List<Fixture>) {
        if (fixtures.isEmpty()) return
        runCatching {
            val batch = firestore.batch()
            fixtures.forEach { f ->
                batch.set(fixturesCol().document(f.id), f.toFirestoreMap(), SetOptions.merge())
            }
            batch.commit().await()
        }
    }

    suspend fun fetchFixturesForTournament(tournamentId: String): List<Fixture> {
        return runCatching {
            fixturesCol()
                .whereEqualTo("tournamentId", tournamentId)
                .get().await()
                .documents.mapNotNull { it.toFixture() }
        }.getOrDefault(emptyList())
    }

    // ── Points Table ──────────────────────────────────────────────────────────

    suspend fun savePointsTable(entries: List<PointsEntry>) {
        if (entries.isEmpty()) return
        runCatching {
            val batch = firestore.batch()
            entries.forEach { e ->
                batch.set(pointsCol().document(e.id), e.toFirestoreMap(), SetOptions.merge())
            }
            batch.commit().await()
        }
    }

    // ── Firestore document → domain model ─────────────────────────────────────

    private fun DocumentSnapshot.toTournament(): Tournament? = runCatching {
        Tournament(
            id                = id,
            name              = getString("name") ?: return null,
            description       = getString("description") ?: "",
            organizerName     = getString("organizerName") ?: "",
            organizerContact  = getString("organizerContact") ?: "",
            bannerUrl         = getString("bannerUrl") ?: "",
            logoUrl           = getString("logoUrl") ?: "",
            venue             = getString("venue") ?: "",
            startDate         = getLong("startDate") ?: 0L,
            endDate           = getLong("endDate") ?: 0L,
            matchFormat       = safeEnum<TournamentMatchFormat>(getString("matchFormat"), TournamentMatchFormat.T20),
            customOvers       = getLong("customOvers")?.toInt() ?: 20,
            ballType          = safeEnum<BallType>(getString("ballType"), BallType.LEATHER),
            tournamentType    = safeEnum<TournamentType>(getString("tournamentType"), TournamentType.LEAGUE),
            status            = safeEnum<TournamentStatus>(getString("status"), TournamentStatus.UPCOMING),
            isPublic          = getBoolean("isPublic") ?: true,
            entryFee          = getDouble("entryFee") ?: 0.0,
            prizeDetails      = getString("prizeDetails") ?: "",
            rules             = getString("rules") ?: "",
            maxTeams          = getLong("maxTeams")?.toInt() ?: 8,
            playersPerTeam    = getLong("playersPerTeam")?.toInt() ?: 11,
            createdByUserId   = getString("createdByUserId") ?: "",
            createdAt         = getLong("createdAt") ?: System.currentTimeMillis()
        )
    }.getOrNull()

    private fun DocumentSnapshot.toTeam(): TournamentTeam? = runCatching {
        TournamentTeam(
            id                   = id,
            tournamentId         = getString("tournamentId") ?: return null,
            name                 = getString("name") ?: return null,
            shortName            = getString("shortName") ?: "",
            logoUrl              = getString("logoUrl") ?: "",
            jerseyColorPrimary   = getString("jerseyColorPrimary") ?: "#1A237E",
            jerseyColorSecondary = getString("jerseyColorSecondary") ?: "#FFFFFF",
            captainPlayerId      = getString("captainPlayerId") ?: "",
            viceCaptainPlayerId  = getString("viceCaptainPlayerId") ?: "",
            homeGround           = getString("homeGround") ?: "",
            registeredAt         = getLong("registeredAt") ?: System.currentTimeMillis()
        )
    }.getOrNull()

    private fun DocumentSnapshot.toPlayer(): TournamentPlayer? = runCatching {
        TournamentPlayer(
            id            = id,
            tournamentId  = getString("tournamentId") ?: return null,
            teamId        = getString("teamId") ?: return null,
            name          = getString("name") ?: return null,
            imageUrl      = getString("imageUrl") ?: "",
            jerseyNumber  = getLong("jerseyNumber")?.toInt() ?: 0,
            role          = safeEnum<PlayerRole>(getString("role"), PlayerRole.BATSMAN),
            battingStyle  = safeEnum<BattingStyle>(getString("battingStyle"), BattingStyle.RIGHT_HAND),
            bowlingStyle  = safeEnum<BowlingStyle>(getString("bowlingStyle"), BowlingStyle.NONE),
            dateOfBirth   = getLong("dateOfBirth"),
            contactNumber = getString("contactNumber") ?: ""
        )
    }.getOrNull()

    private fun DocumentSnapshot.toFixture(): Fixture? = runCatching {
        Fixture(
            id            = id,
            tournamentId  = getString("tournamentId") ?: return null,
            matchId       = getString("matchId")?.takeIf { it.isNotBlank() },
            team1Id       = getString("team1Id") ?: return null,
            team2Id       = getString("team2Id") ?: return null,
            stage         = safeEnum<FixtureStage>(getString("stage"), FixtureStage.GROUP),
            groupName     = getString("groupName") ?: "A",
            matchNumber   = getLong("matchNumber")?.toInt() ?: 1,
            scheduledAt   = getLong("scheduledAt") ?: 0L,
            venue         = getString("venue") ?: "",
            status        = safeEnum<FixtureStatus>(getString("status"), FixtureStatus.UPCOMING),
            winnerId      = getString("winnerId")?.takeIf { it.isNotBlank() },
            resultSummary = getString("resultSummary") ?: ""
        )
    }.getOrNull()

    // ── Domain model → Firestore map ──────────────────────────────────────────

    private fun Tournament.toFirestoreMap() = mapOf(
        "id"              to id,
        "name"            to name,
        "description"     to description,
        "organizerName"   to organizerName,
        "organizerContact" to organizerContact,
        "bannerUrl"       to bannerUrl,
        "logoUrl"         to logoUrl,
        "venue"           to venue,
        "startDate"       to startDate,
        "endDate"         to endDate,
        "matchFormat"     to matchFormat.name,
        "customOvers"     to customOvers,
        "ballType"        to ballType.name,
        "tournamentType"  to tournamentType.name,
        "status"          to status.name,
        "isPublic"        to isPublic,
        "entryFee"        to entryFee,
        "prizeDetails"    to prizeDetails,
        "rules"           to rules,
        "maxTeams"        to maxTeams,
        "playersPerTeam"  to playersPerTeam,
        "createdByUserId" to createdByUserId,
        "createdAt"       to createdAt
    )

    private fun TournamentTeam.toFirestoreMap() = mapOf(
        "id"                   to id,
        "tournamentId"         to tournamentId,
        "name"                 to name,
        "shortName"            to shortName,
        "logoUrl"              to logoUrl,
        "jerseyColorPrimary"   to jerseyColorPrimary,
        "jerseyColorSecondary" to jerseyColorSecondary,
        "captainPlayerId"      to captainPlayerId,
        "viceCaptainPlayerId"  to viceCaptainPlayerId,
        "homeGround"           to homeGround,
        "registeredAt"         to registeredAt
    )

    private fun TournamentPlayer.toFirestoreMap() = mapOf(
        "id"            to id,
        "tournamentId"  to tournamentId,
        "teamId"        to teamId,
        "name"          to name,
        "imageUrl"      to imageUrl,
        "jerseyNumber"  to jerseyNumber,
        "role"          to role.name,
        "battingStyle"  to battingStyle.name,
        "bowlingStyle"  to bowlingStyle.name,
        "dateOfBirth"   to dateOfBirth,
        "contactNumber" to contactNumber
    )

    private fun Fixture.toFirestoreMap() = mapOf(
        "id"            to id,
        "tournamentId"  to tournamentId,
        "matchId"       to (matchId ?: ""),
        "team1Id"       to team1Id,
        "team2Id"       to team2Id,
        "stage"         to stage.name,
        "groupName"     to groupName,
        "matchNumber"   to matchNumber,
        "scheduledAt"   to scheduledAt,
        "venue"         to venue,
        "status"        to status.name,
        "winnerId"      to (winnerId ?: ""),
        "resultSummary" to resultSummary
    )

    private fun PointsEntry.toFirestoreMap() = mapOf(
        "id"                 to id,
        "tournamentId"       to tournamentId,
        "teamId"             to teamId,
        "teamName"           to teamName,
        "teamLogoUrl"        to teamLogoUrl,
        "matchesPlayed"      to matchesPlayed,
        "won"                to won,
        "lost"               to lost,
        "tied"               to tied,
        "noResult"           to noResult,
        "abandoned"          to abandoned,
        "points"             to points,
        "totalRunsScored"    to totalRunsScored,
        "totalOversFaced"    to totalOversFaced,
        "totalRunsConceded"  to totalRunsConceded,
        "totalOversBowled"   to totalOversBowled,
        "nrr"                to nrr,
        "rank"               to rank,
        "isQualified"        to isQualified
    )

    private inline fun <reified T : Enum<T>> safeEnum(value: String?, default: T): T =
        value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
}
