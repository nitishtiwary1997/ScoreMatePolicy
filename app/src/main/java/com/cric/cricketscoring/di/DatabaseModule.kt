package com.cric.cricketscoring.di

import android.content.Context
import androidx.room.Room
import com.cric.cricketscoring.data.local.AppDatabase
import com.cric.cricketscoring.data.local.MIGRATION_4_5
import com.cric.cricketscoring.data.local.MIGRATION_5_6
import com.cric.cricketscoring.data.local.MIGRATION_6_7
import com.cric.cricketscoring.data.local.MIGRATION_7_8
import com.cric.cricketscoring.data.local.MIGRATION_8_9
import com.cric.cricketscoring.data.local.MIGRATION_9_10
import com.cric.cricketscoring.data.local.dao.BallDao
import com.cric.cricketscoring.data.local.dao.FixtureDao
import com.cric.cricketscoring.data.local.dao.MatchDao
import com.cric.cricketscoring.data.local.dao.PlayerDao
import com.cric.cricketscoring.data.local.dao.PointsEntryDao
import com.cric.cricketscoring.data.local.dao.SavedTeamDao
import com.cric.cricketscoring.data.local.dao.TournamentDao
import com.cric.cricketscoring.data.local.dao.TournamentPlayerDao
import com.cric.cricketscoring.data.local.dao.TournamentTeamDao
import com.cric.cricketscoring.data.repository.MatchRepositoryImpl
import com.cric.cricketscoring.domain.repository.MatchRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "cricket_db")
            .addMigrations(
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10
            )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    // --- existing DAOs ---

    @Provides
    fun provideMatchDao(db: AppDatabase): MatchDao = db.matchDao()

    @Provides
    fun providePlayerDao(db: AppDatabase): PlayerDao = db.playerDao()

    @Provides
    fun provideBallDao(db: AppDatabase): BallDao = db.ballDao()

    @Provides
    fun provideSavedTeamDao(db: AppDatabase): SavedTeamDao = db.savedTeamDao()

    // --- tournament DAOs ---

    @Provides
    fun provideTournamentDao(db: AppDatabase): TournamentDao = db.tournamentDao()

    @Provides
    fun provideTournamentTeamDao(db: AppDatabase): TournamentTeamDao = db.tournamentTeamDao()

    @Provides
    fun provideTournamentPlayerDao(db: AppDatabase): TournamentPlayerDao = db.tournamentPlayerDao()

    @Provides
    fun provideFixtureDao(db: AppDatabase): FixtureDao = db.fixtureDao()

    @Provides
    fun providePointsEntryDao(db: AppDatabase): PointsEntryDao = db.pointsEntryDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository
}
