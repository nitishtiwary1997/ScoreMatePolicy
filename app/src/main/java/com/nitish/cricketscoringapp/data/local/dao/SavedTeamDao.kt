package com.nitish.cricketscoringapp.data.local.dao

import androidx.room.*
import com.nitish.cricketscoringapp.data.local.entity.SavedTeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedTeamDao {
    @Query("SELECT * FROM saved_teams ORDER BY name ASC")
    fun getAllSavedTeams(): Flow<List<SavedTeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedTeam(team: SavedTeamEntity)
}
