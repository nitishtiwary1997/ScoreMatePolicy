package com.cric.cricketscoring.domain.model

import java.util.UUID

data class TournamentPlayer(
    val id: String = UUID.randomUUID().toString(),
    val tournamentId: String,
    val teamId: String,
    val name: String,
    val imageUrl: String = "",
    val jerseyNumber: Int = 0,
    val role: PlayerRole = PlayerRole.BATSMAN,
    val battingStyle: BattingStyle = BattingStyle.RIGHT_HAND,
    val bowlingStyle: BowlingStyle = BowlingStyle.NONE,
    val dateOfBirth: Long? = null,
    val contactNumber: String = ""
) {
    val roleLabel: String get() = role.label
    val canBowl: Boolean get() = role == PlayerRole.BOWLER || role == PlayerRole.ALL_ROUNDER
    val isKeeper: Boolean get() = role == PlayerRole.WICKET_KEEPER
}

enum class PlayerRole(val label: String, val shortLabel: String) {
    BATSMAN("Batsman", "BAT"),
    BOWLER("Bowler", "BOWL"),
    ALL_ROUNDER("All-Rounder", "AR"),
    WICKET_KEEPER("Wicket Keeper", "WK")
}

enum class BattingStyle(val label: String) {
    RIGHT_HAND("Right Hand"),
    LEFT_HAND("Left Hand")
}

enum class BowlingStyle(val label: String) {
    RIGHT_ARM_FAST("Right Arm Fast"),
    RIGHT_ARM_MEDIUM("Right Arm Medium"),
    RIGHT_ARM_OFF_SPIN("Right Arm Off Spin"),
    RIGHT_ARM_LEG_SPIN("Right Arm Leg Spin"),
    LEFT_ARM_FAST("Left Arm Fast"),
    LEFT_ARM_MEDIUM("Left Arm Medium"),
    LEFT_ARM_ORTHODOX("Left Arm Orthodox"),
    LEFT_ARM_CHINAMAN("Left Arm Chinaman"),
    NONE("N/A")
}
