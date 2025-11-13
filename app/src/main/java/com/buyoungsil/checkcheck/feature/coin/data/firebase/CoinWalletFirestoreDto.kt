package com.buyoungsil.checkcheck.feature.coin.data.firebase

import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * 코인 지갑 Firestore DTO
 */
data class CoinWalletFirestoreDto(
    @DocumentId
    val userId: String = "",
    val familyCoins: Int = 0,
    val rewardCoins: Int = 0,
    val totalEarned: Int = 0,
    val totalSpent: Int = 0,
    @ServerTimestamp
    val lastUpdated: Date? = null
) {
    constructor() : this("", 0, 0, 0, 0, null)

    fun toDomain(): CoinWallet {
        return CoinWallet(
            userId = userId,
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
                userId = wallet.userId,
                familyCoins = wallet.familyCoins,
                rewardCoins = wallet.rewardCoins,
                totalEarned = wallet.totalEarned,
                totalSpent = wallet.totalSpent,
                lastUpdated = Date(wallet.lastUpdated)
            )
        }
    }
}