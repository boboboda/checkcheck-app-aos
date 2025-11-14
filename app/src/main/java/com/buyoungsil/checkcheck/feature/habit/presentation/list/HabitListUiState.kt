package com.buyoungsil.checkcheck.feature.habit.presentation.list

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics

/**
 * 습관 목록 UI State
 *
 * 🆕 infoMessage 추가 - 이미 체크 완료 등의 안내 메시지
 */
data class HabitListUiState(
    val habits: List<HabitWithStats> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val milestoneMessage: MilestoneMessage? = null,
    val infoMessage: String? = null  // 🆕 추가
)

/**
 * 습관 + 통계 데이터 클래스
 */
data class HabitWithStats(
    val habit: Habit,
    val statistics: HabitStatistics?,
    val isCheckedToday: Boolean,
    val nextMilestoneInfo: NextMilestoneInfo? = null
)

/**
 * 마일스톤 달성 메시지
 */
data class MilestoneMessage(
    val habitTitle: String,
    val streakDays: Int,
    val coinsAwarded: Int
)

