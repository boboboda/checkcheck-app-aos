package com.buyoungsil.checkcheck.feature.ranking.domain.usecase

import com.buyoungsil.checkcheck.feature.ranking.domain.repository.GlobalRankingRepository
import javax.inject.Inject

/**
 * 글로벌 랭킹 업데이트 UseCase
 */
class UpdateGlobalRankingUseCase @Inject constructor(
    private val repository: GlobalRankingRepository
) {
    suspend operator fun invoke(
        userId: String,
        userName: String,
        habitTitle: String,
        currentStreak: Int,
        totalChecks: Int,
        completionRate: Float
    ): Result<Unit> {
        return repository.updateMyRanking(
            userId = userId,
            userName = userName,
            habitTitle = habitTitle,
            currentStreak = currentStreak,
            totalChecks = totalChecks,
            completionRate = completionRate
        )
    }
}