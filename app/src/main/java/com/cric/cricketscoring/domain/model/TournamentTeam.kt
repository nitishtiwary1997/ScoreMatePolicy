package com.cric.cricketscoring.domain.model

import java.util.UUID

data class TournamentTeam(
    val id: String = UUID.randomUUID().toString(),
    val tournamentId: String,
    val name: String,
    val shortName: String = "",
    val logoUrl: String = "",
    val jerseyColorPrimary: String = "#1A237E",
    val jerseyColorSecondary: String = "#FFFFFF",
    val captainPlayerId: String = "",
    val viceCaptainPlayerId: String = "",
    val homeGround: String = "",
    val registeredAt: Long = System.currentTimeMillis()
) {
    val initials: String get() = shortName.ifBlank {
        name.split(" ").take(2).joinToString("") { it.first().uppercase() }
    }
}
