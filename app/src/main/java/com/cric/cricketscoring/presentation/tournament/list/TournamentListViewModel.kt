package com.cric.cricketscoring.presentation.tournament.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cric.cricketscoring.data.remote.UserSession
import com.cric.cricketscoring.domain.model.Tournament
import com.cric.cricketscoring.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TournamentListUiState(
    val tournaments: List<Tournament> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TournamentListViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val userSession: UserSession
) : ViewModel() {

    val uiState: StateFlow<TournamentListUiState> =
        tournamentRepository.getTournamentsByUser(userSession.userId)
            .map { list -> TournamentListUiState(tournaments = list, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TournamentListUiState()
            )

    fun deleteTournament(tournamentId: String) {
        viewModelScope.launch {
            tournamentRepository.deleteTournament(tournamentId)
        }
    }
}
