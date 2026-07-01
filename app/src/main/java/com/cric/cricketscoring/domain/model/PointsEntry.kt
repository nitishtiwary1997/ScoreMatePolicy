package com.cric.cricketscoring.domain.model

import java.util.UUID
import kotlin.math.abs

data class PointsEntry(
    val id: String = UUID.randomUUID().toString(),
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
    val isQualified: Boolean = false
) {
    val nrrFormatted: String get() {
        val prefix = if (nrr >= 0) "+" else "-"
        return "$prefix${String.format("%.3f", abs(nrr))}"
    }

    val matchesRemaining: Int get() = 0  // filled by TournamentDashboardViewModel
}
