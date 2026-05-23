package com.nitish.cricketscoringapp.domain.repository

import com.nitish.cricketscoringapp.domain.model.Ball
import com.nitish.cricketscoringapp.domain.model.Match
import com.nitish.cricketscoringapp.domain.model.Player
import com.nitish.cricketscoringapp.domain.model.SavedTeam
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getAllMatches(): Flow<List<Match>>
    fun getMatchById(matchId: String): Flow<Match?>
    suspend fun getMatchByIdSync(matchId: String): Match?
    fun getMatchesByTournament(tournamentId: String): Flow<List<Match>>
    fun getPlayersForMatch(matchId: String): Flow<List<Player>>
    fun getBallsForInnings(matchId: String, innings: Int): Flow<List<Ball>>
    suspend fun getBallsForInningsSync(matchId: String, innings: Int): List<Ball>

    suspend fun createMatch(match: Match, players: List<Player>)
    suspend fun updateMatch(match: Match)
    suspend fun addPlayer(player: Player)
    suspend fun recordBall(ball: Ball)
    suspend fun undoLastBall(matchId: String, innings: Int)
    suspend fun setTournamentContext(matchId: String, tournamentId: String, fixtureId: String)

    fun getSavedTeams(): Flow<List<SavedTeam>>
    suspend fun saveTeam(team: SavedTeam)

    fun getAllPlayers(): Flow<List<Player>>
    fun getAllBalls(): Flow<List<Ball>>

    suspend fun syncFromCloud()
}
