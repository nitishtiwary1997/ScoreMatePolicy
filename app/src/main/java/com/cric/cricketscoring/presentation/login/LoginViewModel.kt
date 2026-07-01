package com.cric.cricketscoring.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cric.cricketscoring.data.remote.UserSession
import com.cric.cricketscoring.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "Signing in…",
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userSession: UserSession,
    private val repository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigateToHome = Channel<Unit>(Channel.BUFFERED)
    val navigateToHome = _navigateToHome.receiveAsFlow()

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Signing in…", error = null) }
            val result = userSession.signInWithGoogle(idToken)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(loadingMessage = "Syncing your data…") }
                    repository.syncFromCloud()
                    _uiState.update { it.copy(isLoading = false) }
                    _navigateToHome.send(Unit)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Sign-in failed") }
                }
            )
        }
    }



    fun continueAsGuest() {
        viewModelScope.launch { _navigateToHome.send(Unit) }
    }

    fun setError(msg: String) = _uiState.update { it.copy(isLoading = false, error = msg) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
