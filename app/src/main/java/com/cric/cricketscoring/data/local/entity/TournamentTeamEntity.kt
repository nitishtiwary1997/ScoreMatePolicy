package com.cric.cricketscoring.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tournament_teams",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tournamentId")]
)
data class TournamentTeamEntity(
    @PrimaryKey val id: String,
    val tournamentId: String,
    val name: String,
    val shortName: String = "",
    val logoUrl: String = "",
    val jerseyColorPrimary: String = "#1A237E",
    val jerseyColorSecondary: String = "#FFFFFF",
    val captainPlayerId: String = "",
    val viceCaptainPlayerId: String = "",
    val homeGround: String = "",
    val registeredAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
