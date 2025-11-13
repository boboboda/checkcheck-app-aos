package com.buyoungsil.checkcheck.feature.coin.domain.usecase

import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import javax.inject.Inject

class GiftCoinsUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    suspend operator fun invoke(
        fromUserId: String,
        toUserId: String,
        amount: Int,
        message: String?
    ): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(Exception("코인은 1개 이상이어야 합니다"))
        }
        return repository.giftCoins(fromUserId, toUserId, amount, message)
    }
}