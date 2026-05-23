package com.nitish.cricketscoringapp.presentation.scoring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.data.remote.FirebaseLiveScoreDataSource
import com.nitish.cricketscoringapp.domain.model.*
import com.nitish.cricketscoringapp.domain.repository.MatchRepository
import com.nitish.cricketscoringapp.domain.repository.TournamentRepository
import com.nitish.cricketscoringapp.domain.usecase.ScorecardCalculator
import com.nitish.cricketscoringapp.domain.usecase.tournament.CalculatePointsTableUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class ScoringDialog {
    object None : ScoringDialog()
    data class SelectBatsman(
        val available: List<Player>,
        val overCompleteAfter: Boolean,
        val dismissedIsStriker: Boolean = true,
        val retiredHurtIds: Set<String> = emptySet()
    ) : ScoringDialog()
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
    private val tournamentRepository: TournamentRepository,
    private val calculatePointsTable: CalculatePointsTableUseCase,
    private val liveScoreDataSource: FirebaseLiveScoreDataSource,
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

                // Push realtime live score snapshot for tournament matches in progress
                if (match.tournamentId != null &&
                    (match.status == MatchStatus.INNINGS_1 || match.status == MatchStatus.INNINGS_2)) {
                    viewModelScope.launch(Dispatchers.IO) {
                        liveScoreDataSource.push(
                            buildLiveScoreSnapshot(match, players, innings1Score, innings2Score)
                        )
                    }
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
            // Only RETIRED_HURT is not a real wicket — RETIRED_OUT counts as dismissal
            val isRealWicket = isWicket && wicketType != WicketType.RETIRED_HURT
            val wicketsAfter = (currentScore?.wickets ?: 0) + if (isRealWicket) 1 else 0
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
                    val rhIds = retiredHurtIds(state, innings, dismissedId ?: onStrikeId)
                    ScoringDialog.SelectBatsman(available, overCompleteAfter = true, dismissedIsStriker = dismissedIsStriker, retiredHurtIds = rhIds)
                }
                isWicket -> {
                    val available = availableBatsmen(state, innings, dismissedId ?: onStrikeId, newOnStrikeId, newOffStrikeId)
                    val rhIds = retiredHurtIds(state, innings, dismissedId ?: onStrikeId)
                    ScoringDialog.SelectBatsman(available, overCompleteAfter = false, dismissedIsStriker = dismissedIsStriker, retiredHurtIds = rhIds)
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

            // Update fixture + points table when this is a tournament match
            if (match.fixtureId != null && match.tournamentId != null) {
                completeTournamentFixture(
                    match = match,
                    result = result,
                    targetReached = targetReached,
                    innings2Runs = innings2Runs,
                    innings1Total = innings1Total
                )
                // Remove live score doc — match is over
                viewModelScope.launch(Dispatchers.IO) { liveScoreDataSource.clear(matchId) }
            }
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

    // ── Tournament fixture completion ─────────────────────────────────────────

    private fun completeTournamentFixture(
        match: Match,
        result: String,
        targetReached: Boolean,
        innings2Runs: Int,
        innings1Total: Int
    ) {
        val fixtureId = match.fixtureId ?: return
        val tournamentId = match.tournamentId ?: return

        viewModelScope.launch {
            runCatching {
                val fixture = tournamentRepository.getFixtureByIdSync(fixtureId) ?: return@launch

                // Map match batting order → tournament team IDs
                // FixtureDetailViewModel always sets team1Name = fixture.team1, team2Name = fixture.team2
                val winnerId: String? = when {
                    targetReached -> {
                        // Innings 2 batting team won
                        if (match.innings1BattingTeam == 1) fixture.team2Id else fixture.team1Id
                    }
                    innings2Runs < innings1Total -> {
                        // Innings 1 batting team won
                        if (match.innings1BattingTeam == 1) fixture.team1Id else fixture.team2Id
                    }
                    else -> null // Tie — no winner
                }

                tournamentRepository.completeFixture(fixtureId, winnerId, result)
                calculatePointsTable(tournamentId)
            }
            // Silently ignore errors — match is already marked COMPLETED in the UI
        }
    }

    private fun updateBatsBowl(match: Match, innings: Int, newOnStrikeId: String, newOffStrikeId: String, newBowlerId: String) =
        if (innings == 1) match.copy(innings1OnStrikeId = newOnStrikeId, innings1OffStrikeId = newOffStrikeId, innings1BowlerId = newBowlerId)
        else match.copy(innings2OnStrikeId = newOnStrikeId, innings2OffStrikeId = newOffStrikeId, innings2BowlerId = newBowlerId)

    private fun availableBatsmen(
        state: ScoringUiState, innings: Int,
        dismissedId: String, currentOn: String, currentOff: String
    ): List<Player> {
        val battingTeamNum = state.match?.battingTeamNumber(innings) ?: return emptyList()
        val battingTeam = state.players.filter { it.team == battingTeamNum }

        // Batsmen truly dismissed (real wickets, RETIRED_OUT — cannot return)
        val reallyDismissed = state.currentBalls
            .filter { it.isWicket && it.wicketType != WicketType.RETIRED_HURT }
            .mapNotNull { it.dismissedPlayerId }
            .toSet()

        // Batsmen currently retired hurt and not yet re-dismissed — CAN return
        val currentlyRetiredHurt = state.currentBalls
            .filter { it.wicketType == WicketType.RETIRED_HURT }
            .mapNotNull { it.dismissedPlayerId }
            .filter { it !in reallyDismissed }
            .toSet()

        val hasFacedBall = state.currentBalls.map { it.batsmanId }.toSet()

        return battingTeam.filter { p ->
            when {
                p.id == currentOn || p.id == currentOff -> false  // currently at crease
                p.id == dismissedId -> false                        // just dismissed this ball
                p.id in reallyDismissed -> false                    // properly out
                p.id in currentlyRetiredHurt -> true                // retired hurt, eligible to return
                p.id !in hasFacedBall -> true                       // hasn't batted yet
                else -> false
            }
        }
    }

    /** Returns the set of player IDs currently in "Retired Hurt" state (excludes the just-dismissed batsman). */
    private fun retiredHurtIds(state: ScoringUiState, innings: Int, excludeId: String): Set<String> {
        val reallyDismissed = state.currentBalls
            .filter { it.isWicket && it.wicketType != WicketType.RETIRED_HURT }
            .mapNotNull { it.dismissedPlayerId }
            .toSet() + excludeId
        return state.currentBalls
            .filter { it.wicketType == WicketType.RETIRED_HURT }
            .mapNotNull { it.dismissedPlayerId }
            .filter { it !in reallyDismissed }
            .toSet()
    }

    private fun availableBowlers(state: ScoringUiState, innings: Int, lastBowlerId: String): List<Player> {
        val bowlingTeamNum = if ((state.match?.battingTeamNumber(innings) ?: 1) == 1) 2 else 1
        return state.players.filter { it.team == bowlingTeamNum && it.id != lastBowlerId }
    }

    private fun buildLiveScoreSnapshot(
        match: Match,
        players: List<Player>,
        inn1: InningsScore?,
        inn2: InningsScore?
    ): LiveScoreSnapshot {
        val isInn2 = match.status == MatchStatus.INNINGS_2
        val activeScore = if (isInn2) inn2 else inn1
        val striker    = activeScore?.batsmen?.firstOrNull { it.isOnStrike }
        val nonStriker = activeScore?.batsmen?.firstOrNull { !it.isOnStrike && !it.isOut }
        val bowler     = activeScore?.bowlers?.lastOrNull()

        fun fmt(f: Float) = "%.2f".format(f)
        fun fmtSR(f: Float) = "%.1f".format(f)

        return LiveScoreSnapshot(
            matchId         = matchId,
            tournamentId    = match.tournamentId ?: "",
            fixtureId       = match.fixtureId ?: "",
            team1Name       = match.team1Name,
            team2Name       = match.team2Name,
            currentInnings  = if (isInn2) 2 else 1,
            inn1BattingTeam = inn1?.battingTeamName ?: "",
            inn1Runs        = inn1?.totalRuns ?: 0,
            inn1Wickets     = inn1?.wickets ?: 0,
            inn1Overs       = inn1?.oversDisplay ?: "0.0",
            inn1IsCompleted = inn1?.isCompleted ?: false,
            inn2BattingTeam = inn2?.battingTeamName ?: "",
            inn2Runs        = inn2?.totalRuns ?: 0,
            inn2Wickets     = inn2?.wickets ?: 0,
            inn2Overs       = inn2?.oversDisplay ?: "0.0",
            target          = inn2?.target ?: 0,
            requiredRuns    = inn2?.requiredRuns ?: 0,
            requiredBalls   = inn2?.requiredBallsRemaining ?: 0,
            currentRunRate  = fmt(activeScore?.runRate ?: 0f),
            requiredRunRate = inn2?.requiredRunRate?.let { fmt(it) } ?: "-",
            strikerName     = striker?.player?.name ?: "",
            strikerRuns     = striker?.runs ?: 0,
            strikerBalls    = striker?.balls ?: 0,
            strikerFours    = striker?.fours ?: 0,
            strikerSixes    = striker?.sixes ?: 0,
            strikerSR       = fmtSR(striker?.strikeRate ?: 0f),
            nonStrikerName  = nonStriker?.player?.name ?: "",
            nonStrikerRuns  = nonStriker?.runs ?: 0,
            nonStrikerBalls = nonStriker?.balls ?: 0,
            bowlerName      = bowler?.player?.name ?: "",
            bowlerWickets   = bowler?.wickets ?: 0,
            bowlerRuns      = bowler?.runs ?: 0,
            bowlerOvers     = bowler?.oversDisplay ?: "0.0",
            bowlerEcon      = fmt(bowler?.economy ?: 0f),
            lastBallDesc    = activeScore?.lastBallDescription ?: "",
            status          = "LIVE",
            updatedAt       = System.currentTimeMillis()
        )
    }
}

