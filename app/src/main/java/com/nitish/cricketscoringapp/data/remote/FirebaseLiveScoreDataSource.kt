package com.nitish.cricketscoringapp.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nitish.cricketscoringapp.domain.model.LiveScoreSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pushes and listens to live score snapshots at live_scores/{matchId}.
 * Global collection (not user-scoped) so any device can observe live scores.
 */
@Singleton
class FirebaseLiveScoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun doc(matchId: String) = firestore.collection("live_scores").document(matchId)

    suspend fun push(snapshot: LiveScoreSnapshot) {
        runCatching {
            doc(snapshot.matchId).set(snapshot.toMap(), SetOptions.merge()).await()
        }
    }

    suspend fun clear(matchId: String) {
        runCatching { doc(matchId).delete().await() }
    }

    fun observe(matchId: String): Flow<LiveScoreSnapshot?> = callbackFlow {
        val registration = doc(matchId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toSnapshot())
        }
        awaitClose { registration.remove() }
    }

    private fun LiveScoreSnapshot.toMap(): Map<String, Any?> = mapOf(
        "matchId"          to matchId,
        "tournamentId"     to tournamentId,
        "fixtureId"        to fixtureId,
        "team1Name"        to team1Name,
        "team2Name"        to team2Name,
        "currentInnings"   to currentInnings,
        "inn1BattingTeam"  to inn1BattingTeam,
        "inn1Runs"         to inn1Runs,
        "inn1Wickets"      to inn1Wickets,
        "inn1Overs"        to inn1Overs,
        "inn1IsCompleted"  to inn1IsCompleted,
        "inn2BattingTeam"  to inn2BattingTeam,
        "inn2Runs"         to inn2Runs,
        "inn2Wickets"      to inn2Wickets,
        "inn2Overs"        to inn2Overs,
        "target"           to target,
        "requiredRuns"     to requiredRuns,
        "requiredBalls"    to requiredBalls,
        "currentRunRate"   to currentRunRate,
        "requiredRunRate"  to requiredRunRate,
        "strikerName"      to strikerName,
        "strikerRuns"      to strikerRuns,
        "strikerBalls"     to strikerBalls,
        "strikerFours"     to strikerFours,
        "strikerSixes"     to strikerSixes,
        "strikerSR"        to strikerSR,
        "nonStrikerName"   to nonStrikerName,
        "nonStrikerRuns"   to nonStrikerRuns,
        "nonStrikerBalls"  to nonStrikerBalls,
        "bowlerName"       to bowlerName,
        "bowlerWickets"    to bowlerWickets,
        "bowlerRuns"       to bowlerRuns,
        "bowlerOvers"      to bowlerOvers,
        "bowlerEcon"       to bowlerEcon,
        "lastBallDesc"     to lastBallDesc,
        "status"           to status,
        "updatedAt"        to updatedAt
    )

    private fun DocumentSnapshot.toSnapshot(): LiveScoreSnapshot? {
        if (!exists()) return null
        return runCatching {
            LiveScoreSnapshot(
                matchId          = getString("matchId") ?: id,
                tournamentId     = getString("tournamentId") ?: "",
                fixtureId        = getString("fixtureId") ?: "",
                team1Name        = getString("team1Name") ?: "",
                team2Name        = getString("team2Name") ?: "",
                currentInnings   = getLong("currentInnings")?.toInt() ?: 1,
                inn1BattingTeam  = getString("inn1BattingTeam") ?: "",
                inn1Runs         = getLong("inn1Runs")?.toInt() ?: 0,
                inn1Wickets      = getLong("inn1Wickets")?.toInt() ?: 0,
                inn1Overs        = getString("inn1Overs") ?: "0.0",
                inn1IsCompleted  = getBoolean("inn1IsCompleted") ?: false,
                inn2BattingTeam  = getString("inn2BattingTeam") ?: "",
                inn2Runs         = getLong("inn2Runs")?.toInt() ?: 0,
                inn2Wickets      = getLong("inn2Wickets")?.toInt() ?: 0,
                inn2Overs        = getString("inn2Overs") ?: "0.0",
                target           = getLong("target")?.toInt() ?: 0,
                requiredRuns     = getLong("requiredRuns")?.toInt() ?: 0,
                requiredBalls    = getLong("requiredBalls")?.toInt() ?: 0,
                currentRunRate   = getString("currentRunRate") ?: "0.00",
                requiredRunRate  = getString("requiredRunRate") ?: "-",
                strikerName      = getString("strikerName") ?: "",
                strikerRuns      = getLong("strikerRuns")?.toInt() ?: 0,
                strikerBalls     = getLong("strikerBalls")?.toInt() ?: 0,
                strikerFours     = getLong("strikerFours")?.toInt() ?: 0,
                strikerSixes     = getLong("strikerSixes")?.toInt() ?: 0,
                strikerSR        = getString("strikerSR") ?: "0.0",
                nonStrikerName   = getString("nonStrikerName") ?: "",
                nonStrikerRuns   = getLong("nonStrikerRuns")?.toInt() ?: 0,
                nonStrikerBalls  = getLong("nonStrikerBalls")?.toInt() ?: 0,
                bowlerName       = getString("bowlerName") ?: "",
                bowlerWickets    = getLong("bowlerWickets")?.toInt() ?: 0,
                bowlerRuns       = getLong("bowlerRuns")?.toInt() ?: 0,
                bowlerOvers      = getString("bowlerOvers") ?: "0.0",
                bowlerEcon       = getString("bowlerEcon") ?: "0.00",
                lastBallDesc     = getString("lastBallDesc") ?: "",
                status           = getString("status") ?: "LIVE",
                updatedAt        = getLong("updatedAt") ?: 0L
            )
        }.getOrNull()
    }
}
