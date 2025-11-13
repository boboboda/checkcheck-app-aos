package com.buyoungsil.checkcheck.feature.coin.domain.usecase

import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import javax.inject.Inject

class RewardHabitCompletionUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    suspend operator fun invoke(
        userId: String,
        habitId: String,
        amount: Int = 1
    ): Result<Unit> {
        return repository.rewardHabitCompletion(userId, habitId, amount)
    }
}