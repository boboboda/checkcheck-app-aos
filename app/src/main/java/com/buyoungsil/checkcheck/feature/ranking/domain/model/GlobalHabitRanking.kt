package com.buyoungsil.checkcheck.feature.ranking.domain.model

/**
 * 글로벌 습관 랭킹 모델
 */
data class GlobalHabitRanking(
    val habitTitle: String,
    val userRankings: List<UserRanking>
)

/**
 * 사용자 랭킹 정보
 */
data class UserRanking(
    val userId: String,
    val userName: String,
    val currentStreak: Int,
    val totalChecks: Int,
    val completionRate: Float,  // 0.0 ~ 1.0
    val rank: Int
)

/**
 * 내 랭킹 정보
 */
data class MyRankingInfo(
    val habitTitle: String,
    val myRank: Int,
    val totalUsers: Int,
    val myStats: UserRanking
)