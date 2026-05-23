package com.nitish.cricketscoringapp.di

import com.nitish.cricketscoringapp.data.repository.TournamentRepositoryImpl
import com.nitish.cricketscoringapp.domain.repository.TournamentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TournamentModule {

    @Binds
    @Singleton
    abstract fun bindTournamentRepository(
        impl: TournamentRepositoryImpl
    ): TournamentRepository
}
