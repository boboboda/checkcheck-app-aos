package com.buyoungsil.checkcheck.feature.coin.presentation.wallet

import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinTransaction
import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember

data class CoinWalletUiState(
    val wallet: CoinWallet? = null,
    val transactions: List<CoinTransaction> = emptyList(),
    val groupMembers: List<GroupMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)