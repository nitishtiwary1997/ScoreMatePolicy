package com.cric.cricketscoring.presentation.tournament.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cric.cricketscoring.domain.model.Fixture
import com.cric.cricketscoring.domain.model.PointsEntry
import com.cric.cricketscoring.domain.model.Tournament
import com.cric.cricketscoring.domain.model.TournamentStatus
import com.cric.cricketscoring.domain.model.TournamentTeam
import com.cric.cricketscoring.domain.model.TournamentType
import com.cric.cricketscoring.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TournamentDashboardUiState(
    val tournament: Tournament? = null,
    val teamCount: Int = 0,
    val maxTeams: Int = 8,
    val totalFixtures: Int = 0,
    val completedFixtures: Int = 0,
    val upcomingFixtures: Int = 0,
    val liveFixtures: List<Fixture> = emptyList(),
    val topPointsEntries: List<PointsEntry> = emptyList(),
    val teamsMap: Map<String, TournamentTeam> = emptyMap(),
    val showPointsTable: Boolean = false,
    val showKnockoutBracket: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TournamentDashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TournamentRepository
) : ViewModel() {

    val tournamentId: String = checkNotNull(savedStateHandle["tournamentId"])

    val uiState: StateFlow<TournamentDashboardUiState> = combine(
        repository.getTournamentById(tournamentId),
        repository.getTeamsByTournament(tournamentId),
        repository.getFixturesByTournament(tournamentId),
        repository.getPointsTable(tournamentId)
    ) { tournament, teams, fixtures, points ->
        val t = tournament
        TournamentDashboardUiState(
            tournament       = t,
            teamCount        = teams.size,
            maxTeams         = t?.maxTeams ?: 8,
            totalFixtures    = fixtures.size,
            completedFixtures = fixtures.count { it.isCompleted },
            upcomingFixtures  = fixtures.count { it.status.name == "UPCOMING" },
            liveFixtures      = fixtures.filter { it.isLive },
            topPointsEntries  = points.sortedByDescending { it.points }.take(3),
            teamsMap          = teams.associateBy { it.id },
            showPointsTable   = t != null && t.tournamentType != TournamentType.KNOCKOUT,
            showKnockoutBracket = t != null && t.tournamentType != TournamentType.LEAGUE,
            isLoading         = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TournamentDashboardUiState()
    )

    private val _statusUpdating = MutableStateFlow(false)
    val statusUpdating: StateFlow<Boolean> = _statusUpdating.asStateFlow()

    fun startTournament() = updateStatus(TournamentStatus.ONGOING)
    fun completeTournament() = updateStatus(TournamentStatus.COMPLETED)
    fun cancelTournament() = updateStatus(TournamentStatus.CANCELLED)

    private fun updateStatus(status: TournamentStatus) {
        if (_statusUpdating.value) return
        _statusUpdating.value = true
        viewModelScope.launch {
            runCatching { repository.updateTournamentStatus(tournamentId, status) }
                .onFailure { e ->
                    uiState.value.let { /* error exposed via uiState.error if needed */ }
                }
            _statusUpdating.value = false
        }
    }

    fun deleteTournament(onDeleted: () -> Unit) {
        viewModelScope.launch {
            runCatching { repository.deleteTournament(tournamentId) }
                .onSuccess { onDeleted() }
        }
    }
}
