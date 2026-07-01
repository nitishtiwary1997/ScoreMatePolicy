package com.cric.cricketscoring.domain.usecase.tournament

import com.cric.cricketscoring.domain.model.Tournament
import com.cric.cricketscoring.domain.repository.TournamentRepository
import javax.inject.Inject

class CreateTournamentUseCase @Inject constructor(
    private val tournamentRepository: TournamentRepository
) {
    suspend operator fun invoke(tournament: Tournament) {
        tournamentRepository.createTournament(tournament)
    }
}
