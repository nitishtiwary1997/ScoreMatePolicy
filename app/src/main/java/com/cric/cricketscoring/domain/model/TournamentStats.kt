package com.cric.cricketscoring.domain.model

data class TournamentStats(
    val tournamentId: String,
    val orangeCap: PlayerStatLine? = null,
    val purpleCap: PlayerStatLine? = null,
    val mostRuns: List<PlayerStatLine> = emptyList(),
    val mostWickets: List<PlayerStatLine> = emptyList(),
    val highestScore: List<PlayerStatLine> = emptyList(),
    val bestBowling: List<PlayerStatLine> = emptyList(),
    val bestStrikeRate: List<PlayerStatLine> = emptyList(),
    val bestEconomy: List<PlayerStatLine> = emptyList(),
    val mostFifties: List<PlayerStatLine> = emptyList(),
    val mostHundreds: List<PlayerStatLine> = emptyList(),
    val mostCatches: List<PlayerStatLine> = emptyList()
)

data class PlayerStatLine(
    val playerId: String,
    val playerName: String,
    val teamId: String,
    val teamName: String,
    val imageUrl: String = "",
    val primaryValue: String,
    val secondaryValue: String = ""
)

data class KnockoutBracket(
    val tournamentId: String,
    val quarterFinals: List<Fixture> = emptyList(),
    val semiFinals: List<Fixture> = emptyList(),
    val thirdPlacePlayoff: Fixture? = null,
    val final: Fixture? = null,
    val champion: TournamentTeam? = null
)
