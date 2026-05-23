package com.nitish.cricketscoringapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val userId: String = "",
    val team1Name: String,
    val team2Name: String,
    val totalOvers: Int,
    val playersPerTeam: Int = 11,
    val tossWonByTeam: Int = 0,
    val tossChoice: String = "BAT",
    val status: String = "TOSS",
    val innings1BattingTeam: Int = 0,
    val innings1OnStrikeId: String = "",
    val innings1OffStrikeId: String = "",
    val innings1BowlerId: String = "",
    val innings1Completed: Boolean = false,
    val innings2OnStrikeId: String = "",
    val innings2OffStrikeId: String = "",
    val innings2BowlerId: String = "",
    val innings2Completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val tournamentId: String? = null,
    val fixtureId: String? = null
)
