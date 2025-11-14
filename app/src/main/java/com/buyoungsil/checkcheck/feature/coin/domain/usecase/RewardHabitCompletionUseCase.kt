package com.buyoungsil.checkcheck.feature.coin.domain.usecase

import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import javax.inject.Inject

/**
 * 습관 완료 보상 UseCase
 *
 * @param userId 사용자 ID
 * @param habitId 습관 ID
 * @param coins 지급할 코인 수 (기본값 1코인)
 */
class RewardHabitCompletionUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    suspend operator fun invoke(
        userId: String,
        habitId: String,
        coins: Int = 1  // 🆕 amount → coins로 변경
    ): Result<Unit> {
        return repository.rewardHabitCompletion(userId, habitId, coins)
    }
}