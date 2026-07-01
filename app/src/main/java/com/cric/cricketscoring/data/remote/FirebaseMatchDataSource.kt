package com.cric.cricketscoring.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.cric.cricketscoring.domain.model.Ball
import com.cric.cricketscoring.domain.model.ExtraType
import com.cric.cricketscoring.domain.model.Match
import com.cric.cricketscoring.domain.model.MatchStatus
import com.cric.cricketscoring.domain.model.Player
import com.cric.cricketscoring.domain.model.TossChoice
import com.cric.cricketscoring.domain.model.SavedTeam
import com.cric.cricketscoring.domain.model.WicketType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class CloudSyncData(
    val matches: List<Match>,
    val players: List<Player>,
    val balls: List<Ball>,
    val savedTeams: List<SavedTeam> = emptyList()
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
    private fun userRoot(ownerId: String = "") = firestore.collection("users").document(ownerId.ifBlank { userSession.userId })
    private fun matchDoc(ownerId: String, matchId: String) = userRoot(ownerId).collection("matches").document(matchId)
    private fun savedTeamsCol() = userRoot().collection("saved_teams")

    // ── Write operations ──────────────────────────────────────────────────────

    suspend fun saveMatch(ownerId: String, match: Match) {
        runCatching {
            matchDoc(ownerId, match.id).set(match.toFirestoreMap(), SetOptions.merge()).await()
        }.getOrThrow()
    }

    suspend fun savePlayers(ownerId: String, matchId: String, players: List<Player>) {
        runCatching {
            val batch = firestore.batch()
            val col = matchDoc(ownerId, matchId).collection("players")
            players.forEach { p ->
                batch.set(col.document(p.id), p.toFirestoreMap(), SetOptions.merge())
            }
            batch.commit().await()
        }.getOrThrow()
    }

    suspend fun saveBall(ownerId: String, ball: Ball) {
        runCatching {
            matchDoc(ownerId, ball.matchId)
                .collection("balls_innings${ball.innings}")
                .document(ball.id)
                .set(ball.toFirestoreMap(), SetOptions.merge())
                .await()
        }.getOrThrow()
    }

    suspend fun deleteLastBall(ownerId: String, matchId: String, innings: Int, ballId: String) {
        runCatching {
            matchDoc(ownerId, matchId)
                .collection("balls_innings$innings")
                .document(ballId)
                .delete()
                .await()
        }.getOrThrow()
    }

    suspend fun saveSavedTeam(team: SavedTeam) {
        runCatching {
            savedTeamsCol().document(team.name)
                .set(mapOf(
                    "name" to team.name,
                    "playerNames" to team.playerNames
                ), SetOptions.merge())
                .await()
        }.getOrThrow()
    }

    suspend fun deleteSavedTeam(teamName: String) {
        runCatching {
            savedTeamsCol().document(teamName).delete().await()
        }.getOrThrow()
    }

    suspend fun deleteMatch(ownerId: String, matchId: String) {
        runCatching {
            matchDoc(ownerId, matchId).delete().await()
        }.getOrThrow()
    }

    suspend fun transferScoringPermission(ownerId: String, matchId: String, inviteeMobile: String, role: String) {
        runCatching {
            val inviteeQuery = firestore.collection("users")
                .whereEqualTo("mobile", inviteeMobile.trim())
                .get()
                .await()
            
            val inviteeId = inviteeQuery.documents.firstOrNull()?.getString("id") ?: ""

            val permissionData = mapOf(
                "role" to role,
                "active" to true,
                "assignedAt" to System.currentTimeMillis(),
                "userMobile" to inviteeMobile.trim(),
                "inviteeId" to inviteeId
            )
            
            matchDoc(ownerId, matchId)
                .collection("permissions")
                .document(inviteeMobile.trim())
                .set(permissionData, SetOptions.merge())
                .await()

            if (role == "Editor" && inviteeId.isNotEmpty()) {
                matchDoc(ownerId, matchId)
                    .update("currentEditorId", inviteeId)
                    .await()
            }
        }
    }

    suspend fun removePermission(ownerId: String, matchId: String, inviteeMobile: String) {
        runCatching {
            matchDoc(ownerId, matchId)
                .collection("permissions")
                .document(inviteeMobile.trim())
                .update("active", false)
                .await()

            matchDoc(ownerId, matchId)
                .update("currentEditorId", ownerId)
                .await()
        }
    }

    fun listenToMatch(ownerId: String, matchId: String): Flow<Match?> = callbackFlow {
        val docRef = matchDoc(ownerId, matchId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toMatch())
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    fun listenToPlayers(ownerId: String, matchId: String): Flow<List<Player>> = callbackFlow {
        val colRef = matchDoc(ownerId, matchId).collection("players")
        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val playersList = snapshot?.documents?.mapNotNull { it.toPlayer() } ?: emptyList()
            trySend(playersList)
        }
        awaitClose { listener.remove() }
    }

    fun listenToBalls(ownerId: String, matchId: String, innings: Int): Flow<List<Ball>> = callbackFlow {
        val colRef = matchDoc(ownerId, matchId).collection("balls_innings$innings")
        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val ballsList = snapshot?.documents?.mapNotNull { it.toBall() } ?: emptyList()
            trySend(ballsList)
        }
        awaitClose { listener.remove() }
    }

    suspend fun detectAssignedMatches(userMobile: String): List<Match> {
        return try {
            if (userMobile.isBlank()) return emptyList()
            val querySnapshot = firestore.collectionGroup("permissions")
                .whereEqualTo("userMobile", userMobile.trim())
                .whereEqualTo("active", true)
                .get()
                .await()

            val matchesList = mutableListOf<Match>()
            for (doc in querySnapshot.documents) {
                val parentDocRef = doc.reference.parent.parent ?: continue
                val matchSnapshot = parentDocRef.get().await()
                matchSnapshot.toMatch()?.let { matchesList.add(it) }
            }
            matchesList
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Read / sync operation ─────────────────────────────────────────────────

    suspend fun fetchAllUserData(): CloudSyncData {
        val uid = userSession.userId
        if (uid.isEmpty()) return CloudSyncData(emptyList(), emptyList(), emptyList(), emptyList())

        val matches = mutableListOf<Match>()
        val players = mutableListOf<Player>()
        val balls   = mutableListOf<Ball>()
        val savedTeams = mutableListOf<SavedTeam>()

        // Fetch saved teams
        val savedTeamDocs = runCatching {
            savedTeamsCol().get().await()
        }.getOrNull()

        if (savedTeamDocs != null) {
            for (doc in savedTeamDocs.documents) {
                val name = doc.getString("name") ?: continue
                val playerNames = (doc.get("playerNames") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                savedTeams += SavedTeam(name = name, playerNames = playerNames)
            }
        }

        val matchDocs = runCatching {
            userRoot().collection("matches").get().await()
        }.getOrNull()

        if (matchDocs != null) {
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
        }

        return CloudSyncData(matches, players, balls, savedTeams)
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
                createdAt           = getLong("createdAt") ?: System.currentTimeMillis(),
                ownerId             = getString("ownerId") ?: "",
                currentEditorId     = getString("currentEditorId") ?: ""
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
                fielderIds        = getString("fielderIds")?.takeIf { it.isNotBlank() }?.split(",") ?: emptyList(),
                timestamp         = getLong("timestamp") ?: System.currentTimeMillis()
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
        "createdAt"           to createdAt,
        "ownerId"             to ownerId,
        "currentEditorId"     to currentEditorId
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
        "fielderIds"       to fielderIds.joinToString(","),
        "timestamp"        to timestamp
    )
}
