package com.nitish.cricketscoringapp.domain.model

import java.util.UUID

data class Match(
    val id: String = UUID.randomUUID().toString(),
    val team1Name: String,
    val team2Name: String,
    val totalOvers: Int,
    val playersPerTeam: Int = 11,
    val tossWonByTeam: Int = 0,
    val tossChoice: TossChoice = TossChoice.BAT,
    val status: MatchStatus = MatchStatus.TOSS,
    val innings1BattingTeam: Int = 0,
    val innings1OnStrikeId: String = "",
    val innings1OffStrikeId: String = "",
    val innings1BowlerId: String = "",
    val innings1Completed: Boolean = false,
    val innings2OnStrikeId: String = "",
    val innings2OffStrikeId: String = "",
    val innings2BowlerId: String = "",
    val innings2Completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val innings2BattingTeam: Int get() = if (innings1BattingTeam == 1) 2 else 1

    fun currentInnings(): Int = if (status == MatchStatus.INNINGS_2) 2 else 1

    fun currentOnStrikeId(): String = if (currentInnings() == 1) innings1OnStrikeId else innings2OnStrikeId
    fun currentOffStrikeId(): String = if (currentInnings() == 1) innings1OffStrikeId else innings2OffStrikeId
    fun currentBowlerId(): String = if (currentInnings() == 1) innings1BowlerId else innings2BowlerId
    fun battingTeamNumber(innings: Int): Int = if (innings == 1) innings1BattingTeam else innings2BattingTeam

    fun teamName(teamNumber: Int): String = if (teamNumber == 1) team1Name else team2Name
    fun battingTeamName(innings: Int): String = teamName(battingTeamNumber(innings))
    fun bowlingTeamName(innings: Int): String = teamName(if (battingTeamNumber(innings) == 1) 2 else 1)
}

enum class MatchStatus { TOSS, INNINGS_1, INNINGS_2, COMPLETED }
enum class TossChoice { BAT, BOWL }
