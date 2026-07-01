package com.cric.cricketscoring.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balls")
data class BallEntity(
    @PrimaryKey val id: String,
    val userId: String = "",
    val matchId: String,
    val innings: Int,
    val overNumber: Int,
    val ballInOver: Int,
    val batsmanId: String,
    val bowlerId: String,
    val runs: Int = 0,
    val extras: Int = 0,
    val extraType: String? = null,
    val isWicket: Boolean = false,
    val wicketType: String? = null,
    val dismissedPlayerId: String? = null,
    val fielderIds: String = "",
    val isSynced: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
