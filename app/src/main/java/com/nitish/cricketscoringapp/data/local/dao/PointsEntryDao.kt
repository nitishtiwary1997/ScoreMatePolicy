package com.nitish.cricketscoringapp.data.local.dao

import androidx.room.*
import com.nitish.cricketscoringapp.data.local.entity.PointsEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointsEntryDao {

    @Query("SELECT * FROM points_table WHERE tournamentId = :tournamentId ORDER BY rank ASC")
    fun getPointsTable(tournamentId: String): Flow<List<PointsEntryEntity>>

    @Query("SELECT * FROM points_table WHERE tournamentId = :tournamentId ORDER BY rank ASC")
    suspend fun getPointsTableSync(tournamentId: String): List<PointsEntryEntity>

    @Query("SELECT * FROM points_table WHERE tournamentId = :tournamentId AND teamId = :teamId LIMIT 1")
    suspend fun getEntryForTeam(tournamentId: String, teamId: String): PointsEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: PointsEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntries(entries: List<PointsEntryEntity>)

    @Query("UPDATE points_table SET isSynced = 1 WHERE tournamentId = :tournamentId")
    suspend fun markAllSynced(tournamentId: String)

    @Query("DELETE FROM points_table WHERE tournamentId = :tournamentId")
    suspend fun deleteByTournament(tournamentId: String)
}
