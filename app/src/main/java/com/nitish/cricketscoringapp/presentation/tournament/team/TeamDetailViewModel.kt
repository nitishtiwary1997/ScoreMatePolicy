package com.nitish.cricketscoringapp.presentation.tournament.team

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.domain.model.BattingStyle
import com.nitish.cricketscoringapp.domain.model.BowlingStyle
import com.nitish.cricketscoringapp.domain.model.PlayerRole
import com.nitish.cricketscoringapp.domain.model.TournamentPlayer
import com.nitish.cricketscoringapp.domain.model.TournamentTeam
import com.nitish.cricketscoringapp.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPlayerForm(
    val name: String = "",
    val jerseyNumber: String = "",
    val role: PlayerRole = PlayerRole.BATSMAN,
    val battingStyle: BattingStyle = BattingStyle.RIGHT_HAND,
    val bowlingStyle: BowlingStyle = BowlingStyle.NONE,
    val nameError: String? = null
)

data class TeamDetailUiState(
    val team: TournamentTeam? = null,
    val error: String? = null
)

@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TournamentRepository
) : ViewModel() {

    private val tournamentId: String = checkNotNull(savedStateHandle["tournamentId"])
    private val teamId: String = checkNotNull(savedStateHandle["teamId"])

    private val _uiState = MutableStateFlow(TeamDetailUiState())
    val uiState: StateFlow<TeamDetailUiState> = _uiState.asStateFlow()

    val team: StateFlow<TournamentTeam?> =
        repository.getTeamById(teamId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    val players: StateFlow<List<TournamentPlayer>> =
        repository.getPlayersByTeam(teamId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addPlayer(form: AddPlayerForm) {
        if (form.name.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.upsertPlayer(
                    TournamentPlayer(
                        tournamentId = tournamentId,
                        teamId = teamId,
                        name = form.name.trim(),
                        jerseyNumber = form.jerseyNumber.toIntOrNull() ?: 0,
                        role = form.role,
                        battingStyle = form.battingStyle,
                        bowlingStyle = form.bowlingStyle
                    )
                )
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to add player") }
            }
        }
    }

    fun deletePlayer(playerId: String) {
        viewModelScope.launch {
            runCatching {
                repository.deletePlayer(playerId)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to delete player") }
            }
        }
    }

    fun setCaptain(playerId: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateCaptain(teamId, playerId)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to set captain") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
