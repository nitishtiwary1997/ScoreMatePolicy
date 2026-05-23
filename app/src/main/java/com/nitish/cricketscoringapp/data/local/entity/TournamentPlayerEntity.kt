package com.nitish.cricketscoringapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tournament_players",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TournamentTeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tournamentId"), Index("teamId")]
)
data class TournamentPlayerEntity(
    @PrimaryKey val id: String,
    val tournamentId: String,
    val teamId: String,
    val name: String,
    val imageUrl: String = "",
    val jerseyNumber: Int = 0,
    val role: String = "BATSMAN",
    val battingStyle: String = "RIGHT_HAND",
    val bowlingStyle: String = "NONE",
    val dateOfBirth: Long? = null,
    val contactNumber: String = "",
    val isSynced: Boolean = false
)
