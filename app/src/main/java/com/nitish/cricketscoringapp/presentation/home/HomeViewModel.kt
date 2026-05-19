package com.nitish.cricketscoringapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.data.remote.UserSession
import com.nitish.cricketscoringapp.domain.model.Match
import com.nitish.cricketscoringapp.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val matches: List<Match> = emptyList(),
    val userName: String = "",
    val userEmail: String = "",
    val isGuest: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MatchRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            userName = userSession.userName,
            userEmail = userSession.userEmail,
            isGuest = !userSession.isSignedIn
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllMatches().collect { matches ->
                _uiState.update { it.copy(matches = matches) }
            }
        }
    }

    fun signOut() = userSession.signOut()
}
