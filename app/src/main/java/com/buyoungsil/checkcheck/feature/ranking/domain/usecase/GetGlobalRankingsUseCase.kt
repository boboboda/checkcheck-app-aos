package com.buyoungsil.checkcheck.feature.ranking.domain.usecase

import com.buyoungsil.checkcheck.feature.ranking.domain.model.GlobalHabitRanking
import com.buyoungsil.checkcheck.feature.ranking.domain.repository.GlobalRankingRepository
import javax.inject.Inject

/**
 * 글로벌 랭킹 조회 UseCase
 */
class GetGlobalRankingsUseCase @Inject constructor(
    private val repository: GlobalRankingRepository
) {
    suspend operator fun invoke(
        habitTitle: String,
        limit: Int = 100
    ): Result<GlobalHabitRanking> {
        return repository.getTopRankings(habitTitle, limit)
    }
}