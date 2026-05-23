package com.nitish.cricketscoringapp.presentation.livescore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.data.remote.FirebaseLiveScoreDataSource
import com.nitish.cricketscoringapp.domain.model.LiveScoreSnapshot
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
