package com.nitish.cricketscoringapp.data.local.dao

import androidx.room.*
import com.nitish.cricketscoringapp.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE matchId = :matchId")
    fun getPlayersForMatch(matchId: String): Flow<List<PlayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)

    @Query("SELECT * FROM players WHERE userId = :userId ORDER BY name ASC")
    fun getAllPlayers(userId: String): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE matchId = :matchId")
    suspend fun getPlayersForMatchSync(matchId: String): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedPlayers(userId: String): List<PlayerEntity>

    @Query("UPDATE players SET isSynced = 1 WHERE matchId = :matchId")
    suspend fun markPlayersSynced(matchId: String)

    @Query("DELETE FROM players WHERE matchId = :matchId")
    suspend fun deletePlayersForMatch(matchId: String)
}
