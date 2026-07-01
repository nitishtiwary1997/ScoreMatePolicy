package com.cric.cricketscoring.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cric.cricketscoring.data.local.dao.BallDao
import com.cric.cricketscoring.data.local.dao.FixtureDao
import com.cric.cricketscoring.data.local.dao.MatchDao
import com.cric.cricketscoring.data.local.dao.PlayerDao
import com.cric.cricketscoring.data.local.dao.PointsEntryDao
import com.cric.cricketscoring.data.local.dao.SavedTeamDao
import com.cric.cricketscoring.data.local.dao.TournamentDao
import com.cric.cricketscoring.data.local.dao.TournamentPlayerDao
import com.cric.cricketscoring.data.local.dao.TournamentTeamDao
import com.cric.cricketscoring.data.local.entity.BallEntity
import com.cric.cricketscoring.data.local.entity.FixtureEntity
import com.cric.cricketscoring.data.local.entity.MatchEntity
import com.cric.cricketscoring.data.local.entity.PlayerEntity
import com.cric.cricketscoring.data.local.entity.PointsEntryEntity
import com.cric.cricketscoring.data.local.entity.SavedTeamEntity
import com.cric.cricketscoring.data.local.entity.TournamentEntity
import com.cric.cricketscoring.data.local.entity.TournamentPlayerEntity
import com.cric.cricketscoring.data.local.entity.TournamentTeamEntity

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE matches ADD COLUMN playersPerTeam INTEGER NOT NULL DEFAULT 11")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE balls ADD COLUMN fielderIds TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE matches ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE players ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE balls ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE matches ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE players ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE balls ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Link existing matches to tournaments (null = standalone match)
        db.execSQL("ALTER TABLE matches ADD COLUMN tournamentId TEXT")
        db.execSQL("ALTER TABLE matches ADD COLUMN fixtureId TEXT")

        // Tournaments table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS tournaments (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                organizerName TEXT NOT NULL DEFAULT '',
                organizerContact TEXT NOT NULL DEFAULT '',
                bannerUrl TEXT NOT NULL DEFAULT '',
                logoUrl TEXT NOT NULL DEFAULT '',
                venue TEXT NOT NULL DEFAULT '',
                startDate INTEGER NOT NULL,
                endDate INTEGER NOT NULL,
                matchFormat TEXT NOT NULL DEFAULT 'T20',
                customOvers INTEGER NOT NULL DEFAULT 20,
                ballType TEXT NOT NULL DEFAULT 'LEATHER',
                tournamentType TEXT NOT NULL DEFAULT 'LEAGUE',
                status TEXT NOT NULL DEFAULT 'UPCOMING',
                isPublic INTEGER NOT NULL DEFAULT 1,
                entryFee REAL NOT NULL DEFAULT 0.0,
                prizeDetails TEXT NOT NULL DEFAULT '',
                rules TEXT NOT NULL DEFAULT '',
                maxTeams INTEGER NOT NULL DEFAULT 8,
                playersPerTeam INTEGER NOT NULL DEFAULT 11,
                createdByUserId TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Tournament teams
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS tournament_teams (
                id TEXT NOT NULL PRIMARY KEY,
                tournamentId TEXT NOT NULL,
                name TEXT NOT NULL,
                shortName TEXT NOT NULL DEFAULT '',
                logoUrl TEXT NOT NULL DEFAULT '',
                jerseyColorPrimary TEXT NOT NULL DEFAULT '#1A237E',
                jerseyColorSecondary TEXT NOT NULL DEFAULT '#FFFFFF',
                captainPlayerId TEXT NOT NULL DEFAULT '',
                viceCaptainPlayerId TEXT NOT NULL DEFAULT '',
                homeGround TEXT NOT NULL DEFAULT '',
                registeredAt INTEGER NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(tournamentId) REFERENCES tournaments(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_tournament_teams_tournamentId ON tournament_teams(tournamentId)")

        // Tournament players
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS tournament_players (
                id TEXT NOT NULL PRIMARY KEY,
                tournamentId TEXT NOT NULL,
                teamId TEXT NOT NULL,
                name TEXT NOT NULL,
                imageUrl TEXT NOT NULL DEFAULT '',
                jerseyNumber INTEGER NOT NULL DEFAULT 0,
                role TEXT NOT NULL DEFAULT 'BATSMAN',
                battingStyle TEXT NOT NULL DEFAULT 'RIGHT_HAND',
                bowlingStyle TEXT NOT NULL DEFAULT 'NONE',
                dateOfBirth INTEGER,
                contactNumber TEXT NOT NULL DEFAULT '',
                isSynced INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(tournamentId) REFERENCES tournaments(id) ON DELETE CASCADE,
                FOREIGN KEY(teamId) REFERENCES tournament_teams(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_tournament_players_tournamentId ON tournament_players(tournamentId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_tournament_players_teamId ON tournament_players(teamId)")

        // Fixtures
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS fixtures (
                id TEXT NOT NULL PRIMARY KEY,
                tournamentId TEXT NOT NULL,
                matchId TEXT,
                team1Id TEXT NOT NULL,
                team2Id TEXT NOT NULL,
                stage TEXT NOT NULL DEFAULT 'GROUP',
                groupName TEXT NOT NULL DEFAULT 'A',
                matchNumber INTEGER NOT NULL DEFAULT 1,
                scheduledAt INTEGER NOT NULL,
                venue TEXT NOT NULL DEFAULT '',
                status TEXT NOT NULL DEFAULT 'UPCOMING',
                winnerId TEXT,
                resultSummary TEXT NOT NULL DEFAULT '',
                isSynced INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(tournamentId) REFERENCES tournaments(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_fixtures_tournamentId ON fixtures(tournamentId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_fixtures_matchId ON fixtures(matchId)")

        // Points table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS points_table (
                id TEXT NOT NULL PRIMARY KEY,
                tournamentId TEXT NOT NULL,
                teamId TEXT NOT NULL,
                teamName TEXT NOT NULL,
                teamLogoUrl TEXT NOT NULL DEFAULT '',
                matchesPlayed INTEGER NOT NULL DEFAULT 0,
                won INTEGER NOT NULL DEFAULT 0,
                lost INTEGER NOT NULL DEFAULT 0,
                tied INTEGER NOT NULL DEFAULT 0,
                noResult INTEGER NOT NULL DEFAULT 0,
                abandoned INTEGER NOT NULL DEFAULT 0,
                points INTEGER NOT NULL DEFAULT 0,
                totalRunsScored INTEGER NOT NULL DEFAULT 0,
                totalOversFaced REAL NOT NULL DEFAULT 0.0,
                totalRunsConceded INTEGER NOT NULL DEFAULT 0,
                totalOversBowled REAL NOT NULL DEFAULT 0.0,
                nrr REAL NOT NULL DEFAULT 0.0,
                rank INTEGER NOT NULL DEFAULT 0,
                isQualified INTEGER NOT NULL DEFAULT 0,
                isSynced INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(tournamentId) REFERENCES tournaments(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_points_table_tournamentId ON points_table(tournamentId)")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE matches ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE matches ADD COLUMN currentEditorId TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [
        MatchEntity::class,
        PlayerEntity::class,
        BallEntity::class,
        SavedTeamEntity::class,
        TournamentEntity::class,
        TournamentTeamEntity::class,
        TournamentPlayerEntity::class,
        FixtureEntity::class,
        PointsEntryEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun playerDao(): PlayerDao
    abstract fun ballDao(): BallDao
    abstract fun savedTeamDao(): SavedTeamDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun tournamentTeamDao(): TournamentTeamDao
    abstract fun tournamentPlayerDao(): TournamentPlayerDao
    abstract fun fixtureDao(): FixtureDao
    abstract fun pointsEntryDao(): PointsEntryDao
}
