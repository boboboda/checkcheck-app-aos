package com.buyoungsil.checkcheck.feature.coin.presentation.wallet

import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinTransaction
import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet

data class CoinWalletUiState(
    val wallet: CoinWallet? = null,
    val transactions: List<CoinTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)