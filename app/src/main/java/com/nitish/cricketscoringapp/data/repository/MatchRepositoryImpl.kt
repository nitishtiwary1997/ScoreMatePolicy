package com.nitish.cricketscoringapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.nitish.cricketscoringapp.data.local.dao.BallDao
import com.nitish.cricketscoringapp.data.local.dao.MatchDao
import com.nitish.cricketscoringapp.data.local.dao.PlayerDao
import com.nitish.cricketscoringapp.data.local.dao.SavedTeamDao
import com.nitish.cricketscoringapp.data.local.entity.BallEntity
import com.nitish.cricketscoringapp.data.local.entity.MatchEntity
import com.nitish.cricketscoringapp.data.local.entity.PlayerEntity
import com.nitish.cricketscoringapp.data.local.entity.SavedTeamEntity
import com.nitish.cricketscoringapp.data.remote.FirebaseMatchDataSource
import com.nitish.cricketscoringapp.data.remote.UserSession
import com.nitish.cricketscoringapp.domain.model.*
import com.nitish.cricketscoringapp.domain.repository.MatchRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao,
    private val ballDao: BallDao,
    private val savedTeamDao: SavedTeamDao,
    private val firebase: FirebaseMatchDataSource,
    private val userSession: UserSession,
    @ApplicationContext private val context: Context
) : MatchRepository {

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val isSyncing = AtomicBoolean(false)

    init {
        registerNetworkCallback()
        // Attempt sync on cold start in case there are pending items
        syncScope.launch { syncPendingToCloud() }
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    override fun getAllMatches(): Flow<List<Match>> =
        matchDao.getAllMatches(userSession.userId).map { list -> list.map { it.toDomain() } }

    override fun getMatchById(matchId: String): Flow<Match?> =
        matchDao.getMatchById(matchId).map { it?.toDomain() }

    override fun getPlayersForMatch(matchId: String): Flow<List<Player>> =
        playerDao.getPlayersForMatch(matchId).map { list -> list.map { it.toDomain() } }

    override fun getBallsForInnings(matchId: String, innings: Int): Flow<List<Ball>> =
        ballDao.getBallsForInnings(matchId, innings).map { list -> list.map { it.toDomain() } }

    override fun getSavedTeams(): Flow<List<SavedTeam>> =
        savedTeamDao.getAllSavedTeams().map { list -> list.map { it.toDomain() } }

    override fun getAllPlayers(): Flow<List<Player>> =
        playerDao.getAllPlayers(userSession.userId).map { list -> list.map { it.toDomain() } }

    override fun getAllBalls(): Flow<List<Ball>> =
        ballDao.getAllBalls(userSession.userId).map { list -> list.map { it.toDomain() } }

    // ── Write (Room first, Firebase async) ───────────────────────────────────

    override suspend fun createMatch(match: Match, players: List<Player>) {
        matchDao.insertMatch(match.toEntity())                    // isSynced = false
        playerDao.insertPlayers(players.map { it.toEntity() })   // isSynced = false
        syncScope.launch {
            try {
                firebase.saveMatch(match)
                matchDao.markMatchSynced(match.id)
            } catch (_: Exception) { }
            try {
                firebase.savePlayers(match.id, players)
                playerDao.markPlayersSynced(match.id)
            } catch (_: Exception) { }
        }
    }

    override suspend fun updateMatch(match: Match) {
        matchDao.updateMatch(match.toEntity())      // resets isSynced = false on state change
        syncScope.launch {
            try {
                firebase.saveMatch(match)
                matchDao.markMatchSynced(match.id)
            } catch (_: Exception) { }
        }
    }

    override suspend fun addPlayer(player: Player) {
        playerDao.insertPlayer(player.toEntity())
    }

    override suspend fun recordBall(ball: Ball) {
        ballDao.insertBall(ball.toEntity())          // isSynced = false
        syncScope.launch {
            try {
                firebase.saveBall(ball)
                ballDao.markBallSynced(ball.id)
            } catch (_: Exception) { }
        }
    }

    override suspend fun undoLastBall(matchId: String, innings: Int) {
        val lastBallId = ballDao.getLastBallId(matchId, innings)
        ballDao.deleteLastBall(matchId, innings)
        lastBallId?.let { id ->
            syncScope.launch { runCatching { firebase.deleteLastBall(matchId, innings, id) } }
        }
    }

    override suspend fun saveTeam(team: SavedTeam) {
        savedTeamDao.upsertSavedTeam(team.toEntity())
    }

    // ── Cloud sync ────────────────────────────────────────────────────────────

    /** Pulls the current user's data from Firestore into Room on login. */
    override suspend fun syncFromCloud() {
        try {
            val data = firebase.fetchAllUserData()
            if (data.matches.isEmpty()) return
            // Mark as already synced — they came FROM Firebase
            matchDao.insertMatches(data.matches.map { it.toEntity().copy(isSynced = true) })
            playerDao.insertPlayers(data.players.map { it.toEntity().copy(isSynced = true) })
            if (data.balls.isNotEmpty())
                ballDao.insertBalls(data.balls.map { it.toEntity().copy(isSynced = true) })
        } catch (_: Exception) { }
    }

    /**
     * Pushes every unsynced Room item to Firebase match-by-match.
     * On full-bundle success: marks synced.
     * After all items are synced: deletes completed matches from Room to free local storage.
     * Guarded by [isSyncing] so concurrent calls collapse.
     */
    private suspend fun syncPendingToCloud() {
        val uid = userSession.userId
        if (uid.isEmpty() || !isSyncing.compareAndSet(false, true)) return
        try {
            // 1. Sync each match bundle that has unsynced data
            val unsyncedMatches = matchDao.getUnsyncedMatches(uid)
            for (entity in unsyncedMatches) {
                syncMatchBundle(entity)
            }

            // 2. Sync orphaned unsynced players (e.g. addPlayer called without createMatch)
            val orphanPlayers = playerDao.getUnsyncedPlayers(uid)
                .filter { p -> matchDao.getUnsyncedMatches(uid).none { it.id == p.matchId } }
            orphanPlayers.groupBy { it.matchId }.forEach { (matchId, players) ->
                try {
                    firebase.savePlayers(matchId, players.map { it.toDomain() })
                    playerDao.markPlayersSynced(matchId)
                } catch (_: Exception) { }
            }

            // 3. Remove completed + fully synced match bundles from local storage
            val toDelete = matchDao.getFullySyncedCompletedMatches(uid)
            for (match in toDelete) {
                ballDao.deleteBallsForMatch(match.id)
                playerDao.deletePlayersForMatch(match.id)
                matchDao.deleteMatch(match.id)
            }
        } finally {
            isSyncing.set(false)
        }
    }

    /** Syncs one match's full bundle (match doc + players + balls) atomically. */
    private suspend fun syncMatchBundle(entity: MatchEntity) {
        val matchId = entity.id
        try {
            firebase.saveMatch(entity.toDomain())

            val players = playerDao.getPlayersForMatchSync(matchId)
            if (players.isNotEmpty()) {
                firebase.savePlayers(matchId, players.map { it.toDomain() })
            }

            val balls = ballDao.getBallsForMatchSync(matchId)
            for (ball in balls) {
                firebase.saveBall(ball.toDomain())
            }

            // All three steps succeeded → mark everything synced
            matchDao.markMatchSynced(matchId)
            playerDao.markPlayersSynced(matchId)
            ballDao.markBallsSyncedForMatch(matchId)
        } catch (_: Exception) {
            // Partial failure — leave isSynced = false for next retry
        }
    }

    /** Registers a network callback so pending items are synced as soon as connectivity returns. */
    private fun registerNetworkCallback() {
        try {
            val cm = context.getSystemService(ConnectivityManager::class.java)
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    syncScope.launch { syncPendingToCloud() }
                }
            })
        } catch (_: Exception) { }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun MatchEntity.toDomain() = Match(
        id = id, team1Name = team1Name, team2Name = team2Name,
        totalOvers = totalOvers, playersPerTeam = playersPerTeam,
        tossWonByTeam = tossWonByTeam,
        tossChoice = TossChoice.valueOf(tossChoice),
        status = MatchStatus.valueOf(status),
        innings1BattingTeam = innings1BattingTeam,
        innings1OnStrikeId = innings1OnStrikeId, innings1OffStrikeId = innings1OffStrikeId,
        innings1BowlerId = innings1BowlerId, innings1Completed = innings1Completed,
        innings2OnStrikeId = innings2OnStrikeId, innings2OffStrikeId = innings2OffStrikeId,
        innings2BowlerId = innings2BowlerId, innings2Completed = innings2Completed,
        createdAt = createdAt
    )

    private fun Match.toEntity() = MatchEntity(
        id = id, userId = userSession.userId,
        team1Name = team1Name, team2Name = team2Name,
        totalOvers = totalOvers, playersPerTeam = playersPerTeam,
        tossWonByTeam = tossWonByTeam,
        tossChoice = tossChoice.name, status = status.name,
        innings1BattingTeam = innings1BattingTeam,
        innings1OnStrikeId = innings1OnStrikeId, innings1OffStrikeId = innings1OffStrikeId,
        innings1BowlerId = innings1BowlerId, innings1Completed = innings1Completed,
        innings2OnStrikeId = innings2OnStrikeId, innings2OffStrikeId = innings2OffStrikeId,
        innings2BowlerId = innings2BowlerId, innings2Completed = innings2Completed,
        createdAt = createdAt,
        isSynced = false   // always reset on write; Firebase sync marks it true
    )

    private fun PlayerEntity.toDomain() = Player(id = id, name = name, matchId = matchId, team = team)
    private fun Player.toEntity() = PlayerEntity(
        id = id, userId = userSession.userId,
        name = name, matchId = matchId, team = team,
        isSynced = false
    )

    private fun BallEntity.toDomain() = Ball(
        id = id, matchId = matchId, innings = innings,
        overNumber = overNumber, ballInOver = ballInOver,
        batsmanId = batsmanId, bowlerId = bowlerId,
        runs = runs, extras = extras,
        extraType = extraType?.let { ExtraType.valueOf(it) },
        isWicket = isWicket,
        wicketType = wicketType?.let { WicketType.valueOf(it) },
        dismissedPlayerId = dismissedPlayerId,
        fielderIds = if (fielderIds.isBlank()) emptyList() else fielderIds.split(",")
    )

    private fun Ball.toEntity() = BallEntity(
        id = id, userId = userSession.userId,
        matchId = matchId, innings = innings,
        overNumber = overNumber, ballInOver = ballInOver,
        batsmanId = batsmanId, bowlerId = bowlerId,
        runs = runs, extras = extras,
        extraType = extraType?.name, isWicket = isWicket,
        wicketType = wicketType?.name,
        dismissedPlayerId = dismissedPlayerId,
        fielderIds = fielderIds.joinToString(","),
        isSynced = false
    )

    private fun SavedTeamEntity.toDomain() = SavedTeam(
        name = name,
        playerNames = playerNames.split("\n").filter { it.isNotBlank() }
    )

    private fun SavedTeam.toEntity() = SavedTeamEntity(
        name = name,
        playerNames = playerNames.joinToString("\n")
    )
}
