package com.nitish.cricketscoringapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nitish.cricketscoringapp.data.local.dao.BallDao
import com.nitish.cricketscoringapp.data.local.dao.MatchDao
import com.nitish.cricketscoringapp.data.local.dao.PlayerDao
import com.nitish.cricketscoringapp.data.local.dao.SavedTeamDao
import com.nitish.cricketscoringapp.data.local.entity.BallEntity
import com.nitish.cricketscoringapp.data.local.entity.MatchEntity
import com.nitish.cricketscoringapp.data.local.entity.PlayerEntity
import com.nitish.cricketscoringapp.data.local.entity.SavedTeamEntity

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

@Database(
    entities = [MatchEntity::class, PlayerEntity::class, BallEntity::class, SavedTeamEntity::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun playerDao(): PlayerDao
    abstract fun ballDao(): BallDao
    abstract fun savedTeamDao(): SavedTeamDao
}
