package com.buyoungsil.checkcheck.feature.statistics.presentation

import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import java.time.LocalDate

/**
 * 통계 UI State
 * ✅ 월간 체크 데이터 추가
 */
data class StatisticsUiState(
    val habits: List<HabitWithStats> = emptyList(),
    val totalHabits: Int = 0,
    val totalChecks: Int = 0,
    val averageCompletionRate: Float = 0f,
    val longestStreak: Int = 0,
    val currentStreak: Int = 0,
    val thisWeekChecks: Int = 0,
    val thisMonthChecks: Int = 0,
    val monthlyCheckDates: Set<LocalDate> = emptySet(),  // ✅ 이번 달 체크한 날짜들
    val isLoading: Boolean = false,
    val error: String? = null
)

data class GlobalRankingUiState(
    val habitTitle: String = "",
    val rankings: List<com.buyoungsil.checkcheck.feature.ranking.domain.model.UserRanking> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)