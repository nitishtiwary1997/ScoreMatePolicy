package com.nitish.cricketscoringapp.presentation.tournament.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.domain.model.TournamentStats
import com.nitish.cricketscoringapp.domain.usecase.tournament.TournamentStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TournamentStatsUiState(
    val stats: TournamentStats? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TournamentStatsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTournamentStats: TournamentStatsUseCase
) : ViewModel() {

    val tournamentId: String = checkNotNull(savedStateHandle["tournamentId"])

    private val _uiState = MutableStateFlow(TournamentStatsUiState())
    val uiState: StateFlow<TournamentStatsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = TournamentStatsUiState(isLoading = true)
            runCatching { getTournamentStats(tournamentId) }
                .onSuccess { stats -> _uiState.value = TournamentStatsUiState(stats = stats, isLoading = false) }
                .onFailure { e -> _uiState.value = TournamentStatsUiState(isLoading = false, error = e.message ?: "Failed to load stats") }
        }
    }
}
