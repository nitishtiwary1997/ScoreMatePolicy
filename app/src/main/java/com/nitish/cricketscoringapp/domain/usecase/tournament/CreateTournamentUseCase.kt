package com.nitish.cricketscoringapp.domain.usecase.tournament

import com.nitish.cricketscoringapp.domain.model.Tournament
import com.nitish.cricketscoringapp.domain.repository.TournamentRepository
import javax.inject.Inject

class CreateTournamentUseCase @Inject constructor(
    private val tournamentRepository: TournamentRepository
) {
    suspend operator fun invoke(tournament: Tournament) {
        tournamentRepository.createTournament(tournament)
    }
}
