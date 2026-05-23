package com.nitish.cricketscoringapp.presentation.tournament.team

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.domain.model.Tournament
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

data class TeamManagementUiState(
    val teams: List<TournamentTeam> = emptyList(),
    val tournament: Tournament? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TeamManagementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TournamentRepository
) : ViewModel() {

    private val tournamentId: String = checkNotNull(savedStateHandle["tournamentId"])

    private val _uiState = MutableStateFlow(TeamManagementUiState())
    val uiState: StateFlow<TeamManagementUiState> = _uiState.asStateFlow()

    val teams: StateFlow<List<TournamentTeam>> =
        repository.getTeamsByTournament(tournamentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val tournament: StateFlow<Tournament?> =
        repository.getTournamentById(tournamentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    fun addTeam(name: String, shortName: String, homeGround: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.upsertTeam(
                    TournamentTeam(
                        tournamentId = tournamentId,
                        name = name.trim(),
                        shortName = shortName.trim().take(4).uppercase(),
                        homeGround = homeGround.trim()
                    )
                )
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to add team") }
            }
        }
    }

    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            runCatching {
                repository.deletePlayersByTeam(teamId)
                repository.deleteTeam(teamId)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to delete team") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
