package com.nitish.cricketscoringapp.presentation.scoring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.domain.model.*
import com.nitish.cricketscoringapp.domain.repository.MatchRepository
import com.nitish.cricketscoringapp.domain.usecase.ScorecardCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers

sealed class ScoringDialog {
    object None : ScoringDialog()
    data class SelectBatsman(val available: List<Player>, val overCompleteAfter: Boolean, val dismissedIsStriker: Boolean = true) : ScoringDialog()
    data class SelectBowler(val available: List<Player>) : ScoringDialog()
    object InningsComplete : ScoringDialog()
    data class MatchComplete(val result: String) : ScoringDialog()
    object AddPlayer : ScoringDialog()
}

data class ScoringUiState(
    val match: Match? = null,
    val players: List<Player> = emptyList(),
    val innings1Score: InningsScore? = null,
    val innings2Score: InningsScore? = null,
    val innings1Balls: List<Ball> = emptyList(),
    val innings2Balls: List<Ball> = emptyList(),
    val isLoading: Boolean = true,
    val dialog: ScoringDialog = ScoringDialog.None
) {
    val currentInnings: Int get() = match?.currentInnings() ?: 1
    val currentScore: InningsScore? get() = if (currentInnings == 1) innings1Score else innings2Score
    val currentBalls: List<Ball> get() = if (currentInnings == 1) innings1Balls else innings2Balls
}

@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val repository: MatchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: String = savedStateHandle["matchId"] ?: ""
    private val _uiState = MutableStateFlow(ScoringUiState())
    val uiState: StateFlow<ScoringUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getMatchById(matchId),
                repository.getPlayersForMatch(matchId),
                repository.getBallsForInnings(matchId, 1),
                repository.getBallsForInnings(matchId, 2)
            ) { match, players, balls1, balls2 ->
                match to Triple(players, balls1, balls2)
            }.collect { (match, rest) ->
                val (players, balls1, balls2) = rest
                if (match == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }
                val innings1Score = ScorecardCalculator.calculateInnings(
                    inningsNumber = 1,
                    balls = balls1,
                    players = players,
                    battingTeamName = match.battingTeamName(1),
                    bowlingTeamName = match.bowlingTeamName(1),
                    onStrikeId = match.innings1OnStrikeId,
                    offStrikeId = match.innings1OffStrikeId,
                    currentBowlerId = match.innings1BowlerId,
                    isCompleted = match.innings1Completed,
                    totalOvers = match.totalOvers
                )
                val innings2Score = ScorecardCalculator.calculateInnings(
                    inningsNumber = 2,
                    balls = balls2,
                    players = players,
                    battingTeamName = match.battingTeamName(2),
                    bowlingTeamName = match.bowlingTeamName(2),
                    onStrikeId = match.innings2OnStrikeId,
                    offStrikeId = match.innings2OffStrikeId,
                    currentBowlerId = match.innings2BowlerId,
                    isCompleted = match.innings2Completed,
                    totalOvers = match.totalOvers,
                    target = innings1Score.totalRuns + 1
                )
                val currentDialog = _uiState.value.dialog
                _uiState.update {
                    it.copy(
                        match = match,
                        players = players,
                        innings1Score = innings1Score,
                        innings2Score = innings2Score,
                        innings1Balls = balls1,
                        innings2Balls = balls2,
                        isLoading = false,
                        dialog = currentDialog
                    )
                }
            }
        }
    }

    fun recordBall(
        runs: Int,
        extraType: ExtraType? = null,
        isWicket: Boolean = false,
        wicketType: WicketType? = null,
        dismissedPlayerId: String? = null,
        fielderIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            val match = state.match ?: return@launch
            if (state.dialog != ScoringDialog.None) return@launch

            val innings = match.currentInnings()
            val currentBalls = state.currentBalls
            val legalBallCount = currentBalls.count { it.isLegalDelivery }

            val onStrikeId = match.currentOnStrikeId()
            val offStrikeId = match.currentOffStrikeId()
            val bowlerId = match.currentBowlerId()

            val isLegal = extraType != ExtraType.WIDE && extraType != ExtraType.NO_BALL
            val overNumber = legalBallCount / 6
            val ballInOver = if (isLegal) (legalBallCount % 6) + 1 else 0

            val (batsmanRuns, extras) = when (extraType) {
                ExtraType.WIDE -> 0 to (1 + runs)
                ExtraType.NO_BALL -> runs to 1
                ExtraType.BYE, ExtraType.LEG_BYE -> 0 to runs
                null -> runs to 0
            }

            val dismissedIsStriker = dismissedPlayerId?.equals(onStrikeId) ?: true
            val dismissedId = dismissedPlayerId ?: if (isWicket) onStrikeId else null

            val ball = Ball(
                id = UUID.randomUUID().toString(),
                matchId = matchId,
                innings = innings,
                overNumber = overNumber,
                ballInOver = ballInOver,
                batsmanId = onStrikeId,
                bowlerId = bowlerId,
                runs = batsmanRuns,
                extras = extras,
                extraType = extraType,
                isWicket = isWicket,
                wicketType = wicketType,
                dismissedPlayerId = dismissedId,
                fielderIds = fielderIds
            )

            repository.recordBall(ball)

            val newLegalBallCount = legalBallCount + if (isLegal) 1 else 0
            val overComplete = isLegal && newLegalBallCount % 6 == 0
            val currentScore = state.currentScore
            val wicketsAfter = (currentScore?.wickets ?: 0) + if (isWicket) 1 else 0
            val battingTeamNum = match.battingTeamNumber(innings)
            val battingTeamSize = state.players.count { it.team == battingTeamNum }
            val allOut = wicketsAfter >= battingTeamSize - 1
            val oversComplete = newLegalBallCount / 6 >= match.totalOvers

            // Target reached check for innings 2
            val innings1Total = state.innings1Score?.totalRuns ?: 0
            val innings2CurrentRuns = (state.innings2Score?.totalRuns ?: 0) + ball.totalRuns
            val targetReached = innings == 2 && innings2CurrentRuns > innings1Total

            val inningsComplete = allOut || oversComplete || targetReached

            if (inningsComplete) {
                handleInningsComplete(match, innings, allOut, targetReached, state, wicketsAfter, innings2CurrentRuns, innings1Total, battingTeamSize)
                return@launch
            }

            // Strike rotation
            var newOnStrikeId = onStrikeId
            var newOffStrikeId = offStrikeId

            val totalBallRuns = batsmanRuns + extras
            val shouldRotate = extraType != ExtraType.WIDE && totalBallRuns % 2 == 1

            if (shouldRotate && !isWicket) {
                newOnStrikeId = offStrikeId
                newOffStrikeId = onStrikeId
            }
            if (overComplete) {
                // End of over: non-striker becomes striker
                val tmp = newOnStrikeId
                newOnStrikeId = newOffStrikeId
                newOffStrikeId = tmp
            }

            val updatedMatch = updateBatsBowl(match, innings, newOnStrikeId, newOffStrikeId, bowlerId)
            repository.updateMatch(updatedMatch)

            val dialog: ScoringDialog = when {
                isWicket && overComplete -> {
                    val available = availableBatsmen(state, innings, dismissedId ?: onStrikeId, newOnStrikeId, newOffStrikeId)
                    ScoringDialog.SelectBatsman(available, overCompleteAfter = true, dismissedIsStriker = dismissedIsStriker)
                }
                isWicket -> {
                    val available = availableBatsmen(state, innings, dismissedId ?: onStrikeId, newOnStrikeId, newOffStrikeId)
                    ScoringDialog.SelectBatsman(available, overCompleteAfter = false, dismissedIsStriker = dismissedIsStriker)
                }
                overComplete -> {
                    val available = availableBowlers(state, innings, bowlerId)
                    ScoringDialog.SelectBowler(available)
                }
                else -> ScoringDialog.None
            }
            _uiState.update { it.copy(dialog = dialog) }
        }
    }

    private suspend fun handleInningsComplete(
        match: Match,
        innings: Int,
        allOut: Boolean,
        targetReached: Boolean,
        state: ScoringUiState,
        wicketsAfter: Int,
        innings2Runs: Int,
        innings1Total: Int,
        battingTeamSize: Int
    ) {
        if (innings == 1) {
            val updatedMatch = match.copy(status = MatchStatus.INNINGS_2, innings1Completed = true)
            repository.updateMatch(updatedMatch)
            _uiState.update { it.copy(dialog = ScoringDialog.InningsComplete) }
        } else {
            val updatedMatch = match.copy(status = MatchStatus.COMPLETED, innings2Completed = true)
            repository.updateMatch(updatedMatch)
            val innings2BattingTeamName = match.battingTeamName(2)
            val innings1BattingTeamName = match.battingTeamName(1)
            val result = when {
                targetReached -> {
                    val wicketsInHand = battingTeamSize - 1 - wicketsAfter
                    "$innings2BattingTeamName won by $wicketsInHand wicket${if (wicketsInHand == 1) "" else "s"}"
                }
                innings2Runs < innings1Total -> {
                    val margin = innings1Total - innings2Runs
                    "$innings1BattingTeamName won by $margin run${if (margin == 1) "" else "s"}"
                }
                else -> "Match tied!"
            }
            _uiState.update { it.copy(dialog = ScoringDialog.MatchComplete(result)) }
        }
    }

    fun onSelectBatsman(playerId: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val match = state.match ?: return@launch
            val innings = match.currentInnings()
            val dialog = state.dialog as? ScoringDialog.SelectBatsman ?: return@launch

            val currentOnStrike = if (innings == 1) match.innings1OnStrikeId else match.innings2OnStrikeId
            val currentOffStrike = if (innings == 1) match.innings1OffStrikeId else match.innings2OffStrikeId
            val (newOnId, newOffId) = if (dialog.dismissedIsStriker) {
                playerId to currentOffStrike
            } else {
                currentOnStrike to playerId
            }
            val updatedMatch = updateBatsBowl(match, innings,
                newOnStrikeId = newOnId,
                newOffStrikeId = newOffId,
                newBowlerId = match.currentBowlerId()
            )
            repository.updateMatch(updatedMatch)

            if (dialog.overCompleteAfter) {
                val available = availableBowlers(state, innings, match.currentBowlerId())
                _uiState.update { it.copy(dialog = ScoringDialog.SelectBowler(available)) }
            } else {
                _uiState.update { it.copy(dialog = ScoringDialog.None) }
            }
        }
    }

    fun onSelectBowler(playerId: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val match = state.match ?: return@launch
            val innings = match.currentInnings()

            val updatedMatch = if (innings == 1) {
                match.copy(innings1BowlerId = playerId)
            } else {
                match.copy(innings2BowlerId = playerId)
            }
            repository.updateMatch(updatedMatch)
            _uiState.update { it.copy(dialog = ScoringDialog.None) }
        }
    }

    fun onStartInnings2(opener1Id: String, opener2Id: String, bowlerId: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val match = state.match ?: return@launch
            val updatedMatch = match.copy(
                innings2OnStrikeId = opener1Id,
                innings2OffStrikeId = opener2Id,
                innings2BowlerId = bowlerId
            )
            repository.updateMatch(updatedMatch)
            _uiState.update { it.copy(dialog = ScoringDialog.None) }
        }
    }

    fun showAddPlayerDialog() {
        if (_uiState.value.dialog == ScoringDialog.None)
            _uiState.update { it.copy(dialog = ScoringDialog.AddPlayer) }
    }

    fun dismissAddPlayerDialog() {
        if (_uiState.value.dialog == ScoringDialog.AddPlayer)
            _uiState.update { it.copy(dialog = ScoringDialog.None) }
    }

    fun addPlayer(name: String, team: Int) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val player = Player(
                id      = UUID.randomUUID().toString(),
                name    = trimmed,
                matchId = matchId,
                team    = team
            )
            repository.addPlayer(player)
            _uiState.update { it.copy(dialog = ScoringDialog.None) }
        }
    }

    fun undoLastBall() {
        viewModelScope.launch {
            val state = _uiState.value
            val match = state.match ?: return@launch
            if (state.dialog != ScoringDialog.None) {
                _uiState.update { it.copy(dialog = ScoringDialog.None) }
                return@launch
            }
            repository.undoLastBall(matchId, match.currentInnings())
        }
    }

    private fun updateBatsBowl(match: Match, innings: Int, newOnStrikeId: String, newOffStrikeId: String, newBowlerId: String) =
        if (innings == 1) match.copy(innings1OnStrikeId = newOnStrikeId, innings1OffStrikeId = newOffStrikeId, innings1BowlerId = newBowlerId)
        else match.copy(innings2OnStrikeId = newOnStrikeId, innings2OffStrikeId = newOffStrikeId, innings2BowlerId = newBowlerId)

    private fun availableBatsmen(state: ScoringUiState, innings: Int, dismissedId: String, currentOn: String, currentOff: String): List<Player> {
        val battingTeamNum = state.match?.battingTeamNumber(innings) ?: return emptyList()
        val battingTeam = state.players.filter { it.team == battingTeamNum }
        val alreadyBatted = state.currentBalls.map { it.batsmanId }.toSet() +
                state.currentBalls.mapNotNull { it.dismissedPlayerId }.toSet()
        return battingTeam.filter { it.id !in alreadyBatted || it.id == dismissedId }
            .filter { it.id != currentOn && it.id != currentOff && it.id != dismissedId }
    }

    private fun availableBowlers(state: ScoringUiState, innings: Int, lastBowlerId: String): List<Player> {
        val bowlingTeamNum = if ((state.match?.battingTeamNumber(innings) ?: 1) == 1) 2 else 1
        return state.players.filter { it.team == bowlingTeamNum && it.id != lastBowlerId }
    }
}

