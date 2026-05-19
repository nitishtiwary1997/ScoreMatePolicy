package com.nitish.cricketscoringapp.presentation.toss

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.domain.model.*
import com.nitish.cricketscoringapp.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TossStep { TOSS_SETUP, SELECT_OPENERS, SELECT_BOWLER }

data class TossUiState(
    val match: Match? = null,
    val players: List<Player> = emptyList(),
    val step: TossStep = TossStep.TOSS_SETUP,
    val tossWonByTeam: Int = 1,
    val tossChoice: TossChoice = TossChoice.BAT,
    val opener1Id: String = "",
    val opener2Id: String = "",
    val bowlerId: String = "",
    val isStarting: Boolean = false,
    val startedMatchId: String? = null
) {
    val battingTeamNumber: Int
        get() = if (tossChoice == TossChoice.BAT) tossWonByTeam
                else if (tossWonByTeam == 1) 2 else 1

    val battingPlayers: List<Player>
        get() = players.filter { it.team == battingTeamNumber }

    val bowlingTeamNumber: Int
        get() = if (battingTeamNumber == 1) 2 else 1

    val bowlingPlayers: List<Player>
        get() = players.filter { it.team == bowlingTeamNumber }

    val canProceedToss: Boolean
        get() = match != null

    val canProceedOpeners: Boolean
        get() = opener1Id.isNotEmpty() && opener2Id.isNotEmpty() && opener1Id != opener2Id

    val canStartMatch: Boolean
        get() = bowlerId.isNotEmpty()
}

@HiltViewModel
class TossViewModel @Inject constructor(
    private val repository: MatchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: String = savedStateHandle["matchId"] ?: ""
    private val _uiState = MutableStateFlow(TossUiState())
    val uiState: StateFlow<TossUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getMatchById(matchId),
                repository.getPlayersForMatch(matchId)
            ) { match, players -> match to players }
                .collect { (match, players) ->
                    _uiState.update { it.copy(match = match, players = players) }
                }
        }
    }

    fun onTossWonByTeamChange(team: Int) = _uiState.update { it.copy(tossWonByTeam = team) }
    fun onTossChoiceChange(choice: TossChoice) = _uiState.update { it.copy(tossChoice = choice) }
    fun onOpener1Change(id: String) = _uiState.update { it.copy(opener1Id = id) }
    fun onOpener2Change(id: String) = _uiState.update { it.copy(opener2Id = id) }
    fun onBowlerChange(id: String) = _uiState.update { it.copy(bowlerId = id) }

    fun onTossConfirmed() {
        _uiState.update { it.copy(step = TossStep.SELECT_OPENERS) }
    }

    fun onOpenersConfirmed() {
        _uiState.update { it.copy(step = TossStep.SELECT_BOWLER) }
    }

    fun startMatch() {
        val state = _uiState.value
        val match = state.match ?: return

        val innings1BattingTeam = state.battingTeamNumber
        val updatedMatch = match.copy(
            tossWonByTeam = state.tossWonByTeam,
            tossChoice = state.tossChoice,
            status = MatchStatus.INNINGS_1,
            innings1BattingTeam = innings1BattingTeam,
            innings1OnStrikeId = state.opener1Id,
            innings1OffStrikeId = state.opener2Id,
            innings1BowlerId = state.bowlerId
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isStarting = true) }
            repository.updateMatch(updatedMatch)
            _uiState.update { it.copy(isStarting = false, startedMatchId = matchId) }
        }
    }
}
