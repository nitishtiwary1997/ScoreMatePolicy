package com.nitish.cricketscoringapp.data.local.dao

import androidx.room.*
import com.nitish.cricketscoringapp.data.local.entity.TournamentTeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentTeamDao {

    @Query("SELECT * FROM tournament_teams WHERE tournamentId = :tournamentId ORDER BY name ASC")
    fun getTeamsByTournament(tournamentId: String): Flow<List<TournamentTeamEntity>>

    @Query("SELECT * FROM tournament_teams WHERE tournamentId = :tournamentId ORDER BY name ASC")
    suspend fun getTeamsByTournamentSync(tournamentId: String): List<TournamentTeamEntity>

    @Query("SELECT * FROM tournament_teams WHERE id = :teamId")
    fun getTeamById(teamId: String): Flow<TournamentTeamEntity?>

    @Query("SELECT * FROM tournament_teams WHERE id = :teamId")
    suspend fun getTeamByIdSync(teamId: String): TournamentTeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTeam(team: TournamentTeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTeams(teams: List<TournamentTeamEntity>)

    @Update
    suspend fun updateTeam(team: TournamentTeamEntity)

    @Query("UPDATE tournament_teams SET captainPlayerId = :captainId WHERE id = :teamId")
    suspend fun updateCaptain(teamId: String, captainId: String)

    @Query("UPDATE tournament_teams SET isSynced = 1 WHERE id = :teamId")
    suspend fun markSynced(teamId: String)

    @Query("SELECT * FROM tournament_teams WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TournamentTeamEntity>

    @Query("DELETE FROM tournament_teams WHERE id = :teamId")
    suspend fun deleteTeam(teamId: String)

    @Query("SELECT COUNT(*) FROM tournament_teams WHERE tournamentId = :tournamentId")
    suspend fun getTeamCount(tournamentId: String): Int
}
