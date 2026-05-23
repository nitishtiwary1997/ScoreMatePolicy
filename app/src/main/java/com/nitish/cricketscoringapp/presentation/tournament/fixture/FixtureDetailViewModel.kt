package com.nitish.cricketscoringapp.presentation.tournament.fixture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.domain.model.Fixture
import com.nitish.cricketscoringapp.domain.model.Match
import com.nitish.cricketscoringapp.domain.model.Player
import com.nitish.cricketscoringapp.domain.model.Tournament
import com.nitish.cricketscoringapp.domain.model.TournamentPlayer
import com.nitish.cricketscoringapp.domain.model.TournamentTeam
import com.nitish.cricketscoringapp.domain.repository.MatchRepository
import com.nitish.cricketscoringapp.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class FixtureDetailUiState(
    val fixture: Fixture? = null,
    val team1: TournamentTeam? = null,
    val team2: TournamentTeam? = null,
    val tournament: Tournament? = null,
    val isLoading: Boolean = true,
    val isStarting: Boolean = false,
    val startedMatchId: String? = null,
    val error: String? = null
)

@HiltViewModel
class FixtureDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tournamentRepository: TournamentRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val tournamentId: String = checkNotNull(savedStateHandle["tournamentId"])
    private val fixtureId: String = checkNotNull(savedStateHandle["fixtureId"])

    private val _state = MutableStateFlow(FixtureDetailUiState())
    val state: StateFlow<FixtureDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val fixture = tournamentRepository.getFixtureByIdSync(fixtureId)
            val tournament = tournamentRepository.getTournamentByIdSync(tournamentId)
            val team1 = fixture?.let { tournamentRepository.getTeamByIdSync(it.team1Id) }
            val team2 = fixture?.let { tournamentRepository.getTeamByIdSync(it.team2Id) }
            _state.update {
                it.copy(
                    fixture = fixture,
                    tournament = tournament,
                    team1 = team1,
                    team2 = team2,
                    isLoading = false
                )
            }
        }
    }

    fun startMatch() {
        val s = _state.value
        val fixture = s.fixture ?: return
        val tournament = s.tournament ?: return
        val team1 = s.team1 ?: return
        val team2 = s.team2 ?: return
        if (s.isStarting) return

        _state.update { it.copy(isStarting = true, error = null) }

        viewModelScope.launch {
            runCatching {
                val matchId = UUID.randomUUID().toString()

                // Convert registered TournamentPlayers into Match-level Players
                val t1Players = tournamentRepository.getPlayersByTeamSync(team1.id)
                    .map { it.toMatchPlayer(matchId, team = 1) }
                val t2Players = tournamentRepository.getPlayersByTeamSync(team2.id)
                    .map { it.toMatchPlayer(matchId, team = 2) }

                // Fall back to dummy players when team has none registered yet
                val players = (
                    t1Players.ifEmpty { listOf(Player(UUID.randomUUID().toString(), team1.name, matchId, 1)) } +
                    t2Players.ifEmpty { listOf(Player(UUID.randomUUID().toString(), team2.name, matchId, 2)) }
                )

                val match = Match(
                    id = matchId,
                    team1Name = team1.name,
                    team2Name = team2.name,
                    totalOvers = tournament.totalOvers.coerceAtLeast(1),
                    playersPerTeam = tournament.playersPerTeam,
                    tournamentId = tournamentId,
                    fixtureId = fixtureId
                )

                matchRepository.createMatch(match, players)
                tournamentRepository.linkMatchToFixture(fixtureId, matchId)

                _state.update { it.copy(isStarting = false, startedMatchId = matchId) }
            }.onFailure { e ->
                _state.update { it.copy(isStarting = false, error = e.message ?: "Failed to start match") }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearStartedMatch() = _state.update { it.copy(startedMatchId = null) }

    private fun TournamentPlayer.toMatchPlayer(matchId: String, team: Int) =
        Player(id = UUID.randomUUID().toString(), name = name, matchId = matchId, team = team)
}
