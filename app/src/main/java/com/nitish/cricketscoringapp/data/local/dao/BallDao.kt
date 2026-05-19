package com.nitish.cricketscoringapp.data.local.dao

import androidx.room.*
import com.nitish.cricketscoringapp.data.local.entity.BallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BallDao {
    @Query("SELECT * FROM balls WHERE matchId = :matchId AND innings = :innings ORDER BY overNumber ASC, timestamp ASC")
    fun getBallsForInnings(matchId: String, innings: Int): Flow<List<BallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBall(ball: BallEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalls(balls: List<BallEntity>)

    @Query("SELECT id FROM balls WHERE matchId = :matchId AND innings = :innings ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastBallId(matchId: String, innings: Int): String?

    @Query("DELETE FROM balls WHERE id = (SELECT id FROM balls WHERE matchId = :matchId AND innings = :innings ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteLastBall(matchId: String, innings: Int)

    @Query("SELECT * FROM balls WHERE userId = :userId ORDER BY matchId ASC, innings ASC, overNumber ASC, timestamp ASC")
    fun getAllBalls(userId: String): Flow<List<BallEntity>>

    @Query("SELECT * FROM balls WHERE matchId = :matchId ORDER BY innings ASC, overNumber ASC, timestamp ASC")
    suspend fun getBallsForMatchSync(matchId: String): List<BallEntity>

    @Query("SELECT * FROM balls WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedBalls(userId: String): List<BallEntity>

    @Query("UPDATE balls SET isSynced = 1 WHERE id = :ballId")
    suspend fun markBallSynced(ballId: String)

    @Query("UPDATE balls SET isSynced = 1 WHERE matchId = :matchId")
    suspend fun markBallsSyncedForMatch(matchId: String)

    @Query("DELETE FROM balls WHERE matchId = :matchId")
    suspend fun deleteBallsForMatch(matchId: String)
}
