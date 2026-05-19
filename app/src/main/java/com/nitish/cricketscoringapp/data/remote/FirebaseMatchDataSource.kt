package com.nitish.cricketscoringapp.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nitish.cricketscoringapp.domain.model.Ball
import com.nitish.cricketscoringapp.domain.model.ExtraType
import com.nitish.cricketscoringapp.domain.model.Match
import com.nitish.cricketscoringapp.domain.model.MatchStatus
import com.nitish.cricketscoringapp.domain.model.Player
import com.nitish.cricketscoringapp.domain.model.TossChoice
import com.nitish.cricketscoringapp.domain.model.WicketType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class CloudSyncData(
    val matches: List<Match>,
    val players: List<Player>,
    val balls: List<Ball>
)

/**
 * Syncs match data to/from Firestore under users/{userId}/matches/{matchId}.
 * Room is the source of truth; Firestore is the cloud backup.
 */
@Singleton
class FirebaseMatchDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userSession: UserSession
) {
    private fun userRoot() = firestore.collection("users").document(userSession.userId)
    private fun matchDoc(matchId: String) = userRoot().collection("matches").document(matchId)

    // ── Write operations ──────────────────────────────────────────────────────

    suspend fun saveMatch(match: Match) {
        runCatching {
            matchDoc(match.id).set(match.toFirestoreMap(), SetOptions.merge()).await()
        }
    }

    suspend fun savePlayers(matchId: String, players: List<Player>) {
        runCatching {
            val batch = firestore.batch()
            val col = matchDoc(matchId).collection("players")
            players.forEach { p ->
                batch.set(col.document(p.id), p.toFirestoreMap(), SetOptions.merge())
            }
            batch.commit().await()
        }
    }

    suspend fun saveBall(ball: Ball) {
        runCatching {
            matchDoc(ball.matchId)
                .collection("balls_innings${ball.innings}")
                .document(ball.id)
                .set(ball.toFirestoreMap(), SetOptions.merge())
                .await()
        }
    }

    suspend fun deleteLastBall(matchId: String, innings: Int, ballId: String) {
        runCatching {
            matchDoc(matchId)
                .collection("balls_innings$innings")
                .document(ballId)
                .delete()
                .await()
        }
    }

    // ── Read / sync operation ─────────────────────────────────────────────────

    suspend fun fetchAllUserData(): CloudSyncData {
        val uid = userSession.userId
        if (uid.isEmpty()) return CloudSyncData(emptyList(), emptyList(), emptyList())

        val matches = mutableListOf<Match>()
        val players = mutableListOf<Player>()
        val balls   = mutableListOf<Ball>()

        val matchDocs = runCatching {
            userRoot().collection("matches").get().await()
        }.getOrNull() ?: return CloudSyncData(emptyList(), emptyList(), emptyList())

        for (doc in matchDocs.documents) {
            val match = doc.toMatch() ?: continue
            matches += match

            runCatching {
                doc.reference.collection("players").get().await()
            }.getOrNull()?.documents?.mapNotNull { it.toPlayer() }?.let { players += it }

            for (innings in 1..2) {
                runCatching {
                    doc.reference.collection("balls_innings$innings").get().await()
                }.getOrNull()?.documents?.mapNotNull { it.toBall() }?.let { balls += it }
            }
        }

        return CloudSyncData(matches, players, balls)
    }

    // ── Firestore document → domain model parsers ─────────────────────────────

    private fun DocumentSnapshot.toMatch(): Match? {
        return try {
            val team1Name  = getString("team1Name") ?: return null
            val team2Name  = getString("team2Name") ?: return null
            val totalOvers = getLong("totalOvers")?.toInt() ?: return null
            Match(
                id                  = getString("id") ?: id,
                team1Name           = team1Name,
                team2Name           = team2Name,
                totalOvers          = totalOvers,
                playersPerTeam      = getLong("playersPerTeam")?.toInt() ?: 11,
                tossWonByTeam       = getLong("tossWonByTeam")?.toInt() ?: 0,
                tossChoice          = TossChoice.valueOf(getString("tossChoice") ?: "BAT"),
                status              = MatchStatus.valueOf(getString("status") ?: "COMPLETED"),
                innings1BattingTeam = getLong("innings1BattingTeam")?.toInt() ?: 0,
                innings1OnStrikeId  = getString("innings1OnStrikeId") ?: "",
                innings1OffStrikeId = getString("innings1OffStrikeId") ?: "",
                innings1BowlerId    = getString("innings1BowlerId") ?: "",
                innings1Completed   = getBoolean("innings1Completed") ?: false,
                innings2OnStrikeId  = getString("innings2OnStrikeId") ?: "",
                innings2OffStrikeId = getString("innings2OffStrikeId") ?: "",
                innings2BowlerId    = getString("innings2BowlerId") ?: "",
                innings2Completed   = getBoolean("innings2Completed") ?: false,
                createdAt           = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) { null }
    }

    private fun DocumentSnapshot.toPlayer(): Player? {
        return try {
            val name    = getString("name") ?: return null
            val matchId = getString("matchId") ?: return null
            val team    = getLong("team")?.toInt() ?: return null
            Player(id = getString("id") ?: id, name = name, matchId = matchId, team = team)
        } catch (e: Exception) { null }
    }

    private fun DocumentSnapshot.toBall(): Ball? {
        return try {
            val matchId    = getString("matchId") ?: return null
            val innings    = getLong("innings")?.toInt() ?: return null
            val overNumber = getLong("overNumber")?.toInt() ?: return null
            Ball(
                id                = getString("id") ?: id,
                matchId           = matchId,
                innings           = innings,
                overNumber        = overNumber,
                ballInOver        = getLong("ballInOver")?.toInt() ?: 0,
                batsmanId         = getString("batsmanId") ?: "",
                bowlerId          = getString("bowlerId") ?: "",
                runs              = getLong("runs")?.toInt() ?: 0,
                extras            = getLong("extras")?.toInt() ?: 0,
                extraType         = getString("extraType")?.takeIf { it.isNotBlank() }?.let { ExtraType.valueOf(it) },
                isWicket          = getBoolean("isWicket") ?: false,
                wicketType        = getString("wicketType")?.takeIf { it.isNotBlank() }?.let { WicketType.valueOf(it) },
                dismissedPlayerId = getString("dismissedPlayerId")?.takeIf { it.isNotBlank() },
                fielderIds        = getString("fielderIds")?.takeIf { it.isNotBlank() }?.split(",") ?: emptyList()
            )
        } catch (e: Exception) { null }
    }

    // ── Firestore map converters (domain → map) ───────────────────────────────

    private fun Match.toFirestoreMap() = mapOf(
        "id"                  to id,
        "team1Name"           to team1Name,
        "team2Name"           to team2Name,
        "totalOvers"          to totalOvers,
        "playersPerTeam"      to playersPerTeam,
        "tossWonByTeam"       to tossWonByTeam,
        "tossChoice"          to tossChoice.name,
        "status"              to status.name,
        "innings1BattingTeam" to innings1BattingTeam,
        "innings1OnStrikeId"  to innings1OnStrikeId,
        "innings1OffStrikeId" to innings1OffStrikeId,
        "innings1BowlerId"    to innings1BowlerId,
        "innings1Completed"   to innings1Completed,
        "innings2OnStrikeId"  to innings2OnStrikeId,
        "innings2OffStrikeId" to innings2OffStrikeId,
        "innings2BowlerId"    to innings2BowlerId,
        "innings2Completed"   to innings2Completed,
        "createdAt"           to createdAt
    )

    private fun Player.toFirestoreMap() = mapOf(
        "id"      to id,
        "name"    to name,
        "matchId" to matchId,
        "team"    to team
    )

    private fun Ball.toFirestoreMap() = mapOf(
        "id"               to id,
        "matchId"          to matchId,
        "innings"          to innings,
        "overNumber"       to overNumber,
        "ballInOver"       to ballInOver,
        "batsmanId"        to batsmanId,
        "bowlerId"         to bowlerId,
        "runs"             to runs,
        "extras"           to extras,
        "extraType"        to (extraType?.name ?: ""),
        "isWicket"         to isWicket,
        "wicketType"       to (wicketType?.name ?: ""),
        "dismissedPlayerId" to (dismissedPlayerId ?: ""),
        "fielderIds"       to fielderIds.joinToString(",")
    )
}
