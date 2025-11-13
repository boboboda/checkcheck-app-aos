package com.buyoungsil.checkcheck.feature.coin.domain.model

data class CoinWallet(
    val userId: String,
    val familyCoins: Int = 0,
    val rewardCoins: Int = 0,
    val totalEarned: Int = 0,
    val totalSpent: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val totalCoins: Int
        get() = familyCoins + rewardCoins
}