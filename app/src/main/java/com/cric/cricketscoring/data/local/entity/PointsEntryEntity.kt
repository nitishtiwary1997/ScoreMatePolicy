package com.cric.cricketscoring.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "points_table",
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
data class PointsEntryEntity(
    @PrimaryKey val id: String,
    val tournamentId: String,
    val teamId: String,
    val teamName: String,
    val teamLogoUrl: String = "",
    val matchesPlayed: Int = 0,
    val won: Int = 0,
    val lost: Int = 0,
    val tied: Int = 0,
    val noResult: Int = 0,
    val abandoned: Int = 0,
    val points: Int = 0,
    val totalRunsScored: Int = 0,
    val totalOversFaced: Double = 0.0,
    val totalRunsConceded: Int = 0,
    val totalOversBowled: Double = 0.0,
    val nrr: Double = 0.0,
    val rank: Int = 0,
    val isQualified: Boolean = false,
    val isSynced: Boolean = false
)
