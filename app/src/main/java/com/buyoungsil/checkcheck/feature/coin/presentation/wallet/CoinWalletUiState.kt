package com.buyoungsil.checkcheck.feature.coin.presentation.wallet

import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinTransaction
import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember
import com.buyoungsil.checkcheck.feature.group.domain.model.MemberRole

/**
 * 그룹 정보를 포함한 멤버 (UI 전용)
 */
data class MemberWithGroup(
    val userId: String,
    val displayName: String,
    val role: MemberRole,
    val groupId: String,
    val groupName: String
)

data class CoinWalletUiState(
    val wallet: CoinWallet? = null,
    val transactions: List<CoinTransaction> = emptyList(),
    val membersWithGroups: List<MemberWithGroup> = emptyList(), // 🆕 변경됨
    val isLoading: Boolean = false,
    val error: String? = null
)