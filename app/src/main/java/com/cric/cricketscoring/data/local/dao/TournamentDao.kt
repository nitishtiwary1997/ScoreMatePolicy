package com.cric.cricketscoring.data.local.dao

import androidx.room.*
import com.cric.cricketscoring.data.local.entity.TournamentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {

    @Query("SELECT * FROM tournaments WHERE createdByUserId = :userId ORDER BY createdAt DESC")
    fun getTournamentsByUser(userId: String): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE isPublic = 1 ORDER BY startDate DESC")
    fun getPublicTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    fun getTournamentById(id: String): Flow<TournamentEntity?>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    suspend fun getTournamentByIdSync(id: String): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTournament(tournament: TournamentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTournaments(tournaments: List<TournamentEntity>)

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("UPDATE tournaments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE tournaments SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM tournaments WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TournamentEntity>

    @Query("DELETE FROM tournaments WHERE id = :id")
    suspend fun deleteTournament(id: String)
}
