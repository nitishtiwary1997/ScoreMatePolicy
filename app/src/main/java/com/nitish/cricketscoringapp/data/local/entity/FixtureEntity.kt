package com.nitish.cricketscoringapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fixtures",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tournamentId"), Index("matchId")]
)
data class FixtureEntity(
    @PrimaryKey val id: String,
    val tournamentId: String,
    val matchId: String? = null,
    val team1Id: String,
    val team2Id: String,
    val stage: String = "GROUP",
    val groupName: String = "A",
    val matchNumber: Int = 1,
    val scheduledAt: Long,
    val venue: String = "",
    val status: String = "UPCOMING",
    val winnerId: String? = null,
    val resultSummary: String = "",
    val isSynced: Boolean = false
)
