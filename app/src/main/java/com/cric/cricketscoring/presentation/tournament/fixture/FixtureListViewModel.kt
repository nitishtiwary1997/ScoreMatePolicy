package com.cric.cricketscoring.presentation.tournament.fixture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cric.cricketscoring.domain.model.Fixture
import com.cric.cricketscoring.domain.model.FixtureStage
import com.cric.cricketscoring.domain.model.Tournament
import com.cric.cricketscoring.domain.model.TournamentTeam
import com.cric.cricketscoring.domain.repository.TournamentRepository
import com.cric.cricketscoring.domain.usecase.tournament.GenerateFixturesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FixtureListUiState(
    val isGenerating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FixtureListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TournamentRepository,
    private val generateFixtures: GenerateFixturesUseCase
) : ViewModel() {

    val tournamentId: String = checkNotNull(savedStateHandle["tournamentId"])

    private val _uiState = MutableStateFlow(FixtureListUiState())
    val uiState: StateFlow<FixtureListUiState> = _uiState.asStateFlow()

    val tournament: StateFlow<Tournament?> =
        repository.getTournamentById(tournamentId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val fixtures: StateFlow<List<Fixture>> =
        repository.getFixturesByTournament(tournamentId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val teamsMap: StateFlow<Map<String, TournamentTeam>> =
        repository.getTeamsByTournament(tournamentId)
            .map { list -> list.associateBy { it.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val teamCount: StateFlow<Int> =
        repository.getTeamsByTournament(tournamentId)
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun generateSchedule(startDate: Long) {
        if (_uiState.value.isGenerating) return
        _uiState.update { it.copy(isGenerating = true, error = null) }
        viewModelScope.launch {
            runCatching {
                generateFixtures(
                    tournamentId = tournamentId,
                    startDate = startDate,
                    matchGapMs = 4 * 60 * 60 * 1000L
                )
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to generate fixtures") }
            }
            _uiState.update { it.copy(isGenerating = false) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

// Helper to group fixtures by stage in stage-order
fun List<Fixture>.groupedByStage(): Map<FixtureStage, List<Fixture>> {
    val order = FixtureStage.entries
    return groupBy { it.stage }
        .entries
        .sortedBy { order.indexOf(it.key) }
        .associate { it.key to it.value.sortedBy { f -> f.matchNumber } }
}
