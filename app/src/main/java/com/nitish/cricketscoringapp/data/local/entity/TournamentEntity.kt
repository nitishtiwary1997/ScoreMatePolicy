package com.nitish.cricketscoringapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val organizerName: String = "",
    val organizerContact: String = "",
    val bannerUrl: String = "",
    val logoUrl: String = "",
    val venue: String = "",
    val startDate: Long,
    val endDate: Long,
    val matchFormat: String = "T20",
    val customOvers: Int = 20,
    val ballType: String = "LEATHER",
    val tournamentType: String = "LEAGUE",
    val status: String = "UPCOMING",
    val isPublic: Boolean = true,
    val entryFee: Double = 0.0,
    val prizeDetails: String = "",
    val rules: String = "",
    val maxTeams: Int = 8,
    val playersPerTeam: Int = 11,
    val createdByUserId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
