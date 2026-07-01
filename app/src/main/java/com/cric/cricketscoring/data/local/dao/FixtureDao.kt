package com.cric.cricketscoring.data.local.dao

import androidx.room.*
import com.cric.cricketscoring.data.local.entity.FixtureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FixtureDao {

    @Query("SELECT * FROM fixtures WHERE tournamentId = :tournamentId ORDER BY matchNumber ASC")
    fun getFixturesByTournament(tournamentId: String): Flow<List<FixtureEntity>>

    @Query("SELECT * FROM fixtures WHERE tournamentId = :tournamentId ORDER BY matchNumber ASC")
    suspend fun getFixturesByTournamentSync(tournamentId: String): List<FixtureEntity>

    @Query("SELECT * FROM fixtures WHERE tournamentId = :tournamentId AND stage = :stage ORDER BY matchNumber ASC")
    fun getFixturesByStage(tournamentId: String, stage: String): Flow<List<FixtureEntity>>

    @Query("SELECT * FROM fixtures WHERE tournamentId = :tournamentId AND status = 'LIVE'")
    fun getLiveFixtures(tournamentId: String): Flow<List<FixtureEntity>>

    @Query("SELECT * FROM fixtures WHERE tournamentId = :tournamentId AND status = 'UPCOMING' ORDER BY scheduledAt ASC")
    fun getUpcomingFixtures(tournamentId: String): Flow<List<FixtureEntity>>

    @Query("SELECT * FROM fixtures WHERE tournamentId = :tournamentId AND status = 'COMPLETED' ORDER BY scheduledAt DESC")
    fun getCompletedFixtures(tournamentId: String): Flow<List<FixtureEntity>>

    @Query("SELECT * FROM fixtures WHERE id = :fixtureId")
    fun getFixtureById(fixtureId: String): Flow<FixtureEntity?>

    @Query("SELECT * FROM fixtures WHERE id = :fixtureId")
    suspend fun getFixtureByIdSync(fixtureId: String): FixtureEntity?

    @Query("SELECT * FROM fixtures WHERE matchId = :matchId LIMIT 1")
    suspend fun getFixtureByMatchId(matchId: String): FixtureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFixture(fixture: FixtureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFixtures(fixtures: List<FixtureEntity>)

    @Query("UPDATE fixtures SET matchId = :matchId, status = 'LIVE' WHERE id = :fixtureId")
    suspend fun linkMatch(fixtureId: String, matchId: String)

    @Query("UPDATE fixtures SET status = :status, winnerId = :winnerId, resultSummary = :resultSummary WHERE id = :fixtureId")
    suspend fun completeFixture(fixtureId: String, status: String, winnerId: String?, resultSummary: String)

    @Query("UPDATE fixtures SET isSynced = 1 WHERE id = :fixtureId")
    suspend fun markSynced(fixtureId: String)

    @Query("SELECT * FROM fixtures WHERE isSynced = 0")
    suspend fun getUnsynced(): List<FixtureEntity>

    @Query("DELETE FROM fixtures WHERE tournamentId = :tournamentId AND stage = :stage")
    suspend fun deleteFixturesByStage(tournamentId: String, stage: String)

    @Query("DELETE FROM fixtures WHERE id = :fixtureId")
    suspend fun deleteFixture(fixtureId: String)

    @Query("SELECT COUNT(*) FROM fixtures WHERE tournamentId = :tournamentId AND status = 'COMPLETED' AND stage = 'GROUP'")
    suspend fun getCompletedGroupMatchCount(tournamentId: String): Int

    @Query("SELECT COUNT(*) FROM fixtures WHERE tournamentId = :tournamentId AND stage = 'GROUP'")
    suspend fun getTotalGroupMatchCount(tournamentId: String): Int
}
