package com.nitish.cricketscoringapp.data.local.dao

import androidx.room.*
import com.nitish.cricketscoringapp.data.local.entity.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllMatches(userId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun getMatchById(matchId: String): Flow<MatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("SELECT * FROM matches WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedMatches(userId: String): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE userId = :userId AND isSynced = 1 AND status = 'COMPLETED' AND id NOT IN (SELECT matchId FROM players WHERE isSynced = 0) AND id NOT IN (SELECT matchId FROM balls WHERE isSynced = 0)")
    suspend fun getFullySyncedCompletedMatches(userId: String): List<MatchEntity>

    @Query("UPDATE matches SET isSynced = 1 WHERE id = :matchId")
    suspend fun markMatchSynced(matchId: String)

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatch(matchId: String)
}
