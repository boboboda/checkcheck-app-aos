package com.buyoungsil.checkcheck.feature.coin.domain.model

/**
 * 코인 지갑 도메인 모델
 *
 * 🆕 어뷰징 방지를 위한 추가 필드:
 * - monthlyRewardCoins: 이번 달 습관 보상으로 받은 코인 누적
 * - dailyRewardCoins: 오늘 습관 보상으로 받은 코인 누적
 * - lastMonthReset: 마지막 월간 리셋 시간
 * - lastDayReset: 마지막 일간 리셋 시간
 */
data class CoinWallet(
    val userId: String,
    val familyCoins: Int = 0,
    val rewardCoins: Int = 0,
    val totalEarned: Int = 0,
    val totalSpent: Int = 0,

    // 🆕 어뷰징 방지 필드
    val monthlyRewardCoins: Int = 0,
    val dailyRewardCoins: Int = 0,
    val lastMonthReset: Long = System.currentTimeMillis(),
    val lastDayReset: Long = System.currentTimeMillis(),

    val lastUpdated: Long = System.currentTimeMillis()
) {
    val totalCoins: Int
        get() = familyCoins + rewardCoins
}