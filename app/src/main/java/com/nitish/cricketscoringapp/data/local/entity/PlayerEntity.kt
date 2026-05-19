package com.nitish.cricketscoringapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val userId: String = "",
    val name: String,
    val matchId: String,
    val team: Int,
    val isSynced: Boolean = false
)
