package com.cric.cricketscoring.presentation.tournament.bracket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cric.cricketscoring.domain.model.Fixture
import com.cric.cricketscoring.domain.model.FixtureStage
import com.cric.cricketscoring.domain.model.FixtureStatus
import com.cric.cricketscoring.domain.model.KnockoutBracket
import com.cric.cricketscoring.domain.model.Tournament
import com.cric.cricketscoring.domain.model.TournamentTeam
import com.cric.cricketscoring.domain.model.TournamentType
import com.cric.cricketscoring.domain.repository.TournamentRepository
import com.cric.cricketscoring.domain.usecase.tournament.ResolveQualificationUseCase
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

data class KnockoutBracketUiState(
    val tournament: Tournament? = null,
    val bracket: KnockoutBracket? = null,
    val teamsMap: Map<String, TournamentTeam> = emptyMap(),
    val hasKnockoutFixtures: Boolean = false,
    val groupComplete: Boolean = false,
    val qualifiedCount: Int = 0,
    val canResolve: Boolean = false,
    val isResolving: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class KnockoutBracketViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TournamentRepository,
    private val resolveQualification: ResolveQualificationUseCase
) : ViewModel() {

    val tournamentId: String = checkNotNull(savedStateHandle["tournamentId"])

    private val _extra = MutableStateFlow(Pair(false, null as String?)) // isResolving, error
    val extra: StateFlow<Pair<Boolean, String?>> = _extra.asStateFlow()

    val uiState: StateFlow<KnockoutBracketUiState> = combine(
        repository.getTournamentById(tournamentId),
        repository.getFixturesByTournament(tournamentId),
        repository.getTeamsByTournament(tournamentId),
        repository.getPointsTable(tournamentId)
    ) { tournament, fixtures, teams, points ->
        val teamsMap = teams.associateBy { it.id }
        val knockoutFixtures = fixtures.filter { it.stage != FixtureStage.GROUP }
        val groupFixtures = fixtures.filter { it.stage == FixtureStage.GROUP }

        val finalFixture = fixtures.firstOrNull { it.stage == FixtureStage.FINAL }
        val champion = finalFixture?.winnerId?.let { teamsMap[it] }

        val groupComplete = groupFixtures.isNotEmpty() &&
            groupFixtures.all { it.status != FixtureStatus.UPCOMING && it.status != FixtureStatus.LIVE }

        val isLeaguePlusKnockout = tournament?.tournamentType == TournamentType.LEAGUE_PLUS_KNOCKOUT
        val canResolve = isLeaguePlusKnockout && groupComplete && knockoutFixtures.isEmpty()

        KnockoutBracketUiState(
            tournament = tournament,
            bracket = KnockoutBracket(
                tournamentId = tournamentId,
                quarterFinals = fixtures.filter { it.stage == FixtureStage.QUARTER_FINAL },
                semiFinals = fixtures.filter { it.stage == FixtureStage.SEMI_FINAL },
                thirdPlacePlayoff = fixtures.firstOrNull { it.stage == FixtureStage.THIRD_PLACE_PLAYOFF },
                final = finalFixture,
                champion = champion
            ),
            teamsMap = teamsMap,
            hasKnockoutFixtures = knockoutFixtures.isNotEmpty(),
            groupComplete = groupComplete,
            qualifiedCount = points.count { it.isQualified },
            canResolve = canResolve,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KnockoutBracketUiState()
    )

    fun resolveKnockout(startDate: Long) {
        if (_extra.value.first) return
        _extra.update { true to null }
        viewModelScope.launch {
            runCatching {
                resolveQualification(tournamentId, startDate)
            }.onFailure { e ->
                _extra.update { false to (e.message ?: "Failed to generate knockout") }
            }
            _extra.update { it.copy(first = false) }
        }
    }

    fun clearError() = _extra.update { it.copy(second = null) }
}
