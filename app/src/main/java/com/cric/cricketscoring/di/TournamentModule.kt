package com.cric.cricketscoring.di

import com.cric.cricketscoring.data.repository.TournamentRepositoryImpl
import com.cric.cricketscoring.domain.repository.TournamentRepository
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
