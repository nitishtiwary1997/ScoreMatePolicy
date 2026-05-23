package com.nitish.cricketscoringapp.presentation.createMatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.domain.model.Match
import com.nitish.cricketscoringapp.domain.model.MatchStatus
import com.nitish.cricketscoringapp.domain.model.Player
import com.nitish.cricketscoringapp.domain.model.SavedTeam
import com.nitish.cricketscoringapp.domain.model.TossChoice
import com.nitish.cricketscoringapp.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreateMatchUiState(
    val team1Name: String = "",
    val team2Name: String = "",
    val team1Players: List<String> = emptyList(),
    val team2Players: List<String> = emptyList(),
    val totalOvers: String = "20",
    val playersPerTeam: String = "11",
    val newPlayerName: String = "",
    val addingForTeam: Int = 1,
    val isCreating: Boolean = false,
    val createdMatchId: String? = null,
    val error: String? = null,
    val savedTeams: List<SavedTeam> = emptyList(),
    val teamSaved: Boolean = false
) {
    val canCreate: Boolean
        get() = team1Name.isNotBlank() && team2Name.isNotBlank() &&
                team1Players.size >= 2 && team2Players.size >= 2 &&
                (totalOvers.toIntOrNull() ?: 0) > 0 &&
                (playersPerTeam.toIntOrNull() ?: 0) >= 2
}

@HiltViewModel
class CreateMatchViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMatchUiState())
    val uiState: StateFlow<CreateMatchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSavedTeams().collect { teams ->
                _uiState.update { it.copy(savedTeams = teams) }
            }
        }
    }

    fun saveCurrentTeam() {
        val state = _uiState.value
        val teamName = if (state.addingForTeam == 1) state.team1Name.trim() else state.team2Name.trim()
        val players = if (state.addingForTeam == 1) state.team1Players else state.team2Players
        if (teamName.isBlank() || players.isEmpty()) return
        viewModelScope.launch {
            repository.saveTeam(SavedTeam(name = teamName, playerNames = players))
            _uiState.update { it.copy(teamSaved = true) }
            delay(2000)
            _uiState.update { it.copy(teamSaved = false) }
        }
    }

    fun deleteSavedTeam(teamName: String) {
        viewModelScope.launch {
            repository.deleteTeam(teamName)
        }
    }

    fun loadSavedTeamPlayers(playerNames: List<String>) {
        val state = _uiState.value
        if (state.addingForTeam == 1) {
            _uiState.update { it.copy(team1Players = playerNames) }
        } else {
            _uiState.update { it.copy(team2Players = playerNames) }
        }
    }

    fun onTeam1NameChange(name: String) = _uiState.update { it.copy(team1Name = name, error = null) }
    fun onTeam2NameChange(name: String) = _uiState.update { it.copy(team2Name = name, error = null) }
    fun onOversChange(overs: String) = _uiState.update { it.copy(totalOvers = overs, error = null) }
    fun onPlayersPerTeamChange(value: String) = _uiState.update { it.copy(playersPerTeam = value, error = null) }
    fun onNewPlayerNameChange(name: String) = _uiState.update { it.copy(newPlayerName = name) }
    fun onAddingForTeamChange(team: Int) = _uiState.update { it.copy(addingForTeam = team, newPlayerName = "") }

    fun addPlayer() {
        val state = _uiState.value
        val name = state.newPlayerName.trim()
        if (name.isBlank()) return
        if (state.addingForTeam == 1) {
            _uiState.update { it.copy(team1Players = it.team1Players + name, newPlayerName = "") }
        } else {
            _uiState.update { it.copy(team2Players = it.team2Players + name, newPlayerName = "") }
        }
    }

    fun removePlayer(team: Int, index: Int) {
        if (team == 1) {
            _uiState.update { it.copy(team1Players = it.team1Players.toMutableList().also { l -> l.removeAt(index) }) }
        } else {
            _uiState.update { it.copy(team2Players = it.team2Players.toMutableList().also { l -> l.removeAt(index) }) }
        }
    }

    fun createMatch() {
        val state = _uiState.value
        if (!state.canCreate) {
            _uiState.update { it.copy(error = "Fill all fields. Each team needs at least 2 players.") }
            return
        }
        val overs = state.totalOvers.toInt()
        val perTeam = state.playersPerTeam.toInt()
        val matchId = UUID.randomUUID().toString()

        val players = state.team1Players.mapIndexed { i, name ->
            Player(id = UUID.randomUUID().toString(), name = name, matchId = matchId, team = 1)
        } + state.team2Players.mapIndexed { i, name ->
            Player(id = UUID.randomUUID().toString(), name = name, matchId = matchId, team = 2)
        }

        val match = Match(
            id = matchId,
            team1Name = state.team1Name.trim(),
            team2Name = state.team2Name.trim(),
            totalOvers = overs,
            playersPerTeam = perTeam,
            status = MatchStatus.TOSS,
            tossChoice = TossChoice.BAT
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            repository.createMatch(match, players)
            _uiState.update { it.copy(isCreating = false, createdMatchId = matchId) }
        }
    }
}
