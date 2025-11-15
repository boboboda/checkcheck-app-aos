package com.buyoungsil.checkcheck.feature.coin.data.firebase

import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * 코인 지갑 Firestore DTO
 * ⚠️ @DocumentId 제거 - userId는 문서 ID로만 사용
 *
 * 🆕 어뷰징 방지 필드 추가
 */
data class CoinWalletFirestoreDto(
    val familyCoins: Int = 0,
    val rewardCoins: Int = 0,
    val totalEarned: Int = 0,
    val totalSpent: Int = 0,

    // 🆕 어뷰징 방지 필드
    val monthlyRewardCoins: Int = 0,
    val dailyRewardCoins: Int = 0,
    val lastMonthReset: Date? = null,
    val lastDayReset: Date? = null,

    @ServerTimestamp
    val lastUpdated: Date? = null
) {
    constructor() : this(0, 0, 0, 0, 0, 0, null, null, null)

    fun toDomain(userId: String): CoinWallet {
        return CoinWallet(
            userId = userId,
            familyCoins = familyCoins,
            rewardCoins = rewardCoins,
            totalEarned = totalEarned,
            totalSpent = totalSpent,
            monthlyRewardCoins = monthlyRewardCoins,
            dailyRewardCoins = dailyRewardCoins,
            lastMonthReset = lastMonthReset?.time ?: System.currentTimeMillis(),
            lastDayReset = lastDayReset?.time ?: System.currentTimeMillis(),
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
                monthlyRewardCoins = wallet.monthlyRewardCoins,
                dailyRewardCoins = wallet.dailyRewardCoins,
                lastMonthReset = Date(wallet.lastMonthReset),
                lastDayReset = Date(wallet.lastDayReset),
                lastUpdated = Date(wallet.lastUpdated)
            )
        }
    }
}