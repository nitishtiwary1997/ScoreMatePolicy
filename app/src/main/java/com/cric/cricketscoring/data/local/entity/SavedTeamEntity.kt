package com.cric.cricketscoring.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_teams")
data class SavedTeamEntity(
    @PrimaryKey val name: String,
    val playerNames: String   // newline-separated player names
)
