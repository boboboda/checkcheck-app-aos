package com.buyoungsil.checkcheck.feature.habit.presentation.list

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics

/**
 * 습관 목록 UI State
 */
data class HabitListUiState(
    val habits: List<HabitWithStats> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 통계와 함께 있는 습관
 */
data class HabitWithStats(
    val habit: Habit,
    val statistics: HabitStatistics? = null,
    val isCheckedToday: Boolean = false
)