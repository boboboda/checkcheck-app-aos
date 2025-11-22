package com.buyoungsil.checkcheck.feature.ranking.domain.usecase

import com.buyoungsil.checkcheck.feature.ranking.domain.model.MyRankingInfo
import com.buyoungsil.checkcheck.feature.ranking.domain.repository.GlobalRankingRepository
import javax.inject.Inject

/**
 * 내 랭킹 조회 UseCase
 */
class GetMyRankingUseCase @Inject constructor(
    private val repository: GlobalRankingRepository
) {
    suspend operator fun invoke(
        userId: String,
        habitTitle: String
    ): Result<MyRankingInfo> {
        return repository.getMyRanking(userId, habitTitle)
    }
}