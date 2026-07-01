package com.cric.cricketscoring.presentation.summary

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cric.cricketscoring.domain.model.*
import com.cric.cricketscoring.domain.repository.MatchRepository
import com.cric.cricketscoring.domain.usecase.ScorecardCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchSummaryUiState(
    val match: Match? = null,
    val players: List<Player> = emptyList(),
    val innings1Score: InningsScore? = null,
    val innings2Score: InningsScore? = null,
    val result: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class MatchSummaryViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    repository: MatchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _pdfUri = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val pdfUri: SharedFlow<Uri> = _pdfUri.asSharedFlow()

    private val _isPdfGenerating = MutableStateFlow(false)
    val isPdfGenerating: StateFlow<Boolean> = _isPdfGenerating.asStateFlow()

    fun generatePdf() {
        if (_isPdfGenerating.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isPdfGenerating.value = true
            try {
                val file = ScorecardPdfGenerator.generate(context, uiState.value)
                val uri  = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                _pdfUri.emit(uri)
            } finally {
                _isPdfGenerating.value = false
            }
        }
    }

    private val matchId: String = savedStateHandle["matchId"] ?: ""

    val uiState: StateFlow<MatchSummaryUiState> = combine(
        repository.getMatchById(matchId),
        repository.getPlayersForMatch(matchId),
        repository.getBallsForInnings(matchId, 1),
        repository.getBallsForInnings(matchId, 2)
    ) { match, players, balls1, balls2 ->
        if (match == null) return@combine MatchSummaryUiState(isLoading = false)

        val innings1Score = ScorecardCalculator.calculateInnings(
            inningsNumber = 1, balls = balls1, players = players,
            battingTeamName = match.battingTeamName(1),
            bowlingTeamName = match.bowlingTeamName(1),
            onStrikeId = match.innings1OnStrikeId,
            offStrikeId = match.innings1OffStrikeId,
            currentBowlerId = match.innings1BowlerId,
            isCompleted = match.innings1Completed,
            totalOvers = match.totalOvers
        )

        val innings2Score = ScorecardCalculator.calculateInnings(
            inningsNumber = 2, balls = balls2, players = players,
            battingTeamName = match.battingTeamName(2),
            bowlingTeamName = match.bowlingTeamName(2),
            onStrikeId = match.innings2OnStrikeId,
            offStrikeId = match.innings2OffStrikeId,
            currentBowlerId = match.innings2BowlerId,
            isCompleted = match.innings2Completed,
            totalOvers = match.totalOvers,
            target = innings1Score.totalRuns + 1
        )

        val result = buildResult(match, innings1Score, innings2Score)

        MatchSummaryUiState(
            match = match,
            players = players,
            innings1Score = innings1Score,
            innings2Score = innings2Score,
            result = result,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MatchSummaryUiState())

    private fun buildResult(match: Match, i1: InningsScore, i2: InningsScore): String {
        if (match.status != MatchStatus.COMPLETED) return "Match in progress"
        return when {
            i2.totalRuns > i1.totalRuns -> {
                val wkts = 10 - i2.wickets
                "${i2.battingTeamName} won by $wkts wicket${if (wkts == 1) "" else "s"}"
            }
            i2.totalRuns < i1.totalRuns -> {
                val runs = i1.totalRuns - i2.totalRuns
                "${i1.battingTeamName} won by $runs run${if (runs == 1) "" else "s"}"
            }
            else -> "Match tied!"
        }
    }

    fun generateScorecardText(): String {
        val s = uiState.value
        val matchTitle = s.match?.let { "${it.team1Name} v/s ${it.team2Name}" } ?: "Cricket Scorecard"
        val D = "─".repeat(52)
        val H = "═".repeat(52)
        return buildString {
            appendLine(H)
            appendLine("  $matchTitle")
            appendLine(H)
            appendLine("  ${s.result}")
            appendLine(H)
            s.innings1Score?.let { appendInningsText(it, D, H) }
            s.innings2Score?.let {
                if (it.batsmen.isNotEmpty() || it.totalRuns > 0) appendInningsText(it, D, H)
            }
            appendLine()
            appendLine("  Shared via Cricket Scoring App")
            appendLine(H)
        }
    }

    private fun StringBuilder.appendInningsText(score: InningsScore, D: String, H: String) {
        val rr = "%.2f".format(score.runRate)

        // ── Innings header ──────────────────────────────────────────
        appendLine()
        appendLine("  %-28s %s (%s)".format(score.battingTeamName.uppercase(), "${score.totalRuns}-${score.wickets}", score.oversDisplay))
        appendLine(D)

        // ── Batting ─────────────────────────────────────────────────
        appendLine("  %-22s %4s %4s %4s %4s %7s".format("Batsman", "R", "B", "4s", "6s", "SR"))
        appendLine(D)
        val batsmen = score.batsmen.filter { it.balls > 0 || it.isOut }
        batsmen.forEach { b ->
            val sr = "%.2f".format(b.strikeRate)
            appendLine("  %-22s %4d %4d %4d %4d %7s".format(b.player.name.take(22), b.runs, b.balls, b.fours, b.sixes, sr))
            if (b.dismissalInfo != null) appendLine("  ${b.dismissalInfo}")
            else if (!b.isOut)           appendLine("  not out")
        }
        appendLine(D)
        appendLine("  %-28s %s (%s) %s".format("Extras", "(${score.extras})", "${score.byes} B, ${score.legByes} LB, ${score.wides} WD, ${score.noBalls} NB", ""))
        appendLine("  %-28s %s (%s) %s".format("Total", "${score.totalRuns}-${score.wickets}", score.oversDisplay, rr))
        appendLine(D)

        // ── Bowling ──────────────────────────────────────────────────
        appendLine()
        appendLine("  %-22s %5s %4s %4s %4s %7s".format("Bowler", "O", "M", "R", "W", "ER"))
        appendLine(D)
        score.bowlers.filter { it.totalLegalBalls > 0 }.forEach { b ->
            val er = "%.2f".format(b.economy)
            appendLine("  %-22s %5s %4d %4d %4d %7s".format(b.player.name.take(22), b.oversDisplay, b.maidens, b.runs, b.wickets, er))
        }
        appendLine(D)

        // ── Fall of Wickets ──────────────────────────────────────────
        if (score.fallOfWickets.isNotEmpty()) {
            appendLine()
            appendLine("  %-24s %8s %8s".format("Fall of Wickets", "Score", "Over"))
            appendLine(D)
            score.fallOfWickets.forEach { fow ->
                val scoreStr = "${fow.score}/${fow.wicketNumber}"
                appendLine("  %-24s %8s %8s".format(fow.playerName.take(24), scoreStr, fow.overDisplay))
                appendLine("    ${fow.dismissalInfo}")
            }
            appendLine(D)
        }
    }
}
