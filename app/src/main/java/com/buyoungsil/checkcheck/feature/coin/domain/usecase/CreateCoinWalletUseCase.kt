package com.buyoungsil.checkcheck.feature.coin.domain.usecase

import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import javax.inject.Inject

class CreateCoinWalletUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return repository.createCoinWallet(userId)
    }
}