package com.cric.cricketscoring.presentation.livescore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cric.cricketscoring.data.remote.FirebaseLiveScoreDataSource
import com.cric.cricketscoring.domain.model.LiveScoreSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LiveScoreViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val liveScoreDataSource: FirebaseLiveScoreDataSource
) : ViewModel() {

    val matchId: String = checkNotNull(savedStateHandle["matchId"])

    val snapshot: StateFlow<LiveScoreSnapshot?> = liveScoreDataSource
        .observe(matchId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
