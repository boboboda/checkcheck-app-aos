package com.buyoungsil.checkcheck.feature.habit.domain.model

/**
 * 습관 보상 기록 (중복 지급 방지용)
 */
data class HabitRewardRecord(
    val id: String = "",
    val habitId: String,
    val userId: String,
    val streakDays: Int,        // 보상 받은 연속 일수
    val coinsAwarded: Int,      // 지급된 코인
    val awardedAt: Long = System.currentTimeMillis()
)