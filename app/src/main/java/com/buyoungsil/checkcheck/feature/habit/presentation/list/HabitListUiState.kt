package com.buyoungsil.checkcheck.feature.habit.presentation.list

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics

/**
 * 습관 목록 UI State
 * ✅ 마일스톤 달성 메시지 추가
 */
data class HabitListUiState(
    val habits: List<HabitWithStats> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val milestoneMessage: MilestoneMessage? = null  // 🆕 마일스톤 달성 메시지
)

/**
 * 통계와 함께 있는 습관
 */
data class HabitWithStats(
    val habit: Habit,
    val statistics: HabitStatistics? = null,
    val isCheckedToday: Boolean = false
)

/**
 * 마일스톤 달성 메시지
 */
data class MilestoneMessage(
    val habitTitle: String,
    val streakDays: Int,
    val coinsAwarded: Int
)