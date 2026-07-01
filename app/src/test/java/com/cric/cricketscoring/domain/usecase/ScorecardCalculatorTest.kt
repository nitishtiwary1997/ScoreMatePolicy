package com.cric.cricketscoring.domain.usecase

import com.cric.cricketscoring.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class ScorecardCalculatorTest {

    private val player1 = Player("p1", "Player 1", "m1", 1)
    private val player2 = Player("p2", "Player 2", "m1", 1)
    private val bowler1 = Player("b1", "Bowler 1", "m1", 2)
    private val players = listOf(player1, player2, bowler1)

    @Test
    fun testStandardScoringCalculation() {
        val balls = listOf(
            Ball(
                id = UUID.randomUUID().toString(),
                matchId = "m1",
                innings = 1,
                overNumber = 0,
                ballInOver = 1,
                batsmanId = "p1",
                bowlerId = "b1",
                runs = 1,
                extras = 0
            ),
            Ball(
                id = UUID.randomUUID().toString(),
                matchId = "m1",
                innings = 1,
                overNumber = 0,
                ballInOver = 2,
                batsmanId = "p2",
                bowlerId = "b1",
                runs = 4,
                extras = 0
            ),
            // Wide ball
            Ball(
                id = UUID.randomUUID().toString(),
                matchId = "m1",
                innings = 1,
                overNumber = 0,
                ballInOver = 0,
                batsmanId = "p2",
                bowlerId = "b1",
                runs = 0,
                extras = 1,
                extraType = ExtraType.WIDE
            )
        )

        val inningsScore = ScorecardCalculator.calculateInnings(
            inningsNumber = 1,
            balls = balls,
            players = players,
            battingTeamName = "Team A",
            bowlingTeamName = "Team B",
            onStrikeId = "p2",
            offStrikeId = "p1",
            currentBowlerId = "b1",
            isCompleted = false,
            totalOvers = 20
        )

        assertEquals(6, inningsScore.totalRuns) // 1 + 4 + 1 wide = 6
        assertEquals(0, inningsScore.wickets)
        assertEquals(2, inningsScore.legalBallsInCurrentOver) // Wide is not a legal delivery

        val scoreP1 = inningsScore.batsmen.first { it.player.id == "p1" }
        assertEquals(1, scoreP1.runs)
        assertEquals(1, scoreP1.balls)

        val scoreP2 = inningsScore.batsmen.first { it.player.id == "p2" }
        assertEquals(4, scoreP2.runs)
        assertEquals(1, scoreP2.balls) // Wides do not count as balls faced

        val bowlerStats = inningsScore.bowlers.first { it.player.id == "b1" }
        assertEquals(6, bowlerStats.runs)
        assertEquals(2, bowlerStats.totalLegalBalls)
        assertEquals(1, bowlerStats.wides)
    }

    @Test
    fun testWicketRetainsDismissalInfoEvenIfStillAtCrease() {
        val balls = listOf(
            Ball(
                id = UUID.randomUUID().toString(),
                matchId = "m1",
                innings = 1,
                overNumber = 0,
                ballInOver = 1,
                batsmanId = "p1",
                bowlerId = "b1",
                runs = 0,
                extras = 0,
                isWicket = true,
                wicketType = WicketType.BOWLED,
                dismissedPlayerId = "p1"
            )
        )

        // In the database state, before the UI swaps the batsman, onStrikeId might still be p1.
        // We verify that the calculator correctly marks p1 as OUT (dismissalInfo is not null) and isOut = true.
        val inningsScore = ScorecardCalculator.calculateInnings(
            inningsNumber = 1,
            balls = balls,
            players = players,
            battingTeamName = "Team A",
            bowlingTeamName = "Team B",
            onStrikeId = "p1", // Inconsistent database state
            offStrikeId = "p2",
            currentBowlerId = "b1",
            isCompleted = false,
            totalOvers = 20
        )

        val scoreP1 = inningsScore.batsmen.first { it.player.id == "p1" }
        assertTrue(scoreP1.isOut)
        assertNotNull(scoreP1.dismissalInfo)
        assertEquals("b Bowler 1", scoreP1.dismissalInfo)
        assertEquals(1, inningsScore.wickets)
    }

    @Test
    fun testRetiredHurtClearsDismissalInfoWhenActiveAgain() {
        val balls = listOf(
            Ball(
                id = UUID.randomUUID().toString(),
                matchId = "m1",
                innings = 1,
                overNumber = 0,
                ballInOver = 1,
                batsmanId = "p1",
                bowlerId = "b1",
                runs = 0,
                extras = 0,
                isWicket = true,
                wicketType = WicketType.RETIRED_HURT,
                dismissedPlayerId = "p1"
            )
        )

        // If p1 is not at crease, they are retired hurt (out / inactive)
        val scoreHurt = ScorecardCalculator.calculateInnings(
            inningsNumber = 1,
            balls = balls,
            players = players,
            battingTeamName = "Team A",
            bowlingTeamName = "Team B",
            onStrikeId = "p2",
            offStrikeId = "",
            currentBowlerId = "b1",
            isCompleted = false,
            totalOvers = 20
        )
        val batsmanHurt = scoreHurt.batsmen.first { it.player.id == "p1" }
        assertEquals("retired hurt", batsmanHurt.dismissalInfo)

        // If p1 returns to the crease, they are active again, so their retired hurt status should clear (not out).
        val scoreActive = ScorecardCalculator.calculateInnings(
            inningsNumber = 1,
            balls = balls,
            players = players,
            battingTeamName = "Team A",
            bowlingTeamName = "Team B",
            onStrikeId = "p1", // Returned to crease
            offStrikeId = "p2",
            currentBowlerId = "b1",
            isCompleted = false,
            totalOvers = 20
        )
        val batsmanActive = scoreActive.batsmen.first { it.player.id == "p1" }
        assertNull(batsmanActive.dismissalInfo)
        assertFalse(batsmanActive.isOut)
    }
}
