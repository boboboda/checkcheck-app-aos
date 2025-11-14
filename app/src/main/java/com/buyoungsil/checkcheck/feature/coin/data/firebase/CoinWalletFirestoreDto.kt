package com.buyoungsil.checkcheck.feature.coin.data.firebase

import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * 코인 지갑 Firestore DTO
 * ⚠️ @DocumentId 제거 - userId는 문서 ID로만 사용
 */
data class CoinWalletFirestoreDto(
    val familyCoins: Int = 0,
    val rewardCoins: Int = 0,
    val totalEarned: Int = 0,
    val totalSpent: Int = 0,
    @ServerTimestamp
    val lastUpdated: Date? = null
) {
    constructor() : this(0, 0, 0, 0, null)

    fun toDomain(userId: String): CoinWallet {
        return CoinWallet(
            userId = userId,  // 🆕 파라미터로 받음
            familyCoins = familyCoins,
            rewardCoins = rewardCoins,
            totalEarned = totalEarned,
            totalSpent = totalSpent,
            lastUpdated = lastUpdated?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(wallet: CoinWallet): CoinWalletFirestoreDto {
            return CoinWalletFirestoreDto(
                familyCoins = wallet.familyCoins,
                rewardCoins = wallet.rewardCoins,
                totalEarned = wallet.totalEarned,
                totalSpent = wallet.totalSpent,
                lastUpdated = Date(wallet.lastUpdated)
            )
        }
    }
}