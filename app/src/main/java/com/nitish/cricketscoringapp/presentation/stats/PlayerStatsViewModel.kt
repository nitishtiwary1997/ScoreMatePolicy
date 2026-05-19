package com.nitish.cricketscoringapp.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.domain.model.PlayerCareerStats
import com.nitish.cricketscoringapp.domain.repository.MatchRepository
import com.nitish.cricketscoringapp.domain.usecase.PlayerStatsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Overall aggregated stats across every match ───────────────────────────────
data class OverallStats(
    val totalMatches: Int,
    val totalRuns: Int,
    val totalWickets: Int,
    val highScore: Int,
    val highScorePlayer: String,
    val highScoreNotOut: Boolean,
    val totalBalls: Int,
    val totalBallsBowled: Int,
    val totalRunsConceded: Int,
    val totalFours: Int,
    val totalSixes: Int,
    val topScorerName: String,
    val topScorerRuns: Int,
    val topWicketName: String,
    val topWickets: Int
) {
    val strikeRate: Double get() = if (totalBalls == 0) 0.0 else totalRuns * 100.0 / totalBalls
    val economy: Double get() = if (totalBallsBowled == 0) 0.0 else totalRunsConceded * 6.0 / totalBallsBowled
    val highScoreDisplay: String get() = if (highScoreNotOut) "$highScore*" else "$highScore"
}

enum class StatsTab { BATTING, BOWLING, ALL_ROUND }
enum class BattingSort(val label: String) {
    RUNS("Runs"), AVERAGE("Average"), STRIKE_RATE("SR"),
    HIGH_SCORE("HS"), SIXES("6s"), FOURS("4s"), MATCHES("Matches")
}
enum class BowlingSort(val label: String) {
    WICKETS("Wickets"), ECONOMY("Economy"), AVERAGE("Average"),
    BEST("Best"), MAIDENS("Maidens"), MATCHES("Matches")
}

data class PlayerStatsUiState(
    val allStats: List<PlayerCareerStats> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedTab: StatsTab = StatsTab.BATTING,
    val battingSort: BattingSort = BattingSort.RUNS,
    val bowlingSort: BowlingSort = BowlingSort.WICKETS,
    val displayCount: Int = 10
) {
    val overallStats: OverallStats?
        get() {
            if (allStats.isEmpty()) return null
            val matchIds = allStats.flatMap { it.matchIds }.toSet()
            val hsPlayer = allStats.maxByOrNull { it.highScore }
            val topScorer = allStats.maxByOrNull { it.totalRuns }
            val topWicket = allStats.maxByOrNull { it.wickets }
            return OverallStats(
                totalMatches      = matchIds.size,
                totalRuns         = allStats.sumOf { it.totalRuns },
                totalWickets      = allStats.sumOf { it.wickets },
                highScore         = hsPlayer?.highScore ?: 0,
                highScorePlayer   = hsPlayer?.player?.name ?: "",
                highScoreNotOut   = hsPlayer?.highScoreNotOut ?: false,
                totalBalls        = allStats.sumOf { it.totalBalls },
                totalBallsBowled  = allStats.sumOf { it.ballsBowled },
                totalRunsConceded = allStats.sumOf { it.runsConceded },
                totalFours        = allStats.sumOf { it.fours },
                totalSixes        = allStats.sumOf { it.sixes },
                topScorerName     = topScorer?.player?.name ?: "",
                topScorerRuns     = topScorer?.totalRuns ?: 0,
                topWicketName     = topWicket?.player?.name ?: "",
                topWickets        = topWicket?.wickets ?: 0
            )
        }

    private fun buildList(): List<PlayerCareerStats> {
        var list = allStats
        if (searchQuery.isNotBlank()) {
            list = list.filter { it.player.name.contains(searchQuery, ignoreCase = true) }
        }
        list = when (selectedTab) {
            StatsTab.BATTING   -> list.filter { it.isBatter }
            StatsTab.BOWLING   -> list.filter { it.isBowler }
            StatsTab.ALL_ROUND -> list.filter { it.isAllRounder }
        }
        list = when (selectedTab) {
            StatsTab.BATTING -> when (battingSort) {
                BattingSort.RUNS         -> list.sortedByDescending { it.totalRuns }
                BattingSort.AVERAGE      -> list.sortedByDescending { it.battingAverage }
                BattingSort.STRIKE_RATE  -> list.sortedByDescending { it.strikeRate }
                BattingSort.HIGH_SCORE   -> list.sortedByDescending { it.highScore }
                BattingSort.SIXES        -> list.sortedByDescending { it.sixes }
                BattingSort.FOURS        -> list.sortedByDescending { it.fours }
                BattingSort.MATCHES      -> list.sortedByDescending { it.matchesPlayed }
            }
            StatsTab.BOWLING, StatsTab.ALL_ROUND -> when (bowlingSort) {
                BowlingSort.WICKETS  -> list.sortedByDescending { it.wickets }
                BowlingSort.ECONOMY  -> list.filter { it.ballsBowled > 0 }.sortedBy { it.economy }
                BowlingSort.AVERAGE  -> list.filter { it.wickets > 0 }.sortedBy { it.bowlingAverage }
                BowlingSort.BEST     -> list.sortedWith(
                    compareByDescending<PlayerCareerStats> { it.bestBowlingWickets }
                        .thenBy { it.bestBowlingRuns }
                )
                BowlingSort.MAIDENS  -> list.sortedByDescending { it.maidens }
                BowlingSort.MATCHES  -> list.sortedByDescending { it.matchesPlayed }
            }
        }
        return list
    }

    val displayList: List<PlayerCareerStats> get() = buildList().take(displayCount)
    val hasMore: Boolean get() = buildList().size > displayCount
}

@HiltViewModel
class PlayerStatsViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerStatsUiState())
    val uiState: StateFlow<PlayerStatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllMatches(),
                repository.getAllPlayers(),
                repository.getAllBalls()
            ) { matches, players, balls ->
                PlayerStatsCalculator.calculate(matches, players, balls)
            }.collect { stats ->
                _uiState.update { it.copy(allStats = stats, isLoading = false) }
            }
        }
    }

    fun loadMore() = _uiState.update { it.copy(displayCount = it.displayCount + 10) }
    fun setSearch(query: String) = _uiState.update { it.copy(searchQuery = query, displayCount = 10) }
    fun setTab(tab: StatsTab) = _uiState.update { it.copy(selectedTab = tab, displayCount = 10) }
    fun setBattingSort(sort: BattingSort) = _uiState.update { it.copy(battingSort = sort, displayCount = 10) }
    fun setBowlingSort(sort: BowlingSort) = _uiState.update { it.copy(bowlingSort = sort, displayCount = 10) }
}
