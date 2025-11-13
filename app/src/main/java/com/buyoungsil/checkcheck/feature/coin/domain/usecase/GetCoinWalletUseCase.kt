package com.buyoungsil.checkcheck.feature.coin.domain.usecase

import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet
import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCoinWalletUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    operator fun invoke(userId: String): Flow<CoinWallet?> {
        return repository.getCoinWallet(userId)
    }
}