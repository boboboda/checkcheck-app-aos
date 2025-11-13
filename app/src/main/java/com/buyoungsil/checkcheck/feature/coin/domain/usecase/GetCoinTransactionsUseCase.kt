package com.buyoungsil.checkcheck.feature.coin.domain.usecase

import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinTransaction
import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCoinTransactionsUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    operator fun invoke(userId: String): Flow<List<CoinTransaction>> {
        return repository.getCoinTransactions(userId)
    }
}