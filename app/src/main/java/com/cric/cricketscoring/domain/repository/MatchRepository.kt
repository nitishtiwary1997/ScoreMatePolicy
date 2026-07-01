package com.cric.cricketscoring.domain.repository

import com.cric.cricketscoring.domain.model.Ball
import com.cric.cricketscoring.domain.model.Match
import com.cric.cricketscoring.domain.model.Player
import com.cric.cricketscoring.domain.model.SavedTeam
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
    suspend fun deleteTeam(teamName: String)

    fun getAllPlayers(): Flow<List<Player>>
    fun getAllBalls(): Flow<List<Ball>>

    suspend fun syncFromCloud()

    suspend fun insertMatchLocally(match: Match)
    suspend fun insertPlayersLocally(players: List<Player>)
    suspend fun syncBallsLocally(matchId: String, innings: Int, balls: List<Ball>)
    suspend fun checkForAssignedMatches()
    suspend fun deleteMatch(matchId: String)
}
