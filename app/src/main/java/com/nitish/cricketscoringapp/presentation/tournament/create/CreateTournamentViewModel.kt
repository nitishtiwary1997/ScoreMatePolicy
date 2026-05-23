package com.nitish.cricketscoringapp.presentation.tournament.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.data.remote.UserSession
import com.nitish.cricketscoringapp.domain.model.BallType
import com.nitish.cricketscoringapp.domain.model.Tournament
import com.nitish.cricketscoringapp.domain.model.TournamentMatchFormat
import com.nitish.cricketscoringapp.domain.model.TournamentType
import com.nitish.cricketscoringapp.domain.usecase.tournament.CreateTournamentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTournamentState(
    val currentStep: Int = 0,

    // Step 1 — Basic Info
    val name: String = "",
    val organizerName: String = "",
    val organizerContact: String = "",
    val venue: String = "",
    val startDate: Long = defaultStartDate(),
    val endDate: Long = defaultEndDate(),
    val isPublic: Boolean = true,

    // Step 2 — Match Settings
    val tournamentType: TournamentType = TournamentType.LEAGUE,
    val matchFormat: TournamentMatchFormat = TournamentMatchFormat.T20,
    val customOvers: Int = 10,
    val ballType: BallType = BallType.LEATHER,
    val maxTeams: Int = 8,
    val playersPerTeam: Int = 11,

    // Step 3 — Details
    val description: String = "",
    val rules: String = "",
    val entryFeeText: String = "0",
    val prizeDetails: String = "",

    // UI feedback
    val nameError: String? = null,
    val dateError: String? = null,
    val isCreating: Boolean = false,
    val createdTournamentId: String? = null,
    val error: String? = null
)

private fun defaultStartDate(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun defaultEndDate(): Long = defaultStartDate() + 7L * 24 * 60 * 60 * 1000L

@HiltViewModel
class CreateTournamentViewModel @Inject constructor(
    private val createTournamentUseCase: CreateTournamentUseCase,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTournamentState())
    val state: StateFlow<CreateTournamentState> = _state.asStateFlow()

    // ── Step navigation ───────────────────────────────────────────────────────

    fun nextStep() {
        when (_state.value.currentStep) {
            0 -> if (validateStep1()) _state.update { it.copy(currentStep = 1) }
            1 -> _state.update { it.copy(currentStep = 2) }
            2 -> submitCreate()
        }
    }

    fun prevStep() {
        _state.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    // ── Step 1 field updaters ─────────────────────────────────────────────────

    fun updateName(v: String)            = _state.update { it.copy(name = v, nameError = null) }
    fun updateOrganizerName(v: String)   = _state.update { it.copy(organizerName = v) }
    fun updateOrganizerContact(v: String)= _state.update { it.copy(organizerContact = v) }
    fun updateVenue(v: String)           = _state.update { it.copy(venue = v) }
    fun updateStartDate(ms: Long)        = _state.update { it.copy(startDate = ms, dateError = null) }
    fun updateEndDate(ms: Long)          = _state.update { it.copy(endDate = ms, dateError = null) }
    fun togglePublic()                   = _state.update { it.copy(isPublic = !it.isPublic) }

    // ── Step 2 field updaters ─────────────────────────────────────────────────

    fun updateTournamentType(v: TournamentType)       = _state.update { it.copy(tournamentType = v) }
    fun updateMatchFormat(v: TournamentMatchFormat)   = _state.update { it.copy(matchFormat = v) }
    fun updateCustomOvers(v: Int)                     = _state.update { it.copy(customOvers = v.coerceIn(5, 50)) }
    fun updateBallType(v: BallType)                   = _state.update { it.copy(ballType = v) }
    fun updateMaxTeams(v: Int)                        = _state.update { it.copy(maxTeams = v) }
    fun updatePlayersPerTeam(v: Int)                  = _state.update { it.copy(playersPerTeam = v.coerceIn(5, 11)) }

    // ── Step 3 field updaters ─────────────────────────────────────────────────

    fun updateDescription(v: String)  = _state.update { it.copy(description = v) }
    fun updateRules(v: String)        = _state.update { it.copy(rules = v) }
    fun updateEntryFee(v: String)     = _state.update { it.copy(entryFeeText = v.filter { c -> c.isDigit() || c == '.' }) }
    fun updatePrizeDetails(v: String) = _state.update { it.copy(prizeDetails = v) }

    fun clearError() = _state.update { it.copy(error = null) }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validateStep1(): Boolean {
        val s = _state.value
        val nameError  = if (s.name.isBlank()) "Tournament name is required" else null
        val dateError  = if (s.endDate <= s.startDate) "End date must be after start date" else null
        _state.update { it.copy(nameError = nameError, dateError = dateError) }
        return nameError == null && dateError == null
    }

    // ── Creation ──────────────────────────────────────────────────────────────

    private fun submitCreate() {
        val s = _state.value
        _state.update { it.copy(isCreating = true, error = null) }

        viewModelScope.launch {
            runCatching {
                val tournament = Tournament(
                    name             = s.name.trim(),
                    organizerName    = s.organizerName.trim(),
                    organizerContact = s.organizerContact.trim(),
                    venue            = s.venue.trim(),
                    startDate        = s.startDate,
                    endDate          = s.endDate,
                    isPublic         = s.isPublic,
                    matchFormat      = s.matchFormat,
                    customOvers      = s.customOvers,
                    ballType         = s.ballType,
                    tournamentType   = s.tournamentType,
                    maxTeams         = s.maxTeams,
                    playersPerTeam   = s.playersPerTeam,
                    description      = s.description.trim(),
                    rules            = s.rules.trim(),
                    entryFee         = s.entryFeeText.toDoubleOrNull() ?: 0.0,
                    prizeDetails     = s.prizeDetails.trim(),
                    createdByUserId  = userSession.userId
                )
                createTournamentUseCase(tournament)
                _state.update { it.copy(isCreating = false, createdTournamentId = tournament.id) }
            }.onFailure { e ->
                _state.update { it.copy(isCreating = false, error = e.message ?: "Failed to create tournament") }
            }
        }
    }
}
