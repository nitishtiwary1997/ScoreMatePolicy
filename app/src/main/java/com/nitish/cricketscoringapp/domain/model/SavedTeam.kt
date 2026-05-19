package com.nitish.cricketscoringapp.domain.model

data class SavedTeam(
    val name: String,           // also the unique key
    val playerNames: List<String>
)
