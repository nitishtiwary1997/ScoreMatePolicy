package com.cric.cricketscoring.domain.model

data class Player(
    val id: String,
    val name: String,
    val matchId: String,
    val team: Int
)
