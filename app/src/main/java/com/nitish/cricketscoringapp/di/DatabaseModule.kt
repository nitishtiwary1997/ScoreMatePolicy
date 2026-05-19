package com.nitish.cricketscoringapp.di

import android.content.Context
import androidx.room.Room
import com.nitish.cricketscoringapp.data.local.AppDatabase
import com.nitish.cricketscoringapp.data.local.MIGRATION_4_5
import com.nitish.cricketscoringapp.data.local.MIGRATION_5_6
import com.nitish.cricketscoringapp.data.local.MIGRATION_6_7
import com.nitish.cricketscoringapp.data.local.MIGRATION_7_8
import com.nitish.cricketscoringapp.data.local.dao.BallDao
import com.nitish.cricketscoringapp.data.local.dao.MatchDao
import com.nitish.cricketscoringapp.data.local.dao.PlayerDao
import com.nitish.cricketscoringapp.data.local.dao.SavedTeamDao
import com.nitish.cricketscoringapp.data.repository.MatchRepositoryImpl
import com.nitish.cricketscoringapp.domain.repository.MatchRepository
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
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMatchDao(db: AppDatabase): MatchDao = db.matchDao()

    @Provides
    fun providePlayerDao(db: AppDatabase): PlayerDao = db.playerDao()

    @Provides
    fun provideBallDao(db: AppDatabase): BallDao = db.ballDao()

    @Provides
    fun provideSavedTeamDao(db: AppDatabase): SavedTeamDao = db.savedTeamDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository
}
