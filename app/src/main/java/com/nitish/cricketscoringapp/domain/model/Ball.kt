package com.nitish.cricketscoringapp.domain.model

data class Ball(
    val id: String,
    val matchId: String,
    val innings: Int,
    val overNumber: Int,
    val ballInOver: Int,
    val batsmanId: String,
    val bowlerId: String,
    val runs: Int = 0,
    val extras: Int = 0,
    val extraType: ExtraType? = null,
    val isWicket: Boolean = false,
    val wicketType: WicketType? = null,
    val dismissedPlayerId: String? = null,
    val fielderIds: List<String> = emptyList()
) {
    val isLegalDelivery: Boolean
        get() = extraType != ExtraType.WIDE && extraType != ExtraType.NO_BALL

    val totalRuns: Int get() = runs + extras

    val runsForBatsman: Int
        get() = if (extraType == ExtraType.BYE || extraType == ExtraType.LEG_BYE) 0 else runs
}

enum class ExtraType { WIDE, NO_BALL, BYE, LEG_BYE }

enum class WicketType {
    BOWLED, CAUGHT, LBW, RUN_OUT, STUMPED, HIT_WICKET,
    /** Injury/health/emergency — NOT out, no wicket, can return later in same innings. */
    RETIRED_HURT,
    /** Retired without valid reason/umpire permission — OUT, counts as wicket, cannot return. */
    RETIRED_OUT
}
