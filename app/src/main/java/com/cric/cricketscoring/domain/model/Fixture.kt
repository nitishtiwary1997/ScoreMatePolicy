package com.cric.cricketscoring.domain.model

import java.util.UUID

data class Fixture(
    val id: String = UUID.randomUUID().toString(),
    val tournamentId: String,
    val matchId: String? = null,
    val team1Id: String,
    val team2Id: String,
    val stage: FixtureStage = FixtureStage.GROUP,
    val groupName: String = "A",
    val matchNumber: Int = 1,
    val scheduledAt: Long,
    val venue: String = "",
    val status: FixtureStatus = FixtureStatus.UPCOMING,
    val winnerId: String? = null,
    val resultSummary: String = ""
) {
    val isLive: Boolean get() = status == FixtureStatus.LIVE
    val isCompleted: Boolean get() = status == FixtureStatus.COMPLETED
    val hasStarted: Boolean get() = matchId != null
    val stageLabel: String get() = stage.label
}

enum class FixtureStage(val label: String) {
    GROUP("Group Stage"),
    QUARTER_FINAL("Quarter Final"),
    SEMI_FINAL("Semi Final"),
    THIRD_PLACE_PLAYOFF("3rd Place Playoff"),
    FINAL("Final")
}

enum class FixtureStatus(val label: String) {
    UPCOMING("Upcoming"),
    LIVE("Live"),
    COMPLETED("Completed"),
    ABANDONED("Abandoned"),
    NO_RESULT("No Result")
}
