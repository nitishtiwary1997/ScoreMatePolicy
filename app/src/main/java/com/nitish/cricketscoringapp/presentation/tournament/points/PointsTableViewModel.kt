package com.nitish.cricketscoringapp.presentation.tournament.points

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.domain.model.PointsEntry
import com.nitish.cricketscoringapp.domain.model.Tournament
import com.nitish.cricketscoringapp.domain.repository.TournamentRepository
import com.nitish.cricketscoringapp.domain.usecase.tournament.CalculatePointsTableUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PointsTableViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TournamentRepository,
    private val calculatePointsTable: CalculatePointsTableUseCase
) : ViewModel() {

    val tournamentId: String = checkNotNull(savedStateHandle["tournamentId"])

    val tournament: StateFlow<Tournament?> =
        repository.getTournamentById(tournamentId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val pointsTable: StateFlow<List<PointsEntry>> =
        repository.getPointsTable(tournamentId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRecalculating = MutableStateFlow(false)
    val isRecalculating: StateFlow<Boolean> = _isRecalculating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        recalculate()
    }

    fun recalculate() {
        if (_isRecalculating.value) return
        _isRecalculating.value = true
        viewModelScope.launch {
            runCatching { calculatePointsTable(tournamentId) }
                .onFailure { e -> _error.update { e.message ?: "Calculation failed" } }
            _isRecalculating.value = false
        }
    }

    fun clearError() = _error.update { null }
}
