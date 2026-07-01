package com.cric.cricketscoring.data.local.dao

import androidx.room.*
import com.cric.cricketscoring.data.local.entity.TournamentPlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentPlayerDao {

    @Query("SELECT * FROM tournament_players WHERE teamId = :teamId ORDER BY jerseyNumber ASC")
    fun getPlayersByTeam(teamId: String): Flow<List<TournamentPlayerEntity>>

    @Query("SELECT * FROM tournament_players WHERE teamId = :teamId ORDER BY jerseyNumber ASC")
    suspend fun getPlayersByTeamSync(teamId: String): List<TournamentPlayerEntity>

    @Query("SELECT * FROM tournament_players WHERE tournamentId = :tournamentId ORDER BY name ASC")
    fun getPlayersByTournament(tournamentId: String): Flow<List<TournamentPlayerEntity>>

    @Query("SELECT * FROM tournament_players WHERE tournamentId = :tournamentId ORDER BY name ASC")
    suspend fun getPlayersByTournamentSync(tournamentId: String): List<TournamentPlayerEntity>

    @Query("SELECT * FROM tournament_players WHERE id = :playerId")
    fun getPlayerById(playerId: String): Flow<TournamentPlayerEntity?>

    @Query("SELECT * FROM tournament_players WHERE id = :playerId")
    suspend fun getPlayerByIdSync(playerId: String): TournamentPlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayer(player: TournamentPlayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayers(players: List<TournamentPlayerEntity>)

    @Update
    suspend fun updatePlayer(player: TournamentPlayerEntity)

    @Query("UPDATE tournament_players SET isSynced = 1 WHERE id = :playerId")
    suspend fun markSynced(playerId: String)

    @Query("SELECT * FROM tournament_players WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TournamentPlayerEntity>

    @Query("DELETE FROM tournament_players WHERE id = :playerId")
    suspend fun deletePlayer(playerId: String)

    @Query("DELETE FROM tournament_players WHERE teamId = :teamId")
    suspend fun deletePlayersByTeam(teamId: String)

    @Query("SELECT COUNT(*) FROM tournament_players WHERE teamId = :teamId")
    suspend fun getPlayerCount(teamId: String): Int
}
