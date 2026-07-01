package com.cric.cricketscoring.domain.model

import java.util.UUID

data class Tournament(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val organizerName: String = "",
    val organizerContact: String = "",
    val bannerUrl: String = "",
    val logoUrl: String = "",
    val venue: String = "",
    val startDate: Long,
    val endDate: Long,
    val matchFormat: TournamentMatchFormat = TournamentMatchFormat.T20,
    val customOvers: Int = 20,
    val ballType: BallType = BallType.LEATHER,
    val tournamentType: TournamentType = TournamentType.LEAGUE,
    val status: TournamentStatus = TournamentStatus.UPCOMING,
    val isPublic: Boolean = true,
    val entryFee: Double = 0.0,
    val prizeDetails: String = "",
    val rules: String = "",
    val maxTeams: Int = 8,
    val playersPerTeam: Int = 11,
    val createdByUserId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalOvers: Int get() = when (matchFormat) {
        TournamentMatchFormat.T20    -> 20
        TournamentMatchFormat.ODI    -> 50
        TournamentMatchFormat.TEST   -> 0
        TournamentMatchFormat.CUSTOM -> customOvers
    }

    val isOngoing: Boolean get() = status == TournamentStatus.ONGOING
    val isCompleted: Boolean get() = status == TournamentStatus.COMPLETED
}

enum class TournamentMatchFormat(val label: String) {
    T20("T20"),
    ODI("ODI"),
    TEST("Test"),
    CUSTOM("Custom")
}

enum class BallType(val label: String) {
    LEATHER("Leather"),
    TENNIS("Tennis"),
    RUBBER("Rubber"),
    TAPE_BALL("Tape Ball")
}

enum class TournamentType(val label: String) {
    LEAGUE("League (Round Robin)"),
    KNOCKOUT("Knockout"),
    LEAGUE_PLUS_KNOCKOUT("League + Knockout")
}

enum class TournamentStatus(val label: String) {
    UPCOMING("Upcoming"),
    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}
