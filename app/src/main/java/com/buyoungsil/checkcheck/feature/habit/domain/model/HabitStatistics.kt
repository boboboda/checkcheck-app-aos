package com.buyoungsil.checkcheck.feature.habit.domain.model

/**
 * 습관 통계 도메인 모델
 */
data class HabitStatistics(
    val habitId: String,
    val totalChecks: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionRate: Float = 0f, // 0.0 ~ 1.0
    val thisWeekChecks: Int = 0,
    val thisMonthChecks: Int = 0
)